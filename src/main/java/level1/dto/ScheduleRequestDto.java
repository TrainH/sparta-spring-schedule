package level1.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
public class ScheduleRequestDto {
    private String name;
    private String pwd;
    private String todo;
    private String updatedAt;
}

