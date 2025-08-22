package insight.shinanai.distributed_scheduled_task_demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import insight.shinanai.distributed_scheduled_task_demo.domain.JobLog;
import insight.shinanai.distributed_scheduled_task_demo.vo.LogVO;

import java.util.List;

/**
 * @author chitose
 * @description 针对表【job_log】的数据库操作Service
 * @createDate 2025-08-20 05:56:05
 */
public interface JobLogService extends IService<JobLog> {

    List<LogVO> getRecentLogsLimitHistoryCount(String jobId);

    void sendLog(Long jobId, String logLevel, String message);

    void saveCompleteLog(Long jobId,
                         String jobName,
                         int shardItem,
                         String logLevel,
                         String message,
                         String executionId);
}
