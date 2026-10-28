package com.inspien.toto.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** application.yml 의 recruiting-api.* 값을 담는 객체 */
@ConfigurationProperties(prefix = "recruiting-api")
public record RecruitingApiProperties(String url, String username, String password) {
}
