package com.travelagency.web;

import com.travelagency.dto.SupportConversationResponse;
import com.travelagency.dto.SupportMessageRequest;
import com.travelagency.dto.SupportMessageResponse;
import com.travelagency.dto.SupportThreadSummaryResponse;
import com.travelagency.security.UserPrincipal;
import com.travelagency.service.SupportService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/support")
public class SupportController {

    private final SupportService supportService;

    public SupportController(SupportService supportService) {
        this.supportService = supportService;
    }

    @PostMapping("/conversations")
    public SupportConversationResponse create(
            @AuthenticationPrincipal UserPrincipal principal, @RequestParam(required = false) String subject) {
        return supportService.createConversation(principal, subject);
    }

    @GetMapping("/conversations")
    public List<SupportThreadSummaryResponse> list(@AuthenticationPrincipal UserPrincipal principal) {
        return supportService.myConversationSummaries(principal);
    }

    @GetMapping("/conversations/{id}")
    public SupportConversationResponse one(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        return supportService.getConversation(id, principal);
    }

    @PostMapping("/conversations/{id}/messages")
    public SupportMessageResponse post(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody SupportMessageRequest req) {
        return supportService.postUserMessage(id, principal, req);
    }
}
