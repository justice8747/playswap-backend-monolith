package com.justice.playswap.config;

import com.justice.playswap.auth.security.OAuth2AuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;
import reactor.core.publisher.Mono;

import com.justice.playswap.auth.service.JwtService;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Lazy
    @Autowired
    private OAuth2AuthenticationSuccessHandler successHandler;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            ReactiveClientRegistrationRepository clientRegistrationRepository) {
        http
                .cors(Customizer.withDefaults())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/oauth2/**",
                                "/login/**",
                                "/login/oauth2/**",
                                "/api/auth/**",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationRequestResolver(authorizationRequestResolver(clientRegistrationRepository))
                        .authenticationSuccessHandler(successHandler)
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenConverter(exchange -> {
                            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                                return Mono.just(new BearerTokenAuthenticationToken(authHeader.substring(7)));
                            }
                            return Mono.justOrEmpty(exchange.getRequest().getCookies().getFirst("playswap_token"))
                                    .map(HttpCookie::getValue)
                                    .map(BearerTokenAuthenticationToken::new);
                        })
                        .jwt(Customizer.withDefaults())
                )
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .logout(logout -> logout.disable());

        return http.build();
    }

    @Bean
    public ServerOAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ReactiveClientRegistrationRepository clientRegistrationRepository) {
        DefaultServerOAuth2AuthorizationRequestResolver resolver =
                new DefaultServerOAuth2AuthorizationRequestResolver(clientRegistrationRepository);
        resolver.setAuthorizationRequestCustomizer(customizer -> customizer.additionalParameters(params -> {
            params.put("access_type", "offline");
            params.put("prompt", "consent");
            params.put("show_dialog", "true");
        }));
        return resolver;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "https://*.vercel.app",
                "https://playswap-frontend-4xqptpaxl-jus-tech.vercel.app"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        return new DefaultReactiveOAuth2UserService();
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(JwtService jwtService) {
        return NimbusReactiveJwtDecoder.withSecretKey(jwtService.getHmacKey()).build();
    }

    @Bean
    public WebSessionIdResolver webSessionIdResolver() {
        CookieWebSessionIdResolver resolver = new CookieWebSessionIdResolver();
        resolver.setCookieName("AUTH_SESSION");
        resolver.addCookieInitializer(builder -> builder.path("/").sameSite("Lax"));
        return resolver;
    }
}
