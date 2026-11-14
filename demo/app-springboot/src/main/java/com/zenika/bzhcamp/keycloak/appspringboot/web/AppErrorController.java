package com.zenika.bzhcamp.keycloak.appspringboot.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Remplace la page d'erreur Whitelabel de Spring Boot.
 *
 * Spring Boot délègue les erreurs HTTP à "/error" via ErrorController.
 * On récupère les attributs posés par Spring (status, message, trace) et
 * on les passe au template error.ftlh.
 *
 * Pourquoi ne pas utiliser @ControllerAdvice à la place ?
 * @ControllerAdvice intercepte les exceptions lancées dans les contrôleurs,
 * mais pas les erreurs de filtre (401, 403, 404 non routés).
 * ErrorController couvre tous les cas.
 */
@Controller
public class AppErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        model.addAttribute("status", status != null ? status.toString() : "???");
        model.addAttribute("error", message != null && !message.toString().isBlank()
                ? message.toString()
                : "Erreur inattendue");

        // La trace n'est disponible qu'en profil dev (server.error.include-stacktrace=always)
        if (exception instanceof Throwable t) {
            StringBuilder sb = new StringBuilder();
            for (StackTraceElement el : t.getStackTrace()) {
                sb.append("  at ").append(el).append("\n");
            }
            model.addAttribute("trace", sb.toString());
        }

        return "error";
    }
}
