package server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 실행하면 빈으로 등록된 @RestController가 등록되고 (@Service, @Repository는 현 단계에서는 복잡해서 생략함)
// HTTP 호출 시에 @RestController가 있는 ScheduleController가 실행됨

@SpringBootApplication
public class SpringScheduleApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringScheduleApplication.class, args);
    }
}
