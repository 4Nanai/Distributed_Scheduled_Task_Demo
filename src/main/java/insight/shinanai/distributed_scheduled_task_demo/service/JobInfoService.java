package insight.shinanai.distributed_scheduled_task_demo.service;

import insight.shinanai.distributed_scheduled_task_demo.domain.JobInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author chitose
* @description 针对表【job_info】的数据库操作Service
* @createDate 2025-08-11 23:56:35
*/
public interface JobInfoService extends IService<JobInfo> {

    void createScriptJob(String jobName, String cron, int shardingCount, Long scriptId);
}
