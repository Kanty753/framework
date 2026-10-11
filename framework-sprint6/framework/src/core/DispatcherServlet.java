package core;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dto.ControllerResultDTO;
import dto.UrlMappingDTO;
import utils.ModelAndView;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import com.google.gson.Gson;

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

    // private String toJson(Object obj) {
    // if (obj == null)
    // return "null";
    // if (obj instanceof String)
    // return "\"" + obj + "\"";
    // if (obj instanceof Number || obj instanceof Boolean)
    // return obj.toString();

    // // Objet — on lit les getters par réflexion
    // StringBuilder sb = new StringBuilder("{");
    // boolean first = true;

    // for (java.lang.reflect.Method m : obj.getClass().getMethods()) {
    // String name = m.getName();
    // if ((name.startsWith("get") && !name.equals("getClass") &&
    // m.getParameterCount() == 0)
    // || (name.startsWith("is") && m.getParameterCount() == 0)) {

    // String fieldName = name.startsWith("is")
    // ? Character.toLowerCase(name.charAt(2)) + name.substring(3)
    // : Character.toLowerCase(name.charAt(3)) + name.substring(4);

    // try {
    // Object value = m.invoke(obj);
    // if (!first)
    // sb.append(",");
    // sb.append("\"").append(fieldName).append("\":");
    // sb.append(toJson(value));
    // first = false;
    // } catch (Exception ignored) {
    // }
    // }
    // }

    // sb.append("}");
    // return sb.toString();
    // }

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
    // PARAMETER BINDING (Sprint 7)
    // =========================

    /**
     * Construit le tableau d'arguments à passer à method.invoke().
     * Pour chaque paramètre, lit req.getParameter(nomDuParametre) et convertit
     * la valeur String vers le type attendu par la méthode.
     * Requiert que les contrôleurs soient compilés avec javac -parameters.
     */
    private Object[] buildArgs(Method method, HttpServletRequest req) {
        Parameter[] params = method.getParameters();
        if (params.length == 0) {
            return new Object[0];
        }
        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            String nomParam = params[i].getName();
            Class<?> type = params[i].getType();
            String valeur = req.getParameter(nomParam);
            args[i] = convertir(valeur, type);
        }
        return args;
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
            return valeur; // null autorisé
        }
        // Primitif int / Integer
        if (type == int.class || type == Integer.class) {
            if (valeur == null || valeur.isEmpty()) return type == int.class ? 0 : null;
            return Integer.parseInt(valeur);
        }
        // Primitif long / Long
        if (type == long.class || type == Long.class) {
            if (valeur == null || valeur.isEmpty()) return type == long.class ? 0L : null;
            return Long.parseLong(valeur);
        }
        // Primitif double / Double
        if (type == double.class || type == Double.class) {
            if (valeur == null || valeur.isEmpty()) return type == double.class ? 0.0 : null;
            return Double.parseDouble(valeur);
        }
        // Primitif float / Float
        if (type == float.class || type == Float.class) {
            if (valeur == null || valeur.isEmpty()) return type == float.class ? 0.0f : null;
            return Float.parseFloat(valeur);
        }
        // Primitif boolean / Boolean
        if (type == boolean.class || type == Boolean.class) {
            if (valeur == null || valeur.isEmpty()) return type == boolean.class ? false : null;
            return Boolean.parseBoolean(valeur);
        }
        // Type non géré : retourner null
        return null;
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
