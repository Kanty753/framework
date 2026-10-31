package utils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Registre des instances de contrôleurs — Sprint 11.
 *
 * <p>Garantit qu'<strong>une seule instance</strong> existe par classe de contrôleur
 * pendant toute la durée de vie de l'application. L'instanciation est
 * <strong>paresseuse</strong> (lazy) : le contrôleur n'est créé qu'au premier appel
 * d'une de ses routes, jamais au démarrage.</p>
 *
 * <h3>Thread-safety</h3>
 * <p>La map sous-jacente est une {@link ConcurrentHashMap}.
 * {@code computeIfAbsent} y est atomique : même si deux requêtes arrivent
 * simultanément pour un contrôleur qui n'a pas encore été instancié, le
 * constructeur n'est appelé qu'<strong>une seule fois</strong>. C'est le choix le
 * plus simple et le plus efficace pour ce cas d'usage (opération de création
 * rare, lecture fréquente).</p>
 *
 * <h3>Gestion des erreurs</h3>
 * <p>Si le constructeur lève une exception, rien n'est inséré dans la map.
 * Le prochain appel pourra réessayer l'instanciation (fail-open par
 * re-tentative, mais avec un message d'erreur clair).</p>
 *
 * <h3>Fin de vie</h3>
 * <p>Appeler {@link #vider()} dans {@code DispatcherServlet.destroy()} ou
 * {@code AppListener.contextDestroyed()} pour libérer les instances.</p>
 *
 * <h3>⚠ Instance partagée</h3>
 * <p>L'instance d'un contrôleur est <strong>partagée entre tous les utilisateurs
 * et toutes les requêtes simultanées</strong>. Un contrôleur ne doit donc
 * <strong>jamais</strong> stocker d'état propre à une requête ou à un utilisateur
 * dans ses champs d'instance — utiliser les paramètres de méthode et
 * {@link MySession} à la place.</p>
 */
public class ControllerRegistry {

    /**
     * Map Class → instance unique.
     * ConcurrentHashMap : lecture sans verrou, écriture atomique via computeIfAbsent.
     */
    private final ConcurrentHashMap<Class<?>, Object> instances = new ConcurrentHashMap<>();

    // ---- API publique ----

    /**
     * Retourne l'instance unique du contrôleur donné, en la créant si nécessaire.
     *
     * <p>L'instanciation est atomique grâce à {@code computeIfAbsent} :
     * deux appels simultanés ne créeront jamais deux instances.</p>
     *
     * @param classe la classe du contrôleur (doit avoir un constructeur sans argument)
     * @return l'instance unique
     * @throws RuntimeException si le constructeur lève une exception
     */
    public Object obtenirInstance(Class<?> classe) {
        // computeIfAbsent : n'appelle le lambda QUE si la clé est absente, de façon atomique.
        // Si le lambda lève une exception, rien n'est inséré → le prochain appel réessaiera.
        try {
            return instances.computeIfAbsent(classe, c -> {
                try {
                    Object instance = c.getDeclaredConstructor().newInstance();
                    System.out.println("[ControllerRegistry] Instance créée pour : " + c.getSimpleName());
                    return instance;
                } catch (Exception e) {
                    // Remonter via RuntimeException pour sortir du lambda
                    throw new RuntimeException(
                        "Impossible d'instancier le contrôleur '" + c.getSimpleName()
                        + "' : " + e.getMessage(), e);
                }
            });
        } catch (RuntimeException e) {
            // Si computeIfAbsent a lancé une RuntimeException depuis le lambda,
            // la clé n'a pas été insérée — le prochain appel pourra réessayer.
            throw new RuntimeException(
                "Erreur lors de l'obtention de l'instance de '" + classe.getSimpleName() + "'", e);
        }
    }

    /**
     * Vide le registre (à appeler à l'arrêt de l'application).
     * Les instances existantes sont abandonnées au GC.
     */
    public void vider() {
        int nb = instances.size();
        instances.clear();
        System.out.println("[ControllerRegistry] Registre vidé (" + nb + " instance(s) libérée(s)).");
    }

    /**
     * Retourne le nombre d'instances actuellement enregistrées.
     * Utile pour les logs et les tests.
     */
    public int taille() {
        return instances.size();
    }
}

