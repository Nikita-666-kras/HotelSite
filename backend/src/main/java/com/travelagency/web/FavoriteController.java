package com.travelagency.web;

import com.travelagency.dto.TourResponse;
import com.travelagency.security.UserPrincipal;
import com.travelagency.service.FavoriteService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites/tours")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public List<TourResponse> list(@AuthenticationPrincipal UserPrincipal principal) {
        return favoriteService.list(principal);
    }

    @PostMapping("/{tourId}")
    public void add(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long tourId) {
        favoriteService.add(principal, tourId);
    }

    @DeleteMapping("/{tourId}")
    public void remove(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long tourId) {
        favoriteService.remove(principal, tourId);
    }
}
