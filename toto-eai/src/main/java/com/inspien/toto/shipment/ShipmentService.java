package com.inspien.toto.shipment;

import com.inspien.toto.order.OrderIdGenerator;
import com.inspien.toto.order.OrderRepository;
import com.inspien.toto.order.OrderStatus;
import com.inspien.toto.order.StoredOrder;
import com.inspien.toto.receipt.ReceiptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * STATUS가 N인 주문을 처리한다: 영수증 확인, SHIPMENT_TB 등록, STATUS를 Y로 변경.
 * 중간에 실패하면 N으로 남아 다음 주기에 다시 시도한다.
 * 두 DB는 한 트랜잭션으로 묶을 수 없어서 SHIPMENT_TB에 이미 있는지 먼저 확인한다.
 */
@Service
public class ShipmentService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentService.class);

    private final OrderRepository orders;
    private final ShipmentRepository shipments;
    private final ReceiptService receiptService;

    public ShipmentService(OrderRepository orders, ShipmentRepository shipments, ReceiptService receiptService) {
        this.orders = orders;
        this.shipments = shipments;
        this.receiptService = receiptService;
    }

    public synchronized int processPending() {
        List<StoredOrder> targets = orders.findByStatus(OrderStatus.NEW);
        if (targets.isEmpty()) {
            return 0;
        }
        log.info("[SHIPMENT] 처리 대상 {}건", targets.size());

        Set<String> receiptOk = receiptService.processNewOrders();

        int done = 0;
        String last = shipments.maxShipmentId();
        for (StoredOrder o : targets) {
            if (!receiptOk.contains(o.orderId())) {
                log.warn("[SHIPMENT] 영수증 미확인 -> 배송 등록 보류 orderId={}", o.orderId());
                continue;
            }
            try {
                if (shipments.existsByOrderId(o.orderId())) {
                    log.info("[SHIPMENT_INSERT] 이미 등록됨 -> 상태만 갱신 orderId={}", o.orderId());
                } else {
                    // SHIPMENT_ID도 ORDER_ID와 같은 형식
                    String shipmentId = OrderIdGenerator.next(last);
                    shipments.insert(shipmentId, o.orderId(), o.row().itemId(), o.row().address());
                    last = shipmentId;
                    log.info("[SHIPMENT_INSERT] 성공 shipmentId={} orderId={}", shipmentId, o.orderId());
                }
                orders.updateStatus(o.orderId(), OrderStatus.DONE);
                done++;
            } catch (Exception e) {
                Throwable root = org.springframework.core.NestedExceptionUtils.getMostSpecificCause(e);
                log.error("[SHIPMENT_INSERT] 실패 orderId={} : {}", o.orderId(), root.getMessage());
            }
        }
        log.info("[SHIPMENT] 완료(Y) {}건 / 대상 {}건", done, targets.size());
        return done;
    }
}
