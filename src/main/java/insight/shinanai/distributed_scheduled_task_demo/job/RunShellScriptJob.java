package insight.shinanai.distributed_scheduled_task_demo.job;

import com.google.common.base.Charsets;
import insight.shinanai.distributed_scheduled_task_demo.constant.LogLevelConstant;
import insight.shinanai.distributed_scheduled_task_demo.domain.ScriptFiles;
import insight.shinanai.distributed_scheduled_task_demo.service.JobInfoService;
import insight.shinanai.distributed_scheduled_task_demo.service.JobLogService;
import insight.shinanai.distributed_scheduled_task_demo.service.ScriptFilesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Job to run shell scripts
 */
@Slf4j
public class RunShellScriptJob implements SimpleJob {
    // Job parameters
    private final Long jobId;
    private final String jobName;
    private final String cron;
    private final Long scriptFileId;
    private int shardItem;
    private final String commandArgs;
    private final JobInfoService jobInfoService;
    private final ScriptFilesService scriptFilesService;
    private final JobLogService jobLogService;
    private final String executionId;

    // Log collectors
    private final StringBuilder infoLogBuilder = new StringBuilder();
    private final StringBuilder errorLogBuilder = new StringBuilder();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public RunShellScriptJob(Long jobId,
                             String jobName,
                             String cron,
                             Long scriptFileId,
                             String commandArgs,
                             JobInfoService jobInfoService,
                             ScriptFilesService scriptFilesService,
                             JobLogService jobLogService) {
        this.jobId = jobId;
        this.jobName = jobName;
        this.cron = cron;
        this.scriptFileId = scriptFileId;
        this.commandArgs = commandArgs;
        this.jobInfoService = jobInfoService;
        this.scriptFilesService = scriptFilesService;
        this.jobLogService = jobLogService;
        this.executionId = UUID.randomUUID()
                .toString();
    }

    /**
     * Execute the shell script
     * and collect logs
     *
     * @param shardingContext the sharding context provided by ElasticJob
     */
    @Override
    public void execute(ShardingContext shardingContext) {
        // Update job execution time
        jobInfoService.updateJobExecutionTime(jobId, cron);
        this.shardItem = shardingContext.getShardingItem();
        int totalShard = shardingContext.getShardingTotalCount();

        try {
            ScriptFiles scriptFiles = scriptFilesService.getById(scriptFileId);

            String startMessage = String.format("Starting job: %s, Shard: %d/%d, Script ID: %d, Script Name: %s",
                                                jobName,
                                                shardItem,
                                                totalShard - 1,
                                                scriptFileId,
                                                scriptFiles.getFileName()
            );

            log.info(startMessage);
            sendLogAndCollect(LogLevelConstant.INFO, startMessage);

            // exec shell script
            runShellScript(scriptFiles, shardItem, totalShard);

            String successMessage = String.format("Job: %s, Shard: %d/%d, Script ID: %d executed successfully",
                                                  jobName, shardItem, totalShard - 1, scriptFileId
            );

            sendLogAndCollect(LogLevelConstant.INFO, successMessage);
            log.info(successMessage);

        } catch (Exception e) {
            String exceptionMessage = String.format("Job: %s, Shard: %d/%d, Script ID: %d execution failed: %s",
                                                    jobName, shardItem, totalShard - 1, scriptFileId, e.getMessage()
            );

            sendLogAndCollect(LogLevelConstant.ERROR, exceptionMessage);
            log.error(exceptionMessage, e);
            throw new RuntimeException(e);
        } finally {
            saveCompleteLogs();
        }
    }

    /**
     * Run the shell script with the provided script files
     *
     * @param scriptFiles ScriptFiles object containing script details
     * @param shardItem   the shard item number
     * @param totalShard  the total number of shards
     * @throws Exception if an error occurs during script execution
     */
    private void runShellScript(ScriptFiles scriptFiles, int shardItem, int totalShard) throws Exception {
        Path tempScriptPath = createTempScriptFile(scriptFiles);
        try {
            Files.setPosixFilePermissions(tempScriptPath, PosixFilePermissions.fromString("rwxr-xr-x"));
            ProcessBuilder processBuilder = new ProcessBuilder();
            // collect command & args
            List<String> command = new ArrayList<>();
            command.add("bash");
            command.add(tempScriptPath.toString());
            if (StringUtils.hasText(this.commandArgs)) {
                command.addAll(List.of(this.commandArgs.split(" ")));
            }
            processBuilder.command(command);
            processBuilder.environment()
                    .put("SHARD_ITEM", String.valueOf(shardItem));
            Process process = processBuilder.start();
            // Read output and error streams
            readOutput(process, shardItem, totalShard);

            // Wait for the process to complete
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                // TODO: pause job execution if script fails
                throw new RuntimeException("Script execution failed with exit code " + exitCode);
            }
        } finally {
            Files.deleteIfExists(tempScriptPath);
        }
    }

    /**
     * Read the output and error streams of the script process
     *
     * @param process    Process instance of the executed script
     * @param shardItem  the shard item number
     * @param totalShard the total number of shards
     */
    private void readOutput(Process process, int shardItem, int totalShard) {
        // Collect standard output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String message = String.format("[%d/%d] %s", shardItem, totalShard - 1, line);
                sendLogAndCollect(LogLevelConstant.INFO, message);
                log.info(line);
            }
        } catch (IOException e) {
            log.error("Error reading script output", e);
        }

        // Collect error output
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = errorReader.readLine()) != null) {
                String message = String.format("[%d/%d] %s", shardItem, totalShard - 1, line);
                sendLogAndCollect(LogLevelConstant.ERROR, message);
                log.error(line);
            }
        } catch (IOException e) {
            log.error("Error reading script error output", e);
        }
    }

    /**
     * Create a temporary script file from the ScriptFiles object
     *
     * @param scriptFiles the ScriptFiles object containing script details
     * @return Path to the created temporary script file
     * @throws IOException if an I/O error occurs
     */
    private Path createTempScriptFile(ScriptFiles scriptFiles) throws IOException {
        String fileContent = scriptFiles.getFileContent();
        Path path = Files.createTempFile(scriptFiles.getFileName(), scriptFiles.getFileType());
        Files.writeString(path, fileContent, Charsets.UTF_8);
        return path;
    }

    /**
     * Send log messages and collect them for later saving
     *
     * @param logLevel the log level (INFO or ERROR)
     * @param message  the log message to send and collect
     */
    private void sendLogAndCollect(String logLevel, String message) {
        // Send log through webSocket
        jobLogService.sendLog(jobId, logLevel, message);

        // Collect logs with timestamp
        String timestamp = LocalDateTime.now()
                .format(formatter);
        String formattedMessage = timestamp + " - " + message + System.lineSeparator();

        if (LogLevelConstant.INFO.equals(logLevel)) {
            infoLogBuilder.append(formattedMessage);
        } else if (LogLevelConstant.ERROR.equals(logLevel)) {
            errorLogBuilder.append(formattedMessage);
        }
    }

    /**
     * Save the complete logs to the database
     */
    private void saveCompleteLogs() {
        if (!infoLogBuilder.isEmpty()) {
            String infoLogContent = infoLogBuilder.toString();
            jobLogService.saveCompleteLog(jobId, jobName, shardItem, LogLevelConstant.INFO, infoLogContent, executionId);
        }

        if (!errorLogBuilder.isEmpty()) {
            String errorLogContent = errorLogBuilder.toString();
            jobLogService.saveCompleteLog(jobId,
                                          jobName,
                                          shardItem,
                                          LogLevelConstant.ERROR,
                                          errorLogContent,
                                          executionId);
        }
    }
}
