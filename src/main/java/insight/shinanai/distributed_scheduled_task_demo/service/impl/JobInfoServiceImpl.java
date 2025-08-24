package insight.shinanai.distributed_scheduled_task_demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import insight.shinanai.distributed_scheduled_task_demo.domain.JobInfo;
import insight.shinanai.distributed_scheduled_task_demo.domain.ScriptFiles;
import insight.shinanai.distributed_scheduled_task_demo.dto.JobRegistryDTO;
import insight.shinanai.distributed_scheduled_task_demo.job.RunShellScriptJob;
import insight.shinanai.distributed_scheduled_task_demo.mapper.JobInfoMapper;
import insight.shinanai.distributed_scheduled_task_demo.service.JobInfoService;
import insight.shinanai.distributed_scheduled_task_demo.service.JobLogService;
import insight.shinanai.distributed_scheduled_task_demo.service.ScriptFilesService;
import insight.shinanai.distributed_scheduled_task_demo.utils.JobConfigUtils;
import insight.shinanai.distributed_scheduled_task_demo.utils.SecurityUtils;
import insight.shinanai.distributed_scheduled_task_demo.vo.JobDetailVO;
import insight.shinanai.distributed_scheduled_task_demo.vo.JobListVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public JobInfoServiceImpl(ScriptFilesService scriptFilesService, CoordinatorRegistryCenter registryCenter,
                              JobLogService jobLogService, ObjectMapper objectMapper,
                              StringRedisTemplate stringRedisTemplate) {
        this.scriptFilesService = scriptFilesService;
        this.registryCenter = registryCenter;
        this.jobLogService = jobLogService;
        this.objectMapper = objectMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private void createScriptJob(String jobName, String cron, int shardingCount, String commandArgs, Long scriptId,
                                 String description) {
        try {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (jobBootstrapMap.containsKey(JobConfigUtils.getUniqueJobName(jobName, currentUserId))) {
                throw new RuntimeException("Job with name " + jobName + " already exists.");
            }

            JobInfo jobInfo = new JobInfo(null,
                                          currentUserId,
                                          jobName,
                                          cron,
                                          shardingCount,
                                          commandArgs,
                                          scriptId,
                                          "RUNNING",
                                          description,
                                          null,
                                          null,
                                          null,
                                          null
            );
            this.save(jobInfo);
            JobRegistryDTO jobRegistryDTO = new JobRegistryDTO();
            BeanUtils.copyProperties(jobInfo, jobRegistryDTO);
            scheduleScriptJob(jobRegistryDTO);
            log.info("Scheduled job: {}, Cron: {}, Shards: {}, Script ID: {}. Notifying other instances...",
                     jobName,
                     cron,
                     shardingCount,
                     scriptId
            );
            notifyOtherInstances(jobRegistryDTO);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void scheduleScriptJob(JobRegistryDTO jobRegistryDTO) {
        Long jobId = jobRegistryDTO.getId();
        String jobName = jobRegistryDTO.getJobName();
        Long userId = jobRegistryDTO.getUserId();
        String cron = jobRegistryDTO.getCronExpression();
        int shardingCount = jobRegistryDTO.getShardingCount();
        String commandArgs = jobRegistryDTO.getCommandArgs();
        Long scriptId = jobRegistryDTO.getScriptFileId();
        String uniqueJobName = JobConfigUtils.getUniqueJobName(jobName, userId);
        RunShellScriptJob runScriptJob = new RunShellScriptJob(jobId,
                                                               uniqueJobName,
                                                               cron,
                                                               scriptId,
                                                               commandArgs,
                                                               scriptFilesService,
                                                               jobLogService
        );
        JobConfiguration jobConfiguration = JobConfigUtils.generateJobConfiguration(uniqueJobName, cron, shardingCount);
        ScheduleJobBootstrap scheduleJobBootstrap = new ScheduleJobBootstrap(registryCenter,
                                                                             runScriptJob,
                                                                             jobConfiguration
        );
        scheduleJobBootstrap.schedule();
        jobBootstrapMap.put(uniqueJobName, scheduleJobBootstrap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerScriptJob(String jobName,
                                  String cronExpression,
                                  int shardingCount,
                                  MultipartFile scriptFile,
                                  String commandArgs, String description) throws IOException {
        ScriptFiles scriptFiles = scriptFilesService.saveScriptFile(scriptFile, jobName);
        this.createScriptJob(jobName, cronExpression, shardingCount, commandArgs, scriptFiles.getId(), description);
    }

    @Override
    public List<JobInfo> listAllRunningJobs() {
        return this.baseMapper.listAllRunningJobs();
    }

    @Override
    public List<JobListVO> getAllJobs() {
        LambdaQueryWrapper<JobInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(JobInfo::getUserId, SecurityUtils.getCurrentUserId());
        queryWrapper.orderByDesc(JobInfo::getLastExecuteTime);
        List<JobInfo> jobInfos = this.list(queryWrapper);
        return jobInfos.stream()
                .map(jobInfo -> {
                    JobListVO jobListVO = new JobListVO();
                    BeanUtils.copyProperties(jobInfo, jobListVO);
                    return jobListVO;
                })
                .toList();
    }

    @Override
    public JobDetailVO getJobById(Long jobId) {
        JobInfo jobInfo = this.getById(jobId);
        if (jobInfo == null || !jobInfo.getUserId()
                .equals(SecurityUtils.getCurrentUserId())) {
            throw new RuntimeException("Job not found or access denied");
        }
        JobDetailVO jobDetailVO = new JobDetailVO();
        BeanUtils.copyProperties(jobInfo, jobDetailVO);
        return jobDetailVO;
    }

    private void notifyOtherInstances(JobRegistryDTO jobRegistryDTO) {
        try {
            String channel = "job-registry:" + jobRegistryDTO.getId();
            String message = objectMapper.writeValueAsString(jobRegistryDTO);
            stringRedisTemplate.convertAndSend(channel, message);
            log.info("Notified other instances via Redis channel: {}, Message: {}", channel, message);
        } catch (JsonProcessingException e) {
            log.error("Error notifying other instances via Redis", e);
            throw new RuntimeException(e);
        }
    }
}




