package com.inspien.toto.shipment;

import com.inspien.toto.order.OrderIdGenerator;
import com.inspien.toto.order.OrderRepository;
import com.inspien.toto.order.OrderStatus;
import com.inspien.toto.order.StoredOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 배송 처리: 영수증까지 끝난(RECEIPT_DONE) 주문을 운송회사 DB(SHIPMENT_TB)에 넣고 SHIPPED 로 바꾼다.
 *
 * 주문 DB 와 운송 DB 는 서로 다른 DB 일 수 있어 하나의 트랜잭션으로 묶을 수 없다.
 * 그래서 "SHIPMENT_TB 에 이미 있으면 다시 넣지 않는다"는 확인을 먼저 해서,
 * 중간에 실패해도 다시 실행했을 때 배송 정보가 중복으로 들어가지 않게 했다.
 */
@Service
public class ShipmentService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentService.class);

    private final OrderRepository orders;
    private final ShipmentRepository shipments;

    public ShipmentService(OrderRepository orders, ShipmentRepository shipments) {
        this.orders = orders;
        this.shipments = shipments;
    }

    public synchronized int processReceivedOrders() {
        List<StoredOrder> targets = orders.findByStatus(OrderStatus.RECEIPT_DONE);
        if (targets.isEmpty()) {
            return 0;
        }
        log.info("[SHIPMENT] 배송 등록 대상 {}건", targets.size());

        int done = 0;
        String last = shipments.maxShipmentId();
        for (StoredOrder o : targets) {
            try {
                if (shipments.existsByOrderId(o.orderId())) {
                    log.info("[SHIPMENT_INSERT] 이미 등록됨 -> 상태만 갱신 orderId={}", o.orderId());
                } else {
                    // SHIPMENT_ID 도 ORDER_ID 와 같은 규칙(대문자 1자 + 숫자 3자리)
                    String shipmentId = OrderIdGenerator.next(last);
                    shipments.insert(shipmentId, o.orderId(), o.row().itemId(), o.row().address());
                    last = shipmentId;
                    log.info("[SHIPMENT_INSERT] 성공 shipmentId={} orderId={}", shipmentId, o.orderId());
                }
                orders.updateStatus(o.orderId(), OrderStatus.SHIPPED);
                done++;
            } catch (Exception e) {
                log.error("[SHIPMENT_INSERT] 실패 orderId={} : {}", o.orderId(), e.getMessage());
            }
        }
        log.info("[SHIPMENT] 완료 {}건 / 대상 {}건", done, targets.size());
        return done;
    }
}
