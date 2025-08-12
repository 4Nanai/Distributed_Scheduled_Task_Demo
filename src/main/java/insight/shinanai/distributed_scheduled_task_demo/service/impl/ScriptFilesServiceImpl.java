package insight.shinanai.distributed_scheduled_task_demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import insight.shinanai.distributed_scheduled_task_demo.domain.ScriptFiles;
import insight.shinanai.distributed_scheduled_task_demo.service.ScriptFilesService;
import insight.shinanai.distributed_scheduled_task_demo.mapper.ScriptFilesMapper;
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
        String scriptName = scriptFile.getOriginalFilename();
        String extension = scriptName != null ? scriptName.substring(scriptName.lastIndexOf('.') + 1) : "";
        ScriptFiles scriptFiles = new ScriptFiles(null,
                                                  jobName,
                                                  scriptName,
                                                  fileContent,
                                                  extension,
                                                  scriptFile.getSize(),
                                                  null,
                                                  null
        );
        this.save(scriptFiles);
        return scriptFiles;
    }
}




