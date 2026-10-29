package com.inspien.toto.scheduler;

import com.inspien.toto.receipt.ReceiptService;
import com.inspien.toto.shipment.ShipmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 주기적으로 밀린 작업을 처리한다. (기본 30초마다)
 *  1) NEW          -> 영수증 FTP 업로드 -> RECEIPT_DONE
 *  2) RECEIPT_DONE -> 운송 DB 등록      -> SHIPPED
 * 한 단계가 실패해도 다음 단계와 다음 주기는 계속 실행된다.
 * 주기는 application.yml 의 scheduler.interval-ms 로 바꿀 수 있다.
 */
@Component
public class ProcessScheduler {

    private static final Logger log = LoggerFactory.getLogger(ProcessScheduler.class);

    private final ReceiptService receiptService;
    private final ShipmentService shipmentService;

    public ProcessScheduler(ReceiptService receiptService, ShipmentService shipmentService) {
        this.receiptService = receiptService;
        this.shipmentService = shipmentService;
    }

    @Scheduled(initialDelayString = "${scheduler.initial-delay-ms:10000}",
            fixedDelayString = "${scheduler.interval-ms:30000}")
    public void run() {
        try {
            receiptService.processNewOrders();
        } catch (Exception e) {
            log.error("[SCHEDULER] 영수증 처리 중 예기치 않은 오류", e);
        }
        try {
            shipmentService.processReceivedOrders();
        } catch (Exception e) {
            log.error("[SCHEDULER] 배송 처리 중 예기치 않은 오류", e);
        }
    }
}
