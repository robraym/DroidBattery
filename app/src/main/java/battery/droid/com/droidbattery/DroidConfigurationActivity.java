package battery.droid.com.droidbattery;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.graphics.drawable.ColorDrawable;
import android.widget.ListView;


/**
 * Created by Robson on 02/05/2017.
 */

public class DroidConfigurationActivity extends PreferenceActivity {
    private Context context;
    private Preference start;
    private Preference stop;
    private Preference falaBateriaCarregada;
    private Preference dispositivoConectado;
    private Preference dispositivoDesconectado;
    private MultiSelectListPreference multiSelectListPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = getBaseContext();
        super.onCreate(savedInstanceState);
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
        SetPreference();

        ListView listView = getListView();
        listView.setDivider(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        listView.setDividerHeight(0);

        DroidMainService.StartService(context);
    }


    private void SetPreference() {
        try {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));
            addPreferencesFromResource(R.xml.preferences);
            Toast.makeText(this, "Widget Droid Battery está pronto para ser adicionado!",
                    Toast.LENGTH_LONG).show();

            Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (preference.getKey().equals(start.getKey()) || preference.getKey().equals(stop.getKey())) {
                        preference.setSummary(DroidCommon.handleTime(context, newValue.toString()));
                    }
                    if (preference.getKey().equals(falaBateriaCarregada.getKey())) {
                        preference.setSummary(newValue.toString());
                    }
                    if (preference.getKey().equals(dispositivoConectado.getKey())) {
                        preference.setSummary(newValue.toString());
                    }
                    if (preference.getKey().equals(dispositivoDesconectado.getKey())) {
                        preference.setSummary(newValue.toString());
                    }
                    return true;
                }
            };

            MultiSelectListPreference.OnPreferenceChangeListener multiSelectListListener = new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    // 1. Converter o objeto recebido para um Set de Strings
                    Set<String> selecionados = (Set<String>) o;

                    if (selecionados == null || selecionados.isEmpty()) {
                        preference.setSummary("Nenhum percentual selecionado");
                    } else {
                        // 2. Converter para uma lista de Integers para ordenação numérica correta
                        List<Integer> listaNumerica = new ArrayList<>();
                        for (String s : selecionados) {
                            try {
                                listaNumerica.add(Integer.parseInt(s));
                            } catch (NumberFormatException e) {
                                Log.e(DroidCommon.TAG, "Erro ao converter valor: " + s);
                            }
                        }

                        // 3. Ordenar numericamente (5, 10, 15...)
                        Collections.sort(listaNumerica);

                        // 4. Salvar os valores (mantendo o padrão do seu app)
                        List<String> valoresParaSalvar = new ArrayList<>();
                        for (Integer i : listaNumerica) {
                            valoresParaSalvar.add(String.valueOf(i));
                        }
                        DroidCommon.SetList(context, "multiSelectPreference", valoresParaSalvar);

                        // 5. Criar o resumo visual sem colchetes e com o símbolo %
                        StringBuilder resumo = new StringBuilder();
                        for (int i = 0; i < listaNumerica.size(); i++) {
                            resumo.append(listaNumerica.get(i)).append("%");
                            if (i < listaNumerica.size() - 1) {
                                resumo.append(", ");
                            }
                        }
                        preference.setSummary(resumo.toString());
                    }
                    return true;
                }
            };

            ListPreference.OnPreferenceChangeListener listListener = new ListPreference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    String stringValue = o.toString();
                    if (preference.getKey().equals("valorTextoBateriaCarregada")) {
                        DroidCommon.ValorBateriaCarregada = stringValue;
                        preference.setSummary(stringValue);
                    } else  if (preference instanceof ListPreference) {
                        ListPreference listPreference = (ListPreference) preference;
                        int prefIndex = listPreference.findIndexOfValue(stringValue);
                        CharSequence[] labels = listPreference.getEntries();
                        preference.setSummary(labels[prefIndex]);
                        DroidCommon.AtualizaCorBateriaPorPreferenceValor(context);
                        DroidCommon.updateViewsInfoBattery(context,DroidCommon.BatteryCurrent);
                        DroidCommon.LoopingBateria(context);
                    }
                    return true;
                }
            };

            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            start = (Preference) findPreference("startTime");
            start.setSummary(DroidCommon.handleTime(context, mPrefs.getString("startTime", "23:00")));
            start.setOnPreferenceChangeListener(listener);

            stop = (Preference) findPreference("stopTime");
            stop.setSummary(DroidCommon.handleTime(context, mPrefs.getString("stopTime", "09:00")));
            stop.setOnPreferenceChangeListener(listener);

            falaBateriaCarregada = (Preference) findPreference("falaBateriaCarregada");
            falaBateriaCarregada.setSummary(DroidCommon.PreferenceFalaBateriaCarregada(context));
            falaBateriaCarregada.setOnPreferenceChangeListener(listener);

            dispositivoConectado = (Preference) findPreference("dispositivoConectado");
            dispositivoConectado.setSummary(DroidCommon.PreferenceDispositivoConectado(context));
            dispositivoConectado.setOnPreferenceChangeListener(listener);

            dispositivoDesconectado = (Preference) findPreference("dispositivoDesconectado");
            dispositivoDesconectado.setSummary(DroidCommon.PreferenceDispositivoDesconectado(context));
            dispositivoDesconectado.setOnPreferenceChangeListener(listener);

            multiSelectListPreference = (MultiSelectListPreference) findPreference("multiSelectListPreference");
            multiSelectListPreference.setEntries(R.array.arrayPercentualAtingido);
            multiSelectListPreference.setEntryValues(R.array.arrayPercentualAtingidoValues);
            multiSelectListPreference.setDefaultValue(R.array.arrayPercentualAtingidoDefaultValues);
            multiSelectListPreference.setOnPreferenceChangeListener(multiSelectListListener);
            multiSelectListListener.onPreferenceChange(multiSelectListPreference, multiSelectListPreference.getValues());
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()) + " Erro: " + ex.getMessage());

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            MenuInflater inflater = getMenuInflater();

        } catch (Exception ex) {
        }
        return true;
    }
}
