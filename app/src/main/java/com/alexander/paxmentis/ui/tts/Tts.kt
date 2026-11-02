package com.alexander.paxmentis.ui.tts

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Dünne Hülle um [TextToSpeech] für Compose-Screens.
 *
 * Wichtigster Unterschied zum direkten Aufruf: Text, der gesprochen werden soll, bevor die
 * Engine fertig initialisiert ist, geht nicht verloren, sondern wird nachgeholt. Vorher
 * fehlte deshalb in fast jedem Screen der erste Satz.
 */
class TtsController internal constructor() {
    internal var engine: TextToSpeech? = null

    var isReady by mutableStateOf(false)
        internal set

    private var pending: String? = null

    fun speak(text: String) {
        val current = engine
        if (isReady && current != null) {
            current.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            pending = text
        }
    }

    fun stop() {
        pending = null
        engine?.stop()
    }

    internal fun onReady() {
        isReady = true
        pending?.let {
            pending = null
            speak(it)
        }
    }
}

@Composable
fun rememberTts(speechRate: Float = 1f, pitch: Float = 1f): TtsController {
    val context = LocalContext.current
    val controller = remember { TtsController() }

    DisposableEffect(Unit) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.apply {
                    language = Locale.GERMAN
                    setSpeechRate(speechRate)
                    setPitch(pitch)
                }
                controller.onReady()
            }
        }
        controller.engine = engine
        onDispose {
            controller.engine = null
            engine?.stop()
            engine?.shutdown()
        }
    }
    return controller
}
