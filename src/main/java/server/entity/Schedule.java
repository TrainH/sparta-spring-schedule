package server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;



@Getter
@AllArgsConstructor
public class Schedule {
    private Long id;
    private String name;
    private String pwd;
    private String todo;
    private String createdAt;
    private String updatedAt;
}