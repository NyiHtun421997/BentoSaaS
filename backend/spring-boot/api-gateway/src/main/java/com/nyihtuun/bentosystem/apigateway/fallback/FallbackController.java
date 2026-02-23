package com.nyihtuun.bentosystem.apigateway.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/users")
    public ResponseEntity<String> fallbackUser() {
        log.error("Resolved to fallback when connecting to user-service from api-gateway.");
        return ResponseEntity.status(500).body("Service Unavailable! Please try again later.");
    }

    @RequestMapping("/plan-management")
    public ResponseEntity<String> fallbackPlanManagement() {
        log.error("Resolved to fallback when connecting to plan-management-service from api-gateway.");
        return ResponseEntity.status(500).body("Service Unavailable! Please try again later.");
    }

    @RequestMapping("/subscription")
    public ResponseEntity<String> fallbackSubscription() {
        log.error("Resolved to fallback when connecting to subscription-service from api-gateway.");
        return ResponseEntity.status(500).body("Service Unavailable! Please try again later.");
    }

    @RequestMapping("/invoice")
    public ResponseEntity<String> fallbackInvoice() {
        log.error("Resolved to fallback when connecting to invoice-service from api-gateway.");
        return ResponseEntity.status(500).body("Service Unavailable! Please try again later.");
    }

    @RequestMapping("/notification")
    public ResponseEntity<String> fallbackNotification() {
        log.error("Resolved to fallback when connecting to notification-service from api-gateway.");
        return ResponseEntity.status(500).body("Service Unavailable! Please try again later.");
    }
}
