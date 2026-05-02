package com.travelagency.dto;

import java.util.List;

public record BookingPipelinePageResponse(
        List<BookingResponse> content,
        long totalElements,
        int totalPages,
        int page,
        int size,
        long countInWork,
        long countConfirmed) {}
