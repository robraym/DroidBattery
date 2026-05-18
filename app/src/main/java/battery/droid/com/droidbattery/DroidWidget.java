package battery.droid.com.droidbattery;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import static android.app.PendingIntent.FLAG_IMMUTABLE;

/**
 * Created by Robson on 02/05/2017.
 */

public class DroidWidget extends AppWidgetProvider {
    private static final String ACTION_BATTERY_UPDATE = "battery.droid.com.droidbattery.UPDATE";
    private static final String ACTION_WIDGET_REFRESH = "battery.droid.com.droidbattery.WIDGET_REFRESH";
    private static final long WIDGET_REFRESH_INTERVAL_DISCONNECTED = 15 * 60 * 1000L;
    private static final long WIDGET_REFRESH_INTERVAL_CHARGING = 30 * 1000L;

    public static String getActionBatteryUpdate() {
        return ACTION_BATTERY_UPDATE;
    }


    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        super.onRestored(context, oldWidgetIds, newWidgetIds);

    }

    @Override
    public void onEnabled(Context context) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        super.onEnabled(context);
        scheduleNextWidgetRefresh(context);
        DroidCommon.refreshBatteryWidget(context);

    }

    @Override
    public void onDisabled(Context context) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        super.onDisabled(context);
        cancelWidgetRefresh(context);

    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

        try {
            DroidCommon.SetInteger(context, "MIN_WIDTH", newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH));
            scheduleNextWidgetRefresh(context);
            DroidCommon.refreshBatteryWidget(context);
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        scheduleNextWidgetRefresh(context);
        ListenerOnClick(context, appWidgetManager);
    }

    private void ListenerOnClick(Context context, AppWidgetManager appWidgetManager) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        try {
            DroidCommon.refreshBatteryWidget(context);

        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }

    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, FLAG_IMMUTABLE);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        super.onReceive(context, intent);

        try {
            if (ACTION_BATTERY_UPDATE.equals(intent.getAction())) {
                DroidCommon.Vibrar(context, 50);
                DroidCommon.LoopingBateria(context);
                DroidCommon.AtualizaCorBateriaPorPreferenceValor(context);
                DroidMainService.ChamaSinteseVoz(context);
            } else if (ACTION_WIDGET_REFRESH.equals(intent.getAction()) ||
                    Intent.ACTION_POWER_CONNECTED.equals(intent.getAction()) ||
                    Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())) {
                boolean powerEvent = Intent.ACTION_POWER_CONNECTED.equals(intent.getAction()) ||
                        Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction());
                if (powerEvent) {
                    DroidCommon.handlePowerConnectionChanged(context, intent.getAction());
                }
                DroidCommon.refreshBatteryWidget(context);
                DroidCommon.AtualizaCorBateriaPorPreferenceValor(context);
                scheduleNextWidgetRefresh(context);
            }
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }

    private static PendingIntent getWidgetRefreshPendingIntent(Context context) {
        Intent intent = new Intent(context, DroidWidget.class);
        intent.setAction(ACTION_WIDGET_REFRESH);
        return PendingIntent.getBroadcast(
                context,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
    }

    public static void scheduleNextWidgetRefresh(Context context) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                return;
            }

            DroidCommon.refreshBatteryStateFromSystem(context);
            boolean charging = DroidCommon.ObtemStatusDispositivoConectado(context);
            long interval = charging ? WIDGET_REFRESH_INTERVAL_CHARGING : WIDGET_REFRESH_INTERVAL_DISCONNECTED;
            PendingIntent pendingIntent = getWidgetRefreshPendingIntent(context);
            long nextRefresh = SystemClock.elapsedRealtime() + interval;
            alarmManager.cancel(pendingIntent);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        nextRefresh,
                        pendingIntent);
            } else {
                alarmManager.set(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        nextRefresh,
                        pendingIntent);
            }
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }

    private static void cancelWidgetRefresh(Context context) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(getWidgetRefreshPendingIntent(context));
            }
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }
}
