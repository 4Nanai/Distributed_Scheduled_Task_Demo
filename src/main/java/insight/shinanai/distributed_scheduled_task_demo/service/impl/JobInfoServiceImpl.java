package insight.shinanai.distributed_scheduled_task_demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import insight.shinanai.distributed_scheduled_task_demo.domain.JobInfo;
import insight.shinanai.distributed_scheduled_task_demo.domain.ScriptFiles;
import insight.shinanai.distributed_scheduled_task_demo.job.RunScriptJob;
import insight.shinanai.distributed_scheduled_task_demo.mapper.JobInfoMapper;
import insight.shinanai.distributed_scheduled_task_demo.service.JobInfoService;
import insight.shinanai.distributed_scheduled_task_demo.service.JobLogService;
import insight.shinanai.distributed_scheduled_task_demo.service.ScriptFilesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
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
    private final JobLogService jobLogService;

    public JobInfoServiceImpl(ScriptFilesService scriptFilesService, CoordinatorRegistryCenter registryCenter,
                              JobLogService jobLogService) {
        this.scriptFilesService = scriptFilesService;
        this.registryCenter = registryCenter;
        this.jobLogService = jobLogService;
    }

    private void createScriptJob(String jobName, String cron, int shardingCount, Long scriptId,
                                 String description) {
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
                                          description,
                                          null,
                                          null,
                                          null,
                                          null
            );
            this.save(jobInfo);
            scheduleScriptJob(jobInfo.getId(), jobName, cron, shardingCount, scriptId);
            log.info("Scheduled job: {}, Cron: {}, Shards: {}, Script ID: {}, Description: {}",
                     jobName,
                     cron,
                     shardingCount,
                     scriptId,
                     description
            );
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void scheduleScriptJob(Long jobId, String jobName, String cron, int shardingCount, Long scriptId) {
        RunScriptJob runScriptJob = new RunScriptJob(jobId, jobName, scriptId, scriptFilesService, jobLogService);
        JobConfiguration jobConfiguration = generateJobConfiguration(jobName, cron, shardingCount);
        ScheduleJobBootstrap scheduleJobBootstrap = new ScheduleJobBootstrap(registryCenter,
                                                                             runScriptJob,
                                                                             jobConfiguration
        );
        scheduleJobBootstrap.schedule();
        jobBootstrapMap.put(jobName, scheduleJobBootstrap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerScriptJob(String jobName,
                                  String cronExpression,
                                  int shardingCount,
                                  MultipartFile scriptFile,
                                  String commandArgs, String description) throws IOException {
        ScriptFiles scriptFiles = scriptFilesService.saveScriptFile(scriptFile, commandArgs, jobName);
        this.createScriptJob(jobName, cronExpression, shardingCount, scriptFiles.getId(), description);
    }

    @Override
    public List<JobInfo> listAllRunningJobs() {
        return this.baseMapper.listAllRunningJobs();
    }

    private JobConfiguration generateJobConfiguration(String jobName, String cron, int shardingCount) {
        return JobConfiguration.newBuilder(jobName, shardingCount)
                .cron(cron)
                .shardingItemParameters(generateShardingParameters(shardingCount))
                .overwrite(true)
                .failover(true)
                .misfire(true)
                .disabled(false)
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




