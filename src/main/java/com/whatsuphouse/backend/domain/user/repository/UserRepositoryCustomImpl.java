package com.whatsuphouse.backend.domain.user.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.whatsuphouse.backend.domain.application.entity.QApplication;
import com.whatsuphouse.backend.domain.application.enums.ApplicationStatus;
import com.whatsuphouse.backend.domain.user.entity.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Object[]> findUsersWithApplicationStats(String search, Pageable pageable) {
        QUser user = QUser.user;
        QApplication application = QApplication.application;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(user.deletedAt.isNull());
        if (search != null && !search.isBlank()) {
            builder.and(
                    user.nickname.containsIgnoreCase(search)
                            .or(user.email.containsIgnoreCase(search))
            );
        }

        var attendedCount = new CaseBuilder()
                .when(application.status.eq(ApplicationStatus.ATTENDED)
                        .and(application.deletedAt.isNull()))
                .then(1L)
                .otherwise(0L).sum();

        List<Tuple> tuples = queryFactory
                .select(user, application.id.count(), attendedCount)
                .from(user)
                .leftJoin(application).on(
                        application.user.eq(user)
                                .and(application.deletedAt.isNull())
                )
                .where(builder)
                .groupBy(user)
                .orderBy(user.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(user.count())
                .from(user)
                .where(builder)
                .fetchOne();

        List<Object[]> results = tuples.stream()
                .map(t -> new Object[]{
                        t.get(user),
                        t.get(application.id.count()),
                        t.get(attendedCount)
                })
                .toList();

        return new PageImpl<>(results, pageable, total);
    }
}
