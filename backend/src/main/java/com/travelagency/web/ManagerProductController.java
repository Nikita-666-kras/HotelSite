package com.travelagency.web;

import com.travelagency.dto.TravelProductManagerResponse;
import com.travelagency.dto.TravelProductWriteRequest;
import com.travelagency.service.ManagerProductService;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/manager/products")
public class ManagerProductController {

    private final ManagerProductService managerProductService;

    public ManagerProductController(ManagerProductService managerProductService) {
        this.managerProductService = managerProductService;
    }

    @GetMapping
    public List<TravelProductManagerResponse> list() {
        return managerProductService.list();
    }

    @PostMapping
    public TravelProductManagerResponse create(@Valid @RequestBody TravelProductWriteRequest req) {
        return managerProductService.create(req);
    }

    @PutMapping("/{id}")
    public TravelProductManagerResponse update(@PathVariable Long id, @Valid @RequestBody TravelProductWriteRequest req) {
        return managerProductService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        managerProductService.delete(id);
    }

    @PostMapping(value = "/{id}/media/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TravelProductManagerResponse uploadImage(@PathVariable Long id, @RequestPart("file") MultipartFile file) {
        return managerProductService.addImage(id, file);
    }

    @PostMapping(value = "/{id}/media/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TravelProductManagerResponse uploadVideo(@PathVariable Long id, @RequestPart("file") MultipartFile file) {
        return managerProductService.addVideo(id, file);
    }

    @DeleteMapping("/{id}/media/image")
    public TravelProductManagerResponse removeImage(@PathVariable Long id, @RequestParam("objectKey") String objectKey) {
        return managerProductService.removeImage(id, objectKey);
    }

    @DeleteMapping("/{id}/media/video")
    public TravelProductManagerResponse removeVideo(@PathVariable Long id, @RequestParam("objectKey") String objectKey) {
        return managerProductService.removeVideo(id, objectKey);
    }
}
