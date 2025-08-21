package insight.shinanai.distributed_scheduled_task_demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import insight.shinanai.distributed_scheduled_task_demo.domain.User;

/**
 * @author chitose
 * @description 针对表【users(用户信息表)】的数据库操作Mapper
 * @createDate 2025-08-20 20:27:12
 * @Entity insight.shinanai.distributed_scheduled_task_demo.domain.User
 */
public interface UserMapper extends BaseMapper<User> {

    User getUserByUsername(String username);
}




