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
 *
 * @TableName script_files
 */
@TableName(value ="script_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScriptFiles implements Serializable {
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
     * 脚本文件名
     */
    private String fileName;

    /**
     * 脚本内容
     */
    private String fileContent;

    /**
     * 启动命令行参数，多个参数用空格分隔
     */
    private String commandArgs;

    /**
     * 脚本类型 (SHELL, PYTHON)
     */
    private String fileType;

    /**
     * 脚本文件大小
     */
    private Long fileSize;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
