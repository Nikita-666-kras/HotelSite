package com.travelagency.web;

import com.travelagency.dto.TourManagerResponse;
import com.travelagency.dto.TourWriteRequest;
import com.travelagency.service.ManagerTourService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/manager/tours")
public class ManagerTourController {

    private final ManagerTourService managerTourService;

    public ManagerTourController(ManagerTourService managerTourService) {
        this.managerTourService = managerTourService;
    }

    @GetMapping
    public List<TourManagerResponse> list() {
        return managerTourService.list();
    }

    @PostMapping
    public TourManagerResponse create(@Valid @RequestBody TourWriteRequest req) {
        return managerTourService.create(req);
    }

    @PutMapping("/{id}")
    public TourManagerResponse update(@PathVariable UUID id, @Valid @RequestBody TourWriteRequest req) {
        return managerTourService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        managerTourService.delete(id);
    }

    @PostMapping(value = "/{id}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TourManagerResponse uploadMedia(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
        return managerTourService.addMedia(id, file);
    }

    @DeleteMapping("/{id}/media")
    public TourManagerResponse removeMedia(@PathVariable UUID id, @RequestParam("objectKey") String objectKey) {
        return managerTourService.removeMedia(id, objectKey);
    }
}
