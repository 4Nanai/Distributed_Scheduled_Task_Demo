package insight.shinanai.distributed_scheduled_task_demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import insight.shinanai.distributed_scheduled_task_demo.domain.JobLog;
import insight.shinanai.distributed_scheduled_task_demo.event.JobLogEvent;
import insight.shinanai.distributed_scheduled_task_demo.mapper.JobLogMapper;
import insight.shinanai.distributed_scheduled_task_demo.service.JobLogService;
import insight.shinanai.distributed_scheduled_task_demo.vo.LogVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author chitose
 * @description 针对表【job_log】的数据库操作Service实现
 * @createDate 2025-08-20 05:56:05
 */
@Service
public class JobLogServiceImpl extends ServiceImpl<JobLogMapper, JobLog>
        implements JobLogService {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${job.log.history.count:10}")
    private int EXECUTE_LOG_HISTORY_COUNT; // Default history count

    public JobLogServiceImpl(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public List<LogVO> getRecentLogsLimitHistoryCount(String jobId) {
        List<JobLog> jobLogs = this.getBaseMapper()
                .getRecentLogsLimitHistoryCount(jobId, EXECUTE_LOG_HISTORY_COUNT);
        return jobLogs.stream()
                .map(log -> {
                    LogVO logVO = new LogVO();
                    BeanUtils.copyProperties(log, logVO);
                    return logVO;
                })
                .toList();
    }

    @Override
    public void sendLog(Long jobId, String logLevel, String message) {
        LogVO logVO = new LogVO(logLevel, message, new Date());
        applicationEventPublisher.publishEvent(new JobLogEvent(this, String.valueOf(jobId), logVO));
    }

    @Override
    public void saveCompleteLog(Long jobId,
                                String jobName,
                                int shardItem,
                                String logLevel,
                                String message,
                                String executionId) {
        JobLog jobLog = new JobLog(null, jobId, jobName, shardItem, logLevel, message, new Date(), executionId);
        this.save(jobLog);
    }

}




