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
import java.util.List;

/**
 * 영수증 처리: STATUS=NEW 인 주문마다 영수증 XML 을 만들어 FTP 에 올리고, 성공하면 RECEIPT_DONE 으로 바꾼다.
 * 실패한 주문은 NEW 로 남아서 다음 실행 때 자동으로 다시 시도된다.
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

    /** 수신 요청과 스케줄러가 동시에 실행해도 같은 파일을 두 번 올리지 않도록 한 번에 하나만 실행한다. */
    public synchronized int processNewOrders() {
        List<StoredOrder> targets = orders.findByStatus(OrderStatus.NEW);
        if (targets.isEmpty()) {
            return 0;
        }
        log.info("[RECEIPT] 영수증 처리 대상 {}건", targets.size());

        int done = 0;
        try (FtpReceiptClient.Session session = ftp.open()) {
            for (StoredOrder o : targets) {
                String fileName = ReceiptXmlBuilder.fileName(applicant.name(), o.orderId());
                try {
                    if (session.exists(fileName)) {
                        // FTP 는 덮어쓰기가 안 되므로, 이미 있으면 이전에 올린 것으로 보고 상태만 갱신한다
                        log.info("[FTP_UPLOAD] 이미 존재 -> 업로드 건너뜀 orderId={} file={}", o.orderId(), fileName);
                    } else {
                        String xml = ReceiptXmlBuilder.build(o.orderId(), o.row(), LocalDateTime.now());
                        session.upload(fileName, xml.getBytes(StandardCharsets.UTF_8));
                        log.info("[FTP_UPLOAD] 성공 orderId={} file={}", o.orderId(), fileName);
                    }
                    orders.updateStatus(o.orderId(), OrderStatus.RECEIPT_DONE);
                    done++;
                } catch (Exception e) {
                    log.error("[FTP_UPLOAD] 실패 orderId={} file={} : {}", o.orderId(), fileName, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("[FTP] 접속 실패 -> 영수증 처리 중단 (주문은 NEW 로 남음): {}", e.getMessage());
        }
        log.info("[RECEIPT] 완료 {}건 / 대상 {}건", done, targets.size());
        return done;
    }
}
