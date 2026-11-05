package com.justice.playswap.spotify;

import com.justice.playswap.common.provider.TokenProvider;
import com.justice.playswap.spotify.client.SpotifyApiClient;
import com.justice.playswap.spotify.service.SpotifyService;
import com.justice.playswap.spotify.service.TrackMatchingService;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SpotifyServiceTest {

    private final TokenProvider tokenProvider = mock(TokenProvider.class);
    private final SpotifyApiClient spotifyApiClient = mock(SpotifyApiClient.class);
    private final SpotifyService spotifyService =
            new SpotifyService(tokenProvider, spotifyApiClient, mock(TrackMatchingService.class));

    @Test
    void obtainsTokenSeparatelyForEachPlaylistBatch() {
        when(tokenProvider.<Set<String>>withValidAccessToken(eq("user-1"), eq("spotify"), any()))
                .thenAnswer(invocation -> {
                    Function<String, Mono<Set<String>>> operation = invocation.getArgument(2);
                    return operation.apply("token");
                });
        when(spotifyApiClient.addTracksToPlaylistWithResults(eq("playlist-1"), anyList(), eq("token")))
                .thenAnswer(invocation -> Mono.just(Set.copyOf(invocation.getArgument(1))));
        List<String> trackIds = IntStream.range(0, 101).mapToObj(index -> "track-" + index).toList();

        StepVerifier.create(spotifyService.addTracksToPlaylistWithResults("user-1", "playlist-1", trackIds))
                .assertNext(insertedIds -> org.junit.jupiter.api.Assertions.assertEquals(101, insertedIds.size()))
                .verifyComplete();

        verify(tokenProvider, times(2))
                .withValidAccessToken(eq("user-1"), eq("spotify"), any());
        verify(spotifyApiClient).addTracksToPlaylistWithResults(
                eq("playlist-1"), argThat(batch -> batch.size() == 100), eq("token"));
        verify(spotifyApiClient).addTracksToPlaylistWithResults(
                eq("playlist-1"), argThat(batch -> batch.size() == 1), eq("token"));
    }
}
