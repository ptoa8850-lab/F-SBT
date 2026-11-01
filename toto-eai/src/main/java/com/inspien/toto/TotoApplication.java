package com.inspien.toto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** DataSource는 DataSourceConfig에서 직접 만들기 때문에 자동 설정은 제외한다. */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class TotoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TotoApplication.class, args);
    }
}
