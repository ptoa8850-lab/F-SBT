package com.inspien.toto.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 주문 XML 수신 처리: 파싱 -> 중복 확인 -> ORDER_ID 부여 -> ORDER_TB INSERT.
 * 한 건이 실패해도 나머지는 계속 처리하고, 실패 사유는 응답과 로그에 남긴다.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    /** 수신 직후 상태. 이후 단계(영수증 업로드, 배송 등록)에서 값이 바뀐다. */
    public static final String STATUS_NEW = "NEW";

    private final OrderXmlParser parser = new OrderXmlParser();
    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    /** ORDER_ID 를 "현재 최대값 + 1" 로 만들기 때문에, 동시에 두 요청이 들어와도 겹치지 않도록 한 번에 하나씩 처리한다. */
    public synchronized OrderReceiveResponse receive(String xml) {
        ParseResult parsed = parser.parse(xml);   // XML 형식이 깨졌으면 IllegalArgumentException
        log.info("[RECEIVE] HEADER={}건, ITEM={}건, 정상조립={}건, 오류={}건, 경고={}건",
                parsed.headerCount(), parsed.itemCount(), parsed.orders().size(),
                parsed.errors().size(), parsed.warnings().size());
        parsed.errors().forEach(m -> log.warn("[RECEIVE][오류] {}", m));
        parsed.warnings().forEach(m -> log.warn("[RECEIVE][경고] {}", m));

        List<String> savedIds = new ArrayList<>();
        List<String> errors = new ArrayList<>(parsed.errors());
        int duplicates = 0;

        String last = repository.maxOrderId();
        for (OrderRow row : parsed.orders()) {
            String label = "USER_ID=" + row.userId() + ", ITEM_ID=" + row.itemId();
            try {
                if (repository.exists(row.userId(), row.itemId())) {
                    duplicates++;
                    log.info("[DB_INSERT] 이미 저장됨 -> 건너뜀 ({})", label);
                    continue;
                }
                String orderId = OrderIdGenerator.next(last);
                repository.insert(orderId, row, STATUS_NEW);
                last = orderId;   // 저장에 성공했을 때만 번호를 소비한다
                savedIds.add(orderId);
                log.info("[DB_INSERT] 성공 orderId={} ({})", orderId, label);
            } catch (Exception e) {
                String msg = "ORDER_TB 저장 실패 (" + label + "): " + e.getMessage();
                errors.add(msg);
                log.error("[DB_INSERT] {}", msg, e);
            }
        }

        return new OrderReceiveResponse(savedIds.size(), duplicates, errors.size(),
                savedIds, errors, parsed.warnings());
    }
}
