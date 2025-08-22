package insight.shinanai.distributed_scheduled_task_demo.controller;

import insight.shinanai.distributed_scheduled_task_demo.service.JobInfoService;
import insight.shinanai.distributed_scheduled_task_demo.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/jobs")
public class JobRegisterController {

    private final JobInfoService jobInfoService;

    public JobRegisterController(JobInfoService jobInfoService) {
        this.jobInfoService = jobInfoService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerScriptJob(
            @RequestParam("script") MultipartFile scriptFile,
            @RequestParam("cron") String cronExpression,
            @RequestParam("jobName") String jobName,
            @RequestParam(value = "shardingCount", defaultValue = "1") int shardingCount,
            @RequestParam(value = "commandArgs", required = false) String commandArgs,
            @RequestParam(value = "description", required = false) String description
    ) {
        // Logic to register a script job
        try {
            if (scriptFile.isEmpty()) return ResponseUtils.error("Script file is empty", HttpStatus.BAD_REQUEST);
            jobInfoService.registerScriptJob(jobName,
                                             cronExpression,
                                             shardingCount,
                                             scriptFile,
                                             commandArgs,
                                             description
            );
        } catch (Exception e) {
            log.error("Error registering script job", e);
            return ResponseUtils.error("Error registering script job.");
        }
        return ResponseUtils.success("Script job registered successfully");
    }
}
