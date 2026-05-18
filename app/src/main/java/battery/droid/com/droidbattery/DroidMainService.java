package battery.droid.com.droidbattery;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayDeque;
import java.util.Locale;

public class DroidMainService extends Service implements TextToSpeech.OnInitListener {

    public static final String ACTION_REFRESH = "battery.droid.com.droidbattery.ACTION_REFRESH_MONITOR";

    private static final int NOTIFICATION_ID = 1001;
    private static final String NOTIFICATION_CHANNEL_ID = "battery_monitor";

    private static TextToSpeech tts;
    private static boolean ttsReady = false;
    private static final Object ttsLock = new Object();
    private static final ArrayDeque<String> pendingSpeech = new ArrayDeque<>();

    private Context context;

    private final BroadcastReceiver batteryStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
            if (intent == null || !Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                return;
            }
            handleBatteryChanged(intent);
        }
    };

    private final BroadcastReceiver powerConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            if (Intent.ACTION_POWER_CONNECTED.equals(action) || Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
                handlePowerConnectionChanged(action);
            }
        }
    };

    private String lastPowerAction = "";
    private long lastPowerActionAt = 0L;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onInit(int status) {
        handleTtsInit(status);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        context = getApplicationContext();

        try {
            startBatteryMonitorForeground();
            initializeTextToSpeech(context, this);

            Intent stickyBattery = registerReceiver(batteryStatusReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            IntentFilter powerFilter = new IntentFilter();
            powerFilter.addAction(Intent.ACTION_POWER_CONNECTED);
            powerFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
            registerReceiver(powerConnectionReceiver, powerFilter);
            if (stickyBattery != null) {
                handleBatteryChanged(stickyBattery);
            } else {
                refreshBatteryMonitor();
            }
            DroidWidget.scheduleNextWidgetRefresh(context);
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        super.onStartCommand(intent, flags, startId);

        try {
            startBatteryMonitorForeground();
            String action = intent != null ? intent.getAction() : null;
            if (Intent.ACTION_POWER_CONNECTED.equals(action) || Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
                handlePowerConnectionChanged(action);
            } else {
                refreshBatteryMonitor();
            }
            DroidWidget.scheduleNextWidgetRefresh(this);
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        try {
            unregisterReceiver(batteryStatusReceiver);
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
        try {
            unregisterReceiver(powerConnectionReceiver);
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }

        try {
            synchronized (ttsLock) {
                if (tts != null) {
                    tts.stop();
                    tts.shutdown();
                }
                tts = null;
                ttsReady = false;
                pendingSpeech.clear();
            }
            sendBroadcast(new Intent("battery.droid.com.droidbattery.ACTION_RESTART_SERVICE"));
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }

    public static void StopService(Context context) {
        Intent intentService = new Intent(context, DroidMainService.class);
        try {
            context.stopService(intentService);
            DroidCommon.TimeSleep(1000);
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }

    public static void StartService(Context context) {
        StartService(context, ACTION_REFRESH);
    }

    public static void StartService(Context context, String action) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        try {
            Intent intentService = new Intent(context, DroidMainService.class);
            if (action != null && !action.trim().isEmpty()) {
                intentService.setAction(action);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intentService);
            } else {
                context.startService(intentService);
            }
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }

    public static void ChamaSinteseVoz(Context context) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        try {
            ArrayDeque<String> falas = new ArrayDeque<>();
            boolean dispositivoConectado = DroidCommon.ObtemStatusDispositivoConectado(context);
            boolean dispositivoDesconectado = DroidCommon.ObtemStatusDispositivoDesconectado(context);

            if (DroidCommon.InformaDispositivoConectadoDesconectado) {
                if (dispositivoConectado) {
                    falas.add(DroidCommon.PreferenceDispositivoConectado(context));
                    falas.add(DroidCommon.BatteryCurrent + " por cento");
                } else if (dispositivoDesconectado) {
                    falas.add(DroidCommon.PreferenceDispositivoDesconectado(context));
                    falas.add(DroidCommon.BatteryCurrent + " por cento");
                }
            }

            if (dispositivoConectado) {
                if (DroidCommon.InformarBateriaCarregada(context)) {
                    falas.add(DroidCommon.PreferenceFalaBateriaCarregada(context));
                } else if (DroidCommon.InformarPercentualAtingidoMultiSelectPreference(context)) {
                    falas.add(DroidCommon.MultSelectPreferencePercentualAtingido(context) + " por cento");
                }
            }

            if (!falas.isEmpty()) {
                Fala(context, falas);
            }
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }

    public static void VozBateriaCarregada(Context context) {
        Fala(context, DroidCommon.PreferenceFalaBateriaCarregada(context));
    }

    public static void VozPercentualAgingidoMultiSelectPreference(Context context) {
        Fala(context, DroidCommon.MultSelectPreferencePercentualAtingido(context) + " por cento");
    }

    public static void VozPercentualActual(Context context) {
        Fala(context, DroidCommon.BatteryCurrent + " por cento");
    }

    public static void VozDispositivoConectado(Context context) {
        Fala(context, DroidCommon.PreferenceDispositivoConectado(context));
    }

    public static void VozDispositivoDesConectado(Context context) {
        Fala(context, DroidCommon.PreferenceDispositivoDesconectado(context));
    }

    private void handleBatteryChanged(Intent intent) {
        try {
            String previousBattery = DroidCommon.BatteryCurrent;
            int previousPercent = parsePercent(previousBattery);
            updateBatteryStateFromIntent(intent);
            int currentPercent = parsePercent(DroidCommon.BatteryCurrent);
            boolean changed = currentPercent != previousPercent;
            boolean full = DroidCommon.BateriaCarregada && currentPercent == 100;

            DroidCommon.AtualizaCorBateriaPorPreferenceValor(context);
            updateForegroundNotification();

            if (changed || full) {
                ChamaSinteseVoz(context);
            }
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }

    private void handlePowerConnectionChanged(String action) {
        try {
            boolean connected = Intent.ACTION_POWER_CONNECTED.equals(action);
            boolean disconnected = Intent.ACTION_POWER_DISCONNECTED.equals(action);
            if (!connected && !disconnected) {
                return;
            }
            if (isDuplicatePowerEvent(action)) {
                return;
            }

            DroidCommon.SetBoolean(context, "dispositivoConectado", connected);
            DroidCommon.SetBoolean(context, "dispositivoDesconectado", disconnected);
            DroidCommon.refreshBatteryWidget(context);
            DroidCommon.AtualizaCorBateriaPorPreferenceValor(context);
            DroidWidget.scheduleNextWidgetRefresh(context);
            updateForegroundNotification();

            ArrayDeque<String> falas = new ArrayDeque<>();
            falas.add(connected ?
                    DroidCommon.PreferenceDispositivoConectado(context) :
                    DroidCommon.PreferenceDispositivoDesconectado(context));
            falas.add(DroidCommon.BatteryCurrent + " por cento");
            Fala(context, falas);
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }

    private boolean isDuplicatePowerEvent(String action) {
        long now = System.currentTimeMillis();
        if (action.equals(lastPowerAction) && now - lastPowerActionAt < 2000L) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " power duplicado ignorado: " + action);
            return true;
        }
        lastPowerAction = action;
        lastPowerActionAt = now;
        return false;
    }

    private void refreshBatteryMonitor() {
        try {
            DroidCommon.refreshBatteryWidget(context);
            DroidCommon.AtualizaCorBateriaPorPreferenceValor(context);
            updateForegroundNotification();
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }

    private void updateBatteryStateFromIntent(Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);

        if (level >= 0 && scale > 0) {
            int percent = Math.round((level * 100f) / scale);
            DroidCommon.BatteryCurrent = String.valueOf(percent);
            DroidCommon.updateViewsInfoBattery(context, DroidCommon.BatteryCurrent);
        }

        DroidCommon.BateriaCarregada =
                status == BatteryManager.BATTERY_STATUS_FULL ||
                        status == BatteryManager.BATTERY_STATUS_NOT_CHARGING ||
                        DroidCommon.BatteryCurrent.equals(DroidCommon.ValorBateriaCarregada);
        DroidCommon.SetBoolean(context, "dispositivoConectado", plugged != 0);
        DroidCommon.SetBoolean(context, "dispositivoDesconectado", plugged == 0);
    }

    private static void Fala(Context context, String texto) {
        ArrayDeque<String> falas = new ArrayDeque<>();
        falas.add(texto);
        Fala(context, falas);
    }

    private static void Fala(Context context, ArrayDeque<String> falas) {
        if (DroidCommon.SinteseVozNaoPerturbeAtivado(context)) {
            speakSafely(context, falas);
        }
    }

    private static void speakSafely(Context context, ArrayDeque<String> falas) {
        if (falas == null || falas.isEmpty()) {
            return;
        }

        ArrayDeque<String> falasValidas = new ArrayDeque<>();
        for (String fala : falas) {
            if (fala != null && !fala.trim().isEmpty()) {
                falasValidas.add(fala);
            }
        }
        if (falasValidas.isEmpty()) {
            return;
        }

        Toast.makeText(context, falasValidas.peek(), Toast.LENGTH_SHORT).show();
        synchronized (ttsLock) {
            pendingSpeech.clear();
            pendingSpeech.addAll(falasValidas);
            if (tts == null) {
                StartService(context.getApplicationContext());
                initializeTextToSpeech(context.getApplicationContext(), status -> {
                    handleTtsInit(status);
                    if (status != TextToSpeech.SUCCESS) {
                        DroidSpeechHelper.speak(context.getApplicationContext(), joinSpeechMessages(falasValidas));
                    }
                });
                return;
            }

            tts.stop();
            if (!ttsReady) {
                Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " aguardando TTS inicializar");
                return;
            }
        }
        flushPendingSpeech();
    }

    private static void initializeTextToSpeech(Context context, TextToSpeech.OnInitListener listener) {
        synchronized (ttsLock) {
            if (tts != null) {
                return;
            }
            ttsReady = false;
            tts = new TextToSpeech(context.getApplicationContext(), listener);
        }
    }

    private static void handleTtsInit(int status) {
        synchronized (ttsLock) {
            if (status == TextToSpeech.SUCCESS && tts != null) {
                int languageResult = tts.setLanguage(Locale.getDefault());
                if (languageResult == TextToSpeech.LANG_MISSING_DATA ||
                        languageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(new Locale("pt", "BR"));
                }
                ttsReady = true;
            } else {
                Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " TTS init falhou: " + status);
                ttsReady = false;
                pendingSpeech.clear();
                if (tts != null) {
                    tts.shutdown();
                    tts = null;
                }
            }
        }
        flushPendingSpeech();
    }

    private static void flushPendingSpeech() {
        while (true) {
            String texto;
            synchronized (ttsLock) {
                if (!ttsReady || tts == null || pendingSpeech.isEmpty()) {
                    return;
                }
                texto = pendingSpeech.poll();
            }

            try {
                int result = tts.speak(texto, TextToSpeech.QUEUE_ADD, null, "ID_" + System.currentTimeMillis());
                if (result == TextToSpeech.ERROR) {
                    Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " TTS speak retornou ERROR");
                    synchronized (ttsLock) {
                        pendingSpeech.clear();
                        ttsReady = false;
                    }
                    return;
                }
            } catch (Exception ex) {
                Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
                synchronized (ttsLock) {
                    pendingSpeech.clear();
                    ttsReady = false;
                }
                return;
            }
        }
    }

    private static String joinSpeechMessages(ArrayDeque<String> falas) {
        StringBuilder builder = new StringBuilder();
        for (String fala : falas) {
            if (fala == null || fala.trim().isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(". ");
            }
            builder.append(fala);
        }
        return builder.toString();
    }

    private void startBatteryMonitorForeground() {
        createNotificationChannel();
        Notification notification = buildNotification();
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private void updateForegroundNotification() {
        try {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(NOTIFICATION_ID, buildNotification());
            }
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, DroidConfigurationActivity.class);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        String batteryText = DroidCommon.BatteryCurrent == null || DroidCommon.BatteryCurrent.trim().isEmpty() ?
                "--" : DroidCommon.BatteryCurrent + "%";
        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                new Notification.Builder(this, NOTIFICATION_CHANNEL_ID) :
                new Notification.Builder(this);

        builder.setContentTitle(getString(R.string.monitor_bateria_ativo))
                .setContentText(getString(R.string.monitor_bateria_status, batteryText))
                .setSmallIcon(R.drawable.ic_notification_battery)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setShowWhen(false);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(Notification.PRIORITY_LOW);
        }

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.monitor_bateria_canal),
                NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(getString(R.string.monitor_bateria_canal_descricao));
        notificationManager.createNotificationChannel(channel);
    }

    private int parsePercent(String text) {
        try {
            return Integer.parseInt(text);
        } catch (Exception ex) {
            return -1;
        }
    }

    private static boolean isMyServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (DroidMainService.class.getName().equals(service.service.getClassName())) {
                Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " true");
                return true;
            }
        }
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " false");
        return false;
    }
}
