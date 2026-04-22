package battery.droid.com.droidbattery;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Vibrator;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Robson on 04/08/2017.
 */

public class DroidCommon {

    public static String TAG = "DroidBattery";
    public static String BatteryCurrent = "0";
    public static boolean InformaDispositivoConectadoDesconectado;
    public static boolean BateriaCarregada;
    public static String ValorBateriaCarregada = "100";

    public static int ObtemStatusBateria(Context context) {
        int retorno = -1;
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            retorno = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " " + retorno);
            DroidCommon.BateriaCarregada = retorno == BatteryManager.BATTERY_STATUS_FULL || retorno == BatteryManager.BATTERY_STATUS_NOT_CHARGING || DroidCommon.BatteryCurrent.equals(DroidCommon.ValorBateriaCarregada);
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " " + DroidCommon.BateriaCarregada);
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
        return retorno;
    }

    public static String getLogTagWithMethod(Throwable stack) {
        StackTraceElement[] trace = stack.getStackTrace();
        return trace[0].getClassName() + "." + trace[0].getMethodName() + ":" + trace[0].getLineNumber();
    }

    public static final String PREF_ID = "DroidPreferenceBattery";


    public static void SetList(Context context, String chave, List<String> valores) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_ID, 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Set<String> set = new HashSet<String>(valores);
            //set.addAll(valores);
            editor.putStringSet(chave, set);
            editor.commit();
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }

    public static Set<String> GetList(Context context, String chave) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        Set<String> set = new HashSet<String>();
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_ID, 0);
            set.addAll(sharedPreferences.getStringSet(chave, null));
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
        return set;
    }

    public static String GetValueList(Context context, String key) {
        try {
            // Pega as SharedPreferences padrão do sistema
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

            // O segundo parâmetro ("") é o valor padrão caso nada tenha sido selecionado
            return sp.getString(key, "");
        } catch (Exception ex) {
            Log.e(DroidCommon.TAG, "Erro ao ler ListPreference: " + ex.getMessage());
            return "";
        }
    }



    public static void SetInteger(Context context, String chave, int valor) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_ID, 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(chave, valor);
            editor.commit();
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }

    public static int GetInteger(Context context, String chave) {
        int retorno = 0;
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_ID, 0);
            retorno = sharedPreferences.getInt(chave, 0);
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " " + retorno);
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
        return retorno;
    }

    public static void SetBoolean(Context context, String chave, boolean valor) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_ID, 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(chave, valor);
            editor.commit();
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }

    public static boolean GetBoolean(Context context, String chave) {
        boolean retorno = false;
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_ID, 0);
            retorno = sharedPreferences.getBoolean(chave, false);
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " " + retorno);
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
        return retorno;
    }

    public static String handleTime(Context context, String time) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        String retorno = "";
        try {
            String[] timeParts = time.split(":");
            int lastHour = Integer.parseInt(timeParts[0]);
            int lastMinute = Integer.parseInt(timeParts[1]);

            boolean is24HourFormat = DateFormat.is24HourFormat(context);

            if (is24HourFormat) {
                retorno = ((lastHour < 10) ? "0" : "")
                        + Integer.toString(lastHour)
                        + ":" + ((lastMinute < 10) ? "0" : "")
                        + Integer.toString(lastMinute);
            } else {
                int myHour = lastHour % 12;
                retorno = ((myHour == 0) ? "12" : ((myHour < 10) ? "0" : "") + Integer.toString(myHour))
                        + ":" + ((lastMinute < 10) ? "0" : "")
                        + Integer.toString(lastMinute)
                        + ((lastHour >= 12) ? " PM" : " AM");
            }
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
        return retorno;
    }

    public static void onUpdateDroidWidget(Context context) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        try {
            DroidWidget droidWidget = new DroidWidget();
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            droidWidget.onUpdate(context, appWidgetManager, null);
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }

    public static void updateViewsSizeBattery(Context context) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        try {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            Integer min_width = DroidCommon.GetInteger(context, "MIN_WIDTH");

            if (min_width > 110) {
                views.setTextViewTextSize(R.id.batteryText, TypedValue.COMPLEX_UNIT_DIP, 50);
            } else {
                views.setTextViewTextSize(R.id.batteryText, TypedValue.COMPLEX_UNIT_DIP, 30);
            }

            ComponentName componentName = new ComponentName(context, DroidWidget.class);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            appWidgetManager.updateAppWidget(componentName, views);

        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }

    public static boolean PreferenceAtivarSinteseVoz(final Context context) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        boolean spf = true;
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            spf = sp.getBoolean("spf_ativarSinteseVoz", true);
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
        return spf;
    }

    public static String PreferenceDispositivoConectado(final Context context) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        String spf = "";
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            spf = sp.getString("dispositivoConectado", context.getString(R.string.txt_dispositivo_conectado));
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
        return spf;
    }

    public static String PreferenceDispositivoDesconectado(final Context context) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        String spf = "";
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            spf = sp.getString("dispositivoDesconectado", context.getString(R.string.txt_dispositivo_desconectado));
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
        return spf;
    }

    public static String MultSelectPreferencePercentualAtingido(final Context context) {
        String spf = "";
        try {
            int total = -1;
            Set<String> multiSelectPreference = DroidCommon.GetList(context, "multiSelectPreference");
            if (multiSelectPreference != null) {
                total = multiSelectPreference.size();
                for (String retval : multiSelectPreference) {
                    if (DroidCommon.BatteryCurrent.equals(retval)) {
                        spf = retval.toString();
                        break;
                    }
                }
            }
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " " + total);
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
        return spf;
    }

    public static String PreferenceFalaBateriaCarregada(final Context context) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        String spf = "";
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            spf = sp.getString("falaBateriaCarregada", context.getString(R.string.txt_fala_bateria_carregada));
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
        return spf;
    }

    public static void updateViewsInfoBattery(Context context, String batteryLevel) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        try {
            // No Android 16, não podemos mandar apenas o texto.
            // Precisamos mandar a cor junto para o sistema não "resetar" para a cor anterior.
            DroidCommon.BatteryCurrent = batteryLevel;

            // Em vez de atualizar aqui, chamamos o método que manda cor e texto juntos
            // mas sem entrar em loop infinito.
            PintarWidgetCompleto(context);

        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }

    public static void updateViewsColorBattery(Context context, int color) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        try {
            // Guardamos a cor desejada para uso no PintarWidgetCompleto
            DroidCommon.SetInteger(context, "ULTIMA_COR_DEFINIDA", color);
            PintarWidgetCompleto(context);
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }

    // NOVO MÉTODO: Centraliza a pintura para não ter erro no Android 16
    private static void PintarWidgetCompleto(Context context) {
        try {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            // Pega a última cor que deveria estar lá (Padrão Branco se não achar)
            int corParaPintar = context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE).getInt("ULTIMA_COR_DEFINIDA", Color.WHITE);

            views.setTextColor(R.id.batteryText, corParaPintar);
            views.setTextViewText(R.id.batteryText, DroidCommon.BatteryCurrent + "%");

            Integer min_width = DroidCommon.GetInteger(context, "MIN_WIDTH");
            float fontSize = (min_width > 110) ? 50 : 30;
            views.setTextViewTextSize(R.id.batteryText, TypedValue.COMPLEX_UNIT_DIP, fontSize);

            // Intent do clique com as flags obrigatórias do Android 16
            Intent intent = new Intent(context, DroidWidget.class);
            intent.setAction("battery.droid.com.droidbattery.UPDATE");
            int flags = android.os.Build.VERSION.SDK_INT >= 23 ?
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE :
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT;

            android.app.PendingIntent pi = android.app.PendingIntent.getBroadcast(context, 0, intent, flags);
            views.setOnClickPendingIntent(R.id.batteryText, pi);

            AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context, DroidWidget.class), views);
        } catch (Exception e) {}
    }

    public static void TimeSleep(Integer seg) {
        try {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
            Thread.sleep(seg);
        } catch (Exception ex) {
        }
    }

    public static void Vibrar(Context context, int valor) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        try {
            TimeSleep(100);
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(valor);
            TimeSleep(100);
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }



    public static boolean NaoPertube(Context context) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        boolean informarBateriaCarregada = true;
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            boolean naoPertubeAtivado = sp.getBoolean("quiet", false);

            if (naoPertubeAtivado) {
                String startTime = sp.getString("startTime", "23:00");
                String stopTime = sp.getString("stopTime", "09:00");

                SimpleDateFormat sdfDate = new SimpleDateFormat("H:mm");
                String currentTimeStamp = sdfDate.format(new Date());
                int currentHour = Integer.parseInt(currentTimeStamp.split("[:]+")[0]);
                int currentMinute = Integer.parseInt(currentTimeStamp.split("[:]+")[1]);

                int startHour = Integer.parseInt(startTime.split("[:]+")[0]);
                int startMinute = Integer.parseInt(startTime.split("[:]+")[1]);

                int stopHour = Integer.parseInt(stopTime.split("[:]+")[0]);
                int stopMinute = Integer.parseInt(stopTime.split("[:]+")[1]);

                if (startHour < stopHour && currentHour > startHour && currentHour < stopHour)
                    informarBateriaCarregada = false;
                else if (startHour > stopHour && (currentHour > startHour || currentHour < stopHour))
                    informarBateriaCarregada = false;
                else if (currentHour == startHour && currentMinute >= startMinute)
                    informarBateriaCarregada = false;
                else if (currentHour == stopHour && currentMinute <= stopMinute)
                    informarBateriaCarregada = false;

                return informarBateriaCarregada;
            }
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
        return informarBateriaCarregada;
    }

    public static boolean InformarBateriaCarregada(Context context) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        DroidCommon.ObtemStatusBateria(context);
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " " + DroidCommon.BateriaCarregada);
        return DroidCommon.BateriaCarregada;
        //return DroidCommon.ObtemStatusBateria(context) == BatteryManager.BATTERY_STATUS_FULL || DroidCommon.BatteryCurrent.equals(DroidCommon.ValorBateriaCarregada);

    }

    public static boolean InformarPercentualAtingidoMultiSelectPreference(Context context) {
        boolean retorno = false;
        int total = -1;
        try {
            Set<String> multiSelectPreference = DroidCommon.GetList(context, "multiSelectPreference");
            if (multiSelectPreference != null) {
                total = multiSelectPreference.size();
                for (String retval : multiSelectPreference) {
                    retorno = DroidCommon.BatteryCurrent.equals(retval);
                    if (retorno) break;
                }
            }
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " " + retorno + " " + DroidCommon.BatteryCurrent + " " + total);
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
        return retorno;
    }

    public static boolean ObtemStatusDispositivoConectado(Context context) {
        boolean retorno = false;
        try {
            retorno = DroidCommon.GetBoolean(context, "dispositivoConectado");
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " " + retorno);
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
        return retorno;
    }

    public static boolean ObtemStatusDispositivoDesconectado(Context context) {
        boolean retorno = false;
        try {
            retorno = DroidCommon.GetBoolean(context, "dispositivoDesconectado");
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " " + retorno);
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
        return retorno;
    }

    public static boolean SinteseVozNaoPerturbeAtivado(Context context) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        boolean sinteseVozAtivado = DroidCommon.PreferenceAtivarSinteseVoz(context);
        boolean naoPerturbe = DroidCommon.NaoPertube(context);
        String msg = "";
        boolean retorno = (sinteseVozAtivado && naoPerturbe);
        if (!retorno) {
            if (!sinteseVozAtivado) msg = "Sintese de Voz não ativado";
            if (!naoPerturbe) {
                if (!msg.isEmpty()) {
                    msg = msg + " e ";
                }
                msg = msg + "Não perturbe ativado";
            }
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
        return retorno;
    }

    public static int PreferenceCorTextoBateria(final Context context, String key) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        int cor = -1; // branco
        try {
            cor = Integer.parseInt(DroidCommon.GetValueList(context, key ));

        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
        return cor;
    }



    public static void AtualizaCorBateriaPorPreferenceValor(Context context) {

        boolean dispositivoConectado = DroidCommon.ObtemStatusDispositivoConectado(context);

        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " " + dispositivoConectado);
        if (dispositivoConectado) {
            //int statusBateria = DroidCommon.ObtemStatusBateria(context);
            DroidCommon.ObtemStatusBateria(context);
            //DroidCommon.BateriaCarregada = statusBateria == BatteryManager.BATTERY_STATUS_FULL || DroidCommon.BatteryCurrent.equals(DroidCommon.ValorBateriaCarregada);
            if (DroidCommon.BateriaCarregada) {
                DroidCommon.updateViewsColorBattery(context, Color.GREEN);

            } else {
                DroidCommon.updateViewsColorBattery(context, Color.BLUE);
            }
        } else {
            Integer totalBattery = Integer.parseInt(DroidCommon.BatteryCurrent);
            if (totalBattery <= 20) {
                DroidCommon.updateViewsColorBattery(context, Color.BLUE);
            } else {
                DroidCommon.updateViewsColorBattery(context, Color.WHITE);
            }
        }
        DroidCommon.updateViewsInfoBattery(context, DroidCommon.BatteryCurrent);
    }


    public static void LoopingBateria(Context context) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        Integer totalBattery = Integer.parseInt(DroidCommon.BatteryCurrent);
        for (Integer i = 0; i <= totalBattery; i++) {
            DroidCommon.TimeSleep(1);
            DroidCommon.updateViewsInfoBattery(context, i.toString());
        }
        DroidCommon.updateViewsInfoBattery(context, DroidCommon.BatteryCurrent);
    }

}