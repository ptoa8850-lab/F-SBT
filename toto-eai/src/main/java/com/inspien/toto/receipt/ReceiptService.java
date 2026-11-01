package com.inspien.toto.receipt;

import com.inspien.toto.config.ApplicantProperties;
import com.inspien.toto.ftp.FtpReceiptClient;
import com.inspien.toto.order.OrderRepository;
import com.inspien.toto.order.OrderStatus;
import com.inspien.toto.order.StoredOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * STATUS가 N인 주문의 영수증 XML을 FTP에 올린다.
 * 같은 이름의 파일이 이미 있으면 올리지 않으므로 여러 번 실행해도 중복되지 않는다.
 */
@Service
public class ReceiptService {

    private static final Logger log = LoggerFactory.getLogger(ReceiptService.class);

    private final OrderRepository orders;
    private final FtpReceiptClient ftp;
    private final ApplicantProperties applicant;

    public ReceiptService(OrderRepository orders, FtpReceiptClient ftp, ApplicantProperties applicant) {
        this.orders = orders;
        this.ftp = ftp;
        this.applicant = applicant;
    }

    /**
     * 주문 수신과 스케줄러가 동시에 실행하지 않도록 한 번에 하나만 실행한다.
     * 반환값은 영수증이 FTP에 있는 것으로 확인된 ORDER_ID 목록이다.
     */
    public synchronized Set<String> processNewOrders() {
        Set<String> receiptOk = new HashSet<>();
        List<StoredOrder> targets = orders.findByStatus(OrderStatus.NEW);
        if (targets.isEmpty()) {
            return receiptOk;
        }
        log.info("[RECEIPT] 영수증 확인 대상 {}건", targets.size());

        try (FtpReceiptClient.Session session = ftp.open()) {
            for (StoredOrder o : targets) {
                String fileName = ReceiptXmlBuilder.fileName(applicant.name(), o.orderId());
                try {
                    if (session.exists(fileName)) {
                        // 덮어쓰기가 안 되므로 이미 있으면 올린 것으로 본다
                        log.info("[FTP_UPLOAD] 이미 존재 -> 업로드 건너뜀 orderId={} file={}", o.orderId(), fileName);
                    } else {
                        String xml = ReceiptXmlBuilder.build(o.orderId(), o.row(), LocalDateTime.now());
                        session.upload(fileName, xml.getBytes(StandardCharsets.UTF_8));
                        log.info("[FTP_UPLOAD] 성공 orderId={} file={}", o.orderId(), fileName);
                    }
                    receiptOk.add(o.orderId());
                } catch (Exception e) {
                    log.error("[FTP_UPLOAD] 실패 orderId={} file={} : {}", o.orderId(), fileName, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("[FTP] 접속 실패 -> 영수증 처리 중단 (주문은 N 으로 남음): {}", e.getMessage());
        }
        log.info("[RECEIPT] 영수증 확인됨 {}건 / 대상 {}건", receiptOk.size(), targets.size());
        return receiptOk;
    }
}
