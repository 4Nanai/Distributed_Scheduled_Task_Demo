package insight.shinanai.distributed_scheduled_task_demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import insight.shinanai.distributed_scheduled_task_demo.domain.User;
import insight.shinanai.distributed_scheduled_task_demo.dto.UserRegistryDTO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author chitose
 * @description 针对表【users(用户信息表)】的数据库操作Service
 * @createDate 2025-08-20 20:27:12
 */
public interface UserService extends IService<User> {

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    boolean existsByUsername(String username);

    void saveUser(UserRegistryDTO userRegistryDTO);
}
