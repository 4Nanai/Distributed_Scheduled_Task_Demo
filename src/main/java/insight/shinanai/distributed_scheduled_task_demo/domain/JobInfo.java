package insight.shinanai.distributed_scheduled_task_demo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @TableName job_info
 */
@TableName(value ="job_info")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobInfo {
    private Long id;

    private String jobName;

    private String cronExpression;

    private Integer shardingCount;

    private Long scriptFileId;

    private String jobStatus;

    private String description;

    private Date createTime;

    private Date updateTime;

    private Date lastExecuteTime;

    private Date nextExecuteTime;
}
