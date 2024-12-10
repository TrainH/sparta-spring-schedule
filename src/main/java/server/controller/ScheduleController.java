package server.controller;

import server.dto.ScheduleRequestDto;
import server.dto.ScheduleResponseDto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;


import java.time.LocalDateTime;
import java.util.*;

// CRUD 필수 기능은 모두 데이터베이스 연결 및 JDBC 를 사용해서 개발

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final JdbcTemplate jdbcTemplate;

    public ScheduleController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // v 1. 일정 생성 및 조회: 일정 생성(일정 작성하기)
    @PostMapping()
    public ResponseEntity<ScheduleResponseDto> createSchedule(@RequestBody ScheduleRequestDto requestDto) {

        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("schedule")
                .usingGeneratedKeyColumns("id");

        String now = LocalDateTime.now().toString(); // 현재시간

        // 할일, 작성자명, 비밀번호, 작성/수정일을 저장
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", requestDto.getName());
        parameters.put("pwd", requestDto.getPwd());
        parameters.put("todo", requestDto.getTodo());
        parameters.put("createdAt", now);
        parameters.put("updatedAt", now);

        // 각 일정의 고유 식별자(ID)를 자동으로 생성하고 새로운 id(key)를 반환
        Number key = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(parameters));

        // 비밀번호 제외하고 반환
        ScheduleResponseDto scheduleResponseDto = new ScheduleResponseDto(
                key.longValue(),
                requestDto.getName(),
                requestDto.getTodo(),
                now,
                now);

        return new ResponseEntity<>(scheduleResponseDto, HttpStatus.CREATED);
    }

    // v 1. 일정 생성 및 조회: 전체 일정 조회(등록된 일정 불러오기)
    @GetMapping
    public List<ScheduleResponseDto> findAllSchedules(@RequestBody ScheduleRequestDto requestDto) {

        // 요청 입력값
        // - `수정일` (형식 : YYYY-MM-DD)
        // - `작성자명`
        String name = requestDto.getName();
        String updatedAt = requestDto.getUpdatedAt();

        // 쿼리에 name과 updatedAt의 값 유무 따라서 DB 쿼리 조회 조건 입력
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

        List<ScheduleResponseDto> scheduleList = jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) ->
                new ScheduleResponseDto(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("todo"),
                        rs.getString("createdAt"),
                        rs.getString("updatedAt")
                )
        );

        // 결과 조회
        return scheduleList;
    }


    // v 1. 일정 생성 및 조회: 선택 일정 조회(선택한 일정 정보 불러오기)
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
        // 예외 처리: 데이터가 비어 잇으면 Not Found
        if (scheduleList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(scheduleList.get(0), HttpStatus.OK);
    }
    // Lv 2. 일정 수정 및 삭제 : 선택한 일정 수정
    @PutMapping("/{id}")
    public ResponseEntity<ScheduleResponseDto> updateSchedule(
            @PathVariable Long id, @RequestBody ScheduleRequestDto requestDto
    ) {
        // 작성자명, 할일만 수정 가능
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
    // Lv 2. 일정 수정 및 삭제 : 선택한 일정 삭제
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
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
