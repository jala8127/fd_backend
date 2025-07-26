package com.fixed.deposit.service;

import com.fixed.deposit.model.Support;
import com.fixed.deposit.repository.SupportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SupportService {

    @Autowired
    private SupportRepository supportRepository;

    public Support createTicket(Support ticket) {
        return supportRepository.save(ticket);
    }

    public List<Support> getOpenTickets() {
        return supportRepository.findByStatus("OPEN");
    }
    // Add this method to your SupportService
    public Support resolveTicket(Long ticketId) {
        Support ticket = supportRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
        ticket.setStatus("RESOLVED");
        return supportRepository.save(ticket);
    }
}