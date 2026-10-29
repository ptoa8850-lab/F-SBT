package com.inspien.toto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 애플리케이션 시작점.
 * DataSourceAutoConfiguration 을 제외한 이유:
 * DB 접속 정보는 yml 에 적혀 있지 않고, 시작 후 "접속 정보 조회 API"를 호출해 복호화해야 알 수 있습니다.
 * 그래서 DataSource 는 다음 단계에서 직접 코드로 만들 예정입니다.
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class TotoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TotoApplication.class, args);
    }
}
