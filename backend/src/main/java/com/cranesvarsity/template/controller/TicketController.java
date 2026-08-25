package com.cranesvarsity.template.controller;

import com.cranesvarsity.template.security.AuthenticatedStudent;
import com.cranesvarsity.template.service.TicketException;
import com.cranesvarsity.template.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/student/helpdesk")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping(value = "/tickets", consumes = "multipart/form-data")
    public ResponseEntity<?> submit(@AuthenticationPrincipal AuthenticatedStudent student,
                                     @RequestParam("identityOption") String identityOption,
                                     @RequestParam("subject") String subject,
                                     @RequestParam("concernDescription") String description,
                                     @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            String ticketId = ticketService.submit(student, identityOption, subject, description, file);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Your support ticket has been submitted successfully!",
                    "ticketId", ticketId
            ));
        } catch (TicketException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }
}
