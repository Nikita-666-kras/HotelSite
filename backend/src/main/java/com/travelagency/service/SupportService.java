package com.travelagency.service;

import com.travelagency.domain.Role;
import com.travelagency.domain.SupportConversation;
import com.travelagency.domain.SupportMessage;
import com.travelagency.domain.User;
import com.travelagency.dto.SupportConversationResponse;
import com.travelagency.dto.SupportMessageRequest;
import com.travelagency.dto.SupportMessageResponse;
import com.travelagency.dto.SupportThreadSummaryResponse;
import com.travelagency.repository.SupportConversationRepository;
import com.travelagency.repository.SupportMessageRepository;
import com.travelagency.repository.UserRepository;
import com.travelagency.security.UserPrincipal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupportService {

    private final SupportConversationRepository conversationRepository;
    private final SupportMessageRepository messageRepository;
    private final UserRepository userRepository;

    public SupportService(
            SupportConversationRepository conversationRepository,
            SupportMessageRepository messageRepository,
            UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public SupportConversationResponse createConversation(UserPrincipal principal, String subject) {
        User u =
                userRepository.findById(principal.getId()).orElseThrow(() -> new NotFoundException("User not found"));
        SupportConversation c = new SupportConversation();
        c.setUser(u);
        c.setSubject(subject != null && !subject.isBlank() ? subject.trim() : "Чат с поддержкой");
        conversationRepository.save(c);
        SupportMessage m = new SupportMessage();
        m.setConversation(c);
        m.setAuthor(null);
        m.setBody("Здравствуйте! Напишите ваш вопрос — менеджер ответит в ближайшее время.");
        m.setStaffReply(true);
        messageRepository.save(m);
        return loadConversation(c.getId());
    }

    @Transactional(readOnly = true)
    public List<SupportThreadSummaryResponse> myConversationSummaries(UserPrincipal principal) {
        return conversationRepository.findByUserIdOrderByCreatedAtDesc(principal.getId()).stream()
                .map(c -> new SupportThreadSummaryResponse(c.getId(), c.getSubject(), c.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public SupportConversationResponse getConversation(Long id, UserPrincipal principal) {
        SupportConversation c =
                conversationRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found"));
        if (principal.getRole() == Role.USER && !c.getUser().getId().equals(principal.getId())) {
            throw new NotFoundException("Not found");
        }
        return loadConversation(id);
    }

    @Transactional
    public SupportMessageResponse postUserMessage(Long conversationId, UserPrincipal principal, SupportMessageRequest req) {
        SupportConversation c =
                conversationRepository
                        .findById(conversationId)
                        .orElseThrow(() -> new NotFoundException("Not found"));
        if (!c.getUser().getId().equals(principal.getId())) {
            throw new NotFoundException("Not found");
        }
        User u =
                userRepository.findById(principal.getId()).orElseThrow(() -> new NotFoundException("User not found"));
        return saveMessage(c, u, req.body(), false);
    }

    @Transactional(readOnly = true)
    public List<SupportThreadSummaryResponse> allSummariesForStaff() {
        return conversationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(c -> new SupportThreadSummaryResponse(c.getId(), c.getSubject(), c.getCreatedAt()))
                .toList();
    }

    @Transactional
    public SupportMessageResponse postStaffMessage(Long conversationId, UserPrincipal principal, SupportMessageRequest req) {
        SupportConversation c =
                conversationRepository
                        .findById(conversationId)
                        .orElseThrow(() -> new NotFoundException("Not found"));
        if (principal.getRole() != Role.MANAGER && principal.getRole() != Role.ADMIN) {
            throw new NotFoundException("Not found");
        }
        User u =
                userRepository.findById(principal.getId()).orElseThrow(() -> new NotFoundException("User not found"));
        return saveMessage(c, u, req.body(), true);
    }

    @Transactional(readOnly = true)
    public SupportConversationResponse getConversationForStaff(Long id) {
        return loadConversation(id);
    }

    private SupportMessageResponse saveMessage(
            SupportConversation c, User author, String body, boolean staff) {
        SupportMessage m = new SupportMessage();
        m.setConversation(c);
        m.setAuthor(author);
        m.setBody(body);
        m.setStaffReply(staff);
        messageRepository.save(m);
        return new SupportMessageResponse(
                m.getId(), m.getBody(), m.isStaffReply(), author.getEmail(), m.getCreatedAt());
    }

    private SupportConversationResponse loadConversation(Long id) {
        SupportConversation c =
                conversationRepository
                        .findDetailedById(id)
                        .orElseThrow(() -> new NotFoundException("Not found"));
        List<SupportMessageResponse> msgs =
                c.getMessages().stream()
                        .map(
                                m ->
                                        new SupportMessageResponse(
                                                m.getId(),
                                                m.getBody(),
                                                m.isStaffReply(),
                                                m.getAuthor() != null ? m.getAuthor().getEmail() : null,
                                                m.getCreatedAt()))
                        .toList();
        return new SupportConversationResponse(c.getId(), c.getSubject(), c.getCreatedAt(), msgs);
    }
}
