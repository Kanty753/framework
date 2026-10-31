package annotation;

import java.lang.annotation.*;

/**
 * Restreint l'accès à une méthode ou une classe aux utilisateurs ayant
 * au moins l'un des rôles spécifiés.
 *
 * Implique automatiquement @Authenticated : un utilisateur non connecté
 * sera traité comme NON_AUTHENTIFIE (et non INTERDIT).
 *
 * Règle fail-fast : un tableau de rôles vide est interdit et fera planter
 * l'application au démarrage (AppListener).
 *
 * Exemple :
 *   @Role("admin")
 *   @Role({"admin", "moderateur"})
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Role {
    /** Rôles autorisés. Au moins un rôle doit être fourni. */
    String[] value();
}
