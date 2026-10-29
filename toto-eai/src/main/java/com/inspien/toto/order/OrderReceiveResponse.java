package com.inspien.toto.order;

import java.util.List;

/** 주문 XML 수신 결과 (API 응답 본문). 어떤 주문이 저장됐고 무엇이 잘못됐는지 그대로 보여준다. */
public record OrderReceiveResponse(int savedCount, int duplicateCount, int errorCount,
                                   List<String> savedOrderIds, List<String> errors, List<String> warnings) {
}
