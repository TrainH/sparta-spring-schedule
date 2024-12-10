package server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonIgnore;


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



    public ScheduleResponseDto(Long id, String name, String todo, String createdAt, String updatedAt) {
        this.id = id;
        this.name = name;
        this.todo = todo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
