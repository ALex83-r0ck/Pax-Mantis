package com.alexander.paxmentis.domain

/**
 * Austauschformat des Krisenplans: eine Textdatei, die Menschen lesen und ausdrucken können
 * und die PaxMentis wieder einlesen kann. Der Plan überlebt so Deinstallation und Handywechsel –
 * der Datenbankschlüssel liegt im Keystore und geht dabei verloren.
 *
 * ```
 * PaxMentis Krisenplan
 *
 * ## Menschen, die mir helfen [HELP_PEOPLE]
 * - Mama | Tel: 0170 1234567
 * ```
 * Der Schlüssel in eckigen Klammern macht den Import unabhängig von späteren Titeländerungen.
 */
data class PlanEntry(val section: CrisisPlanSection, val text: String, val phone: String? = null)

const val CRISIS_PLAN_HEADER = "PaxMentis Krisenplan"
private const val PHONE_MARKER = " | Tel: "
private val SECTION_LINE = Regex("""^##\s.*\[([A-Z_]+)]\s*$""")

fun exportCrisisPlan(entries: List<PlanEntry>, exportedOn: String): String = buildString {
    appendLine(CRISIS_PLAN_HEADER)
    appendLine("Stand: $exportedOn")
    CrisisPlanSection.entries.forEach { section ->
        val items = entries.filter { it.section == section }
        if (items.isEmpty()) return@forEach
        appendLine()
        appendLine("## ${section.title} [${section.name}]")
        items.forEach { entry ->
            append("- ").append(escape(entry.text))
            entry.phone?.takeIf { it.isNotBlank() }?.let { append(PHONE_MARKER).append(escape(it)) }
            appendLine()
        }
    }
}

/** @return die Einträge, oder null, wenn es keine Krisenplan-Datei ist. */
fun parseCrisisPlan(content: String): List<PlanEntry>? {
    val lines = content.removePrefix("\uFEFF").lines()
    if (lines.firstOrNull()?.trim() != CRISIS_PLAN_HEADER) return null

    var section: CrisisPlanSection? = null
    return lines.drop(1).mapNotNull { raw ->
        val line = raw.trimEnd()
        SECTION_LINE.find(line)?.let { match ->
            section = CrisisPlanSection.fromStored(match.groupValues[1])
            return@mapNotNull null
        }
        val current = section ?: return@mapNotNull null
        if (!line.startsWith("- ")) return@mapNotNull null
        val (text, phone) = splitPhone(line.removePrefix("- "))
        unescape(text).trim().takeIf { it.isNotEmpty() }?.let {
            PlanEntry(current, it, phone?.let(::unescape)?.trim()?.ifEmpty { null })
        }
    }
}

/** Einträge, die es im Plan so noch nicht gibt – ein erneuter Import verdoppelt nichts. */
fun newEntries(existing: List<PlanEntry>, imported: List<PlanEntry>): List<PlanEntry> =
    imported.distinct().filterNot { it in existing }

// Zeilenumbrüche und das Trennzeichen im Text maskieren, damit jeder Eintrag eine Zeile bleibt.
private fun escape(value: String): String =
    value.replace("\\", "\\\\").replace("|", "\\|").replace("\r\n", "\n").replace("\n", "\\n")

private fun unescape(value: String): String = buildString {
    var i = 0
    while (i < value.length) {
        val c = value[i]
        if (c == '\\' && i + 1 < value.length) {
            append(if (value[i + 1] == 'n') '\n' else value[i + 1])
            i += 2
        } else {
            append(c)
            i++
        }
    }
}

/** Trennt am ersten nicht maskierten " | Tel: ". */
private fun splitPhone(line: String): Pair<String, String?> {
    var i = 0
    while (i < line.length) {
        when {
            line[i] == '\\' -> i += 2
            line.startsWith(PHONE_MARKER, i) -> return line.substring(0, i) to line.substring(i + PHONE_MARKER.length)
            else -> i++
        }
    }
    return line to null
}
