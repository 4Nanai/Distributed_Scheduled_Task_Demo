package insight.shinanai.distributed_scheduled_task_demo.utils;

import org.apache.shardingsphere.elasticjob.api.JobConfiguration;

public class JobConfigUtils {
    /**
     * Helper method to generate JobConfiguration
     *
     * @param uniqueJobName unique job name
     * @param cron          cron expression
     * @param shardingCount number of shards
     * @return JobConfiguration object
     */
    public static JobConfiguration generateJobConfiguration(String uniqueJobName, String cron, int shardingCount) {
        return JobConfiguration.newBuilder(uniqueJobName, shardingCount)
                .cron(cron)
                .shardingItemParameters(generateShardingParameters(shardingCount))
                .overwrite(true)
                .failover(true)
                .misfire(true)
                .disabled(false)
                .build();
    }

    /**
     * Helper method to generate sharding parameters in the format "0=0,1=1,2=2,..."
     *
     * @param shardingCount number of shards
     * @return sharding parameters string
     */
    public static String generateShardingParameters(int shardingCount) {
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < shardingCount; i++) {
            if (i > 0) {
                params.append(",");
            }
            params.append(i)
                    .append("=")
                    .append(i);
        }
        return params.toString();
    }

    /**
     * Helper method to create a unique job name by appending user ID
     *
     * @param jobName original job name
     * @param userId  user ID
     * @return unique job name
     */
    public static String getUniqueJobName(String jobName, Long userId) {
        return jobName + "_user_" + userId;
    }
}
