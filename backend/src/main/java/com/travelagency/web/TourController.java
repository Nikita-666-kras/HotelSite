package com.travelagency.web;

import com.travelagency.dto.TourResponse;
import com.travelagency.dto.TourSearchRequest;
import com.travelagency.service.TourService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tours")
public class TourController {

    private final TourService tourService;

    public TourController(TourService tourService) {
        this.tourService = tourService;
    }

    @GetMapping("/search")
    public List<TourResponse> search(@ModelAttribute TourSearchRequest req) {
        return tourService.search(req);
    }

    @GetMapping("/{slug}")
    public TourResponse bySlug(@PathVariable String slug) {
        return tourService.getBySlug(slug);
    }
}
