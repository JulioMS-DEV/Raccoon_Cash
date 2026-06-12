package com.raccooncash.api.transaction;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepositoryCustom {
    List<Transaction> findWithFilters(Long accountId,
                                      Long categoryId,
                                      TransactionType type,
                                      LocalDateTime fromDate,
                                      LocalDateTime toDate);
}
