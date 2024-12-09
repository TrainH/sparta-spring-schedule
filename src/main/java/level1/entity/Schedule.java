package level1.entity;

import level1.dto.ScheduleRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
public class Schedule {
    private Long id;
    private String name;
    private String pwd;
    private String todo;
    private String createdAt;
    private String updatedAt;

    public Schedule(String name, String pwd, String todo, String createdAt, String updatedAt){
        this.name = name;
        this.pwd = pwd;
        this.todo= todo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void update(ScheduleRequestDto requestDto) {
        this.name = requestDto.getName();
        this.pwd = requestDto.getPwd();
        this.todo = requestDto.getTodo();
        this.updatedAt = requestDto.getUpdatedAt();
    }

    public void updateTodo(ScheduleRequestDto requestDto){this.todo = requestDto.getTodo();}

}