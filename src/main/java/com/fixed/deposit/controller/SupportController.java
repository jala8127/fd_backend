package com.fixed.deposit.controller;

import com.fixed.deposit.model.Support;
import com.fixed.deposit.service.SupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/support")
public class SupportController {

    @Autowired
    private SupportService supportService;

    @PostMapping("/create")
    public ResponseEntity<?> createTicket(@RequestBody Support ticket) {
        try {
            supportService.createTicket(ticket);
            return ResponseEntity.ok(Map.of("message", "Support ticket created successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to create ticket: " + e.getMessage()));
        }
    }

    @GetMapping("/open")
    public ResponseEntity<List<Support>> getOpenTickets() {
        List<Support> openTickets = supportService.getOpenTickets();
        return ResponseEntity.ok(openTickets);
    }
    // Add this endpoint to your SupportController
    @PutMapping("/{id}/resolve")
    public ResponseEntity<?> resolveTicket(@PathVariable Long id) {
        try {
            supportService.resolveTicket(id);
            return ResponseEntity.ok(Map.of("message", "Ticket " + id + " has been resolved."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
