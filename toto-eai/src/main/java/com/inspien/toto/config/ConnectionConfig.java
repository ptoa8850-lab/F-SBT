package com.inspien.toto.config;

import com.inspien.toto.conn.ConnectionInfo;
import com.inspien.toto.conn.ConnectionInfoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 접속 정보 조회 API 를 앱 시작 시 한 번만 호출해서, 결과를 빈(Bean)으로 공유한다. */
@Configuration
public class ConnectionConfig {

    @Bean
    public ConnectionInfo connectionInfo(ConnectionInfoClient client) {
        return client.fetch();
    }
}
