package insight.shinanai.distributed_scheduled_task_demo.controller;

import insight.shinanai.distributed_scheduled_task_demo.service.JobInfoService;
import insight.shinanai.distributed_scheduled_task_demo.utils.ResponseUtils;
import insight.shinanai.distributed_scheduled_task_demo.vo.JobDetailVO;
import insight.shinanai.distributed_scheduled_task_demo.vo.JobListVO;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobInfoController {
    private final JobInfoService jobInfoService;

    public JobInfoController(JobInfoService jobInfoService) {
        this.jobInfoService = jobInfoService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllJobs() {
        List<JobListVO> jobs = jobInfoService.getAllJobs();
        return ResponseUtils.success(jobs);
    }

    @GetMapping("/{jobId}/detail")
    public ResponseEntity<?> getJobById(@PathVariable("jobId") Long jobId) {
        JobDetailVO jobDetailVO = jobInfoService.getJobById(jobId);
        return ResponseUtils.success(jobDetailVO);
    }

    // TODO: update fields undecided
    // Unimplemented
    @PutMapping("/{jobId}")
    public ResponseEntity<?> updateJobInfo(@PathVariable("jobId") Long jobId,
                                           @RequestParam("jobName") String jobName,
                                           @RequestParam("cron") String cron,
                                           @RequestParam(value = "shardingCount", defaultValue = "1") int shardingCount,
                                           @RequestParam(value = "commandArgs", required = false) String commandArgs,
                                           @RequestParam(value = "description", required = false) String description) {
        if (!CronExpression.isValidExpression(cron)) return ResponseUtils.error("Invalid cron expression");
        return null;
    }
}
