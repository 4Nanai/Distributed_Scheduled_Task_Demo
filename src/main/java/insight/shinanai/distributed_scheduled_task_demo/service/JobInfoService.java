package insight.shinanai.distributed_scheduled_task_demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import insight.shinanai.distributed_scheduled_task_demo.domain.JobInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author chitose
 * @description 针对表【job_info】的数据库操作Service
 * @createDate 2025-08-11 23:56:35
 */
public interface JobInfoService extends IService<JobInfo> {

    void scheduleScriptJob(String jobName, String cron, int shardingCount, Long scriptId);

    void registerScriptJob(String jobName,
                           String cronExpression,
                           int shardingCount,
                           MultipartFile scriptFiles,
                           String commandArgs,
                           String description) throws IOException;

    List<JobInfo> listAllRunningJobs();
}
