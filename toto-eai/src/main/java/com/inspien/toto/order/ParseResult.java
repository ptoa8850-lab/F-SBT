package com.inspien.toto.order;

import java.util.List;

/**
 * 파싱 결과.
 * orders   : DB에 넣을 수 있는 정상 주문들
 * errors   : 건너뛴(저장하지 않은) 데이터와 사유 -> 로그/응답으로 돌려줘서 "무엇이 잘못됐는지" 확인 가능하게 함
 * warnings : 저장에는 문제없지만 알려야 할 사항
 */
public record ParseResult(int headerCount, int itemCount,
                          List<OrderRow> orders, List<String> errors, List<String> warnings) {
}
