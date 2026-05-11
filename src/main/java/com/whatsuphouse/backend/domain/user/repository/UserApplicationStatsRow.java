package com.whatsuphouse.backend.domain.user.repository;

import com.whatsuphouse.backend.domain.user.entity.User;

public record UserApplicationStatsRow(User user, long totalApplications, long attendedCount) {
}
