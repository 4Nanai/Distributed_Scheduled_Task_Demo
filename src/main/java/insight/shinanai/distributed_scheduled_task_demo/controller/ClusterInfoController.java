package insight.shinanai.distributed_scheduled_task_demo.controller;

import insight.shinanai.distributed_scheduled_task_demo.utils.ResponseUtils;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ClusterInfoController {
    private final CoordinatorRegistryCenter registryCenter;

    public ClusterInfoController(CoordinatorRegistryCenter registryCenter) {
        this.registryCenter = registryCenter;
    }

    @GetMapping("/cluster-info/instances")
    ResponseEntity<?> getClusterInstance() {
        try {
            List<String> instances = registryCenter.getChildrenKeys("/insight.shinanai.distributed_scheduled_task_demo.job.ClockRemindJob/instances");
            return ResponseUtils.success(instances);
        } catch (Exception e) {
            return ResponseUtils.error("Error retrieving cluster instances: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
