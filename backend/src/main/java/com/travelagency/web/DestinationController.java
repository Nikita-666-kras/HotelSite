package com.travelagency.web;

import com.travelagency.service.DestinationSuggestService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/destinations")
public class DestinationController {

    private final DestinationSuggestService destinationSuggestService;

    public DestinationController(DestinationSuggestService destinationSuggestService) {
        this.destinationSuggestService = destinationSuggestService;
    }

    @GetMapping("/suggest")
    public List<String> suggest(@RequestParam(name = "q", required = false, defaultValue = "") String q) {
        return destinationSuggestService.suggest(q);
    }
}
