package utils;

import java.io.InputStream;
import java.util.Properties;

/**
 * Charge la configuration d'autorisation depuis le fichier "auth.properties"
 * situé dans le classpath. Le fichier est lu une seule fois (lazy init).
 *
 * Valeurs par défaut si le fichier est absent ou si une clé manque :
 *   auth.session.key  → "user"
 *   auth.role.key     → "role"
 *   auth.denied.mode  → "redirect"
 *   auth.login.url    → "/app/login"
 */
public class AuthConfig {

    private static final String FICHIER_CONFIG = "auth.properties";

    // --- Clés du fichier properties ---
    private static final String CLE_SESSION_KEY  = "auth.session.key";
    private static final String CLE_ROLE_KEY     = "auth.role.key";
    private static final String CLE_DENIED_MODE  = "auth.denied.mode";
    private static final String CLE_LOGIN_URL    = "auth.login.url";

    // --- Valeurs par défaut ---
    private static final String DEFAULT_SESSION_KEY = "user";
    private static final String DEFAULT_ROLE_KEY    = "role";
    private static final String DEFAULT_DENIED_MODE = "redirect";
    private static final String DEFAULT_LOGIN_URL   = "/app/login";

    /** Instance unique (lazy, thread-safe via holder) */
    private static class Holder {
        static final AuthConfig INSTANCE = new AuthConfig();
    }

    // --- Champs chargés depuis le fichier ---
    private final String sessionKey;
    private final String roleKey;
    private final String deniedMode;
    private final String loginUrl;

    /**
     * Constructeur privé : charge le fichier auth.properties depuis le classpath.
     * Si le fichier est absent, les valeurs par défaut sont utilisées silencieusement.
     */
    private AuthConfig() {
        Properties props = new Properties();

        // Tentative de lecture du fichier depuis le classpath
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(FICHIER_CONFIG)) {

            if (is != null) {
                props.load(is);
                System.out.println("[AuthConfig] Fichier '" + FICHIER_CONFIG + "' chargé depuis le classpath.");
            } else {
                System.out.println("[AuthConfig] Fichier '" + FICHIER_CONFIG
                        + "' introuvable dans le classpath — valeurs par défaut utilisées.");
            }

        } catch (Exception e) {
            System.out.println("[AuthConfig] Erreur lors de la lecture de '" + FICHIER_CONFIG
                    + "' : " + e.getMessage() + " — valeurs par défaut utilisées.");
        }

        // Lecture des propriétés avec valeurs par défaut
        this.sessionKey = lirePropriete(props, CLE_SESSION_KEY, DEFAULT_SESSION_KEY);
        this.roleKey    = lirePropriete(props, CLE_ROLE_KEY,    DEFAULT_ROLE_KEY);
        this.deniedMode = lirePropriete(props, CLE_DENIED_MODE, DEFAULT_DENIED_MODE);
        this.loginUrl   = lirePropriete(props, CLE_LOGIN_URL,   DEFAULT_LOGIN_URL);

        System.out.println("[AuthConfig] sessionKey='" + sessionKey
                + "', roleKey='" + roleKey
                + "', deniedMode='" + deniedMode
                + "', loginUrl='" + loginUrl + "'");
    }

    /**
     * Lit une propriété depuis la map, en appliquant la valeur par défaut
     * si la clé est absente ou vide.
     */
    private static String lirePropriete(Properties props, String cle, String defaut) {
        String valeur = props.getProperty(cle);
        if (valeur == null || valeur.trim().isEmpty()) {
            return defaut;
        }
        return valeur.trim();
    }

    /** Retourne l'instance unique. */
    public static AuthConfig getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Clé de session utilisée pour vérifier si l'utilisateur est connecté.
     * Valeur par défaut : "user".
     */
    public String getSessionKey() {
        return sessionKey;
    }

    /**
     * Clé de session utilisée pour lire le rôle de l'utilisateur connecté.
     * Valeur par défaut : "role".
     */
    public String getRoleKey() {
        return roleKey;
    }

    /**
     * Mode de refus d'accès : "redirect" (redirection vers la page de login)
     * ou "status" (réponse HTTP 401/403 sans redirection).
     * Valeur par défaut : "redirect".
     */
    public String getDeniedMode() {
        return deniedMode;
    }

    /**
     * URL de la page de login, utilisée en mode "redirect".
     * Valeur par défaut : "/app/login".
     */
    public String getLoginUrl() {
        return loginUrl;
    }
}
