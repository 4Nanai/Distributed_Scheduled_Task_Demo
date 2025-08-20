package insight.shinanai.distributed_scheduled_task_demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import insight.shinanai.distributed_scheduled_task_demo.domain.JobLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author chitose
 * @description 针对表【job_log】的数据库操作Mapper
 * @createDate 2025-08-20 05:56:05
 * @Entity insight.shinanai.distributed_scheduled_task_demo.domain.JobLog
 */
public interface JobLogMapper extends BaseMapper<JobLog> {

    List<JobLog> getRecentLogsLimitHistoryCount(String jobId, @Param("historyCount") int executeLogHistoryCount);
}




