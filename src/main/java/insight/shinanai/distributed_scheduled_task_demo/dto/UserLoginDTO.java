package insight.shinanai.distributed_scheduled_task_demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginDTO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
