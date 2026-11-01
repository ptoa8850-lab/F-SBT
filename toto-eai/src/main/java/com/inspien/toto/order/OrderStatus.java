package com.inspien.toto.order;

/**
 * ORDER_TB.STATUS 값. 제공된 테이블의 체크 제약이 N, Y만 허용한다.
 * N: 저장됨 (영수증 업로드 또는 배송 등록 전), Y: 영수증 업로드와 배송 등록 완료
 */
public final class OrderStatus {
    public static final String NEW = "N";
    public static final String DONE = "Y";

    private OrderStatus() {
    }
}
