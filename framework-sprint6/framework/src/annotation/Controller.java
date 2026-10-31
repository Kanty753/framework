package annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marque une classe comme contrôleur MVC géré par le framework.
 *
 * <p>Les classes annotées sont détectées au démarrage par {@code AppListener}
 * et leurs routes {@code @UrlMapping} sont enregistrées dans la map globale.</p>
 *
 * <h3>⚠ Instance unique — pas d'état par requête (Sprint 11)</h3>
 * <p>Depuis le Sprint 11, le framework maintient <strong>une seule instance</strong>
 * par classe de contrôleur, partagée entre <strong>tous les utilisateurs et toutes
 * les requêtes simultanées</strong>.</p>
 * <p>Un contrôleur annoté {@code @Controller} ne doit donc <strong>jamais</strong>
 * stocker dans ses champs d'instance un état propre à une requête ou à un
 * utilisateur (ex. : l'utilisateur connecté, les données d'un formulaire…).
 * Utiliser les <strong>paramètres de méthode</strong> et {@link utils.MySession}
 * à la place.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {
}
