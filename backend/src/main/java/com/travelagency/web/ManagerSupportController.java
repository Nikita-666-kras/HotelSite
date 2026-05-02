package com.travelagency.web;

import com.travelagency.dto.SupportConversationResponse;
import com.travelagency.dto.SupportMessageRequest;
import com.travelagency.dto.SupportMessageResponse;
import com.travelagency.dto.SupportThreadSummaryResponse;
import com.travelagency.security.UserPrincipal;
import com.travelagency.service.SupportService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manager/support")
public class ManagerSupportController {

    private final SupportService supportService;

    public ManagerSupportController(SupportService supportService) {
        this.supportService = supportService;
    }

    @GetMapping("/conversations")
    public List<SupportThreadSummaryResponse> all() {
        return supportService.allSummariesForStaff();
    }

    @GetMapping("/conversations/{id}")
    public SupportConversationResponse one(@PathVariable UUID id) {
        return supportService.getConversationForStaff(id);
    }

    @PostMapping("/conversations/{id}/messages")
    public SupportMessageResponse reply(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody SupportMessageRequest req) {
        return supportService.postStaffMessage(id, principal, req);
    }
}
