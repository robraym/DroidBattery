package battery.droid.com.droidbattery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Robson on 03/05/2017.
 */

public class DroidSetStatusBatteryReceiver extends BroadcastReceiver {

    private boolean dispositivoConectado;
    private boolean dispositivoDesconectado;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
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
            if (dispositivoConectado || dispositivoDesconectado) {
                try {
                    DroidCommon.InformaDispositivoConectadoDesconectado = true;
                    DroidCommon.updateViewsSizeBattery(context);
                    DroidCommon.onUpdateDroidWidget(context);
                    DroidCommon.AtualizaCorBateriaPorPreferenceValor(context);
                    DroidCommon.LoopingBateria(context);
                    DroidMainService.ChamaSinteseVoz(context);
                } catch (Exception ex) {
                    Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());
                } finally {
                    DroidCommon.InformaDispositivoConectadoDesconectado = false;
                }
            }
        }
    }
}
