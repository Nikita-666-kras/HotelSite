package com.travelagency.service;

import com.travelagency.domain.ProductBooking;
import com.travelagency.domain.ProductBookingStatus;
import com.travelagency.domain.TravelProduct;
import com.travelagency.domain.User;
import com.travelagency.dto.ProductBookingRequest;
import com.travelagency.dto.ProductSearchRequest;
import com.travelagency.dto.TravelProductResponse;
import com.travelagency.repository.ProductBookingRepository;
import com.travelagency.repository.TravelProductRepository;
import com.travelagency.repository.UserRepository;
import com.travelagency.security.UserPrincipal;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final TravelProductRepository travelProductRepository;
    private final ProductBookingRepository productBookingRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    public ProductService(
            TravelProductRepository travelProductRepository,
            ProductBookingRepository productBookingRepository,
            UserRepository userRepository,
            StorageService storageService) {
        this.travelProductRepository = travelProductRepository;
        this.productBookingRepository = productBookingRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public List<TravelProductResponse> search(ProductSearchRequest req) {
        String o = blankToNull(req.origin());
        String d = blankToNull(req.destination());
        String originPattern = o == null ? null : "%" + o.toLowerCase() + "%";
        String destPattern = d == null ? null : "%" + d.toLowerCase() + "%";
        if (req.type() == null
                && originPattern == null
                && destPattern == null
                && req.maxPrice() == null
                && req.departAfter() == null
                && req.checkInFrom() == null) {
            return travelProductRepository.findAllByOrderByPriceAsc().stream()
                    .map(this::toResponse)
                    .toList();
        }
        return travelProductRepository
                .search(
                        req.type(),
                        originPattern,
                        destPattern,
                        req.maxPrice(),
                        req.departAfter(),
                        req.checkInFrom())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TravelProductResponse get(Long id) {
        TravelProduct p =
                travelProductRepository.findById(id).orElseThrow(() -> new NotFoundException("Product not found"));
        return toResponse(p);
    }

    @Transactional
    public Long book(UserPrincipal principal, ProductBookingRequest req) {
        TravelProduct p =
                travelProductRepository
                        .findById(req.productId())
                        .orElseThrow(() -> new NotFoundException("Product not found"));
        User u =
                userRepository.findById(principal.getId()).orElseThrow(() -> new NotFoundException("User not found"));
        ProductBooking b = new ProductBooking();
        b.setUser(u);
        b.setProduct(p);
        b.setStatus(ProductBookingStatus.PENDING);
        b.setDetails(req.details());
        productBookingRepository.save(b);
        return b.getId();
    }

    public TravelProductResponse toResponse(TravelProduct p) {
        List<String> imgs = resolveMediaList(p.getImageUrls());
        List<String> vids = resolveMediaList(p.getVideoUrls());
        return new TravelProductResponse(
                p.getId(),
                p.getType(),
                p.getName(),
                p.getDescription(),
                p.getOrigin(),
                p.getDestination(),
                p.getDepartAt(),
                p.getArriveAt(),
                p.getCheckIn(),
                p.getCheckOut(),
                p.getPrice(),
                p.getStars(),
                p.getCarrier(),
                p.getExternalRef(),
                imgs,
                vids);
    }

    private List<String> resolveMediaList(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyList();
        }
        return raw.stream().map(this::resolveMediaUrl).filter(u -> u != null).toList();
    }

    private String resolveMediaUrl(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        if (s.startsWith("http://") || s.startsWith("https://")) {
            return s;
        }
        return storageService.presignedGetUrl(s);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
