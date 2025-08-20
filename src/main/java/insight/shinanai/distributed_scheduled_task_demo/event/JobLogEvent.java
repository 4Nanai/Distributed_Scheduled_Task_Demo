package insight.shinanai.distributed_scheduled_task_demo.event;

import insight.shinanai.distributed_scheduled_task_demo.vo.LogVO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class JobLogEvent extends ApplicationEvent {
    private final String jobId;
    private final LogVO logVO;

    public JobLogEvent(Object source, String jobId, LogVO logVO) {
        super(source);
        this.jobId = jobId;
        this.logVO = logVO;
    }
}
