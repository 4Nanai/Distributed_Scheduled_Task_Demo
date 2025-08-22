package insight.shinanai.distributed_scheduled_task_demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("insight.shinanai.distributed_scheduled_task_demo.mapper")
@EnableTransactionManagement
public class DistributedScheduledTaskDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistributedScheduledTaskDemoApplication.class, args);
    }

}
