package listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.util.List;
import java.util.Map;
import dto.ControllerResultDTO;
import dto.UrlMappingDTO;

public class AppListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String controllersPackage = sce.getServletContext().getInitParameter("controller");
        try {
            List<Class<?>> controllerClasses = utils.ControllerUtils.getControllerClasses(controllersPackage);
            Map<UrlMappingDTO, ControllerResultDTO> map = utils.ControllerUtils.getAllMapUrlMethod(controllerClasses);

            sce.getServletContext().setAttribute("urlMap", map);
            sce.getServletContext().setAttribute("controllerClasses", controllerClasses);

            System.out.println("=== AppListener : Application démarrée ===");
            System.out.println("Package scanné : " + controllersPackage);
            System.out.println("Controllers trouvés : " + controllerClasses.size());
            for (Class<?> c : controllerClasses) {
                System.out.println("  -> " + c.getName());
            }
            System.out.println("Routes enregistrées : " + map.size());
            for (UrlMappingDTO key : map.keySet()) {
                ControllerResultDTO val = map.get(key);
                System.out.println("  [" + key.getMethod() + "] " + key.getUrl()
                        + " -> " + val.getClasse().getSimpleName() + "." + val.getMethod().getName() + "()");
            }

            // ========================= VALIDATION FAIL-FAST (Sprint 10) =========================
            // Vérifie au démarrage que les annotations d'autorisation sont cohérentes.
            // Une mauvaise configuration ne doit JAMAIS ouvrir l'accès par défaut.
            utils.AuthConfig authConfig = utils.AuthConfig.getInstance();
            for (Map.Entry<dto.UrlMappingDTO, dto.ControllerResultDTO> entry : map.entrySet()) {
                java.lang.reflect.Method m = entry.getValue().getMethod();
                Class<?> classe = entry.getValue().getClasse();

                // Règle 1 : @Role avec tableau de rôles vide est interdit
                // Un tableau vide ne protège rien → erreur de configuration explicite
                annotation.Role roleAnnotation = m.isAnnotationPresent(annotation.Role.class)
                    ? m.getAnnotation(annotation.Role.class)
                    : classe.getAnnotation(annotation.Role.class);
                if (roleAnnotation != null && roleAnnotation.value().length == 0) {
                    throw new RuntimeException(
                        "@Role avec tableau vide sur "
                        + classe.getSimpleName() + "." + m.getName()
                        + " — une mauvaise configuration ne doit jamais ouvrir l'accès.");
                }
                // Règle 1-bis : @Role ne doit pas contenir de chaîne vide (Sprint 10-bis)
                if (roleAnnotation != null) {
                    for (String role : roleAnnotation.value()) {
                        if (role == null || role.trim().isEmpty()) {
                            throw new RuntimeException(
                                "@Role contient un rôle vide ou null sur "
                                + classe.getSimpleName() + "." + m.getName()
                                + " — une chaîne vide n'est jamais un rôle valide.");
                        }
                    }
                }

                // Règle 2 : mode "redirect" sans URL de login est interdit si des routes sont protégées
                boolean aAnnotationAuth =
                    m.isAnnotationPresent(annotation.Authenticated.class)
                    || m.isAnnotationPresent(annotation.Role.class)
                    || classe.isAnnotationPresent(annotation.Authenticated.class)
                    || classe.isAnnotationPresent(annotation.Role.class);
                if (aAnnotationAuth
                    && "redirect".equals(authConfig.getDeniedMode())
                    && (authConfig.getLoginUrl() == null || authConfig.getLoginUrl().trim().isEmpty())) {
                    throw new RuntimeException(
                        "Mode 'redirect' configuré sans auth.login.url"
                        + " — impossible de rediriger vers la page de login.");
                }
            }
            System.out.println("=== AppListener : Validation des autorisations réussie ===");
            // =====================================================================================

        } catch (RuntimeException e) {
            System.out.println("Erreur AppListener : " + e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'initialisation de l'application", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().removeAttribute("urlMap");
        sce.getServletContext().removeAttribute("controllerClasses");
        System.out.println("=== AppListener : Application arrêtée ===");
    }
}
