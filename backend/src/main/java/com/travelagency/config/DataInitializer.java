package com.travelagency.config;

import com.travelagency.domain.ProductType;
import com.travelagency.domain.Role;
import com.travelagency.domain.Tour;
import com.travelagency.domain.TourCategory;
import com.travelagency.domain.TravelProduct;
import com.travelagency.domain.User;
import com.travelagency.repository.TourRepository;
import com.travelagency.repository.TravelProductRepository;
import com.travelagency.repository.UserRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    /** Demo staff logins; ensure created on startup if emails are free. */
    public static final String ADMIN_EMAIL = "admin@agency.local";

    public static final String ADMIN_PASSWORD = "Admin123!Secure";
    public static final String MANAGER_EMAIL = "manager@agency.local";
    public static final String MANAGER_PASSWORD = "Manager123!Secure";

    @Bean
    CommandLineRunner seedUsers(
            UserRepository users, PasswordEncoder encoder, TourRepository tours, TravelProductRepository products) {
        return args -> {
            ensureStaffAccounts(users, encoder);

            if (tours.count() > 0) {
                return;
            }
            log.warn("Seeding demo catalog (disable in production by pre-loading DB)");

            Tour t1 = new Tour();
            t1.setTitle("Анталья, всё включено");
            t1.setSlug("antalya-all-inclusive");
            t1.setDescription(
                    "Пляжный отдых 5*, трансфер, страховка. Идеально для семей с детьми.");
            t1.setDestination("Турция, Анталья");
            t1.setStartDate(LocalDate.now().plusMonths(2).withDayOfMonth(10));
            t1.setEndDate(LocalDate.now().plusMonths(2).withDayOfMonth(17));
            t1.setPrice(new BigDecimal("89990"));
            t1.setMaxParticipants(24);
            t1.setCategory(TourCategory.BEACH);
            t1.setFeatured(true);
            tours.save(t1);

            Tour t2 = new Tour();
            t2.setTitle("Горнолыжный Красная Поляна");
            t2.setSlug("krasnaya-polyana-ski");
            t2.setDescription("Отель у подножия трасс, ски-пасс, инструктор по запросу.");
            t2.setDestination("Россия, Сочи");
            t2.setStartDate(LocalDate.now().plusMonths(1).withDayOfMonth(5));
            t2.setEndDate(LocalDate.now().plusMonths(1).withDayOfMonth(12));
            t2.setPrice(new BigDecimal("65000"));
            t2.setMaxParticipants(16);
            t2.setCategory(TourCategory.SKI);
            t2.setFeatured(true);
            tours.save(t2);

            TravelProduct hotel = new TravelProduct();
            hotel.setType(ProductType.HOTEL);
            hotel.setName("Отель «Морской бриз» 4*");
            hotel.setDescription("Номер с видом на море, завтрак.");
            hotel.setOrigin("Анталья");
            hotel.setDestination("Лара");
            hotel.setCheckIn(LocalDate.now().plusWeeks(3));
            hotel.setCheckOut(LocalDate.now().plusWeeks(3).plusDays(7));
            hotel.setPrice(new BigDecimal("42000"));
            hotel.setStars(4);
            hotel.setImageUrls(
                    new ArrayList<>(
                            List.of(
                                    "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=80",
                                    "https://images.unsplash.com/photo-1582719508461-905c673771fd?w=800&q=80",
                                    "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800&q=80")));
            products.save(hotel);

            TravelProduct flight = new TravelProduct();
            flight.setType(ProductType.FLIGHT);
            flight.setName("SVO — AYT прямой");
            flight.setDescription("Багаж 23 кг включён.");
            flight.setOrigin("Москва");
            flight.setDestination("Анталья");
            flight.setDepartAt(LocalDateTime.now().plusWeeks(4).withHour(10).withMinute(0).withSecond(0).withNano(0));
            flight.setArriveAt(LocalDateTime.now().plusWeeks(4).withHour(14).withMinute(30).withSecond(0).withNano(0));
            flight.setPrice(new BigDecimal("28500"));
            flight.setCarrier("Demo Air");
            flight.setImageUrls(
                    new ArrayList<>(
                            List.of(
                                    "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=800&q=80",
                                    "https://images.unsplash.com/photo-1540962351504-03099e0a754b?w=800&q=80",
                                    "https://images.unsplash.com/photo-1464037866556-6812c9d1c72e?w=800&q=80")));
            products.save(flight);

            TravelProduct train = new TravelProduct();
            train.setType(ProductType.TRAIN);
            train.setName("Сапсан Москва — Санкт-Петербург");
            train.setDescription("Бизнес-класс, питание.");
            train.setOrigin("Москва");
            train.setDestination("Санкт-Петербург");
            train.setDepartAt(LocalDateTime.now().plusDays(10).withHour(6).withMinute(30).withSecond(0).withNano(0));
            train.setArriveAt(LocalDateTime.now().plusDays(10).withHour(10).withMinute(45).withSecond(0).withNano(0));
            train.setPrice(new BigDecimal("8900"));
            train.setCarrier("РЖД");
            train.setImageUrls(
                    new ArrayList<>(
                            List.of(
                                    "https://images.unsplash.com/photo-1474487548417-781cb714cb94?w=800&q=80",
                                    "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=800&q=80",
                                    "https://images.unsplash.com/photo-1515165562835-c11a0e1c8dca?w=800&q=80")));
            products.save(train);
        };
    }

    private void ensureStaffAccounts(UserRepository users, PasswordEncoder encoder) {
        ensureUser(
                users,
                encoder,
                ADMIN_EMAIL,
                ADMIN_PASSWORD,
                "Администратор",
                "+70000000001",
                Role.ADMIN);
        ensureUser(
                users,
                encoder,
                MANAGER_EMAIL,
                MANAGER_PASSWORD,
                "Менеджер Иванова",
                "+70000000002",
                Role.MANAGER);
    }

    private void ensureUser(
            UserRepository users,
            PasswordEncoder encoder,
            String email,
            String plainPassword,
            String fullName,
            String phone,
            Role role) {
        if (users.findByEmailIgnoreCase(email).isPresent()) {
            return;
        }
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(plainPassword));
        u.setFullName(fullName);
        u.setPhone(phone);
        u.setRole(role);
        users.save(u);
        log.warn("Created default {} account: {} (change password in production)", role, email);
    }
}
