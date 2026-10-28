package com.inspien.toto.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** application.yml 의 applicant.* 값을 담는 객체 */
@ConfigurationProperties(prefix = "applicant")
public record ApplicantProperties(String name, String phoneNumber, String email) {
}
