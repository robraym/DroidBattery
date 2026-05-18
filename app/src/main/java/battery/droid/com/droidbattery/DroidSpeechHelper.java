package battery.droid.com.droidbattery;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
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
                    String utteranceId = "ID_" + System.currentTimeMillis();
                    ttsHolder[0].setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            shutdown(ttsHolder);
                        }

                        @Override
                        public void onError(String utteranceId) {
                            shutdown(ttsHolder);
                        }
                    });
                    ttsHolder[0].speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId);
                }
            } catch (Exception ex) {
                Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
                shutdown(ttsHolder);
            }
        });
    }

    private static void shutdown(final TextToSpeech[] ttsHolder) {
        try {
            if (ttsHolder[0] != null) {
                ttsHolder[0].shutdown();
                ttsHolder[0] = null;
            }
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }
}
