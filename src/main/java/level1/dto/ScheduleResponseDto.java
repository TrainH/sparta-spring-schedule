package level1.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import level1.entity.Schedule;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor

public class ScheduleResponseDto {
    private Long id;
    private String name;

    @JsonIgnore
    private String pwd;

    private String todo;

    @JsonIgnore
    private String createdAt;
    private String updatedAt;



    public ScheduleResponseDto(Schedule schedule) {
        this.id = schedule.getId();
        this.name = schedule.getName();
        this.pwd = schedule.getPwd();
        this.todo = schedule.getTodo();
        this.createdAt = schedule.getCreatedAt();
        this.updatedAt = schedule.getUpdatedAt();
    }

    public ScheduleResponseDto(Long id, String name, String todo, String createdAt, String updatedAt) {
        this.id = id;
        this.name = name;
        this.todo = todo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
