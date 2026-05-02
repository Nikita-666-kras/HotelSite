package com.travelagency.service;

import org.springframework.stereotype.Service;

@Service
public class CrmMailService {

    private final ResendEmailService resendEmailService;

    public CrmMailService(ResendEmailService resendEmailService) {
        this.resendEmailService = resendEmailService;
    }

    public boolean isAvailable() {
        return resendEmailService.isAvailable();
    }

    public void sendPlain(String to, String subject, String body) {
        resendEmailService.sendPlain(to, subject, body);
    }
}
