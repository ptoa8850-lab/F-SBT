package com.inspien.toto.order;

import com.inspien.toto.shipment.ShipmentRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/** 처리 현황 조회 (GET /api/summary): 상태별 주문 건수와 배송 등록 건수 */
@RestController
@RequestMapping("/api/summary")
public class SummaryController {

    private final OrderRepository orders;
    private final ShipmentRepository shipments;

    public SummaryController(OrderRepository orders, ShipmentRepository shipments) {
        this.orders = orders;
        this.shipments = shipments;
    }

    @GetMapping
    public Map<String, Object> summary() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderStatusCounts", orders.countByStatus());
        result.put("shipmentCount", shipments.count());
        return result;
    }
}
