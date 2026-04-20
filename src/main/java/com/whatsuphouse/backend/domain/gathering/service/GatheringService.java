package com.whatsuphouse.backend.domain.gathering.service;

import com.whatsuphouse.backend.domain.gathering.dto.GatheringResponse;
import com.whatsuphouse.backend.domain.gathering.enums.GatheringStatus;
import com.whatsuphouse.backend.domain.gathering.repository.GatheringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GatheringService {

    private final GatheringRepository gatheringRepository;

    public List<GatheringResponse> getGatherings(LocalDate date, GatheringStatus status) {
        if (date != null && status != null) {
            return gatheringRepository.findByEventDateAndStatusAndDeletedAtIsNull(date, status)
                    .stream().map(GatheringResponse::from).toList();
        }
        if (date != null) {
            return gatheringRepository.findByEventDateAndDeletedAtIsNull(date)
                    .stream().map(GatheringResponse::from).toList();
        }
        if (status != null) {
            return gatheringRepository.findByStatusAndDeletedAtIsNull(status)
                    .stream().map(GatheringResponse::from).toList();
        }
        return gatheringRepository.findByDeletedAtIsNull()
                .stream().map(GatheringResponse::from).toList();
    }
}
