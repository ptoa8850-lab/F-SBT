package com.inspien.toto.conn;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspien.toto.config.ApplicantProperties;
import com.inspien.toto.config.RecruitingApiProperties;
import com.inspien.toto.crypto.AesUtil;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 슬라이드 5: 이름/전화번호/이메일을 보내 DB·FTP 접속 정보를 받아오고 복호화한다.
 */
@Component
public class ConnectionInfoClient {

    private final ApplicantProperties applicant;
    private final RecruitingApiProperties api;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();

    public ConnectionInfoClient(ApplicantProperties applicant, RecruitingApiProperties api) {
        this.applicant = applicant;
        this.api = api;
    }

    public ConnectionInfo fetch() {
        JsonNode root = call();
        return new ConnectionInfo(
                root.path("APPLICANT_KEY").asText(),
                decryptField(root.path("ORDER_TB_CONN")),
                decryptField(root.path("SHIPMENT_TB_CONN")),
                decryptField(root.path("FTP_CONN")),
                decodeSample(root.path("SAMPLE_DATA").asText(null))
        );
    }

    /** 요청 JSON 을 만들어 POST 하고 응답 JSON 을 돌려준다. */
    private JsonNode call() {
        try {
            // 문자열을 직접 이어붙이지 않고 ObjectMapper 로 만들어 한글/특수문자 깨짐을 막는다.
            Map<String, String> body = new LinkedHashMap<>();
            body.put("NAME", applicant.name());
            body.put("PHONE_NUMBER", applicant.phoneNumber());
            body.put("E_MAIL", applicant.email());

            String basic = Base64.getEncoder().encodeToString(
                    (api.username() + ":" + api.password()).getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder(URI.create(api.url()))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Authorization", "Basic " + basic)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> res = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (res.statusCode() / 100 != 2) {
                throw new IllegalStateException("접속 정보 조회 실패 HTTP " + res.statusCode() + " : " + res.body());
            }
            return mapper.readTree(res.body());
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("접속 정보 조회 API 호출 중 오류", e);
        }
    }

    /**
     * *_CONN 필드가 어떤 모양이어도 처리하기 위한 메서드.
     * - 문자열 하나가 통째로 암호화된 경우: 복호화 후 JSON 이면 펼치고, 아니면 "value" 키로 담는다.
     * - 객체이고 안의 값들이 각각 암호화된 경우: 값마다 복호화한다.
     */
    Map<String, String> decryptField(JsonNode node) {
        Map<String, String> result = new LinkedHashMap<>();
        if (node == null || node.isMissingNode() || node.isNull()) {
            return result;
        }
        try {
            if (node.isTextual()) {
                String plain = AesUtil.decrypt(node.asText(), applicant.phoneNumber());
                JsonNode inner = tryParse(plain);
                if (inner != null && inner.isObject()) {
                    inner.fields().forEachRemaining(e -> result.put(e.getKey(), e.getValue().asText()));
                } else {
                    result.put("value", plain);
                }
            } else if (node.isObject()) {
                node.fields().forEachRemaining(e -> {
                    JsonNode v = e.getValue();
                    result.put(e.getKey(), v.isTextual() ? decryptOrKeep(v.asText()) : v.toString());
                });
            }
        } catch (RuntimeException e) {
            throw new IllegalStateException("접속 정보 복호화 실패", e);
        }
        return result;
    }

    private String decryptOrKeep(String text) {
        try {
            return AesUtil.decrypt(text, applicant.phoneNumber());
        } catch (RuntimeException e) {
            return text; // 암호화되지 않은 평문 값(예: APPLICANT_KEY 성격)은 그대로 둔다.
        }
    }

    private JsonNode tryParse(String s) {
        try {
            return mapper.readTree(s);
        } catch (Exception e) {
            return null;
        }
    }

    /** SAMPLE_DATA: Base64 -> EUC-KR 문자열(XML) */
    private String decodeSample(String base64) {
        if (base64 == null || base64.isBlank()) {
            return null;
        }
        return new String(Base64.getDecoder().decode(base64), Charset.forName("EUC-KR"));
    }
}
