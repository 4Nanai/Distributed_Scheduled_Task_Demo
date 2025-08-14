package insight.shinanai.distributed_scheduled_task_demo.controller;

import insight.shinanai.distributed_scheduled_task_demo.service.JobInfoService;
import insight.shinanai.distributed_scheduled_task_demo.service.ScriptFilesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
public class JobRegisterController {

    private final ScriptFilesService scriptFilesService;
    private final JobInfoService jobInfoService;

    public JobRegisterController(ScriptFilesService scriptFilesService, JobInfoService jobInfoService) {
        this.scriptFilesService = scriptFilesService;
        this.jobInfoService = jobInfoService;
    }

    @PostMapping("/register-script-job")
    public ResponseEntity<?> registerScriptJob(
            @RequestParam("script") MultipartFile scriptFile,
            @RequestParam("cron") String cronExpression,
            @RequestParam("jobName") String jobName,
            @RequestParam(value = "shardingCount", defaultValue = "3") int shardingCount,
            @RequestParam(value = "commandArgs", required = false) String commandArgs,
            @RequestParam(value = "description", required = false) String description
    ) {
        // Logic to register a script job
        try {
            if (scriptFile.isEmpty()) return ResponseEntity.badRequest()
                    .body("Script file is empty");
            jobInfoService.registerScriptJob(jobName,
                                             cronExpression,
                                             shardingCount,
                                             scriptFile,
                                             commandArgs,
                                             description
            );
        } catch (Exception e) {
            log.error("Error registering script job", e);
            return ResponseEntity.status(500)
                    .body("Error registering script job: " + e.getMessage());
        }
        return ResponseEntity.ok()
                .body("Script job registered successfully");
    }
}
