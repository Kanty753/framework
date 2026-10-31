package app.controllers;

import annotation.Authenticated;
import annotation.Controller;
import annotation.Role;
import annotation.UrlMapping;
import annotation.WebApi;
import utils.ModelAndView;
import utils.MySession;

/**
 * Contrôleur de test pour le Sprint 10 — Système d'autorisation.
 *
 * Démontre les trois niveaux d'accès :
 *   1. Routes publiques (aucune annotation) — accessibles par tous
 *   2. Routes @Authenticated — réservées aux utilisateurs connectés
 *   3. Routes @Role — réservées aux utilisateurs ayant le bon rôle
 *
 * Utilisateurs en dur (pour les tests) :
 *   - admin / admin123 → rôle "admin"
 *   - user  / user123  → rôle "user"
 *
 * NOTE : Le changement d'ID de session après login (changeSessionId)
 * n'est pas géré dans ce sprint car MySession est indexée par l'ancien ID.
 * TODO Sprint 11 : exposer une méthode refreshSession() pour gérer changeSessionId.
 */
@Controller
public class TestController {

    // -----------------------------------------------------------------------
    // Route publique - accessible à tous (aucune annotation d'autorisation)
    // -----------------------------------------------------------------------

    @UrlMapping(url = "/public", method = "GET")
    public ModelAndView publicPage() {
        ModelAndView mv = new ModelAndView("public");
        mv.addAttribute("message", "Page publique - accessible à tous");
        return mv;
    }

    // -----------------------------------------------------------------------
    // Login simulé — POST /login
    // Identifiants en dur : admin/admin123 et user/user123
    // Stocke en session : "user" (nom d'utilisateur) et "role" (rôle)
    // -----------------------------------------------------------------------

    @UrlMapping(url = "/login", method = "POST")
    public ModelAndView login(String username, String password, MySession session) {
        if ("admin".equals(username) && "admin123".equals(password)) {
            // Connexion admin réussie — enregistrement en session
            session.set("user", username);
            session.set("role", "admin");
            ModelAndView mv = new ModelAndView("profil");
            mv.addAttribute("message", "Bienvenue, admin !");
            mv.addAttribute("user", username);
            mv.addAttribute("role", "admin");
            return mv;

        } else if ("user".equals(username) && "user123".equals(password)) {
            // Connexion utilisateur simple réussie
            session.set("user", username);
            session.set("role", "user");
            ModelAndView mv = new ModelAndView("profil");
            mv.addAttribute("message", "Bienvenue, user !");
            mv.addAttribute("user", username);
            mv.addAttribute("role", "user");
            return mv;

        } else {
            // Identifiants incorrects
            ModelAndView mv = new ModelAndView("login");
            mv.addAttribute("erreur", "Identifiants incorrects");
            return mv;
        }
    }

    // -----------------------------------------------------------------------
    // Profil — @Authenticated : réservé aux utilisateurs connectés
    // Un utilisateur non connecté sera redirigé vers /app/login (mode redirect)
    // ou recevra un 401 (mode status).
    // -----------------------------------------------------------------------

    @UrlMapping(url = "/profil", method = "GET")
    @Authenticated
    public ModelAndView profil(MySession session) {
        ModelAndView mv = new ModelAndView("profil");
        mv.addAttribute("user", session.get("user"));
        mv.addAttribute("role", session.get("role"));
        return mv;
    }

    // -----------------------------------------------------------------------
    // Admin — @Role("admin") : réservé aux administrateurs
    // Un utilisateur avec le rôle "user" recevra un 403.
    // -----------------------------------------------------------------------

    @UrlMapping(url = "/admin", method = "GET")
    @Role("admin")
    public ModelAndView admin(MySession session) {
        ModelAndView mv = new ModelAndView("admin");
        mv.addAttribute("user", session.get("user"));
        return mv;
    }

    // -----------------------------------------------------------------------
    // API — @WebApi + @Authenticated : renvoie 401 JSON si non connecté
    // Pas de redirection pour les API ; le client doit gérer le code HTTP.
    // -----------------------------------------------------------------------

    @WebApi
    @UrlMapping(url = "/api/me", method = "GET")
    @Authenticated
    public Object apiMe(MySession session) {
        return new java.util.HashMap<String, Object>() {{
            put("user", session.get("user"));
            put("role", session.get("role"));
        }};
    }

    // -----------------------------------------------------------------------
    // Logout — invalide la session et retourne à la page de login
    // -----------------------------------------------------------------------

    @UrlMapping(url = "/logout", method = "POST")
    public ModelAndView logout(MySession session) {
        session.invalidate();
        ModelAndView mv = new ModelAndView("login");
        mv.addAttribute("message", "Déconnexion réussie");
        return mv;
    }
}
