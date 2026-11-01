package com.inspien.toto.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** recruiting-api.* 설정 */
@ConfigurationProperties(prefix = "recruiting-api")
public record RecruitingApiProperties(String url, String username, String password) {
}
