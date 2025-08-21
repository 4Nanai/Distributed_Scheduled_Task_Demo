package insight.shinanai.distributed_scheduled_task_demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import insight.shinanai.distributed_scheduled_task_demo.domain.User;
import insight.shinanai.distributed_scheduled_task_demo.dto.UserRegistryDTO;
import insight.shinanai.distributed_scheduled_task_demo.mapper.UserMapper;
import insight.shinanai.distributed_scheduled_task_demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * @author chitose
 * @description 针对表【users(用户信息表)】的数据库操作Service实现
 * @createDate 2025-08-20 20:27:12
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService, UserDetailsService {

    private final RedisTemplate<Object, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(RedisTemplate<Object, Object> redisTemplate, PasswordEncoder passwordEncoder) {
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean existsByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return this.count(queryWrapper) > 0;
    }

    private UserDetails getUserFromRedis(String username) {
        try {
            return (UserDetails) redisTemplate.opsForValue()
                    .get("user:" + username);
        } catch (Exception e) {
            return null;
        }
    }

    private void cacheUserToRedis(String username, User user) {
        try {
            redisTemplate.opsForValue()
                    .set("user:" + username, user, Duration.ofMinutes(30));
        } catch (Exception e) {
            log.error("Error caching user to Redis", e);
        }
    }

    public void saveUser(UserRegistryDTO registryDTO) {
        User user = new User();
        BeanUtils.copyProperties(registryDTO, user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        this.save(user);
        cacheUserToRedis(user.getUsername(), user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails cachedUser = getUserFromRedis(username);
        if (cachedUser != null) {
            return cachedUser;
        }

        User user = this.getBaseMapper()
                .getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        cacheUserToRedis(username, user);
        return user;
    }
}




