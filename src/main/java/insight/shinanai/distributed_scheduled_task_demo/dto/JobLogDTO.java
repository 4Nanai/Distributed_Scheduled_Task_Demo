package insight.shinanai.distributed_scheduled_task_demo.dto;

import insight.shinanai.distributed_scheduled_task_demo.vo.LogVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobLogDTO {
    private String jobId;
    private LogVO logVO;
}
