package com.codeit.hrbank.repository.querydsl;

import java.time.Instant;

public interface ChangeLogQueryRepository {
   boolean existsByAtAfter(Instant lastEndedAt);
}
