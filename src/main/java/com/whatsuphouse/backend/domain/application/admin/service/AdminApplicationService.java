package com.whatsuphouse.backend.domain.application.admin.service;

import com.whatsuphouse.backend.domain.application.admin.dto.request.AdminApplicationStatusRequest;
import com.whatsuphouse.backend.domain.application.admin.dto.response.AdminApplicationDeleteResponse;
import com.whatsuphouse.backend.domain.application.admin.dto.response.AdminApplicationResponse;
import com.whatsuphouse.backend.domain.application.admin.dto.response.AdminApplicationStatusResponse;
import com.whatsuphouse.backend.domain.application.entity.Application;
import com.whatsuphouse.backend.domain.application.enums.ApplicationStatus;
import com.whatsuphouse.backend.domain.application.repository.ApplicationRepository;
import com.whatsuphouse.backend.domain.mileage.entity.MileageHistory;
import com.whatsuphouse.backend.domain.mileage.service.MileageService;
import com.whatsuphouse.backend.domain.user.entity.User;
import com.whatsuphouse.backend.global.exception.CustomException;
import com.whatsuphouse.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminApplicationService {

    private final ApplicationRepository applicationRepository;
    private final MileageService mileageService;

    public List<AdminApplicationResponse> getAllApplications(UUID gatheringId, ApplicationStatus status) {
        if (gatheringId != null) {
            return applicationRepository.findByGatheringIdAndDeletedAtIsNull(gatheringId)
                    .stream()
                    .map(AdminApplicationResponse::from)
                    .toList();
        }
        if (status != null) {
            return applicationRepository.findByStatusAndDeletedAtIsNull(status)
                    .stream()
                    .map(AdminApplicationResponse::from)
                    .toList();
        }
        return applicationRepository.findByDeletedAtIsNull()
                .stream()
                .map(AdminApplicationResponse::from)
                .toList();
    }

    public AdminApplicationResponse getApplication(UUID id) {
        Application application = applicationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));
        return AdminApplicationResponse.from(application);
    }

    @Transactional
    public AdminApplicationDeleteResponse deleteApplication(UUID id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if (application.getStatus() == ApplicationStatus.CANCELLED && application.getDeletedAt() != null) {
            return AdminApplicationDeleteResponse.from(application);
        }

        if (application.getStatus() == ApplicationStatus.ATTENDED) {
            throw new CustomException(ErrorCode.CANNOT_DELETE);
        }

        application.cancel();
        return AdminApplicationDeleteResponse.from(application);
    }

    @Transactional
    public AdminApplicationStatusResponse changeStatus(UUID id, AdminApplicationStatusRequest request) {
        Application application = applicationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        ApplicationStatus newStatus = request.getStatus();
        switch (newStatus) {
            case CONFIRMED -> application.confirm();
            case CANCELLED -> application.cancel();
            case ATTENDED -> {
                if (application.getStatus() == ApplicationStatus.ATTENDED) {
                    throw new CustomException(ErrorCode.ALREADY_ATTENDED);
                }
                application.attend();
                return rewardAttendanceMileage(application);
            }
            default -> throw new CustomException(ErrorCode.CANNOT_CANCEL);
        }

        return AdminApplicationStatusResponse.of(application.getId(), application.getStatus(), null, null);
    }

    private AdminApplicationStatusResponse rewardAttendanceMileage(Application application) {
        User user = application.getUser();
        if (user == null) {
            return AdminApplicationStatusResponse.of(application.getId(), application.getStatus(), null, null);
        }

        MileageHistory history = mileageService.rewardAttendance(user, application.getId());
        return AdminApplicationStatusResponse.of(
                application.getId(),
                application.getStatus(),
                history.getAmount(),
                history.getBalanceAfter()
        );
    }
}
