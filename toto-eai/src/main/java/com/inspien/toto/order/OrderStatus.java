package com.inspien.toto.order;

/**
 * ORDER_TB.STATUS 값. 주문은 아래 순서로만 진행된다.
 * NEW -> RECEIPT_DONE -> SHIPPED
 * DB 를 보면 "어떤 주문이 어디까지 처리됐는지" 바로 알 수 있다. (실패한 주문은 이전 단계에 머물러 있다)
 */
public final class OrderStatus {
    public static final String NEW = "NEW";                    // 주문 XML 수신, DB 저장 완료
    public static final String RECEIPT_DONE = "RECEIPT_DONE";  // 영수증 FTP 업로드 완료
    public static final String SHIPPED = "SHIPPED";            // 운송회사 DB(SHIPMENT_TB) 등록 완료

    private OrderStatus() {
    }
}
