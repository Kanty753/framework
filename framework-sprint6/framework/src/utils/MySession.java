package utils;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsule la session HTTP du conteneur et expose une API simple
 * (get / set / remove) pour les contrôleurs.
 *
 * <p>La {@code Map<String, Object>} interne est la source de vérité :
 * chaque modification est immédiatement répercutée sur la {@code HttpSession}
 * sous-jacente, de sorte que le conteneur reste synchronisé.</p>
 *
 * <p>Une seule instance existe par session HTTP ; elle est stockée dans
 * le {@code ServletContext} avec la clé {@code "mySession_" + sessionId}.</p>
 */
public class MySession {

    /** Préfixe utilisé pour stocker l'instance dans le ServletContext. */
    public static final String CONTEXT_KEY_PREFIX = "mySession_";

    /** Session HTTP sous-jacente. */
    private final HttpSession httpSession;

    /** Source de vérité : attributs de session gérés par le développeur. */
    private final Map<String, Object> attributes;

    // ---- Construction ----

    public MySession(HttpSession httpSession) {
        this.httpSession = httpSession;
        this.attributes = new HashMap<>();
    }

    // ---- API publique — attributs ----

    /**
     * Récupère un attribut de session.
     *
     * @param key nom de l'attribut
     * @return la valeur, ou {@code null} si absente
     */
    public Object get(String key) {
        return attributes.get(key);
    }

    /**
     * Stocke un attribut dans la session.
     * La valeur est immédiatement propagée à la {@code HttpSession}.
     *
     * @param key   nom de l'attribut
     * @param value valeur à stocker
     */
    public void set(String key, Object value) {
        attributes.put(key, value);
        httpSession.setAttribute(key, value);
    }

    /**
     * Supprime un attribut de la session.
     * L'attribut est également retiré de la {@code HttpSession}.
     *
     * @param key nom de l'attribut
     */
    public void remove(String key) {
        attributes.remove(key);
        httpSession.removeAttribute(key);
    }

    // ---- API publique — délégation HttpSession ----

    /** Retourne l'identifiant unique de la session. */
    public String getId() {
        return httpSession.getId();
    }

    /** Retourne l'instant de création (epoch millis). */
    public long getCreationTime() {
        return httpSession.getCreationTime();
    }

    /** Retourne l'instant du dernier accès (epoch millis). */
    public long getLastAccessedTime() {
        return httpSession.getLastAccessedTime();
    }

    /** Retourne le timeout d'inactivité maximal (en secondes). */
    public int getMaxInactiveInterval() {
        return httpSession.getMaxInactiveInterval();
    }

    /** Modifie le timeout d'inactivité maximal (en secondes). */
    public void setMaxInactiveInterval(int interval) {
        httpSession.setMaxInactiveInterval(interval);
    }

    /**
     * Invalide la session.
     * L'instance sera retirée du {@code ServletContext} par le
     * {@code SessionListener}.
     */
    public void invalidate() {
        attributes.clear();
        httpSession.invalidate();
    }

    /** Indique si la session vient d'être créée. */
    public boolean isNew() {
        return httpSession.isNew();
    }

    // ---- Accès interne ----

    /** Retourne la map interne (lecture seule recommandée). */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /** Retourne la session HTTP sous-jacente. */
    public HttpSession getHttpSession() {
        return httpSession;
    }

    @Override
    public String toString() {
        return "MySession{id='" + getId() + "', attributes=" + attributes + "}";
    }
}
