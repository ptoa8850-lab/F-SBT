package com.inspien.toto.order;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 주문 XML 수신 API.   POST /api/orders   (본문: 샘플과 같은 형태의 XML)
 * 본문을 byte[] 로 받아 직접 글자로 바꾼다 (EUC-KR/UTF-8 모두 처리하기 위해).
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> receive(@RequestBody byte[] body, HttpServletRequest request) {
        if (body == null || body.length == 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "요청 본문이 비어 있습니다"));
        }
        try {
            String xml = XmlDecoder.decode(body, request.getCharacterEncoding());
            return ResponseEntity.ok(service.receive(xml));
        } catch (IllegalArgumentException e) {   // XML 형식 오류
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
