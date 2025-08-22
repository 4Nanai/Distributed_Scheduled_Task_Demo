package insight.shinanai.distributed_scheduled_task_demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import insight.shinanai.distributed_scheduled_task_demo.domain.ScriptFiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
* @author chitose
* @description 针对表【script_files】的数据库操作Service
* @createDate 2025-08-11 23:35:59
*/
public interface ScriptFilesService extends IService<ScriptFiles> {

    ScriptFiles saveScriptFile(MultipartFile scriptFile, String jobName) throws IOException;
}
