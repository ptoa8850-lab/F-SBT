package com.inspien.toto.order;

import com.inspien.toto.receipt.ReceiptService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService service;
    private final ReceiptService receiptService;

    public OrderController(OrderService service, ReceiptService receiptService) {
        this.service = service;
        this.receiptService = receiptService;
    }

    @PostMapping
    public ResponseEntity<?> receive(@RequestBody byte[] body, HttpServletRequest request) {
        if (body == null || body.length == 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "요청 본문이 비어 있습니다"));
        }
        OrderReceiveResponse response;
        try {
            String xml = XmlDecoder.decode(body, request.getCharacterEncoding());
            response = service.receive(xml);
        } catch (IllegalArgumentException e) {   // XML 형식 오류
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }

        // 저장된 주문의 영수증을 바로 올린다. 실패해도 주문 저장은 이미 끝났으므로 응답은 정상으로 돌려주고,
        // 실패한 건은 NEW 로 남아 스케줄러가 다시 시도한다.
        if (response.savedCount() > 0) {
            try {
                receiptService.processNewOrders();
            } catch (Exception e) {
                log.error("[RECEIPT] 수신 직후 영수증 처리 실패 (스케줄러가 재시도): {}", e.getMessage());
            }
        }
        return ResponseEntity.ok(response);
    }
}
