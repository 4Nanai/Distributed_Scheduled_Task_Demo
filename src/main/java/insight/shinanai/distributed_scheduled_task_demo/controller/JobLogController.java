package insight.shinanai.distributed_scheduled_task_demo.controller;

import insight.shinanai.distributed_scheduled_task_demo.service.JobLogService;
import insight.shinanai.distributed_scheduled_task_demo.utils.ResponseUtils;
import insight.shinanai.distributed_scheduled_task_demo.vo.LogVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobLogController {
    private final JobLogService jobLogService;

    public JobLogController(JobLogService jobLogService) {
        this.jobLogService = jobLogService;
    }

    @GetMapping("/{jobId}/logs")
    public ResponseEntity<?> getRecentLogsLimitHistoryCount(@PathVariable("jobId") String jobId) {
        List<LogVO> logVOS = jobLogService.getRecentLogsLimitHistoryCount(jobId);
        return ResponseUtils.success(logVOS);
    }
}
