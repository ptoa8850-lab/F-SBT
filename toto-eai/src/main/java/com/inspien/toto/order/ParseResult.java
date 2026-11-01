package com.inspien.toto.order;

import java.util.List;

/** 파싱 결과. orders는 저장할 주문, errors는 건너뛴 데이터와 사유, warnings는 참고 사항이다. */
public record ParseResult(int headerCount, int itemCount,
                          List<OrderRow> orders, List<String> errors, List<String> warnings) {
}
