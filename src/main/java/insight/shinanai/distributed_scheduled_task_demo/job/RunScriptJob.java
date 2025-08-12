package insight.shinanai.distributed_scheduled_task_demo.job;

import insight.shinanai.distributed_scheduled_task_demo.domain.ScriptFiles;
import insight.shinanai.distributed_scheduled_task_demo.service.ScriptFilesService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;

@Slf4j
public class RunScriptJob implements SimpleJob {
    private String jobName;
    private Long scriptFileId;
    private ScriptFilesService scriptFilesService;

    public RunScriptJob(String jobName, Long scriptFileId, ScriptFilesService scriptFilesService) {
        this.jobName = jobName;
        this.scriptFileId = scriptFileId;
        this.scriptFilesService = scriptFilesService;
    }

    @Override
    public void execute(ShardingContext shardingContext) {
            try {
                ScriptFiles scriptFiles = scriptFilesService.getById(scriptFileId);
                log.info("Executing job: {}, Shard: {}, Script ID: {}, Script Name: {}",
                         jobName,
                         shardingContext.getShardingItem(),
                         scriptFiles.getId(),
                         scriptFiles.getFileName()
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }
}
