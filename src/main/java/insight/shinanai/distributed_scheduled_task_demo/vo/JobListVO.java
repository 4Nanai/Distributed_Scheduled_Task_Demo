package insight.shinanai.distributed_scheduled_task_demo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobListVO {
    private Long id;
    private String jobName;
    private String cronExpression;
    private Integer shardingCount;
    private String commandArgs;
    private String description;
    private String jobStatus;
}
