package battery.droid.com.droidbattery;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class DroidSpeechHelper {

    public static void speak(final Context context, final String text) {
        speak(context, text, null);
    }

    public static void speak(final Context context, final String text, final Runnable onFinished) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        if (text == null || text.trim().isEmpty()) {
            finish(onFinished);
            return;
        }

        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        final Context appContext = context.getApplicationContext();
        final TextToSpeech[] ttsHolder = new TextToSpeech[1];
        final AtomicBoolean finished = new AtomicBoolean(false);
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            if (finished.compareAndSet(false, true)) {
                shutdown(ttsHolder);
                finish(onFinished);
            }
        }, 25000);

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
                            if (finished.compareAndSet(false, true)) {
                                shutdown(ttsHolder);
                                finish(onFinished);
                            }
                        }

                        @Override
                        public void onError(String utteranceId) {
                            if (finished.compareAndSet(false, true)) {
                                shutdown(ttsHolder);
                                finish(onFinished);
                            }
                        }
                    });
                    ttsHolder[0].speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId);
                } else if (finished.compareAndSet(false, true)) {
                    shutdown(ttsHolder);
                    finish(onFinished);
                }
            } catch (Exception ex) {
                Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
                if (finished.compareAndSet(false, true)) {
                    shutdown(ttsHolder);
                    finish(onFinished);
                }
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

    private static void finish(final Runnable onFinished) {
        if (onFinished == null) {
            return;
        }
        try {
            onFinished.run();
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }
}
