package com.travelagency.service;

import com.travelagency.domain.FavoriteTour;
import com.travelagency.domain.Tour;
import com.travelagency.domain.User;
import com.travelagency.dto.TourResponse;
import com.travelagency.repository.FavoriteTourRepository;
import com.travelagency.repository.TourRepository;
import com.travelagency.repository.UserRepository;
import com.travelagency.security.UserPrincipal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoriteService {

    private final FavoriteTourRepository favoriteTourRepository;
    private final UserRepository userRepository;
    private final TourRepository tourRepository;
    private final TourService tourService;

    public FavoriteService(
            FavoriteTourRepository favoriteTourRepository,
            UserRepository userRepository,
            TourRepository tourRepository,
            TourService tourService) {
        this.favoriteTourRepository = favoriteTourRepository;
        this.userRepository = userRepository;
        this.tourRepository = tourRepository;
        this.tourService = tourService;
    }

    @Transactional(readOnly = true)
    public List<TourResponse> list(UserPrincipal principal) {
        return favoriteTourRepository.findByUserIdOrderByCreatedAtDesc(principal.getId()).stream()
                .map(FavoriteTour::getTour)
                .map(tourService::toResponse)
                .toList();
    }

    @Transactional
    public void add(UserPrincipal principal, Long tourId) {
        if (favoriteTourRepository.existsByUserIdAndTourId(principal.getId(), tourId)) {
            return;
        }
        User user =
                userRepository.findById(principal.getId()).orElseThrow(() -> new NotFoundException("User not found"));
        Tour tour = tourRepository.findById(tourId).orElseThrow(() -> new NotFoundException("Tour not found"));
        FavoriteTour ft = new FavoriteTour();
        ft.setUser(user);
        ft.setTour(tour);
        favoriteTourRepository.save(ft);
    }

    @Transactional
    public void remove(UserPrincipal principal, Long tourId) {
        favoriteTourRepository.deleteByUserIdAndTourId(principal.getId(), tourId);
    }
}
