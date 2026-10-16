package com.justice.playswap;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PlaySwapApplicationTests {

    @MockBean
    private ReactiveJwtDecoder reactiveJwtDecoder;

    @MockBean
    private ReactiveClientRegistrationRepository clientRegistrationRepository;

    @Test
    void contextLoads() {
    }
}
