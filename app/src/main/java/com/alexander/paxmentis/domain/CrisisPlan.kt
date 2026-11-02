package com.alexander.paxmentis.domain

/**
 * Persönlicher Krisenplan nach dem Safety-Planning-Ansatz (Stanley & Brown).
 * Reine Logik ohne Android-Abhängigkeiten, damit sie per JVM-Unit-Test prüfbar ist.
 *
 * Der Plan wird in einer ruhigen Phase ausgefüllt. In der Krise geht man die Schritte der Reihe
 * nach durch: Hilft ein Schritt nicht, kommt der nächste – bis hin zur fachlichen Hilfe.
 */
enum class CrisisPlanSection(
    val title: String,
    val question: String,
    val example: String,
    val hasPhone: Boolean = false
) {
    WARNING_SIGNS(
        "Meine Warnzeichen",
        "Woran merke ich, dass eine Krise beginnt?",
        "z. B. Ich ziehe mich zurück, schlafe kaum, denke „Es hat alles keinen Sinn“."
    ),
    OWN_STRATEGIES(
        "Was ich selbst tun kann",
        "Was hilft mir, ohne dass ich jemanden brauche?",
        "z. B. kalt duschen, Box-Atmung, laute Musik, rausgehen."
    ),
    DISTRACTION(
        "Menschen und Orte, die mich ablenken",
        "Wo oder mit wem komme ich auf andere Gedanken?",
        "z. B. das Café um die Ecke, meine Nachbarin, der Sportverein.",
        hasPhone = true
    ),
    HELP_PEOPLE(
        "Menschen, die ich um Hilfe bitten kann",
        "Wem kann ich sagen, wie es mir wirklich geht?",
        "z. B. meine Schwester, mein bester Freund.",
        hasPhone = true
    ),
    PROFESSIONALS(
        "Fachliche Hilfe",
        "Wen kann ich professionell erreichen?",
        "z. B. meine Therapeutin, der Krisendienst, die Klinik in der Nähe.",
        hasPhone = true
    ),
    SAFE_ENVIRONMENT(
        "Meine Umgebung sicher machen",
        "Was kann ich weglegen, abgeben oder wegschließen?",
        "z. B. Medikamente bei jemandem lassen, das Auto stehen lassen."
    ),
    REASONS(
        "Wofür es sich lohnt",
        "Was ist mir wichtig? Wofür will ich weiterleben?",
        "z. B. mein Hund, meine Nichte, das Meer im Sommer."
    );

    companion object {
        /** Gespeichert wird der Name; unbekannte Werte (z. B. aus einer neueren Version) werden ignoriert. */
        fun fromStored(name: String): CrisisPlanSection? = entries.firstOrNull { it.name == name }
    }
}

/**
 * Die Schritte für den Krisenmodus, in der Reihenfolge des Plans. Leere Abschnitte werden
 * übersprungen. „Wofür es sich lohnt“ ist kein Schritt, sondern steht in der Krise immer sichtbar oben.
 */
fun crisisSteps(filled: Set<CrisisPlanSection>): List<CrisisPlanSection> =
    CrisisPlanSection.entries.filter { it != CrisisPlanSection.REASONS && it in filled }

/** Eine Nummer gilt als wählbar, sobald sie mindestens drei Ziffern enthält (112 ist die kürzeste). */
fun isDialable(phone: String?): Boolean = (phone?.count { it.isDigit() } ?: 0) >= 3
