package com.travelagency.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tours")
public class Tour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(nullable = false, length = 200)
    private String destination;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "max_participants", nullable = false)
    private int maxParticipants = 20;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TourCategory category = TourCategory.EXCURSION;

    @ElementCollection
    @CollectionTable(name = "tour_media_keys", joinColumns = @JoinColumn(name = "tour_id"))
    @Column(name = "object_key", length = 512)
    private List<String> mediaObjectKeys = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "tour_hotels", joinColumns = @JoinColumn(name = "tour_id"))
    @Column(name = "hotel_name", length = 200)
    private List<String> hotels = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "tour_carriers", joinColumns = @JoinColumn(name = "tour_id"))
    @Column(name = "carrier_name", length = 200)
    private List<String> carriers = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "tour_tickets", joinColumns = @JoinColumn(name = "tour_id"))
    @Column(name = "ticket_name", length = 300)
    private List<String> tickets = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "tour_excursions", joinColumns = @JoinColumn(name = "tour_id"))
    @Column(name = "excursion_name", length = 300)
    private List<String> excursions = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "tour_layovers", joinColumns = @JoinColumn(name = "tour_id"))
    @Column(name = "layover_text", length = 300)
    private List<String> layovers = new ArrayList<>();

    @Column(name = "featured")
    private boolean featured;

    @Column(name = "enable_flight_registration", nullable = false)
    private boolean enableFlightRegistration;

    @Column(name = "enable_hotel_registration", nullable = false)
    private boolean enableHotelRegistration;

    @Column(name = "enable_rail_registration", nullable = false)
    private boolean enableRailRegistration;

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public TourCategory getCategory() {
        return category;
    }

    public void setCategory(TourCategory category) {
        this.category = category;
    }

    public List<String> getMediaObjectKeys() {
        return mediaObjectKeys;
    }

    public void setMediaObjectKeys(List<String> mediaObjectKeys) {
        this.mediaObjectKeys = mediaObjectKeys;
    }

    public List<String> getHotels() {
        return hotels;
    }

    public void setHotels(List<String> hotels) {
        this.hotels = hotels != null ? new ArrayList<>(hotels) : new ArrayList<>();
    }

    public List<String> getCarriers() {
        return carriers;
    }

    public void setCarriers(List<String> carriers) {
        this.carriers = carriers != null ? new ArrayList<>(carriers) : new ArrayList<>();
    }

    public List<String> getExcursions() {
        return excursions;
    }

    public void setExcursions(List<String> excursions) {
        this.excursions = excursions != null ? new ArrayList<>(excursions) : new ArrayList<>();
    }

    public List<String> getTickets() {
        return tickets;
    }

    public void setTickets(List<String> tickets) {
        this.tickets = tickets != null ? new ArrayList<>(tickets) : new ArrayList<>();
    }

    public List<String> getLayovers() {
        return layovers;
    }

    public void setLayovers(List<String> layovers) {
        this.layovers = layovers != null ? new ArrayList<>(layovers) : new ArrayList<>();
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public boolean isEnableFlightRegistration() {
        return enableFlightRegistration;
    }

    public void setEnableFlightRegistration(boolean enableFlightRegistration) {
        this.enableFlightRegistration = enableFlightRegistration;
    }

    public boolean isEnableHotelRegistration() {
        return enableHotelRegistration;
    }

    public void setEnableHotelRegistration(boolean enableHotelRegistration) {
        this.enableHotelRegistration = enableHotelRegistration;
    }

    public boolean isEnableRailRegistration() {
        return enableRailRegistration;
    }

    public void setEnableRailRegistration(boolean enableRailRegistration) {
        this.enableRailRegistration = enableRailRegistration;
    }
}
