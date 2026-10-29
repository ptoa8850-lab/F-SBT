package com.inspien.toto.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yml 의 db.* 설정.
 *
 * db.mode = local    : 내 PC 의 Oracle 사용 (db.local.* 값으로 접속)
 * db.mode = provided : 과제에서 제공하는 DB 사용 (접속 정보 API 에서 받아 복호화한 값으로 접속)
 */
@ConfigurationProperties(prefix = "db")
public record DbProperties(String mode, Local local) {

    public record Local(String url, String username, String password) {
    }

    public boolean isLocal() {
        return mode == null || mode.equalsIgnoreCase("local");
    }
}
