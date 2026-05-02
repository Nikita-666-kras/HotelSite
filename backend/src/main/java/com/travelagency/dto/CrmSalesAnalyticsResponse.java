package com.travelagency.dto;

import java.math.BigDecimal;

public record CrmSalesAnalyticsResponse(
        long totalBookings,
        long confirmedBookings,
        long cancelledOrRejected,
        BigDecimal confirmedRevenue,
        BigDecimal conversionRatePercent) {}
