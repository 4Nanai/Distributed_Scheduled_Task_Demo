package insight.shinanai.distributed_scheduled_task_demo.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum Role {
    USER("USER"),
    ADMIN("ADMIN");

    @EnumValue
    private final String value;

    Role(String value) {
        this.value = value;
    }

    private String getValue() {
        return this.value;
    }
}
