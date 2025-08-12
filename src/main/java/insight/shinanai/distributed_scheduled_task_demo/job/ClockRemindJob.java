package insight.shinanai.distributed_scheduled_task_demo.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class ClockRemindJob implements SimpleJob {

    public static int count = 1;
    @Override
    public void execute(ShardingContext shardingContext) {
        String jobParameter = shardingContext.getJobParameter();
        String time = new Date().toString();
        log.info("ClockRemindJob executed at {}, Sharding Item: {}, Job Parameter: {}, Execution Count: {}",
                time, shardingContext.getShardingItem(), jobParameter, count++);
    }
}
