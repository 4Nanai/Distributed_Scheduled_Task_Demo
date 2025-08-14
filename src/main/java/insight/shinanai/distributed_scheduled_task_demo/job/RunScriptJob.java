package insight.shinanai.distributed_scheduled_task_demo.job;

import com.google.common.base.Charsets;
import insight.shinanai.distributed_scheduled_task_demo.domain.ScriptFiles;
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

@Slf4j
public class RunScriptJob implements SimpleJob {
    private final String jobName;
    private final Long scriptFileId;
    private final ScriptFilesService scriptFilesService;

    public RunScriptJob(String jobName, Long scriptFileId, ScriptFilesService scriptFilesService) {
        this.jobName = jobName;
        this.scriptFileId = scriptFileId;
        this.scriptFilesService = scriptFilesService;
    }

    @Override
    public void execute(ShardingContext shardingContext) {
        try {
            ScriptFiles scriptFiles = scriptFilesService.getById(scriptFileId);
            // 当前分片
            int shardItem = shardingContext.getShardingItem();
            int totalShard = shardingContext.getShardingTotalCount();
            log.info("Executing job: {}, Shard: {}/{}, Script ID: {}, Script Name: {}",
                     jobName,
                     shardItem,
                     totalShard - 1,
                     scriptFileId,
                     scriptFiles.getFileName()
            );
            runShellScript(scriptFiles, shardItem, totalShard);
        } catch (Exception e) {
            log.error("Error executing job: {}, Shard: {}/{}, Script ID: {}",
                      jobName,
                      shardingContext.getShardingItem(),
                      shardingContext.getShardingTotalCount() - 1,
                      scriptFileId,
                      e
            );
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
            readOutput(process);

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("Script executed successfully for job: {}, Shard: {}/{}", jobName, shardItem, totalShard - 1);
            } else {
                log.error("Script execution failed for job: {}, Shard: {}/{}. Exit code: {}",
                          jobName,
                          shardItem,
                          totalShard - 1,
                          exitCode
                );
                throw new RuntimeException("Script execution failed with exit code " + exitCode);
            }
        } finally {
            Files.deleteIfExists(tempScriptPath);
        }
    }

    private String readOutput(Process process) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line)
                        .append(System.lineSeparator());
                log.info("Script output: {}", line);
            }
        } catch (IOException e) {
            log.error("Error reading script output", e);
        }
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = errorReader.readLine()) != null) {
                sb.append(line)
                        .append(System.lineSeparator());
                log.error("Script error output: {}", line);
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
