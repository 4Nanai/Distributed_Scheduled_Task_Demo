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
 * @TableName job_info
 */
@TableName(value = "job_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobInfo implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
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
     * 脚本文件ID
     */
    private Long scriptFileId;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
