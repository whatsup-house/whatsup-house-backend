package com.whatsuphouse.backend.domain.mileage.service;

import com.whatsuphouse.backend.domain.mileage.entity.MileageHistory;
import com.whatsuphouse.backend.domain.mileage.enums.MileageType;
import com.whatsuphouse.backend.domain.mileage.repository.MileageHistoryRepository;
import com.whatsuphouse.backend.domain.review.enums.ReviewType;
import com.whatsuphouse.backend.domain.user.entity.User;
import com.whatsuphouse.backend.global.exception.CustomException;
import com.whatsuphouse.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class MileageService {

    public static final int SIGNUP_REWARD_AMOUNT = 1000;
    public static final int ATTENDANCE_REWARD_AMOUNT = 1000;
    public static final int TEXT_REVIEW_REWARD_AMOUNT = 500;
    public static final int PHOTO_REVIEW_REWARD_AMOUNT = 1000;
    public static final int REVIEW_UPGRADE_REWARD_AMOUNT = 500;

    private final MileageHistoryRepository mileageHistoryRepository;

    public MileageHistory rewardSignup(User user) {
        return earn(user, MileageType.SIGNUP, SIGNUP_REWARD_AMOUNT, user.getId());
    }

    public MileageHistory rewardAttendance(User user, UUID applicationId) {
        return earn(user, MileageType.ATTENDANCE, ATTENDANCE_REWARD_AMOUNT, applicationId);
    }

    public MileageHistory rewardReview(User user, UUID reviewId, ReviewType reviewType) {
        int amount = reviewType == ReviewType.PHOTO ? PHOTO_REVIEW_REWARD_AMOUNT : TEXT_REVIEW_REWARD_AMOUNT;
        return earn(user, MileageType.REVIEW_REWARD, amount, reviewId);
    }

    public MileageHistory rewardReviewUpgrade(User user, UUID reviewId) {
        return earn(user, MileageType.REVIEW_UPGRADE, REVIEW_UPGRADE_REWARD_AMOUNT, reviewId);
    }

    public boolean rewardReviewUpgradeIfAbsent(User user, UUID reviewId) {
        if (mileageHistoryRepository.existsByUserAndTypeAndRelatedId(user, MileageType.REVIEW_UPGRADE, reviewId)) {
            return false;
        }

        earn(user, MileageType.REVIEW_UPGRADE, REVIEW_UPGRADE_REWARD_AMOUNT, reviewId);
        return true;
    }

    public MileageHistory earn(User user, MileageType type, int amount, UUID relatedId) {
        if (mileageHistoryRepository.existsByUserAndTypeAndRelatedId(user, type, relatedId)) {
            throw new CustomException(ErrorCode.MILEAGE_ALREADY_REWARDED);
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
