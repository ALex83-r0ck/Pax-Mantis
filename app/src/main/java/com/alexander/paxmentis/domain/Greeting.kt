package com.alexander.paxmentis.domain

/** Begrüßung nach Tageszeit. Nachts kein „Gute Nacht“ – das wäre ein Abschied. */
fun greetingFor(hour: Int, name: String): String {
    val salutation = when {
        isNightTime(hour) -> "Hallo"
        hour < 11 -> "Guten Morgen"
        hour < 18 -> "Hallo"
        else -> "Guten Abend"
    }
    return if (name.isBlank()) salutation else "$salutation, ${name.trim()}"
}

fun greetingSubtitleFor(hour: Int): String =
    if (isNightTime(hour)) "Schön, dass du da bist – auch mitten in der Nacht."
    else "Schön, dass du da bist."
