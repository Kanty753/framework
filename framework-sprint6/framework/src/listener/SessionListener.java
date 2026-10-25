package listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import utils.MySession;

/**
 * Nettoie l'instance {@link MySession} du {@code ServletContext}
 * lorsqu'une session HTTP est détruite (invalidation explicite ou
 * expiration par timeout).
 *
 * <p>Cela évite les fuites mémoire : sans ce listener, les instances
 * de {@code MySession} resteraient indéfiniment dans le contexte
 * applicatif.</p>
 */
public class SessionListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // Rien à faire : l'instance MySession est créée à la demande
        // dans le DispatcherServlet lors du premier accès.
        System.out.println("[SessionListener] Session créée : " + se.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        String sessionId = se.getSession().getId();
        String contextKey = MySession.CONTEXT_KEY_PREFIX + sessionId;

        ServletContext ctx = se.getSession().getServletContext();
        Object removed = ctx.getAttribute(contextKey);

        if (removed != null) {
            ctx.removeAttribute(contextKey);
            System.out.println("[SessionListener] MySession nettoyée pour la session : " + sessionId);
        } else {
            System.out.println("[SessionListener] Aucune MySession à nettoyer pour la session : " + sessionId);
        }
    }
}
