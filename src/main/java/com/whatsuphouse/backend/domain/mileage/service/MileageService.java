package com.whatsuphouse.backend.domain.mileage.service;

import com.whatsuphouse.backend.domain.mileage.entity.MileageHistory;
import com.whatsuphouse.backend.domain.mileage.enums.MileageType;
import com.whatsuphouse.backend.domain.mileage.repository.MileageHistoryRepository;
import com.whatsuphouse.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class MileageService {

    public static final int SIGNUP_REWARD_AMOUNT = 1000;

    private final MileageHistoryRepository mileageHistoryRepository;

    public MileageHistory rewardSignup(User user) {
        return earn(user, MileageType.SIGNUP, SIGNUP_REWARD_AMOUNT, user.getId());
    }

    public MileageHistory earn(User user, MileageType type, int amount, UUID relatedId) {
        if (mileageHistoryRepository.existsByUserAndTypeAndRelatedId(user, type, relatedId)) {
            throw new IllegalStateException("이미 지급된 마일리지입니다.");
        }

        Integer balanceAfter = user.addMileage(amount);
        MileageHistory history = MileageHistory.builder()
                .user(user)
                .type(type)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .relatedId(relatedId)
                .build();

        return mileageHistoryRepository.save(history);
    }
}
