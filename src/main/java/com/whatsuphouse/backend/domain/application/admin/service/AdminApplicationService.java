package com.whatsuphouse.backend.domain.application.admin.service;

import com.whatsuphouse.backend.domain.application.admin.dto.request.AdminApplicationStatusRequest;
import com.whatsuphouse.backend.domain.application.admin.dto.response.AdminApplicationDeleteResponse;
import com.whatsuphouse.backend.domain.application.admin.dto.response.AdminApplicationResponse;
import com.whatsuphouse.backend.domain.application.entity.Application;
import com.whatsuphouse.backend.domain.application.enums.ApplicationStatus;
import com.whatsuphouse.backend.domain.application.repository.ApplicationRepository;
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

    public List<AdminApplicationResponse> getAllApplications(UUID gatheringId, ApplicationStatus status) {
        return applicationRepository.findApplications(gatheringId, status)
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
    public AdminApplicationResponse changeStatus(UUID id, AdminApplicationStatusRequest request) {
        Application application = applicationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        ApplicationStatus newStatus = request.getStatus();
        switch (newStatus) {
            case CONFIRMED -> application.confirm();
            case ATTENDED -> application.attend();
            default -> throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        return AdminApplicationResponse.from(application);
    }
}
