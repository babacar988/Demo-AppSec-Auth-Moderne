package com.zenika.bzhcamp.keycloak.appspringboot.web;

import java.util.List;

import com.zenika.bzhcamp.keycloak.appspringboot.service.PlanetsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PlanetsController {

    private static final Logger logger = LoggerFactory.getLogger(PlanetsController.class);

    private final PlanetsService planetsService;
    private final HttpServletRequest request;
    private final OAuth2AuthorizedClientService authorizedClientService;

    /**
     * Injection par constructeur.
     * OAuth2AuthorizedClientService permet de récupérer l'access token de l'utilisateur
     * connecté pour le propager vers le service-planets.
     */
    public PlanetsController(PlanetsService planetsService,
                             HttpServletRequest request,
                             OAuth2AuthorizedClientService authorizedClientService) {
        this.planetsService = planetsService;
        this.request = request;
        this.authorizedClientService = authorizedClientService;
    }

    @RequestMapping(value = "/")
    public String landing() {
        return "landing";
    }

    @RequestMapping(value = "/logout")
    public String handleLogout(HttpServletRequest request) throws ServletException {
        request.logout();
        return "redirect:/";
    }

    @RequestMapping(value = "/about")
    public String about() {
        return "about";
    }

    @GetMapping("favicon.ico")
    @ResponseBody
    void returnNoFavicon() {
    }

    /**
     * Endpoint protégé par rôle Keycloak.
     *
     * @PreAuthorize("hasRole('user')") vérifie que l'utilisateur possède le rôle "user"
     * dans Keycloak (mappé en "ROLE_user" par userAuthoritiesMapperForKeycloak).
     *
     * L'access token OAuth2 est récupéré depuis OAuth2AuthorizedClientService
     * et transmis au service-planets via le header Authorization: Bearer.
     */
    @RequestMapping(value = "/planets")
    @PreAuthorize("hasRole('user')")
    public String handlePlanetsRequest(@AuthenticationPrincipal OidcUser oidcUser,
                                       OAuth2AuthenticationToken authentication,
                                       Model model) {
        logger.info("User {} accessed /planets", oidcUser.getPreferredUsername());
        model.addAttribute("principal", authentication);

        // Récupérer l'access token OAuth2 (différent de l'ID token OIDC)
        // L'access token est signé par Keycloak et contient realm_access.roles
        // C'est lui que service-planets attend pour valider les droits
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        logger.debug("Propagating access token to service-planets (first 20 chars): {}...",
                accessToken.substring(0, Math.min(20, accessToken.length())));

        List<String> planets = planetsService.getPlanets(accessToken);
        model.addAttribute("planets", planets);

        return "planets";
    }
}
