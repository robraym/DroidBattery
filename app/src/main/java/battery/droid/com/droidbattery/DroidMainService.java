package battery.droid.com.droidbattery;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;
import java.util.Set;

/**
 * Created by Robson on 12/08/2017.
 */

public class DroidMainService extends Service implements TextToSpeech.OnInitListener {

    private static TextToSpeech tts;
    private Context context;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.getDefault());
            // Aqui você pode definir uma velocidade se quiser: tts.setSpeechRate(1.0f);
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        try {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        try {
            try {
                context = getBaseContext();
                tts = new TextToSpeech(context, this);
                tts.setLanguage(Locale.getDefault());
            } catch (Exception ex) {
                Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
            }
            registerReceiver(batteryStatusReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            registerReceiver(batteryPowerReceiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
            registerReceiver(batteryPowerReceiver, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));


        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        try {
            if (batteryStatusReceiver != null) {
                unregisterReceiver(batteryStatusReceiver);
            }
            if (batteryPowerReceiver != null) {
                unregisterReceiver(batteryPowerReceiver);
            }
            if (tts != null) {
                tts.stop();
                tts.shutdown();
            }
            Intent broadcastIntent = new Intent("battery.droid.com.droidbattery.ACTION_RESTART_SERVICE");
            sendBroadcast(broadcastIntent);
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        super.onStartCommand(intent, flags, startId);
        DroidCommon.TimeSleep(2000);
        return START_STICKY;
    }

    public static void StopService(Context context) {
        if (isMyServiceRunning(context)) {
            Intent intentService = new Intent(context, DroidMainService.class);
            try {
                context.stopService(intentService);
                DroidCommon.TimeSleep(1000);
            } catch (Exception ex) {
                Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
            }
        }
    }

    public static void StartService(Context context) {
        if (!isMyServiceRunning(context)) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
            Intent intentService = new Intent(context, DroidMainService.class);
            try {
                context.startService(intentService);
                DroidCommon.TimeSleep(1000);
            } catch (Exception ex) {
            }
        }
    }

    public static void ChamaSinteseVoz(Context context) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        try {
            boolean dispositivoConectado = DroidCommon.ObtemStatusDispositivoConectado(context);
            boolean dispositivoDesconectado = DroidCommon.ObtemStatusDispositivoDesconectado(context);
            if (DroidCommon.InformaDispositivoConectadoDesconectado) {
                if (dispositivoConectado) {
                    VozDispositivoConectado(context);
                    VozPercentualActual(context);
                } else if (dispositivoDesconectado) {
                    VozDispositivoDesConectado(context);
                    VozPercentualActual(context);
                }
            }
            if (dispositivoConectado) {
                if (DroidCommon.InformarBateriaCarregada(context)) {
                    VozBateriaCarregada(context);
                } else if (DroidCommon.InformarPercentualAtingidoMultiSelectPreference(context)) {
                    VozPercentualAgingidoMultiSelectPreference(context);
                }
            }
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }




    private static boolean isMyServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (DroidMainService.class.getName().equals(service.service.getClassName())) {
                Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " true");
                return true;
            }
        }
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " false");
        return false;
    }

    public static void VozBateriaCarregada(Context context) {
        Fala(context, DroidCommon.PreferenceFalaBateriaCarregada(context));
    }

    public static void VozPercentualAgingidoMultiSelectPreference(Context context) {
        Fala(context, DroidCommon.MultSelectPreferencePercentualAtingido(context) + " por cento");
    }

    public static void VozPercentualActual(Context context) {
        Fala(context, DroidCommon.BatteryCurrent.toString() + " por cento");
    }


    public static void VozDispositivoConectado(Context context) {
        Fala(context, DroidCommon.PreferenceDispositivoConectado(context));
    }

    public static void VozDispositivoDesConectado(Context context) {
        Fala(context, DroidCommon.PreferenceDispositivoDesconectado(context));
    }

    private static void Fala(Context context, String texto) {
        if (DroidCommon.SinteseVozNaoPerturbeAtivado(context)) {
            Toast.makeText(context, texto, Toast.LENGTH_SHORT).show();
            // Usamos null no Listener se não for dar stopSelf imediatamente
            tts.speak(texto, TextToSpeech.QUEUE_ADD, null, "ID_" + System.currentTimeMillis());
        }
    }

    // Remova o loop 'while' daqui. Se você tem outras classes chamando esse método,
// deixe-o vazio apenas para não dar erro de compilação.
    private static void AguardandoFalar() {
        // O controle agora é assíncrono via UtteranceProgressListener no método Fala.
        Log.d(DroidCommon.TAG, "Aguardando conclusão da fala via Listener...");
    }

    public static BroadcastReceiver batteryPowerReceiver = new BroadcastReceiver() {
        private boolean dispositivoConectado;
        private boolean dispositivoDesconectado;
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
            Log.d(DroidCommon.TAG, "DroidSetStatusBatteryReceiver: " + intent.getAction());
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||
                    intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
                Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable())+ " " + " ACTION BOOT or MY_PACKAGE_REPLACED ");
                DroidMainService.StartService(context);
            } else {
                dispositivoConectado = intent.getAction().equals(Intent.ACTION_POWER_CONNECTED);
                dispositivoDesconectado = intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED);
                try {
                    DroidCommon.SetBoolean(context, "dispositivoConectado", dispositivoConectado);
                    DroidCommon.SetBoolean(context, "dispositivoDesconectado", dispositivoDesconectado);
                } catch (Exception ex) {
                    Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
                }
                // Dentro do seu batteryPowerReceiver
                if (dispositivoConectado || dispositivoDesconectado) {
                    DroidCommon.InformaDispositivoConectadoDesconectado = true;

                    // 1. Atualiza cor (fica branco ou azul na hora)
                    DroidCommon.AtualizaCorBateriaPorPreferenceValor(context);

                    // 2. Dispara a voz e a animação
                    DroidMainService.ChamaSinteseVoz(context);
                    DroidCommon.LoopingBateria(context);

                    DroidCommon.InformaDispositivoConectadoDesconectado = false;
                }
            }
        }
    };

    public static BroadcastReceiver batteryStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));

            try {
                int level = intent.getIntExtra("level", 0);
                String battery = String.valueOf(level);
                Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " " + battery);
                boolean alterouBateria = !DroidCommon.BatteryCurrent.contains(battery);
                boolean bateria100 = battery.equals("100") || battery.equals(DroidCommon.ValorBateriaCarregada);

                if (alterouBateria || (bateria100 && !DroidCommon.BateriaCarregada)) {
                    DroidCommon.BatteryCurrent = battery;

                    if (bateria100) {
                        DroidCommon.ObtemStatusBateria(context);
                        //int statusBateria = DroidCommon.ObtemStatusBateria(context);
                       // DroidCommon.BateriaCarregada = statusBateria == BatteryManager.BATTERY_STATUS_FULL || statusBateria == BatteryManager.BATTERY_STATUS_NOT_CHARGING || DroidCommon.BatteryCurrent.equals(DroidCommon.ValorBateriaCarregada);
                    }
                    if (alterouBateria || DroidCommon.BateriaCarregada ) {
                        DroidCommon.AtualizaCorBateriaPorPreferenceValor(context);
                        ChamaSinteseVoz(context);
                    }
                }
            } catch (Exception ex) {
                Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
            }
        }
    };
}
