package com.travelagency.service;

import com.travelagency.domain.Tour;
import com.travelagency.dto.TourResponse;
import com.travelagency.dto.TourSearchRequest;
import com.travelagency.repository.TourRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TourService {

    private final TourRepository tourRepository;
    private final StorageService storageService;

    public TourService(TourRepository tourRepository, StorageService storageService) {
        this.tourRepository = tourRepository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public List<TourResponse> search(TourSearchRequest req) {
        String dest = blankToNull(req.destination());
        String destPattern = dest == null ? null : "%" + dest.toLowerCase() + "%";
        return tourRepository
                .search(
                        destPattern,
                        req.category(),
                        req.dateFrom(),
                        req.dateTo(),
                        req.maxPrice(),
                        req.featured())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TourResponse getBySlug(String slug) {
        Tour tour =
                tourRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException("Tour not found"));
        return toResponse(tour);
    }

    @Transactional(readOnly = true)
    public TourResponse getById(Long id) {
        Tour tour = tourRepository.findById(id).orElseThrow(() -> new NotFoundException("Tour not found"));
        return toResponse(tour);
    }

    public TourResponse toResponse(Tour t) {
        List<String> urls =
                t.getMediaObjectKeys().stream().map(storageService::presignedGetUrl).filter(u -> u != null).toList();
        return new TourResponse(
                t.getId(),
                t.getTitle(),
                t.getSlug(),
                t.getDescription(),
                t.getDestination(),
                t.getStartDate(),
                t.getEndDate(),
                t.getPrice(),
                t.getMaxParticipants(),
                t.getCategory(),
                urls,
                t.isFeatured(),
                new ArrayList<>(t.getHotels()),
                new ArrayList<>(t.getCarriers()),
                new ArrayList<>(t.getTickets()),
                new ArrayList<>(t.getExcursions()),
                new ArrayList<>(t.getLayovers()),
                t.isEnableFlightRegistration(),
                t.isEnableHotelRegistration(),
                t.isEnableRailRegistration());
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
