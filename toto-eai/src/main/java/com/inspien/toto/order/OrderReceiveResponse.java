package com.inspien.toto.order;

import java.util.List;

/** 주문 XML 수신 결과 */
public record OrderReceiveResponse(int savedCount, int duplicateCount, int errorCount,
                                   List<String> savedOrderIds, List<String> errors, List<String> warnings) {
}
