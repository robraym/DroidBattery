package battery.droid.com.droidbattery;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import static android.app.PendingIntent.FLAG_IMMUTABLE;

/**
 * Created by Robson on 02/05/2017.
 */

public class DroidWidget extends AppWidgetProvider {
    private static final String ACTION_BATTERY_UPDATE = "battery.droid.com.droidbattery.UPDATE";

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

    }

    @Override
    public void onDisabled(Context context) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        super.onDisabled(context);

    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

        try {
            DroidCommon.SetInteger(context, "MIN_WIDTH", newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH));
            DroidCommon.refreshBatteryWidget(context);
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        super.onUpdate(context, appWidgetManager, appWidgetIds);
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
            }
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }
}
