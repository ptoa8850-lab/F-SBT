package com.inspien.toto.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 주문 XML 수신 처리: 파싱, 중복 확인, ORDER_ID 부여, ORDER_TB 저장.
 * 한 건이 실패해도 나머지는 계속 처리한다.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderXmlParser parser = new OrderXmlParser();
    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    /** ORDER_ID를 최대값 + 1로 만들기 때문에 동시에 하나씩만 처리한다. */
    public synchronized OrderReceiveResponse receive(String xml) {
        ParseResult parsed = parser.parse(xml);   // XML 형식이 잘못되면 IllegalArgumentException
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
                repository.insert(orderId, row, OrderStatus.NEW);
                last = orderId;   // 저장에 성공한 경우에만 번호를 올린다
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
