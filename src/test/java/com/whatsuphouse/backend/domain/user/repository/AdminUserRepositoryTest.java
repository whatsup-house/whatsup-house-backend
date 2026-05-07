package com.whatsuphouse.backend.domain.user.repository;

import com.whatsuphouse.backend.domain.application.entity.Application;
import com.whatsuphouse.backend.domain.application.repository.ApplicationRepository;
import com.whatsuphouse.backend.domain.gathering.entity.Gathering;
import com.whatsuphouse.backend.domain.gathering.repository.GatheringRepository;
import com.whatsuphouse.backend.domain.user.entity.User;
import com.whatsuphouse.backend.global.common.enums.Gender;
import com.whatsuphouse.backend.global.config.TestJpaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestJpaConfig.class)
@ActiveProfiles("test")
class AdminUserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private GatheringRepository gatheringRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private Gathering gathering;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .email("test@example.com")
                .password("encoded")
                .name("홍길동")
                .gender(Gender.MALE)
                .age(25)
                .nickname("gildong")
                .phone("01012345678")
                .build());

        gathering = gatheringRepository.save(Gathering.builder()
                .title("테스트 게더링")
                .eventDate(LocalDate.now().plusDays(7))
                .maxAttendees(10)
                .build());

        // PENDING 신청 1건
        applicationRepository.save(Application.builder()
                .bookingNumber("WH001")
                .gathering(gathering)
                .user(user)
                .name(user.getName())
                .phone(user.getPhone())
                .build());

        // ATTENDED 신청 1건
        Application attended = applicationRepository.save(Application.builder()
                .bookingNumber("WH002")
                .gathering(gathering)
                .user(user)
                .name(user.getName())
                .phone(user.getPhone())
                .build());
        attended.attend();

        // CANCELLED 신청 1건 (cancel()이 내부적으로 soft-delete까지 처리)
        Application cancelled = applicationRepository.save(Application.builder()
                .bookingNumber("WH003")
                .gathering(gathering)
                .user(user)
                .name(user.getName())
                .phone(user.getPhone())
                .build());
        cancelled.cancel();

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("search=null이면 삭제되지 않은 전체 회원 반환")
    void findUsersWithStats_nullSearch_returnsAllActiveUsers() {
        // TODO(human): search=null, page=0, size=20으로 쿼리 실행 후
        // 결과의 totalElements가 1이고 row[0]이 user임을 검증해줘.
        Page<Object[]> result = userRepository.findUsersWithApplicationStats(null, PageRequest.of(0, 20));
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(((User) result.getContent().get(0)[0]).getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("닉네임 부분 일치 검색")
    void findUsersWithStats_searchByNickname() {
        // TODO(human): search="gild"로 조회 시 user가 포함되는지,
        // search="xyz"로 조회 시 결과가 비어있는지 검증해줘.
        // "gild" → user가 있어야 함
        Page<Object[]> hit = userRepository.findUsersWithApplicationStats("gild",
                PageRequest.of(0, 20));
        assertThat(hit.getTotalElements()).isEqualTo(1);
        assertThat(((User)
                hit.getContent().get(0)[0]).getEmail()).isEqualTo("test@example.com");

        // "xyz" → 아무도 없어야 함
        Page<Object[]> miss = userRepository.findUsersWithApplicationStats("xyz",
                PageRequest.of(0, 20));
        assertThat(miss.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("totalApplications는 CANCELLED(soft-deleted) 제외, attendedCount는 ATTENDED만 집계")
    void findUsersWithStats_aggregatesCorrectly() {
        // TODO(human): row[1](totalApplications)이 2L(PENDING+ATTENDED),
        // row[2](attendedCount)가 1L임을 검증해줘.
        Page<Object[]> result = userRepository.findUsersWithApplicationStats(null,
                PageRequest.of(0, 20));
        Object[] row = result.getContent().get(0);
        assertThat(((Number) row[1]).longValue()).isEqualTo(2L);  // PENDING + ATTENDED
        assertThat(((Number) row[2]).longValue()).isEqualTo(1L);  // ATTENDED만
    }

    @Test
    @DisplayName("삭제된 회원은 결과에서 제외")
    void findUsersWithStats_excludesDeletedUsers() {
        User deletedUser = userRepository.save(User.builder()
                .email("deleted@example.com")
                .password("encoded")
                .name("삭제유저")
                .gender(Gender.FEMALE)
                .age(30)
                .nickname("deleted")
                .phone("01099999999")
                .build());
        deletedUser.delete();
        em.flush();
        em.clear();

        Page<Object[]> result = userRepository.findUsersWithApplicationStats(null, PageRequest.of(0, 20));

        assertThat(result.getContent())
                .extracting(row -> ((User) row[0]).getEmail())
                .doesNotContain("deleted@example.com");
    }
}
