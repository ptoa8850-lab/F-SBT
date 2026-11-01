package com.inspien.toto.scheduler;

import com.inspien.toto.shipment.ShipmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** STATUS가 N인 주문을 주기적으로 처리한다 (기본 30초, scheduler.interval-ms로 변경 가능). */
@Component
public class ProcessScheduler {

    private static final Logger log = LoggerFactory.getLogger(ProcessScheduler.class);

    private final ShipmentService shipmentService;

    public ProcessScheduler(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @Scheduled(initialDelayString = "${scheduler.initial-delay-ms:10000}",
            fixedDelayString = "${scheduler.interval-ms:30000}")
    public void run() {
        try {
            shipmentService.processPending();
        } catch (Exception e) {
            log.error("[SCHEDULER] 처리 중 예기치 않은 오류", e);
        }
    }
}
