package insight.shinanai.distributed_scheduled_task_demo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobDetailVO {
    private Long id;

    /**
     * 作业名称
     */
    private String jobName;

    /**
     * Cron表达式
     */
    private String cronExpression;

    /**
     * 分片数量
     */
    private Integer shardingCount;

    /**
     * 启动命令行参数，多个参数用空格分隔
     */
    private String commandArgs;

    /**
     * 作业状态 (STARTED, STOPPED, PAUSED)
     */
    private String jobStatus;

    /**
     * 作业描述
     */
    private String description;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 上次执行时间
     */
    private Date lastExecuteTime;

    /**
     * 下次执行时间
     */
    private Date nextExecuteTime;
}
