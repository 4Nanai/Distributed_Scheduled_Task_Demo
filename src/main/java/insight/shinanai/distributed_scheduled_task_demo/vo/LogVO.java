package insight.shinanai.distributed_scheduled_task_demo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogVO {
    private String logLevel;
    private String message;
    private Date createTime;
}
