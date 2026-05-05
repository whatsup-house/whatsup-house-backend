package com.whatsuphouse.backend.domain.user.repository;

import com.whatsuphouse.backend.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<User> findByIdAndDeletedAtIsNull(UUID id);

    @Query(
        value = """
            SELECT u,
                SUM(CASE WHEN a.deletedAt IS NULL THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = com.whatsuphouse.backend.domain.application.enums.ApplicationStatus.ATTENDED
                         AND a.deletedAt IS NULL THEN 1 ELSE 0 END)
            FROM User u
            LEFT JOIN Application a ON a.user = u
            WHERE u.deletedAt IS NULL
              AND (:search IS NULL
                   OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))
            GROUP BY u
            ORDER BY u.createdAt DESC
            """,
        countQuery = """
            SELECT COUNT(DISTINCT u)
            FROM User u
            WHERE u.deletedAt IS NULL
              AND (:search IS NULL
                   OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))
            """
    )
    Page<Object[]> findUsersWithApplicationStats(@Param("search") String search, Pageable pageable);
}
