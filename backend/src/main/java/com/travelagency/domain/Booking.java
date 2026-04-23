package com.travelagency.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BookingStatus status = BookingStatus.NEW;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_manager_id")
    private User assignedManager;

    @Column(length = 2000)
    private String notes;

    @Column(name = "contact_phone", length = 32)
    private String contactPhone;

    @Column(name = "flight_passenger_doc_number", length = 120)
    private String flightPassengerDocNumber;

    @Column(name = "flight_citizenship", length = 120)
    private String flightCitizenship;

    @Column(name = "flight_loyalty_program", length = 120)
    private String flightLoyaltyProgram;

    @Column(name = "flight_baggage_notes", length = 300)
    private String flightBaggageNotes;

    @Column(name = "hotel_guest_full_name", length = 200)
    private String hotelGuestFullName;

    @Column(name = "hotel_document_number", length = 120)
    private String hotelDocumentNumber;

    @Column(name = "hotel_estimated_arrival_time", length = 120)
    private String hotelEstimatedArrivalTime;

    @Column(name = "hotel_special_requests", length = 500)
    private String hotelSpecialRequests;

    @Column(name = "rail_passenger_doc_number", length = 120)
    private String railPassengerDocNumber;

    @Column(name = "rail_preferred_seat", length = 80)
    private String railPreferredSeat;

    @Column(name = "rail_wagon_preferences", length = 120)
    private String railWagonPreferences;

    @Column(name = "rail_notes", length = 300)
    private String railNotes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingParticipant> participants = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public User getAssignedManager() {
        return assignedManager;
    }

    public void setAssignedManager(User assignedManager) {
        this.assignedManager = assignedManager;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getFlightPassengerDocNumber() {
        return flightPassengerDocNumber;
    }

    public void setFlightPassengerDocNumber(String flightPassengerDocNumber) {
        this.flightPassengerDocNumber = flightPassengerDocNumber;
    }

    public String getFlightCitizenship() {
        return flightCitizenship;
    }

    public void setFlightCitizenship(String flightCitizenship) {
        this.flightCitizenship = flightCitizenship;
    }

    public String getFlightLoyaltyProgram() {
        return flightLoyaltyProgram;
    }

    public void setFlightLoyaltyProgram(String flightLoyaltyProgram) {
        this.flightLoyaltyProgram = flightLoyaltyProgram;
    }

    public String getFlightBaggageNotes() {
        return flightBaggageNotes;
    }

    public void setFlightBaggageNotes(String flightBaggageNotes) {
        this.flightBaggageNotes = flightBaggageNotes;
    }

    public String getHotelGuestFullName() {
        return hotelGuestFullName;
    }

    public void setHotelGuestFullName(String hotelGuestFullName) {
        this.hotelGuestFullName = hotelGuestFullName;
    }

    public String getHotelDocumentNumber() {
        return hotelDocumentNumber;
    }

    public void setHotelDocumentNumber(String hotelDocumentNumber) {
        this.hotelDocumentNumber = hotelDocumentNumber;
    }

    public String getHotelEstimatedArrivalTime() {
        return hotelEstimatedArrivalTime;
    }

    public void setHotelEstimatedArrivalTime(String hotelEstimatedArrivalTime) {
        this.hotelEstimatedArrivalTime = hotelEstimatedArrivalTime;
    }

    public String getHotelSpecialRequests() {
        return hotelSpecialRequests;
    }

    public void setHotelSpecialRequests(String hotelSpecialRequests) {
        this.hotelSpecialRequests = hotelSpecialRequests;
    }

    public String getRailPassengerDocNumber() {
        return railPassengerDocNumber;
    }

    public void setRailPassengerDocNumber(String railPassengerDocNumber) {
        this.railPassengerDocNumber = railPassengerDocNumber;
    }

    public String getRailPreferredSeat() {
        return railPreferredSeat;
    }

    public void setRailPreferredSeat(String railPreferredSeat) {
        this.railPreferredSeat = railPreferredSeat;
    }

    public String getRailWagonPreferences() {
        return railWagonPreferences;
    }

    public void setRailWagonPreferences(String railWagonPreferences) {
        this.railWagonPreferences = railWagonPreferences;
    }

    public String getRailNotes() {
        return railNotes;
    }

    public void setRailNotes(String railNotes) {
        this.railNotes = railNotes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<BookingParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<BookingParticipant> participants) {
        this.participants = participants;
    }
}
