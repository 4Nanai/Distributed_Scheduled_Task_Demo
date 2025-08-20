package insight.shinanai.distributed_scheduled_task_demo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName job_log
 */
@TableName(value = "job_log")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobLog implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 作业ID
     */
    private Long jobId;

    /**
     * 作业名称
     */
    private String jobName;

    /**
     * 分片项
     */
    private Integer shardingItem;

    /**
     * 日志级别 (INFO, WARN, ERROR, DEBUG)
     */
    private String logLevel;

    /**
     * 日志信息
     */
    private String message;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 执行ID
     */
    private String executionId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
