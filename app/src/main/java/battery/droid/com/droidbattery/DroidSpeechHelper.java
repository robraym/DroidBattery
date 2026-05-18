package battery.droid.com.droidbattery;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

public class DroidSpeechHelper {

    public static void speak(final Context context, final String text) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        final Context appContext = context.getApplicationContext();
        final TextToSpeech[] ttsHolder = new TextToSpeech[1];

        ttsHolder[0] = new TextToSpeech(appContext, status -> {
            try {
                if (status == TextToSpeech.SUCCESS && ttsHolder[0] != null) {
                    ttsHolder[0].setLanguage(Locale.getDefault());
                    ttsHolder[0].speak(text, TextToSpeech.QUEUE_ADD, null, "ID_" + System.currentTimeMillis());
                }
            } catch (Exception ex) {
                Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
            }
        });

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                if (ttsHolder[0] != null) {
                    ttsHolder[0].stop();
                    ttsHolder[0].shutdown();
                }
            } catch (Exception ex) {
                Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
            }
        }, 12000);
    }
}
