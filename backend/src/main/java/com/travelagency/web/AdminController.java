package com.travelagency.web;

import com.travelagency.domain.Role;
import com.travelagency.dto.AdminCreateManagerRequest;
import com.travelagency.dto.AdminSetPasswordRequest;
import com.travelagency.dto.AdminUpdateUserRequest;
import com.travelagency.dto.AdminUserResponse;
import com.travelagency.dto.CrmSalesAnalyticsResponse;
import com.travelagency.security.UserPrincipal;
import com.travelagency.service.AdminService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/analytics/sales")
    public CrmSalesAnalyticsResponse sales(@RequestParam(defaultValue = "30") int days) {
        return adminService.salesAnalytics(days);
    }

    @GetMapping("/users")
    public Page<AdminUserResponse> users(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminService.listUsers(role, q, pageable);
    }

    @PostMapping("/managers")
    public AdminUserResponse createManager(@Valid @RequestBody AdminCreateManagerRequest req) {
        return adminService.createManager(req);
    }

    @PatchMapping("/users/{id}")
    public AdminUserResponse updateUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal actor,
            @Valid @RequestBody AdminUpdateUserRequest req) {
        return adminService.updateUser(id, req, actor);
    }

    @PatchMapping("/users/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setPassword(@PathVariable UUID id, @Valid @RequestBody AdminSetPasswordRequest req) {
        adminService.setUserPassword(id, req);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disableUser(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal actor) {
        adminService.disableUser(id, actor);
    }
}
