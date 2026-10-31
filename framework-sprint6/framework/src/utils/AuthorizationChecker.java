package utils;

import annotation.Authenticated;
import annotation.Role;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Vérifie les droits d'accès d'un utilisateur sur une méthode de contrôleur.
 *
 * Logique de résolution des annotations (méthode prime sur classe) :
 *   1. Si la méthode a @Role  → utilise @Role (implique @Authenticated)
 *   2. Si la méthode a @Authenticated → utilise @Authenticated
 *   3. Si la classe a @Role  → utilise @Role
 *   4. Si la classe a @Authenticated → utilise @Authenticated
 *   5. Aucune annotation → accès libre (OK)
 *
 * Résultat encapsulé dans l'enum {@link AuthResult}.
 */
public class AuthorizationChecker {

    /**
     * Résultat de la vérification d'autorisation.
     */
    public enum AuthResult {
        /** Accès autorisé. */
        OK,
        /** Utilisateur non authentifié (session absente ou clé manquante). */
        NON_AUTHENTIFIE,
        /** Utilisateur authentifié mais rôle insuffisant. */
        INTERDIT
    }

    /**
     * Vérifie si l'utilisateur représenté par {@code session} est autorisé
     * à appeler {@code method} du contrôleur {@code controllerClass}.
     *
     * @param method          la méthode de contrôleur cible
     * @param controllerClass la classe du contrôleur
     * @param session         la session courante, peut être {@code null}
     * @return {@link AuthResult#OK}, {@link AuthResult#NON_AUTHENTIFIE}
     *         ou {@link AuthResult#INTERDIT}
     */
    public AuthResult check(Method method, Class<?> controllerClass, MySession session) {

        // Étape 1 : résoudre l'annotation effective
        Annotation annotation = resoudreAnnotation(method, controllerClass);

        // Aucune restriction configurée → accès libre
        if (annotation == null) {
            return AuthResult.OK;
        }

        // Étape 2 : vérifier la session
        if (session == null) {
            return AuthResult.NON_AUTHENTIFIE;
        }

        // Lire la valeur de session qui prouve l'authentification
        Object userValue;
        try {
            userValue = session.get(AuthConfig.getInstance().getSessionKey());
        } catch (Exception e) {
            // Erreur inattendue lors de la lecture de la session → refus par sécurité
            System.out.println("[AuthorizationChecker] Erreur de lecture de session : " + e.getMessage());
            return AuthResult.NON_AUTHENTIFIE;
        }

        // Clé absente → non authentifié
        if (userValue == null) {
            return AuthResult.NON_AUTHENTIFIE;
        }

        // L'utilisateur est authentifié.
        // Si l'annotation est @Authenticated uniquement → accès OK
        if (annotation instanceof Authenticated) {
            return AuthResult.OK;
        }

        // Étape 3 : annotation @Role → vérifier le rôle
        if (annotation instanceof Role) {
            Role roleAnnotation = (Role) annotation;
            String[] rolesRequis = roleAnnotation.value();

            // Lire le rôle depuis la session
            Object roleValue;
            try {
                roleValue = session.get(AuthConfig.getInstance().getRoleKey());
            } catch (Exception e) {
                System.out.println("[AuthorizationChecker] Erreur de lecture du rôle en session : " + e.getMessage());
                return AuthResult.INTERDIT;
            }

            // Aucun rôle en session
            if (roleValue == null) {
                return AuthResult.INTERDIT;
            }

            // Comparer chaque rôle requis au rôle stocké en session
            // La valeur peut être une String ou une Collection<String>
            for (String roleRequis : rolesRequis) {
                if (roleValue instanceof String) {
                    if (roleValue.equals(roleRequis)) {
                        return AuthResult.OK;
                    }
                } else if (roleValue instanceof Collection) {
                    @SuppressWarnings("unchecked")
                    Collection<Object> roles = (Collection<Object>) roleValue;
                    if (roles.contains(roleRequis)) {
                        return AuthResult.OK;
                    }
                }
            }

            // Aucun rôle ne correspond
            return AuthResult.INTERDIT;
        }

        // Cas normalement impossible si les annotations sont bien définies
        return AuthResult.OK;
    }

    /**
     * Résout l'annotation d'autorisation effective pour une méthode donnée.
     * La méthode prime sur la classe.
     *
     * @param method          la méthode à inspecter
     * @param controllerClass la classe du contrôleur
     * @return l'annotation effective (@Role ou @Authenticated), ou {@code null}
     *         si aucune restriction n'est configurée
     */
    private Annotation resoudreAnnotation(Method method, Class<?> controllerClass) {
        // 1. @Role sur la méthode (implique authentification)
        if (method.isAnnotationPresent(Role.class)) {
            return method.getAnnotation(Role.class);
        }
        // 2. @Authenticated sur la méthode
        if (method.isAnnotationPresent(Authenticated.class)) {
            return method.getAnnotation(Authenticated.class);
        }
        // 3. @Role sur la classe
        if (controllerClass.isAnnotationPresent(Role.class)) {
            return controllerClass.getAnnotation(Role.class);
        }
        // 4. @Authenticated sur la classe
        if (controllerClass.isAnnotationPresent(Authenticated.class)) {
            return controllerClass.getAnnotation(Authenticated.class);
        }
        // 5. Aucune restriction
        return null;
    }
}
