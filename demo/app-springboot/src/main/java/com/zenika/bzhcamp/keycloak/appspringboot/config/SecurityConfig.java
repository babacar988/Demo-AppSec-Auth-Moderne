
package com.zenika.bzhcamp.keycloak.appspringboot.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String GROUPS_CLAIM = "groups";

    private final KeycloakLogoutHandler keycloakLogoutHandler;

    SecurityConfig(KeycloakLogoutHandler keycloakLogoutHandler) {
        this.keycloakLogoutHandler = keycloakLogoutHandler;
    }

    // Gestion des sessions
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(sessionRegistry());
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    // Gestion du client OAuth2 : C'EST LE BEAN QUI MANQUAIT
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        AuthenticatedPrincipalOAuth2AuthorizedClientRepository authorizedClientRepository =
                new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(
                        authorizedClientService
                );

        DefaultOAuth2AuthorizedClientManager authorizedClientManager =
                new DefaultOAuth2AuthorizedClientManager(
                        clientRegistrationRepository,
                        authorizedClientRepository
                );

        authorizedClientManager.setAuthorizedClientProvider(
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .authorizationCode()
                        .refreshToken()
                        .clientCredentials()
                        .build()
        );

        return authorizedClientManager;
    }

    // Configuration de la sécurité HTTP
    @Bean
    public SecurityFilterChain resourceServerFilterChain(
            HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/", "/about", "/error", "/favicon.ico").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/planets", "/planets/**").authenticated()
                .anyRequest().authenticated()
        );

        // Validation des JWT envoyés directement à l'application
        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(
                        jwtAuthenticationConverter()
                ))
        );

        // Connexion navigateur via Keycloak et déconnexion
        http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                        .userAuthoritiesMapper(userAuthoritiesMapperForKeycloak())
                )
        );

        http.logout(logout -> logout
                .addLogoutHandler(keycloakLogoutHandler)
                .logoutSuccessUrl("/")
        );

        return http.build();
    }

    // Mapping des rôles pour OAuth2 Login
    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapperForKeycloak() {
        return authorities -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            if (authorities == null) {
                return mappedAuthorities;
            }

            for (GrantedAuthority authority : authorities) {

                if (authority instanceof OidcUserAuthority oidcAuthority) {
                    Map<String, Object> claims = oidcAuthority.getUserInfo() != null
                            ? oidcAuthority.getUserInfo().getClaims()
                            : oidcAuthority.getIdToken().getClaims();

                    addKeycloakRoles(claims, mappedAuthorities);
                }

                if (authority instanceof OAuth2UserAuthority oauth2Authority) {
                    addKeycloakRoles(
                            oauth2Authority.getAttributes(),
                            mappedAuthorities
                    );
                }

                // Conserver les autorités déjà présentes
                if (!(authority instanceof OidcUserAuthority)
                        && !(authority instanceof OAuth2UserAuthority)) {
                    mappedAuthorities.add(authority);
                }
            }

            return mappedAuthorities;
        };
    }

    // Mapping des rôles pour Resource Server JWT
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter defaultConverter =
                new JwtGrantedAuthoritiesConverter();

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities =
                    new HashSet<>(defaultConverter.convert(jwt));

            addKeycloakRoles(jwt.getClaims(), authorities);

            return authorities;
        });

        return converter;
    }

    // Extraction des rôles Keycloak
    private void addKeycloakRoles(
            Map<String, Object> claims,
            Collection<GrantedAuthority> authorities) {

        Object realmAccessObject = claims.get(REALM_ACCESS_CLAIM);

        if (realmAccessObject instanceof Map<?, ?> realmAccess) {
            Object rolesObject = realmAccess.get(ROLES_CLAIM);

            if (rolesObject instanceof Collection<?> roles) {
                for (Object role : roles) {
                    if (role instanceof String roleName) {
                        authorities.add(
                                new SimpleGrantedAuthority("ROLE_" + roleName)
                        );
                    }
                }
            }
        }

        Object groupsObject = claims.get(GROUPS_CLAIM);

        if (groupsObject instanceof Collection<?> groups) {
            for (Object group : groups) {
                if (group instanceof String groupName) {
                    authorities.add(
                            new SimpleGrantedAuthority("ROLE_" + groupName)
                    );
                }
            }
        }
    }

    // WebClient : simple, sans filtre OAuth2 automatique
    // Le token est passé explicitement par PlanetsService via le header Authorization
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
