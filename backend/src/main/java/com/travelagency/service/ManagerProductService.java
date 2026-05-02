package com.travelagency.service;

import com.travelagency.domain.ProductType;
import com.travelagency.domain.TravelProduct;
import com.travelagency.dto.TravelProductManagerResponse;
import com.travelagency.dto.TravelProductWriteRequest;
import com.travelagency.repository.TravelProductRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ManagerProductService {

    private final TravelProductRepository productRepository;
    private final ProductService productService;
    private final StorageService storageService;

    public ManagerProductService(
            TravelProductRepository productRepository, ProductService productService, StorageService storageService) {
        this.productRepository = productRepository;
        this.productService = productService;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public List<TravelProductManagerResponse> list() {
        return productRepository.findAllByOrderByIdDesc().stream().map(this::wrap).toList();
    }

    @Transactional
    public TravelProductManagerResponse create(TravelProductWriteRequest req) {
        TravelProduct p = new TravelProduct();
        apply(p, req);
        productRepository.save(p);
        return wrap(p);
    }

    @Transactional
    public TravelProductManagerResponse update(UUID id, TravelProductWriteRequest req) {
        TravelProduct p = productRepository.findById(id).orElseThrow(() -> new NotFoundException("Product not found"));
        apply(p, req);
        productRepository.save(p);
        return wrap(p);
    }

    private TravelProductManagerResponse wrap(TravelProduct p) {
        return TravelProductManagerResponse.of(
                productService.toResponse(p), new ArrayList<>(p.getImageUrls()), new ArrayList<>(p.getVideoUrls()));
    }

    @Transactional
    public void delete(UUID id) {
        TravelProduct p = productRepository.findById(id).orElseThrow(() -> new NotFoundException("Product not found"));
        for (String key : new ArrayList<>(p.getImageUrls())) {
            if (!isExternalUrl(key)) {
                storageService.deleteObject(key);
            }
        }
        for (String key : new ArrayList<>(p.getVideoUrls())) {
            if (!isExternalUrl(key)) {
                storageService.deleteObject(key);
            }
        }
        productRepository.delete(p);
    }

    @Transactional
    public TravelProductManagerResponse addImage(UUID productId, MultipartFile file) {
        validateImage(file);
        TravelProduct p = productRepository.findById(productId).orElseThrow(() -> new NotFoundException("Product not found"));
        String key = storageService.upload("products/" + productId + "/img", file);
        p.getImageUrls().add(key);
        productRepository.save(p);
        return wrap(p);
    }

    @Transactional
    public TravelProductManagerResponse addVideo(UUID productId, MultipartFile file) {
        validateVideo(file);
        TravelProduct p = productRepository.findById(productId).orElseThrow(() -> new NotFoundException("Product not found"));
        String key = storageService.upload("products/" + productId + "/vid", file);
        p.getVideoUrls().add(key);
        productRepository.save(p);
        return wrap(p);
    }

    @Transactional
    public TravelProductManagerResponse removeImage(UUID productId, String objectKey) {
        return removeMedia(productId, objectKey, true);
    }

    @Transactional
    public TravelProductManagerResponse removeVideo(UUID productId, String objectKey) {
        return removeMedia(productId, objectKey, false);
    }

    private TravelProductManagerResponse removeMedia(UUID productId, String objectKey, boolean image) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new BadRequestException("objectKey required");
        }
        TravelProduct p = productRepository.findById(productId).orElseThrow(() -> new NotFoundException("Product not found"));
        List<String> list = image ? p.getImageUrls() : p.getVideoUrls();
        boolean removed = list.removeIf(objectKey::equals);
        if (!removed) {
            throw new BadRequestException("Media not found");
        }
        if (!isExternalUrl(objectKey)) {
            storageService.deleteObject(objectKey);
        }
        productRepository.save(p);
        return wrap(p);
    }

    private void apply(TravelProduct p, TravelProductWriteRequest req) {
        p.setType(req.type());
        p.setName(req.name());
        p.setDescription(req.description());
        p.setOrigin(req.origin());
        p.setDestination(req.destination());
        p.setDepartAt(req.departAt());
        p.setArriveAt(req.arriveAt());
        p.setCheckIn(req.checkIn());
        p.setCheckOut(req.checkOut());
        p.setPrice(req.price());
        p.setStars(req.stars());
        p.setCarrier(req.carrier());
        p.setExternalRef(req.externalRef());
        validateTypeFields(req.type(), p);
    }

    private static void validateTypeFields(ProductType type, TravelProduct p) {
        switch (type) {
            case FLIGHT -> {
                if (p.getDepartAt() == null || p.getArriveAt() == null) {
                    throw new BadRequestException("Для авиабилета укажите время вылета и прилёта");
                }
            }
            case TRAIN -> {
                if (p.getDepartAt() == null || p.getArriveAt() == null) {
                    throw new BadRequestException("Для ж/д укажите время отправления и прибытия");
                }
            }
            case HOTEL -> {
                if (p.getCheckIn() == null || p.getCheckOut() == null) {
                    throw new BadRequestException("Для отеля укажите даты заезда и выезда");
                }
            }
            default -> {}
        }
    }

    private static boolean isExternalUrl(String s) {
        return s != null && (s.startsWith("http://") || s.startsWith("https://"));
    }

    private static void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Файл пустой");
        }
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/")) {
            throw new BadRequestException("Нужен файл изображения");
        }
    }

    private static void validateVideo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Файл пустой");
        }
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("video/")) {
            throw new BadRequestException("Нужен видеофайл");
        }
    }
}
