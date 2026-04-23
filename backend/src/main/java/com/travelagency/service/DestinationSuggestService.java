package com.travelagency.service;

import com.travelagency.repository.TourRepository;
import com.travelagency.repository.TravelProductRepository;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DestinationSuggestService {

    private static final int MAX_QUERY_LEN = 80;
    private static final int MAX_RESULTS = 24;

    private final TourRepository tourRepository;
    private final TravelProductRepository travelProductRepository;

    public DestinationSuggestService(
            TourRepository tourRepository, TravelProductRepository travelProductRepository) {
        this.tourRepository = tourRepository;
        this.travelProductRepository = travelProductRepository;
    }

    @Transactional(readOnly = true)
    public List<String> suggest(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        String trimmed = raw.trim();
        if (trimmed.length() > MAX_QUERY_LEN) {
            trimmed = trimmed.substring(0, MAX_QUERY_LEN);
        }
        String pattern = "%" + stripLikeWildcards(trimmed).toLowerCase() + "%";
        LinkedHashSet<String> out = new LinkedHashSet<>();
        tourRepository.findDistinctDestinationsMatching(pattern).forEach(out::add);
        travelProductRepository.findDistinctOriginsMatching(pattern).forEach(out::add);
        travelProductRepository.findDistinctProductDestinationsMatching(pattern).forEach(out::add);
        return out.stream().limit(MAX_RESULTS).toList();
    }

    /** Removes LIKE wildcards so user input cannot broaden the match. */
    private static String stripLikeWildcards(String s) {
        return s.replace("%", "").replace("_", "").replace("\\", "");
    }
}
