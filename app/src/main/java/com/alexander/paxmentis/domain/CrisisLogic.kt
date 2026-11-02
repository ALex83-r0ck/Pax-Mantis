package com.alexander.paxmentis.domain

/**
 * Reine Logik ohne Android-Abhängigkeiten, damit sie per JVM-Unit-Test prüfbar ist.
 */

// --- Panikhilfe -------------------------------------------------------------------------

val PANIC_STEPS = listOf(
    "Atme tief ein und zähle bis 4",
    "Halte den Atem für 4 Sekunden",
    "Atme langsam aus und zähle bis 8",
    "Fokussiere dich auf einen Punkt in deiner Umgebung",
    "Wiederhole die Atmung, bis du ruhiger bist"
)

/** step == PANIC_STEPS.size bedeutet: Übung durchlaufen. */
fun isPanicDone(step: Int): Boolean = step >= PANIC_STEPS.size

fun nextPanicStep(step: Int): Int = (step + 1).coerceAtMost(PANIC_STEPS.size)

// --- Nachtwache -------------------------------------------------------------------------

const val MORNING_HOUR = 6

/** Minuten bis [MORNING_HOUR]:00 Uhr, ausgehend von der aktuellen Uhrzeit. */
fun minutesUntilMorning(hour: Int, minute: Int): Int {
    val now = hour * 60 + minute
    val morning = MORNING_HOUR * 60
    val diff = morning - now
    return if (diff > 0) diff else diff + 24 * 60
}

fun formatTimeUntilMorning(hour: Int, minute: Int): String {
    val total = minutesUntilMorning(hour, minute)
    val h = total / 60
    val m = total % 60
    val hours = if (h == 1) "1 Stunde" else "$h Stunden"
    val minutes = if (m == 1) "1 Minute" else "$m Minuten"
    return when {
        h == 0 -> minutes
        m == 0 -> hours
        else -> "$hours und $minutes"
    }
}

fun isNightTime(hour: Int): Boolean = hour >= 22 || hour < MORNING_HOUR

// --- Shared Reality ---------------------------------------------------------------------

/**
 * WhatsApp erwartet die Nummer international, nur Ziffern, ohne "+" oder führende Nullen.
 * Deutsche Nummern im Format "0170…" werden zu "49170…".
 */
fun normalizeWhatsAppNumber(raw: String, defaultCountryCode: String = "49"): String {
    val trimmed = raw.trim()
    val digits = trimmed.filter { it.isDigit() }
    return when {
        trimmed.startsWith("+") -> digits
        digits.startsWith("00") -> digits.drop(2)
        digits.startsWith("0") -> defaultCountryCode + digits.drop(1)
        else -> digits
    }
}

// --- Neutral-Messaging ------------------------------------------------------------------

enum class NeutralTemplate(val label: String, val text: String) {
    ANGER(
        "Wut/Vorwurf",
        "Ich habe die Situation wahrgenommen und konzentriere mich aktuell auf meine Ruhe."
    ),
    JUSTIFICATION(
        "Rechtfertigung",
        "Ich respektiere deine Sichtweise, bleibe aber bei meiner aktuellen Entscheidung zum Schutz meiner Stabilität."
    ),
    BOUNDARY(
        "Abgrenzung",
        "Ich benötige gerade Zeit für mich und werde mich melden, wenn ich bereit für ein sachliches Gespräch bin."
    ),
    TRIGGER(
        "Trigger-Reaktion",
        "Diese Information löst Unruhe aus. Ich nehme sie zur Kenntnis, reagiere aber erst später darauf."
    ),
    NEUTRAL(
        "Neutral",
        "Ich nehme das zur Kenntnis und werde zu einem geeigneten Zeitpunkt sachlich darauf zurückkommen."
    )
}

private val ANGER_WORDS = listOf("sauer", "wütend", "wut", "hasse", "unfair", "ätzend", "nervt")
private val JUSTIFICATION_WORDS = listOf("warum", "wieso", "weshalb", "rechtfertigen", "erklären")
private val BOUNDARY_WORDS = listOf("lass mich", "in ruhe", "ruhe", "abstand", "zeit für mich", "kein kontakt")
private val TRIGGER_WORDS = listOf("panik", "angst", "trigger", "unruhig", "zittere")

/**
 * Wählt anhand von Schlüsselwörtern eine Vorlage. Bewusst einfach – das ist eine
 * Vorlagen-Auswahl, keine echte Umformulierung. Es werden ganze Wörter verglichen,
 * damit z. B. "aber" in "Ich habe aber Hunger" nicht mehr fälschlich anschlägt.
 */
fun suggestNeutralTemplate(input: String): NeutralTemplate {
    val text = input.lowercase()
    fun containsAny(words: List<String>) = words.any { word ->
        Regex("(^|[^\\p{L}])${Regex.escape(word)}($|[^\\p{L}])").containsMatchIn(text)
    }
    return when {
        containsAny(ANGER_WORDS) -> NeutralTemplate.ANGER
        containsAny(BOUNDARY_WORDS) -> NeutralTemplate.BOUNDARY
        containsAny(TRIGGER_WORDS) -> NeutralTemplate.TRIGGER
        containsAny(JUSTIFICATION_WORDS) -> NeutralTemplate.JUSTIFICATION
        else -> NeutralTemplate.NEUTRAL
    }
}
