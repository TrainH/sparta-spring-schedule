package level1.controller;

import level1.dto.ScheduleRequestDto;
import level1.dto.ScheduleResponseDto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;


import java.time.LocalDateTime;
import java.util.*;


@RestController
@RequestMapping("/api/schedules")

public class ScheduleController {

    private final JdbcTemplate jdbcTemplate;

    public ScheduleController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping()
    public ResponseEntity<ScheduleResponseDto> createSchedule(@RequestBody ScheduleRequestDto requestDto) {

        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("schedule")
                .usingGeneratedKeyColumns("id");

        String now = LocalDateTime.now().toString();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", requestDto.getName());
        parameters.put("pwd", requestDto.getPwd());
        parameters.put("todo", requestDto.getTodo());
        parameters.put("createdAt", now);
        parameters.put("updatedAt", now);


        Number key = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(parameters));

        ScheduleResponseDto scheduleResponseDto = new ScheduleResponseDto(
                key.longValue(),
                requestDto.getName(),
                requestDto.getTodo(),
                now,
                now);

        return new ResponseEntity<>(scheduleResponseDto, HttpStatus.CREATED);
    }


    @GetMapping
    public List<ScheduleResponseDto> findAllSchedules(@RequestBody ScheduleRequestDto requestDto) {
        String name = requestDto.getName();
        String updatedAt = requestDto.getUpdatedAt();

        StringBuilder sql = new StringBuilder("SELECT * FROM schedule WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            sql.append(" AND name = ?");
            params.add(name);
        }

        if (updatedAt != null && !updatedAt.isEmpty()) {
            sql.append(" AND DATE(updatedAt) = ?");
            params.add(updatedAt);
        }

        // SQL 디버깅 출력
        System.out.println("Generated SQL: " + sql);
        System.out.println("Parameters: " + params);

        // 결과 조회
        return jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) ->
                new ScheduleResponseDto(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("todo"),
                        rs.getString("createdAt"),
                        rs.getString("updatedAt")
                )
        );
    }



    @GetMapping("/{id}")
    public ResponseEntity<ScheduleResponseDto> findScheduleById(@PathVariable Long id) {

        List<ScheduleResponseDto> scheduleList = jdbcTemplate.query("SELECT * FROM schedule WHERE id = ?", new Object[]{id}, (rs, rowNum) ->
                new ScheduleResponseDto(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("todo"),
                        rs.getString("createdAt"),
                        rs.getString("updatedAt")
                ));

        if (scheduleList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(scheduleList.get(0), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleResponseDto> updateSchedule(
            @PathVariable Long id, @RequestBody ScheduleRequestDto requestDto
    ) {

        String name = requestDto.getName();
        String todo = requestDto.getTodo();

        // DB에서 해당 ID의 schedule을 조회
        List<ScheduleResponseDto> scheduleList = jdbcTemplate.query("SELECT * FROM schedule WHERE id = ?", new Object[]{id}, (rs, rowNum) ->
                new ScheduleResponseDto(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("pwd"),
                        rs.getString("todo"),
                        rs.getString("createdAt"),
                        rs.getString("updatedAt")
                ));

        // Lv 5. 예외 처리: 선택한 일정 정보를 조회할 수 없을 때
        if (scheduleList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }


        ScheduleResponseDto scheduleFromDb = scheduleList.get(0);

        String now = LocalDateTime.now().toString();

        // Lv 5. 예외 처리: 비밀번호가 일치하지 않을 때
        if (scheduleFromDb.getPwd().equals(requestDto.getPwd())) {
            jdbcTemplate.update(
                    "update schedule set todo = ?, name = ?, updatedAt = ? where id = ?",
                    requestDto.getTodo(),
                    requestDto.getName(),
                    now,
                    id
            );
            ScheduleResponseDto result = new ScheduleResponseDto(id, requestDto.getName(), requestDto.getTodo(),
                                                                requestDto.getUpdatedAt(), now);

            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {

            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id, @RequestBody ScheduleRequestDto requestDto) {

        // DB에서 해당 ID의 schedule을 조회
        List<ScheduleResponseDto> scheduleList = jdbcTemplate.query("SELECT * FROM schedule WHERE id = ?", new Object[]{id}, (rs, rowNum) ->
                new ScheduleResponseDto(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("pwd"),
                        rs.getString("todo"),
                        rs.getString("createdAt"),
                        rs.getString("updatedAt")
                ));

        // Lv 5. 예외 처리: 선택한 일정 정보를 조회할 수 없을 때
        if (scheduleList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ScheduleResponseDto scheduleFromDb = scheduleList.get(0);


        // Lv 5. 예외 처리: 비밀번호가 일치하지 않을 때
        if (scheduleFromDb.getPwd().equals(requestDto.getPwd())){
            jdbcTemplate.update(
                    "delete from schedule where id = ?",
                    id
            );
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
