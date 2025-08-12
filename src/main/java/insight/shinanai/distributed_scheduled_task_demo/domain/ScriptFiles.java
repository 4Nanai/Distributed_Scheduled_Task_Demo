package insight.shinanai.distributed_scheduled_task_demo.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @TableName script_files
 */
@TableName(value ="script_files")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScriptFiles {
    private Long id;

    private String jobName;

    private String fileName;

    private String fileContent;

    private String fileType;

    private Long fileSize;

    private Date createTime;

    private Date updateTime;
}
