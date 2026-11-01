package com.inspien.toto.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * db.mode가 local이면 db.local.* 의 로컬 Oracle을,
 * provided이면 접속 정보 API로 받은 제공 DB를 사용한다.
 */
@ConfigurationProperties(prefix = "db")
public record DbProperties(String mode, Local local) {

    public record Local(String url, String username, String password) {
    }

    public boolean isLocal() {
        return mode == null || mode.equalsIgnoreCase("local");
    }
}
