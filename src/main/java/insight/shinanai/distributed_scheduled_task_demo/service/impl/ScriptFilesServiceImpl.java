package insight.shinanai.distributed_scheduled_task_demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import insight.shinanai.distributed_scheduled_task_demo.domain.ScriptFiles;
import insight.shinanai.distributed_scheduled_task_demo.mapper.ScriptFilesMapper;
import insight.shinanai.distributed_scheduled_task_demo.service.ScriptFilesService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author chitose
 * @description 针对表【script_files】的数据库操作Service实现
 * @createDate 2025-08-11 23:35:59
 */
@Service
public class ScriptFilesServiceImpl extends ServiceImpl<ScriptFilesMapper, ScriptFiles>
        implements ScriptFilesService {

    @Override
    public ScriptFiles saveScriptFile(MultipartFile scriptFile, String jobName) throws IOException {
        String fileContent = new String(scriptFile.getBytes(), StandardCharsets.UTF_8);
        String fileName = scriptFile.getOriginalFilename();
        String fileType = fileName != null ? fileName.substring(fileName.lastIndexOf('.') + 1) : "";
        if (!"sh".equals(fileType) && !"py".equals(fileType)) {
            throw new RuntimeException("Unsupported script type: " + fileType);
        }
        ScriptFiles scriptFiles = new ScriptFiles(null,
                                                  jobName,
                                                  fileName,
                                                  fileContent,
                                                  fileType,
                                                  scriptFile.getSize(),
                                                  null,
                                                  null
        );
        this.save(scriptFiles);
        return scriptFiles;
    }
}




