package com.justice.playswap.transfer.controller;

import com.justice.playswap.transfer.dto.AnalysisSummaryResponse;
import com.justice.playswap.transfer.dto.AnalyzePlaylistsRequest;
import com.justice.playswap.transfer.dto.ExecuteTransferRequest;
import com.justice.playswap.transfer.dto.ExecuteTransferResponse;
import com.justice.playswap.transfer.dto.TransferSessionHistoryDto;
import com.justice.playswap.transfer.service.PlaylistAnalysisService;
import com.justice.playswap.transfer.service.PlaylistTransferExecutionService;
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
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final PlaylistAnalysisService playlistAnalysisService;
    private final PlaylistTransferExecutionService playlistTransferExecutionService;
    private final TransferHistoryService transferHistoryService;

    @PostMapping("/analyze")
    public Mono<AnalysisSummaryResponse> analyzePlaylists(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestBody(required = false) AnalyzePlaylistsRequest request) {

        String userId = resolveUserId(jwt, headerUserId);
        if (userId == null || userId.isBlank()) {
            log.error("Rejecting analyze request: Missing authenticated user");
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated user"));
        }

        if (request == null || request.getPlaylistIds() == null || request.getPlaylistIds().isEmpty()) {
            log.error("Rejecting analyze request from user {}: Request body or playlistIds is null/empty", userId);
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body must contain non-empty 'playlistIds' list."));
        }
        if (request.getSourcePlatform() == null || request.getSourcePlatform().isBlank()) {
            request.setSourcePlatform("SPOTIFY");
        }
        if (request.getTargetPlatform() == null || request.getTargetPlatform().isBlank()) {
            request.setTargetPlatform("YTMUSIC");
        }
        log.info("Received analyze request from user {} for {} playlists (Source: {}, Target: {})",
                userId, request.getPlaylistIds().size(), request.getSourcePlatform(), request.getTargetPlatform());
        return playlistAnalysisService.analyzePlaylists(userId, request);
    }

    @PostMapping({"/execute", "/execute/transfer", "/transfer/execute"})
    public Mono<ExecuteTransferResponse> executeTransfer(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestBody(required = false) ExecuteTransferRequest request) {

        String userId = resolveUserId(jwt, headerUserId);
        if (userId == null || userId.isBlank()) {
            log.error("Rejecting execute request: Missing authenticated user");
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated user"));
        }

        if (request == null || request.getSessionId() == null) {
            log.error("Rejecting execute request from user {}: Request body or sessionId is null", userId);
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body must contain non-null 'sessionId'."));
        }
        log.info("Received execute transfer request from user {} for session {}", userId, request.getSessionId());
        return playlistTransferExecutionService.executeTransfer(userId, request);
    }

    @PostMapping({"/resume/{sessionId}", "/resume"})
    public Mono<ExecuteTransferResponse> resumeTransfer(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable(required = false) UUID sessionId,
            @RequestBody(required = false) ExecuteTransferRequest request) {

        String userId = resolveUserId(jwt, headerUserId);
        if (userId == null || userId.isBlank()) {
            log.error("Rejecting resume request: Missing authenticated user");
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated user"));
        }

        UUID targetSessionId = sessionId != null ? sessionId : (request != null ? request.getSessionId() : null);
        if (targetSessionId == null) {
            log.error("Rejecting resume request from user {}: Missing sessionId", userId);
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Must provide sessionId path parameter or body property."));
        }
        log.info("Received resume transfer request from user {} for session {}", userId, targetSessionId);
        ExecuteTransferRequest req = ExecuteTransferRequest.builder()
                .sessionId(targetSessionId)
                .build();
        return playlistTransferExecutionService.executeTransfer(userId, req);
    }

    @GetMapping({"/history", "/history/sessions", "/user/history"})
    public Mono<List<TransferSessionHistoryDto>> getTransferHistory(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {

        String userId = resolveUserId(jwt, headerUserId);
        if (userId == null || userId.isBlank()) {
            log.error("Rejecting history request: Missing authenticated user");
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated user"));
        }

        log.info("Received transfer history request for user {}", userId);
        return Mono.just(transferHistoryService.getUserTransferHistory(userId));
    }

    private String resolveUserId(Jwt jwt, String headerUserId) {
        if (jwt != null) {
            String preferredUsername = jwt.getClaimAsString("preferred_username");
            if (preferredUsername != null && !preferredUsername.isBlank()) {
                return preferredUsername;
            }
            if (jwt.getSubject() != null && !jwt.getSubject().isBlank()) {
                return jwt.getSubject();
            }
        }
        return headerUserId;
    }
}
