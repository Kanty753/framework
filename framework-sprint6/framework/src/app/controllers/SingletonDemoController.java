package app.controllers;

import annotation.Controller;
import annotation.UrlMapping;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contrôleur de démonstration du singleton — Sprint 11.
 *
 * <p>Montre qu'une seule instance est créée pendant toute la vie de
 * l'application :</p>
 * <ul>
 *   <li>Le constructeur affiche un message avec le {@code hashCode} de
 *       l'instance. Ce message ne doit apparaître <strong>qu'une seule fois</strong>
 *       dans les logs, lors du premier appel d'une route de ce contrôleur.</li>
 *   <li>{@code GET /singleton/compteur} : incrémente un compteur thread-safe
 *       et retourne sa valeur. 50 appels parallèles doivent donner 50.</li>
 *   <li>{@code GET /singleton/hash} : retourne le {@code hashCode} de
 *       l'instance — doit rester identique à travers tous les appels.</li>
 * </ul>
 *
 * <p><strong>Usage légal du champ d'instance :</strong> ici {@code compteur}
 * est un état <em>global à l'application</em> (total de visites), pas un état
 * propre à un utilisateur ou à une requête. C'est l'un des rares cas où un
 * champ d'instance est approprié dans un contrôleur singleton. Pour un état
 * par utilisateur, utiliser {@link utils.MySession}.</p>
 */
@Controller
public class SingletonDemoController {

    /**
     * Compteur d'appels global — thread-safe grâce à {@link AtomicInteger}.
     * Intentionnellement un champ d'instance pour démontrer la persistance
     * entre les requêtes (usage légal : état applicatif global).
     */
    private final AtomicInteger compteur = new AtomicInteger(0);

    /**
     * Constructeur — doit s'afficher UNE SEULE FOIS dans les logs.
     * Au démarrage : aucun message (instanciation paresseuse).
     * Au premier appel d'une route : un message avec le hashCode de l'instance.
     */
    public SingletonDemoController() {
        System.out.println("[SingletonDemo] instance créée : " + this.hashCode());
    }

    // -----------------------------------------------------------------------
    // GET /singleton/compteur
    // Incrémente le compteur global et retourne sa valeur.
    // Thread-safe : AtomicInteger.incrementAndGet() est atomique.
    // -----------------------------------------------------------------------

    @UrlMapping(url = "/singleton/compteur", method = "GET")
    public String incrementerCompteur() {
        int valeur = compteur.incrementAndGet();
        return "Compteur : " + valeur + " (instance hashCode : " + this.hashCode() + ")";
    }

    // -----------------------------------------------------------------------
    // GET /singleton/hash
    // Retourne le hashCode de l'instance courante.
    // Doit être identique à travers tous les appels et au message du constructeur.
    // -----------------------------------------------------------------------

    @UrlMapping(url = "/singleton/hash", method = "GET")
    public String obtenirHash() {
        return "HashCode de l'instance : " + this.hashCode()
            + " | Compteur actuel : " + compteur.get();
    }
}

