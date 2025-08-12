package insight.shinanai.distributed_scheduled_task_demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import insight.shinanai.distributed_scheduled_task_demo.domain.JobInfo;
import insight.shinanai.distributed_scheduled_task_demo.job.RunScriptJob;
import insight.shinanai.distributed_scheduled_task_demo.service.JobInfoService;
import insight.shinanai.distributed_scheduled_task_demo.mapper.JobInfoMapper;
import insight.shinanai.distributed_scheduled_task_demo.service.ScriptFilesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.JobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chitose
 * @description 针对表【job_info】的数据库操作Service实现
 * @createDate 2025-08-11 23:56:35
 */
@Slf4j
@Service
public class JobInfoServiceImpl extends ServiceImpl<JobInfoMapper, JobInfo>
        implements JobInfoService {

    private final Map<String, ScheduleJobBootstrap> jobBootstrapMap = new ConcurrentHashMap<>();
    private final ScriptFilesService scriptFilesService;
    private final CoordinatorRegistryCenter registryCenter;

    public JobInfoServiceImpl(ScriptFilesService scriptFilesService, CoordinatorRegistryCenter registryCenter) {
        this.scriptFilesService = scriptFilesService;
        this.registryCenter = registryCenter;
    }

    @Override
    public void createScriptJob(String jobName, String cron, int shardingCount, Long scriptId) {
        try {
            if (jobBootstrapMap.containsKey(jobName)) {
                throw new RuntimeException("Job with name " + jobName + " already exists.");
            }

            JobInfo jobInfo = new JobInfo(null,
                                          jobName,
                                          cron,
                                          shardingCount,
                                          scriptId,
                                          "RUNNING",
                                          null,
                                          null,
                                          null,
                                          null,
                                          null
            );
            this.save(jobInfo);

            RunScriptJob runScriptJob = new RunScriptJob(jobName, scriptId, scriptFilesService);

            JobConfiguration jobConfiguration = generateJobConfiguration(jobName, cron, shardingCount);

            ScheduleJobBootstrap scheduleJobBootstrap = new ScheduleJobBootstrap(registryCenter,
                                                                                 runScriptJob,
                                                                                 jobConfiguration
            );
            scheduleJobBootstrap.schedule();
            jobBootstrapMap.put(jobName, scheduleJobBootstrap);

            log.info("Scheduled job: {}, Cron: {}, Shards: {}, Script ID: {}",
                     jobName,
                     cron,
                     shardingCount,
                     scriptId
            );
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

    }

    private JobConfiguration generateJobConfiguration(String jobName, String cron, int shardingCount) {
        return JobConfiguration.newBuilder(jobName, shardingCount)
                .cron(cron)
                .shardingItemParameters(generateShardingParameters(shardingCount))
                .jobExecutorServiceHandlerType("LOG")
                .overwrite(true)
                .failover(true)
                .misfire(true)
                .disabled(false)
                .jobListenerTypes("LOG")
                .build();
    }

    private String generateShardingParameters(int shardingCount) {
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < shardingCount; i++) {
            if (i > 0) {
                params.append(",");
            }
            params.append(i)
                    .append("=")
                    .append(i);
        }
        return params.toString();
    }
}




