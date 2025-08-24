package insight.shinanai.distributed_scheduled_task_demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobRegistryDTO {
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

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
     * 脚本文件ID
     */
    private Long scriptFileId;
}
