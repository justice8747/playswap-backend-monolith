package com.justice.playswap.transfer.controller;

import com.justice.playswap.transfer.dto.TransferSessionHistoryDto;
import com.justice.playswap.transfer.service.TransferHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final TransferHistoryService transferHistoryService;

    @GetMapping
    public Mono<List<TransferSessionHistoryDto>> getHistory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {

        String userId = null;
        if (jwt != null) {
            String preferredUsername = jwt.getClaimAsString("preferred_username");
            userId = (preferredUsername != null && !preferredUsername.isBlank()) ? preferredUsername : jwt.getSubject();
        }
        if (userId == null) {
            userId = headerUserId;
        }

        if (userId == null || userId.isBlank()) {
            log.error("Rejecting /api/history request: Missing authenticated user");
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated user"));
        }

        log.info("Received /api/history request for user {}", userId);
        return Mono.just(transferHistoryService.getUserTransferHistory(userId));
    }
}
