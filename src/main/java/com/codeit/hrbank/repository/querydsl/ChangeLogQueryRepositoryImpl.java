package com.codeit.hrbank.repository.querydsl;

import com.codeit.hrbank.entity.changeLog.QChangeLog;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChangeLogQueryRepositoryImpl implements ChangeLogQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public boolean existsByAtAfter(Instant lastEndedAt) {
    QChangeLog changeLog = QChangeLog.changeLog;

    Integer fetchOne = queryFactory
        .selectOne()
        .from(changeLog)
        .where(changeLog.at.after(lastEndedAt))
        .fetchFirst();

    return fetchOne != null;
  }
}