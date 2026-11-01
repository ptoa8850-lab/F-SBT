package com.inspien.toto.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** applicant.* 설정 */
@ConfigurationProperties(prefix = "applicant")
public record ApplicantProperties(String name, String phoneNumber, String email) {
}
