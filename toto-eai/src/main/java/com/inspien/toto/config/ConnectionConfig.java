package com.inspien.toto.config;

import com.inspien.toto.conn.ConnectionInfo;
import com.inspien.toto.conn.ConnectionInfoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 접속 정보 조회 API는 시작할 때 한 번만 호출하고 결과를 Bean으로 공유한다. */
@Configuration
public class ConnectionConfig {

    @Bean
    public ConnectionInfo connectionInfo(ConnectionInfoClient client) {
        return client.fetch();
    }
}
