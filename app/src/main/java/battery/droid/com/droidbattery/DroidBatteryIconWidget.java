package battery.droid.com.droidbattery;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DroidBatteryIconWidget extends AppWidgetProvider {

    @Override
    public void onEnabled(Context context) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        super.onEnabled(context);
        DroidCommon.refreshBatteryWidget(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        DroidCommon.refreshBatteryWidget(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        super.onReceive(context, intent);

        try {
            if (DroidWidget.getActionBatteryUpdate().equals(intent.getAction())) {
                DroidCommon.Vibrar(context, 50);
                DroidCommon.LoopingBateria(context);
                DroidCommon.AtualizaCorBateriaPorPreferenceValor(context);
                DroidMainService.ChamaSinteseVoz(context);
            } else if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction()) ||
                    Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())) {
                DroidCommon.handlePowerConnectionChanged(context, intent.getAction());
            } else {
                DroidCommon.refreshBatteryWidget(context);
            }
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
        }
    }
}
