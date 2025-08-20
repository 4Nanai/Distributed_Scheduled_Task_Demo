package insight.shinanai.distributed_scheduled_task_demo.job;

import com.google.common.base.Charsets;
import insight.shinanai.distributed_scheduled_task_demo.domain.ScriptFiles;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class RunScriptJob implements SimpleJob {
    private final Long jobId;
    private final String jobName;
    private final Long scriptFileId;
    private final ScriptFilesService scriptFilesService;
    private final JobLogService jobLogService;
    private String executionId;

    public RunScriptJob(Long jobId,
                        String jobName,
                        Long scriptFileId,
                        ScriptFilesService scriptFilesService,
                        JobLogService jobLogService) {
        this.jobId = jobId;
        this.jobName = jobName;
        this.scriptFileId = scriptFileId;
        this.scriptFilesService = scriptFilesService;
        this.jobLogService = jobLogService;
        this.executionId = UUID.randomUUID()
                .toString();
    }

    @Override
    public void execute(ShardingContext shardingContext) {
        try {
            ScriptFiles scriptFiles = scriptFilesService.getById(scriptFileId);
            // 当前分片
            int shardItem = shardingContext.getShardingItem();
            int totalShard = shardingContext.getShardingTotalCount();
            String startMessage = String.format("Starting job: %s, Shard: %d/%d, Script ID: %d, Script Name: %s",
                                                jobName,
                                                shardItem,
                                                totalShard - 1,
                                                scriptFileId,
                                                scriptFiles.getFileName()
            );
            log.info(startMessage);
            jobLogService.sendAndSaveLog(jobId, jobName, "INFO", startMessage, shardItem, executionId);
            runShellScript(scriptFiles, shardItem, totalShard);

            String successMessage = String.format("Job: %s, Shard: %d/%d, Script ID: %d executed successfully",
                                                  jobName,
                                                  shardItem,
                                                  totalShard - 1,
                                                  scriptFileId
            );
            jobLogService.sendAndSaveLog(jobId, jobName, "INFO", successMessage, shardItem, executionId);
            log.info(successMessage);
        } catch (Exception e) {
            String exceptionMessage = String.format("Job: %s, Shard: %d/%d, Script ID: %d execution failed: %s",
                                                    jobName,
                                                    shardingContext.getShardingItem(),
                                                    shardingContext.getShardingTotalCount() - 1,
                                                    scriptFileId,
                                                    e.getMessage()
            );
            jobLogService.sendAndSaveLog(jobId,
                                         jobName,
                                         "ERROR",
                                         exceptionMessage,
                                         shardingContext.getShardingItem(),
                                         executionId
            );
            log.error(exceptionMessage, e);
            throw new RuntimeException(e);
        }
    }

    private void runShellScript(ScriptFiles scriptFiles, int shardItem, int totalShard) throws Exception {
        Path tempScriptPath = createTempScriptFile(scriptFiles);
        try {
            Files.setPosixFilePermissions(tempScriptPath, PosixFilePermissions.fromString("rwxr-xr-x"));
            ProcessBuilder processBuilder = new ProcessBuilder();
            List<String> command = new ArrayList<>();
            command.add("bash");
            command.add(tempScriptPath.toString());
            if (StringUtils.hasText(scriptFiles.getCommandArgs())) {
                command.addAll(List.of(scriptFiles.getCommandArgs()
                                               .split(" ")));
            }
            processBuilder.command(command);

            processBuilder.environment()
                    .put("SHARD_ITEM", String.valueOf(shardItem));

            Process process = processBuilder.start();
            readOutput(process, shardItem, totalShard);

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Script execution failed with exit code " + exitCode);
            }
        } finally {
            Files.deleteIfExists(tempScriptPath);
        }
    }

    private String readOutput(Process process, int shardItem, int totalShard) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line)
                        .append(System.lineSeparator());
                String message = String.format("[%d/%d] %s", shardItem, totalShard - 1, line);
                jobLogService.sendAndSaveLog(jobId, jobName, "INFO", message, shardItem, executionId);
                log.info(line);
            }
        } catch (IOException e) {
            log.error("Error reading script output", e);
        }
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = errorReader.readLine()) != null) {
                sb.append(line)
                        .append(System.lineSeparator());
                String message = String.format("[%d/%d] %s", shardItem, totalShard - 1, line);
                jobLogService.sendAndSaveLog(jobId, jobName, "ERROR", message, shardItem, executionId);
            }
        } catch (IOException e) {
            log.error("Error reading script error output", e);
        }
        return sb.toString();
    }

    private Path createTempScriptFile(ScriptFiles scriptFiles) throws IOException {
        String fileContent = scriptFiles.getFileContent();
        Path path = Files.createTempFile(scriptFiles.getFileName(), scriptFiles.getFileType());
        Files.writeString(path, fileContent, Charsets.UTF_8);
        return path;
    }
}
