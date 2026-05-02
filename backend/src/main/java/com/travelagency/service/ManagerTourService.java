package com.travelagency.service;

import com.travelagency.domain.Tour;
import com.travelagency.dto.TourManagerResponse;
import com.travelagency.dto.TourWriteRequest;
import com.travelagency.repository.TourRepository;
import com.travelagency.util.SlugUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ManagerTourService {

    private final TourRepository tourRepository;
    private final TourService tourService;
    private final StorageService storageService;

    public ManagerTourService(TourRepository tourRepository, TourService tourService, StorageService storageService) {
        this.tourRepository = tourRepository;
        this.tourService = tourService;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public List<TourManagerResponse> list() {
        return tourRepository.findAllByOrderByIdDesc().stream().map(this::wrap).toList();
    }

    @Transactional
    public TourManagerResponse create(TourWriteRequest req) {
        String slug = buildSlug(req, null);
        Tour t = new Tour();
        apply(t, req, slug);
        tourRepository.save(t);
        return wrap(t);
    }

    @Transactional
    public TourManagerResponse update(UUID id, TourWriteRequest req) {
        Tour t = tourRepository.findById(id).orElseThrow(() -> new NotFoundException("Tour not found"));
        String slug = buildSlug(req, id);
        apply(t, req, slug);
        tourRepository.save(t);
        return wrap(t);
    }

    private TourManagerResponse wrap(Tour t) {
        return TourManagerResponse.of(tourService.toResponse(t), new ArrayList<>(t.getMediaObjectKeys()));
    }

    @Transactional
    public void delete(UUID id) {
        Tour t = tourRepository.findById(id).orElseThrow(() -> new NotFoundException("Tour not found"));
        for (String key : new ArrayList<>(t.getMediaObjectKeys())) {
            storageService.deleteObject(key);
        }
        tourRepository.delete(t);
    }

    @Transactional
    public TourManagerResponse addMedia(UUID tourId, MultipartFile file) {
        validateMedia(file);
        Tour t = tourRepository.findById(tourId).orElseThrow(() -> new NotFoundException("Tour not found"));
        String key = storageService.upload("tours/" + tourId, file);
        t.getMediaObjectKeys().add(key);
        tourRepository.save(t);
        return wrap(t);
    }

    @Transactional
    public TourManagerResponse removeMedia(UUID tourId, String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new BadRequestException("objectKey required");
        }
        Tour t = tourRepository.findById(tourId).orElseThrow(() -> new NotFoundException("Tour not found"));
        boolean removed = t.getMediaObjectKeys().removeIf(objectKey::equals);
        if (!removed) {
            throw new BadRequestException("Media not found on tour");
        }
        storageService.deleteObject(objectKey);
        tourRepository.save(t);
        return wrap(t);
    }

    private void apply(Tour t, TourWriteRequest req, String slug) {
        t.setTitle(req.title());
        t.setSlug(slug);
        t.setDescription(req.description());
        t.setDestination(req.destination());
        t.setStartDate(req.startDate());
        t.setEndDate(req.endDate());
        t.setPrice(req.price());
        t.setMaxParticipants(req.maxParticipants());
        t.setCategory(req.category());
        t.setFeatured(req.featured());
        t.setHotels(normalizeList(req.hotels()));
        t.setCarriers(normalizeList(req.carriers()));
        t.setTickets(normalizeList(req.tickets()));
        t.setExcursions(normalizeList(req.excursions()));
        t.setLayovers(normalizeList(req.layovers()));
        t.setEnableFlightRegistration(req.enableFlightRegistration());
        t.setEnableHotelRegistration(req.enableHotelRegistration());
        t.setEnableRailRegistration(req.enableRailRegistration());
    }

    private String buildSlug(TourWriteRequest req, UUID excludeId) {
        String raw = req.slug() != null && !req.slug().isBlank() ? req.slug() : req.title();
        String base = SlugUtils.slugify(raw);
        if (excludeId == null) {
            return SlugUtils.uniqueSlug(base, tourRepository::existsBySlug);
        }
        return SlugUtils.uniqueSlug(base, s -> tourRepository.existsBySlugAndIdNot(s, excludeId));
    }

    private static void validateMedia(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Файл пустой");
        }
        String ct = file.getContentType();
        if (ct == null || (!ct.startsWith("image/") && !ct.startsWith("video/"))) {
            throw new BadRequestException("Допустимы только изображения и видео");
        }
    }

    private static List<String> normalizeList(List<String> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        return source.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }
}
