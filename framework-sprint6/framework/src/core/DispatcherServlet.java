package core;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dto.ControllerResultDTO;
import dto.MapFile;
import dto.UrlMappingDTO;
import utils.ModelAndView;
import utils.MySession;
import utils.UploadConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;
import com.google.gson.Gson;

// Limite : 10 Mo par fichier, 50 Mo par requête, seuil mémoire 256 Ko
@MultipartConfig(
    maxFileSize      = 1024 * 1024 * 10,   // 10 Mo par fichier
    maxRequestSize   = 1024 * 1024 * 50,    // 50 Mo au total
    fileSizeThreshold = 1024 * 256           // 256 Ko en mémoire avant écriture disque
)
public class DispatcherServlet extends HttpServlet {
    List<Class<?>> controllerClasses = new ArrayList<>();
    Map<UrlMappingDTO, ControllerResultDTO> map;

    @Override
    @SuppressWarnings("unchecked")
    public void init() throws ServletException {
        map = (Map<UrlMappingDTO, ControllerResultDTO>) getServletContext().getAttribute("urlMap");
        controllerClasses = (List<Class<?>>) getServletContext().getAttribute("controllerClasses");

        if (map == null || controllerClasses == null) {
            throw new ServletException(
                    "urlMap ou controllerClasses non initialisés — AppListener a-t-il bien démarré ?");
        }

        System.out.println("DispatcherServlet initialisé, " + map.size() + " route(s) chargée(s).");
    }

    public void affichage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

        if (path == null || path.isEmpty()) {
            path = request.getServletPath();
        }

        if (path == null || path.isEmpty()) {
            path = "/";
        }

        String httpMethod = request.getMethod();

        UrlMappingDTO key = new UrlMappingDTO(path, httpMethod);

        ControllerResultDTO found = map.get(key);

        // =========================
        // URL CONNUE
        // =========================
        if (found != null) {

            Method method = found.getMethod();
            Class<?> controllerClasse = found.getClasse();

            // ========================= AUTORISATION (Sprint 10) =========================
            // Obtenir la session EXISTANTE sans la créer si elle n'existe pas encore.
            // Cela évite de créer une session HTTP inutile pour les visiteurs anonymes
            // qui accèdent à des routes publiques.
            HttpSession httpSess = request.getSession(false);
            MySession sessionAuth = null;
            if (httpSess != null) {
                String ctxKey = MySession.CONTEXT_KEY_PREFIX + httpSess.getId();
                sessionAuth = (MySession) getServletContext().getAttribute(ctxKey);
            }
            // sessionAuth peut être null → AuthorizationChecker gère ce cas (NON_AUTHENTIFIE)

            utils.AuthorizationChecker checker = new utils.AuthorizationChecker();
            utils.AuthorizationChecker.AuthResult authResult =
                checker.check(method, controllerClasse, sessionAuth);

            if (authResult != utils.AuthorizationChecker.AuthResult.OK) {
                boolean estWebApi = utils.ControllerUtils.isWebApi(method);

                if (authResult == utils.AuthorizationChecker.AuthResult.NON_AUTHENTIFIE) {
                    // Utilisateur non connecté
                    if (estWebApi || "status".equals(utils.AuthConfig.getInstance().getDeniedMode())) {
                        // API ou mode status → 401 JSON
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().println("{\"erreur\":\"Authentification requise\",\"code\":401}");
                    } else {
                        // Mode redirect → 302 vers la page de login
                        response.setStatus(HttpServletResponse.SC_FOUND);
                        response.sendRedirect(utils.AuthConfig.getInstance().getLoginUrl());
                    }
                    return;
                }

                if (authResult == utils.AuthorizationChecker.AuthResult.INTERDIT) {
                    // Utilisateur connecté mais rôle insuffisant → 403
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    if (estWebApi) {
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().println(
                            "{\"erreur\":\"Accès interdit : rôle insuffisant\",\"code\":403}");
                    } else {
                        response.setContentType("text/plain;charset=UTF-8");
                        response.getWriter().println(
                            "403 - Accès interdit : vous n'avez pas le rôle requis pour accéder à cette ressource.");
                    }
                    return;
                }
            }
            // ============================================================================

            try {
                Object controllerInstance = controllerClasse.getDeclaredConstructor().newInstance();

                Object[] args = buildArgs(method, request);
                Object result = method.invoke(controllerInstance, args);

                // Retour ModelAndView — forward vers JSP
                if (result instanceof ModelAndView) {
                    ModelAndView mv = (ModelAndView) result;

                    if (response.isCommitted()) {
                        throw new ServletException("Impossible de forward : la réponse est déjà commitée");
                    }

                    response.resetBuffer();
                    response.setContentType("text/html;charset=UTF-8");

                    String nomPackage = (String) getServletContext().getInitParameter("pafSource");
                    String nomExtension = (String) getServletContext().getInitParameter("extension");
                    String view = "/" + nomPackage + mv.getView() + nomExtension;

                    Map<String, Object> attributes = mv.getAttributes();
                    if (attributes != null) {
                        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                            request.setAttribute(entry.getKey(), entry.getValue());
                        }
                    }

                    request.getRequestDispatcher(view).forward(request, response);
                    return;
                }

                // Retour JSON — contrôleur @WebApi
                if (utils.ControllerUtils.isWebApi(method)) {
                    response.setContentType("application/json;charset=UTF-8");
                    Gson gson = new Gson();
                    String json = gson.toJson(result);
                    response.getWriter().println(json);
                    return;
                }

                // Retour texte simple — contrôleur @Controller
                response.setContentType("text/plain");
                response.getWriter().println("=== ROUTE TROUVEE ===");
                response.getWriter().println("URL : " + path);
                response.getWriter().println("HTTP : " + httpMethod);
                response.getWriter().println("Controller : " + controllerClasse.getName());
                response.getWriter().println("Méthode Java : " + method.getName());
                response.getWriter().println();
                response.getWriter().println("=== RESULTAT DE L'EXECUTION ===");

                if (method.getReturnType().equals(Void.TYPE)) {
                    response.getWriter().println("(méthode void exécutée avec succès)");
                } else {
                    response.getWriter().println(String.valueOf(result));
                }

            } catch (Exception e) {
                response.setContentType("text/plain");
                response.getWriter().println("=== ERREUR D'EXECUTION ===");
                response.getWriter().println(e.getCause() != null ? e.getCause().toString() : e.toString());
            }

            return;
        }

        // =========================
        // URL INCONNUE
        // =========================
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().println("Route introuvable");
        response.getWriter().println("URL : " + path);
        response.getWriter().println("HTTP : " + httpMethod);
        response.getWriter().println();

        response.getWriter().println("=== ROUTES DISPONIBLES ===");
        for (UrlMappingDTO mapping : map.keySet()) {
            ControllerResultDTO r = map.get(mapping);
            response.getWriter().println(
                    mapping.getMethod()
                            + " "
                            + mapping.getUrl()
                            + " -> "
                            + r.getClasse().getSimpleName()
                            + "."
                            + r.getMethod().getName());
        }

        response.getWriter().println();
        response.getWriter().println("=== CONTROLLERS ===");
        for (Class<?> controllerClass : controllerClasses) {
            response.getWriter().println(controllerClass.getName());
        }
    }

    // =========================
    // PARAMETER BINDING (Sprint 7 + 7-bis)
    // =========================

    /**
     * Construit le tableau d'arguments à passer à method.invoke().
     * - Type simple  → lecture directe de req.getParameter(nomParam)
     * - Type objet   → construireObjet() avec le nom du paramètre comme préfixe
     * Requiert javac -parameters pour les vrais noms.
     */
    private Object[] buildArgs(Method method, HttpServletRequest req) {
        Parameter[] params = method.getParameters();
        if (params.length == 0) {
            return new Object[0];
        }
        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            String nomParam = params[i].getName();
            Class<?> type   = params[i].getType();

            // Injection de MySession
            if (type == MySession.class) {
                args[i] = getOrCreateMySession(req);
            }
            // Types simples (String, int, double…)
            else if (estSimple(type)) {
                String valeur = req.getParameter(nomParam);
                args[i] = convertir(valeur, type);
            }
            // Types complexes (objets, MapFile…)
            else {
                args[i] = construireObjet(type, nomParam, req);
            }
        }
        return args;
    }

    /**
     * Récupère l'instance unique de {@link MySession} pour la session HTTP
     * courante, ou la crée si c'est le premier accès.
     *
     * L'instance est stockée dans le {@code ServletContext} avec la clé
     * {@code "mySession_" + sessionId} pour garantir une seule instance
     * par session et par application.
     *
     * TODO Sprint 11 : gestion du changeSessionId — si HttpSession.changeSessionId()
     * est appelé, l'ancienne clé dans le ServletContext devient orpheline.
     * Il faudra exposer une méthode refreshSession() dans MySession ou ici.
     */
    private MySession getOrCreateMySession(HttpServletRequest req) {
        HttpSession httpSession = req.getSession(true);
        String contextKey = MySession.CONTEXT_KEY_PREFIX + httpSession.getId();

        // Chercher dans le contexte applicatif
        MySession mySession = (MySession) getServletContext().getAttribute(contextKey);

        if (mySession == null) {
            // Première requête de cette session : créer et enregistrer
            mySession = new MySession(httpSession);
            getServletContext().setAttribute(contextKey, mySession);
            System.out.println("[DispatcherServlet] Nouvelle MySession créée pour : " + httpSession.getId());
        }

        return mySession;
    }


    /**
     * Retourne true si le type est considéré comme simple :
     * primitifs, String, Number (Integer, Long, Double, Float…), Boolean.
     */
    private boolean estSimple(Class<?> type) {
        return type.isPrimitive()
            || type == String.class
            || Number.class.isAssignableFrom(type)
            || type == Boolean.class
            || type == Character.class;
    }

    /**
     * Crée une instance du type donné par réflexion (constructeur sans argument)
     * et remplit ses attributs depuis les paramètres HTTP.
     * Nom complet d'un champ = prefixe + "." + nomAttribut.
     * Attribut simple  → req.getParameter(nomComplet) + conversion.
     * Attribut objet   → appel récursif avec le préfixe allongé.
     * Les attributs static sont ignorés.
     */
    private Object construireObjet(Class<?> type, String prefixe, HttpServletRequest req) {
        try {
            Object instance = type.getDeclaredConstructor().newInstance();

            for (java.lang.reflect.Field field : type.getDeclaredFields()) {
                // Ignorer les champs static
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                String nomChamp  = field.getName();
                Class<?> typeChamp = field.getType();
                String nomComplet = prefixe + "." + nomChamp;

                Object valeurChamp;
                if (estSimple(typeChamp)) {
                    String raw = req.getParameter(nomComplet);
                    valeurChamp = convertir(raw, typeChamp);
                } else if (typeChamp == MapFile.class) {
                    // Sprint 8 : binding d'un fichier uploadé
                    valeurChamp = construireMapFile(nomComplet, req);
                } else {
                    valeurChamp = construireObjet(typeChamp, nomComplet, req);
                }

                // Appel du setter : setNom, setAge, setAdresse…
                String nomSetter = "set"
                    + Character.toUpperCase(nomChamp.charAt(0))
                    + nomChamp.substring(1);
                java.lang.reflect.Method setter =
                    type.getMethod(nomSetter, typeChamp);
                setter.invoke(instance, valeurChamp);
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException(
                "Erreur lors de la construction de " + type.getSimpleName()
                + " (préfixe='" + prefixe + "')", e);
        }
    }

    /**
     * Convertit une String (potentiellement null si absente) vers le type Java cible.
     * Types supportés : String, int/Integer, long/Long, double/Double,
     *                   float/Float, boolean/Boolean.
     * Valeur absente (null) : valeur par défaut pour les primitifs (0 / false),
     *                         null pour les types objet.
     */
    private Object convertir(String valeur, Class<?> type) {
        if (type == String.class) {
            return valeur;
        }
        if (type == int.class || type == Integer.class) {
            if (valeur == null || valeur.isEmpty()) return type == int.class ? 0 : null;
            return Integer.parseInt(valeur);
        }
        if (type == long.class || type == Long.class) {
            if (valeur == null || valeur.isEmpty()) return type == long.class ? 0L : null;
            return Long.parseLong(valeur);
        }
        if (type == double.class || type == Double.class) {
            if (valeur == null || valeur.isEmpty()) return type == double.class ? 0.0 : null;
            return Double.parseDouble(valeur);
        }
        if (type == float.class || type == Float.class) {
            if (valeur == null || valeur.isEmpty()) return type == float.class ? 0.0f : null;
            return Float.parseFloat(valeur);
        }
        if (type == boolean.class || type == Boolean.class) {
            if (valeur == null || valeur.isEmpty()) return type == boolean.class ? false : null;
            return Boolean.parseBoolean(valeur);
        }
        return null;
    }

    // =========================
    // UPLOAD DE FICHIERS (Sprint 8)
    // =========================

    /**
     * Construit un MapFile à partir de la Part correspondante dans la requête.
     * Gère les cas limites : requête non multipart, champ vide, nom dangereux.
     *
     * @param nomChamp le nom du champ formulaire (ex. "p.photo")
     * @param req      la requête HTTP
     * @return un MapFile rempli, ou null si aucun fichier n'a été envoyé
     */
    private MapFile construireMapFile(String nomChamp, HttpServletRequest req) {
        try {
            // Vérifier que la requête est bien multipart
            String contentType = req.getContentType();
            if (contentType == null || !contentType.toLowerCase().startsWith("multipart/")) {
                System.out.println("[Upload] Requête non multipart, champ '" + nomChamp + "' ignoré.");
                return null;
            }

            // Récupérer la Part correspondante
            Part part = req.getPart(nomChamp);

            // Aucun fichier envoyé pour ce champ
            if (part == null || part.getSize() == 0) {
                return null;
            }

            // Extraire le nom de fichier d'origine
            String nomOriginal = part.getSubmittedFileName();
            if (nomOriginal == null || nomOriginal.trim().isEmpty()) {
                // C'est un champ texte, pas un fichier
                return null;
            }

            // Nettoyer le nom contre le path traversal
            String nomNettoye = UploadConfig.nettoyerNomFichier(nomOriginal);

            // Lire le contenu en byte[]
            byte[] contenu;
            try (InputStream is = part.getInputStream()) {
                contenu = is.readAllBytes();
            }

            // Récupérer le type MIME
            String typeMime = part.getContentType();

            return new MapFile(nomNettoye, typeMime, contenu);

        } catch (IllegalStateException e) {
            // Déclenché quand le fichier dépasse la limite @MultipartConfig
            throw new RuntimeException(
                "Fichier trop volumineux pour le champ '" + nomChamp
                + "'. Vérifiez les limites dans @MultipartConfig.", e);
        } catch (Exception e) {
            throw new RuntimeException(
                "Erreur lors de la lecture du fichier pour le champ '" + nomChamp + "'", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        affichage(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        affichage(request, response);
    }
}
