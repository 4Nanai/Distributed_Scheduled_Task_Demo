package insight.shinanai.distributed_scheduled_task_demo.runner;

import insight.shinanai.distributed_scheduled_task_demo.domain.JobInfo;
import insight.shinanai.distributed_scheduled_task_demo.service.JobInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Order(1)
@Profile("!test")
public class JobStarterRunner implements ApplicationRunner {

    private final JobInfoService jobInfoService;

    public JobStarterRunner(JobInfoService jobInfoService) {
        this.jobInfoService = jobInfoService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Start to register all jobs from database");
        try {
            List<JobInfo> jobInfos = jobInfoService.listAllRunningJobs();
            if (jobInfos.isEmpty()) {
                log.info("No running job scripts found in database");
                return;
            }
            CompletableFuture<?>[] futures = jobInfos.stream()
                    .map(job -> CompletableFuture.runAsync(() -> {
                        try {
                            jobInfoService.scheduleScriptJob(job.getId(),
                                                             job.getJobName(),
                                                             job.getCronExpression(),
                                                             job.getShardingCount(),
                                                             job.getScriptFileId()
                            );
                        } catch (Exception e) {
                            log.error("Failed to schedule job: {}, Error: {}", job.getJobName(), e.getMessage(), e);
                        }
                    }))
                    .toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(futures)
                    .get(30, TimeUnit.SECONDS);
            log.info("All jobs registered successfully");
        } catch (Exception e) {
            log.error("Error registering jobs from database", e);
            throw new RuntimeException("Failed to register jobs from database", e);
        }
    }
}
