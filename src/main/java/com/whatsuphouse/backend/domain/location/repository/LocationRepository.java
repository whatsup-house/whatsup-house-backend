package com.whatsuphouse.backend.domain.location.repository;

import com.whatsuphouse.backend.domain.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {

    List<Location> findByDeletedAtIsNull();
    Optional<Location> findByIdAndDeletedAtIsNull(UUID id);
}
