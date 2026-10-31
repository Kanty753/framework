package annotation;

import java.lang.annotation.*;

/**
 * Marque une méthode ou une classe comme nécessitant une authentification.
 * L'utilisateur doit être connecté (clé de session présente) pour accéder
 * à la ressource annotée.
 *
 * Peut être placée sur la classe entière (toutes les routes) ou sur une
 * méthode individuelle (une seule route). La méthode prime sur la classe.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Authenticated {}
