package insight.shinanai.distributed_scheduled_task_demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import insight.shinanai.distributed_scheduled_task_demo.domain.JobInfo;

import java.util.List;

/**
* @author chitose
* @description 针对表【job_info】的数据库操作Mapper
 * @createDate 2025-08-13 23:22:38
* @Entity insight.shinanai.distributed_scheduled_task_demo.domain.JobInfo
*/
public interface JobInfoMapper extends BaseMapper<JobInfo> {

    List<JobInfo> listAllRunningJobs();
}




