package battery.droid.com.droidbattery;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Robson on 02/05/2017.
 */

public class DroidConfigurationActivity extends Activity {
    private static final int COLOR_BACKGROUND = Color.rgb(0, 0, 0);
    private static final int COLOR_GROUP = Color.rgb(28, 29, 33);
    private static final int COLOR_GROUP_PRESSED = Color.rgb(34, 38, 50);
    private static final int COLOR_PRIMARY_TEXT = Color.rgb(248, 248, 250);
    private static final int COLOR_SECONDARY_TEXT = Color.rgb(180, 182, 190);
    private static final int COLOR_DIVIDER = Color.rgb(52, 53, 58);
    private static final int COLOR_BLUE = Color.rgb(66, 133, 244);
    private static final int COLOR_BLUE_TRACK = Color.rgb(25, 78, 163);
    private static final int COLOR_DIALOG = Color.rgb(31, 32, 36);
    private static final int COLOR_DIALOG_PICKER = Color.rgb(42, 43, 48);
    private static final int COLOR_ICON_VOICE = Color.rgb(90, 88, 235);
    private static final int COLOR_ICON_BATTERY = Color.rgb(47, 125, 255);
    private static final int COLOR_ICON_CHARGED = Color.rgb(28, 177, 116);
    private static final int COLOR_ICON_POWER = Color.rgb(232, 92, 8);
    private static final int COLOR_ICON_RESTORE = Color.rgb(126, 99, 245);
    private static final int COLOR_ICON_QUIET = Color.rgb(232, 92, 8);
    private static final int COLOR_ICON_TIME = Color.rgb(93, 91, 235);

    private Context context;
    private SharedPreferences preferences;
    private LinearLayout content;
    private TextView percentSummary;
    private TextView falaBateriaCarregadaSummary;
    private TextView dispositivoConectadoSummary;
    private TextView dispositivoDesconectadoSummary;
    private TextView startSummary;
    private TextView stopSummary;
    private Switch speechSwitch;
    private Switch quietSwitch;
    private LinearLayout quietGroup;
    private View quietDividerStart;
    private View quietDividerStop;
    private View startRow;
    private View stopRow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = getBaseContext();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState);
        Log.d(DroidCommon.TAG, DroidCommon.getLogTagWithMethod(new Throwable()));

        buildSettingsScreen();
        DroidMainService.StartService(context);
    }

    private void buildSettingsScreen() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(COLOR_BACKGROUND);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(34), dp(18), dp(30));
        scrollView.addView(content, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT));

        TextView title = new TextView(this);
        title.setText("Config.");
        title.setTextColor(COLOR_PRIMARY_TEXT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.START);
        title.setPadding(dp(34), 0, 0, dp(30));
        setTextSize(title, 33);
        content.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        buildVoiceGroup();
        buildPercentGroup();
        buildMessagesGroup();
        buildQuietGroup();

        setContentView(scrollView);
        refreshSummaries();
        refreshQuietDependency();
    }

    private void buildVoiceGroup() {
        LinearLayout group = createGroup();
        speechSwitch = createSwitch();
        speechSwitch.setChecked(preferences.getBoolean("spf_ativarSinteseVoz", true));
        speechSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.edit().putBoolean("spf_ativarSinteseVoz", isChecked).apply();
                refreshQuietDependency();
            }
        });
        group.addView(createSwitchRow(
                getString(R.string.spf_ativar_sintese_voz),
                null,
                R.drawable.ic_oneui_voice,
                COLOR_ICON_VOICE,
                speechSwitch,
                true));
        content.addView(group);
    }

    private void buildPercentGroup() {
        LinearLayout group = createGroup();
        View row = createNavigationRow(
                getString(R.string.titulo_pecentual_atingido),
                "",
                R.drawable.ic_oneui_battery,
                COLOR_ICON_BATTERY,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPercentDialog();
                    }
                });
        percentSummary = (TextView) row.findViewWithTag("summary");
        group.addView(row);
        content.addView(group);
    }

    private void buildMessagesGroup() {
        LinearLayout group = createGroup();

        View falaRow = createNavigationRow(
                getString(R.string.titulo_bateria_carregada),
                "",
                R.drawable.ic_oneui_battery,
                COLOR_ICON_CHARGED,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditTextDialog("falaBateriaCarregada",
                                getString(R.string.titulo_bateria_carregada),
                                getString(R.string.txt_fala_bateria_carregada));
                    }
                });
        falaBateriaCarregadaSummary = (TextView) falaRow.findViewWithTag("summary");
        group.addView(falaRow);
        group.addView(createDivider());

        View conectadoRow = createNavigationRow(
                getString(R.string.titulo_dispositivo_conectado),
                "",
                R.drawable.ic_oneui_power,
                COLOR_ICON_POWER,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditTextDialog("dispositivoConectado",
                                getString(R.string.titulo_dispositivo_conectado),
                                getString(R.string.txt_dispositivo_conectado));
                    }
                });
        dispositivoConectadoSummary = (TextView) conectadoRow.findViewWithTag("summary");
        group.addView(conectadoRow);
        group.addView(createDivider());

        View desconectadoRow = createNavigationRow(
                getString(R.string.titulo_dispositivo_desconectado),
                "",
                R.drawable.ic_oneui_power,
                COLOR_ICON_POWER,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditTextDialog("dispositivoDesconectado",
                                getString(R.string.titulo_dispositivo_desconectado),
                                getString(R.string.txt_dispositivo_desconectado));
                    }
                });
        dispositivoDesconectadoSummary = (TextView) desconectadoRow.findViewWithTag("summary");
        group.addView(desconectadoRow);
        group.addView(createDivider());

        group.addView(createActionRow(
                getString(R.string.titulo_restaurar_mensagens_padrao),
                getString(R.string.resumo_restaurar_mensagens_padrao),
                R.drawable.ic_oneui_restore,
                COLOR_ICON_RESTORE,
                getString(R.string.acao_restaurar),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showRestoreMessagesDialog();
                    }
                }));
        content.addView(group);
    }

    private void buildQuietGroup() {
        quietGroup = createGroup();

        quietSwitch = createSwitch();
        quietSwitch.setChecked(preferences.getBoolean("quiet", false));
        quietSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.edit().putBoolean("quiet", isChecked).apply();
                refreshQuietDependency();
            }
        });
        quietGroup.addView(createSwitchRow(
                getString(R.string.quietSum),
                null,
                R.drawable.ic_oneui_moon,
                COLOR_ICON_QUIET,
                quietSwitch,
                true));

        quietDividerStart = createDivider();
        quietGroup.addView(quietDividerStart);

        startRow = createNavigationRow(
                getString(R.string.startTime),
                "",
                R.drawable.ic_oneui_clock,
                COLOR_ICON_TIME,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showTimeDialog("startTime", getString(R.string.startTime), "23:00");
                    }
                });
        startSummary = (TextView) startRow.findViewWithTag("summary");
        quietGroup.addView(startRow);

        quietDividerStop = createDivider();
        quietGroup.addView(quietDividerStop);

        stopRow = createNavigationRow(
                getString(R.string.stopTime),
                "",
                R.drawable.ic_oneui_clock,
                COLOR_ICON_TIME,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showTimeDialog("stopTime", getString(R.string.stopTime), "09:00");
                    }
                });
        stopSummary = (TextView) stopRow.findViewWithTag("summary");
        quietGroup.addView(stopRow);

        content.addView(quietGroup);
    }

    private TextView createSection(String title) {
        TextView section = new TextView(this);
        section.setText(title);
        section.setTextColor(COLOR_BLUE);
        section.setTypeface(Typeface.DEFAULT);
        section.setAllCaps(false);
        setTextSize(section, 15);
        section.setPadding(dp(4), dp(24), 0, dp(10));
        return section;
    }

    private LinearLayout createGroup() {
        LinearLayout group = new LinearLayout(this);
        group.setOrientation(LinearLayout.VERTICAL);
        group.setBackground(createRoundedBackground(COLOR_GROUP, 32));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(14), 0, 0);
        group.setLayoutParams(params);
        return group;
    }

    private View createSwitchRow(String title, String summary, int iconResId, int iconColor, final Switch rowSwitch, boolean enabled) {
        LinearLayout row = createBaseRow(enabled);
        addRowIcon(row, iconResId, iconColor);
        LinearLayout texts = createTexts(title, summary);
        row.addView(texts, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        row.addView(rowSwitch);
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rowSwitch.isEnabled()) {
                    rowSwitch.setChecked(!rowSwitch.isChecked());
                }
            }
        });
        return row;
    }

    private View createNavigationRow(String title, String summary, int iconResId, int iconColor, View.OnClickListener listener) {
        LinearLayout row = createBaseRow(true);
        addRowIcon(row, iconResId, iconColor);
        LinearLayout texts = createTextsWithValue(title, summary);
        row.addView(texts, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView arrow = new TextView(this);
        arrow.setText(">");
        arrow.setTextColor(Color.rgb(176, 179, 188));
        arrow.setGravity(Gravity.CENTER);
        setTextSize(arrow, 22);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(24), LinearLayout.LayoutParams.WRAP_CONTENT));

        row.setOnClickListener(listener);
        return row;
    }

    private View createActionRow(String title, String summary, int iconResId, int iconColor, String buttonText,
                                 View.OnClickListener listener) {
        LinearLayout row = createBaseRow(true);
        addRowIcon(row, iconResId, iconColor);
        LinearLayout texts = createTextsWithValue(title, summary);
        row.addView(texts, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView actionButton = createInlineActionButton(buttonText);
        actionButton.setOnClickListener(listener);
        row.addView(actionButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        return row;
    }

    private TextView createInlineActionButton(String text) {
        TextView button = new TextView(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setGravity(Gravity.CENTER);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setAllCaps(false);
        setTextSize(button, 14);
        button.setPadding(dp(14), dp(8), dp(14), dp(8));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            button.setBackground(createRoundedBackground(COLOR_BLUE, 18));
        }
        return button;
    }

    private LinearLayout createTextsWithValue(String title, String summary) {
        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setPadding(0, 0, dp(14), 0);

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(COLOR_PRIMARY_TEXT);
        titleView.setTypeface(Typeface.DEFAULT);
        titleView.setSingleLine(false);
        titleView.setLineSpacing(dp(1), 1.0f);
        setTextSize(titleView, 18);
        texts.addView(titleView);

        TextView summaryView = new TextView(this);
        summaryView.setTag("summary");
        summaryView.setText(summary);
        summaryView.setTextColor(COLOR_SECONDARY_TEXT);
        summaryView.setSingleLine(false);
        summaryView.setMaxLines(2);
        summaryView.setEllipsize(TextUtils.TruncateAt.END);
        summaryView.setPadding(0, dp(3), 0, 0);
        summaryView.setLineSpacing(dp(1), 1.0f);
        setTextSize(summaryView, 15);
        texts.addView(summaryView);

        return texts;
    }

    private void addRowIcon(LinearLayout row, int iconResId, int iconColor) {
        ImageView icon = new ImageView(this);
        icon.setImageResource(iconResId);
        icon.setColorFilter(Color.WHITE);
        icon.setPadding(dp(9), dp(9), dp(9), dp(9));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            icon.setBackground(createCircleBackground(iconColor));
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(34), dp(34));
        params.setMargins(0, 0, dp(22), 0);
        row.addView(icon, params);
    }

    private LinearLayout createBaseRow(boolean enabled) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setEnabled(enabled);
        row.setMinimumHeight(dp(74));
        row.setPadding(dp(12), dp(13), dp(18), dp(13));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            row.setBackground(createPressedBackground());
        }
        return row;
    }

    private LinearLayout createTexts(String title, String summary) {
        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setPadding(0, 0, dp(14), 0);

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(COLOR_PRIMARY_TEXT);
        titleView.setTypeface(Typeface.DEFAULT);
        titleView.setSingleLine(false);
        titleView.setLineSpacing(dp(1), 1.0f);
        setTextSize(titleView, 18);
        texts.addView(titleView);

        if (!TextUtils.isEmpty(summary)) {
            TextView summaryView = new TextView(this);
            summaryView.setText(summary);
            summaryView.setTextColor(COLOR_SECONDARY_TEXT);
            summaryView.setSingleLine(false);
            summaryView.setPadding(0, dp(4), 0, 0);
            summaryView.setLineSpacing(dp(1), 1.0f);
            setTextSize(summaryView, 15);
            texts.addView(summaryView);
        }

        return texts;
    }

    private View createDivider() {
        View divider = new View(this);
        divider.setBackgroundColor(COLOR_DIVIDER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                Math.max(1, dp(1)));
        params.setMargins(dp(18), 0, dp(18), 0);
        divider.setLayoutParams(params);
        return divider;
    }

    private Switch createSwitch() {
        Switch itemSwitch = new Switch(this);
        styleSwitch(itemSwitch);
        return itemSwitch;
    }

    private void styleSwitch(Switch itemSwitch) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_enabled},
                new int[]{}
        };

        itemSwitch.setThumbTintList(new ColorStateList(states, new int[]{
                COLOR_BLUE,
                Color.rgb(120, 123, 132),
                Color.rgb(232, 234, 237)
        }));
        itemSwitch.setTrackTintList(new ColorStateList(states, new int[]{
                COLOR_BLUE_TRACK,
                Color.rgb(50, 52, 58),
                Color.rgb(88, 91, 99)
        }));
    }

    private void refreshSummaries() {
        if (percentSummary != null) {
            percentSummary.setText(getPercentSummary());
        }
        if (falaBateriaCarregadaSummary != null) {
            falaBateriaCarregadaSummary.setText(DroidCommon.PreferenceFalaBateriaCarregada(context));
        }
        if (dispositivoConectadoSummary != null) {
            dispositivoConectadoSummary.setText(DroidCommon.PreferenceDispositivoConectado(context));
        }
        if (dispositivoDesconectadoSummary != null) {
            dispositivoDesconectadoSummary.setText(DroidCommon.PreferenceDispositivoDesconectado(context));
        }
        if (startSummary != null) {
            startSummary.setText(DroidCommon.handleTime(context, preferences.getString("startTime", "23:00")));
        }
        if (stopSummary != null) {
            stopSummary.setText(DroidCommon.handleTime(context, preferences.getString("stopTime", "09:00")));
        }
    }

    private void refreshQuietDependency() {
        boolean speechEnabled = speechSwitch == null || speechSwitch.isChecked();
        if (quietSwitch != null) {
            quietSwitch.setEnabled(speechEnabled);
            quietSwitch.setAlpha(speechEnabled ? 1f : 0.45f);
        }

        boolean timeEnabled = speechEnabled && quietSwitch != null && quietSwitch.isChecked();
        setRowEnabled(startRow, timeEnabled);
        setRowEnabled(stopRow, timeEnabled);
        if (quietDividerStart != null) quietDividerStart.setVisibility(timeEnabled ? View.VISIBLE : View.GONE);
        if (quietDividerStop != null) quietDividerStop.setVisibility(timeEnabled ? View.VISIBLE : View.GONE);
    }

    private void setRowEnabled(View row, boolean enabled) {
        if (row != null) {
            row.setEnabled(enabled);
            row.setAlpha(enabled ? 1f : 0.45f);
        }
    }

    private String getPercentSummary() {
        Set<String> selected = new HashSet<>();
        try {
            selected.addAll(DroidCommon.GetList(context, "multiSelectPreference"));
        } catch (Exception ex) {
            selected.add("80");
        }

        if (selected.isEmpty()) {
            selected.add("80");
        }

        List<Integer> values = new ArrayList<>();
        for (String item : selected) {
            try {
                values.add(Integer.parseInt(item));
            } catch (NumberFormatException ex) {
                Log.e(DroidCommon.TAG, "Erro ao converter valor: " + item);
            }
        }
        Collections.sort(values);

        if (values.isEmpty()) {
            return "Nenhum percentual selecionado";
        }

        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            summary.append(values.get(i)).append("%");
            if (i < values.size() - 1) {
                summary.append(", ");
            }
        }
        return summary.toString();
    }

    private void showPercentDialog() {
        final String[] entries = getResources().getStringArray(R.array.arrayPercentualAtingido);
        final String[] values = getResources().getStringArray(R.array.arrayPercentualAtingidoValues);
        final Set<String> selected = new HashSet<>();
        try {
            selected.addAll(DroidCommon.GetList(context, "multiSelectPreference"));
        } catch (Exception ex) {
            selected.add("80");
        }
        if (selected.isEmpty()) {
            selected.add("80");
        }

        boolean[] checked = new boolean[values.length];
        for (int i = 0; i < values.length; i++) {
            checked[i] = selected.contains(values[i]);
        }

        final Dialog dialog = new Dialog(this);
        LinearLayout container = createDialogContainer();

        TextView titleView = createDialogTitle(getString(R.string.titulo_pecentual_atingido));
        container.addView(titleView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView hintView = createDialogMessage("Escolha os níveis que devem gerar alerta.");
        hintView.setPadding(0, dp(4), 0, dp(14));
        container.addView(hintView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        ScrollView listScroll = new ScrollView(this);
        listScroll.setFillViewport(false);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        listScroll.addView(list, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT));

        for (int i = 0; i < values.length; i++) {
            final String value = values[i];
            CheckBox checkBox = createPercentCheckBox(entries[i], checked[i]);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        selected.add(value);
                    } else {
                        selected.remove(value);
                    }
                }
            });
            list.addView(checkBox, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(48)));
        }

        container.addView(listScroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(390)));

        LinearLayout actions = createDialogActions();
        TextView cancel = createDialogButton(getString(R.string.cancel));
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        actions.addView(cancel);

        TextView ok = createDialogButton(getString(R.string.set));
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePercentValues(selected);
                dialog.dismiss();
            }
        });
        actions.addView(ok);
        container.addView(actions, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        showStyledDialog(dialog, container);
    }

    private void savePercentValues(Set<String> selected) {
        List<Integer> numericValues = new ArrayList<>();
        for (String item : selected) {
            try {
                numericValues.add(Integer.parseInt(item));
            } catch (NumberFormatException ex) {
                Log.e(DroidCommon.TAG, "Erro ao converter valor: " + item);
            }
        }
        Collections.sort(numericValues);

        List<String> valuesToSave = new ArrayList<>();
        for (Integer value : numericValues) {
            valuesToSave.add(String.valueOf(value));
        }
        DroidCommon.SetList(context, "multiSelectPreference", valuesToSave);
        refreshSummaries();
    }

    private void showEditTextDialog(final String key, String title, String defaultValue) {
        final EditText editText = new EditText(this);
        editText.setSingleLine(false);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setText(preferences.getString(key, defaultValue));
        editText.setSelection(editText.getText().length());
        editText.setPadding(dp(16), dp(12), dp(16), dp(12));
        editText.setMinLines(3);
        editText.setTextColor(COLOR_PRIMARY_TEXT);
        editText.setHintTextColor(COLOR_SECONDARY_TEXT);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp(17));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            editText.setBackground(createRoundedBackground(COLOR_DIALOG_PICKER, 18));
        } else {
            editText.setBackgroundColor(COLOR_DIALOG_PICKER);
        }

        final Dialog dialog = new Dialog(this);
        LinearLayout container = createDialogContainer();
        container.addView(createDialogTitle(title), new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        TextView hintView = createDialogMessage("Edite a mensagem que será falada pelo app.");
        hintView.setPadding(0, dp(4), 0, dp(14));
        container.addView(hintView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        container.addView(editText, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout actions = createDialogActions();
        TextView cancel = createDialogButton(getString(R.string.cancel));
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        actions.addView(cancel);

        TextView ok = createDialogButton(getString(R.string.set));
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences.edit().putString(key, editText.getText().toString()).apply();
                refreshSummaries();
                dialog.dismiss();
            }
        });
        actions.addView(ok);
        container.addView(actions, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        showStyledDialog(dialog, container);
    }

    private void showTimeDialog(final String key, String title, String defaultValue) {
        String time = preferences.getString(key, defaultValue);
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        final Dialog dialog = new Dialog(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(24), dp(22), dp(24), dp(16));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            container.setBackground(createRoundedBackground(COLOR_DIALOG, 28));
        } else {
            container.setBackgroundColor(COLOR_DIALOG);
        }

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(COLOR_PRIMARY_TEXT);
        titleView.setTypeface(Typeface.DEFAULT);
        setTextSize(titleView, 22);
        container.addView(titleView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView hintView = new TextView(this);
        hintView.setText("Defina o horário");
        hintView.setTextColor(COLOR_SECONDARY_TEXT);
        setTextSize(hintView, 15);
        hintView.setPadding(0, dp(3), 0, dp(18));
        container.addView(hintView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout pickerPanel = new LinearLayout(this);
        pickerPanel.setGravity(Gravity.CENTER);
        pickerPanel.setOrientation(LinearLayout.HORIZONTAL);
        pickerPanel.setPadding(dp(8), dp(14), dp(8), dp(14));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            pickerPanel.setBackground(createRoundedBackground(COLOR_DIALOG_PICKER, 22));
        } else {
            pickerPanel.setBackgroundColor(COLOR_DIALOG_PICKER);
        }

        final NumberPicker hourPicker = createTimeNumberPicker(0, 23, hour);
        final NumberPicker minutePicker = createTimeNumberPicker(0, 59, minute);

        pickerPanel.addView(hourPicker, new LinearLayout.LayoutParams(dp(92), dp(168)));

        TextView separator = new TextView(this);
        separator.setText(":");
        separator.setTextColor(COLOR_PRIMARY_TEXT);
        separator.setGravity(Gravity.CENTER);
        separator.setTypeface(Typeface.DEFAULT_BOLD);
        setTextSize(separator, 34);
        pickerPanel.addView(separator, new LinearLayout.LayoutParams(dp(28), LinearLayout.LayoutParams.MATCH_PARENT));

        pickerPanel.addView(minutePicker, new LinearLayout.LayoutParams(dp(92), dp(168)));

        container.addView(pickerPanel, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        actions.setPadding(0, dp(18), 0, 0);

        TextView cancel = createDialogButton(getString(R.string.cancel));
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        actions.addView(cancel);

        TextView ok = createDialogButton(getString(R.string.set));
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences.edit()
                        .putString(key, hourPicker.getValue() + ":" + minutePicker.getValue())
                        .apply();
                refreshSummaries();
                dialog.dismiss();
            }
        });
        actions.addView(ok);

        container.addView(actions, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        dialog.setContentView(container);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
        Window shownWindow = dialog.getWindow();
        if (shownWindow != null) {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(shownWindow.getAttributes());
            params.width = getResources().getDisplayMetrics().widthPixels - dp(56);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.dimAmount = 0.62f;
            shownWindow.setAttributes(params);
            shownWindow.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            shownWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private NumberPicker createTimeNumberPicker(int min, int max, int value) {
        NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(min);
        picker.setMaxValue(max);
        picker.setValue(value);
        picker.setWrapSelectorWheel(true);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        picker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return value < 10 ? "0" + value : String.valueOf(value);
            }
        });
        styleNumberPicker(picker);
        return picker;
    }

    private TextView createDialogButton(String text) {
        TextView button = new TextView(this);
        button.setText(text);
        button.setTextColor(COLOR_BLUE);
        button.setGravity(Gravity.CENTER);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setAllCaps(true);
        setTextSize(button, 15);
        button.setPadding(dp(18), dp(10), dp(4), dp(10));
        return button;
    }

    private LinearLayout createDialogContainer() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(24), dp(22), dp(24), dp(16));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            container.setBackground(createRoundedBackground(COLOR_DIALOG, 28));
        } else {
            container.setBackgroundColor(COLOR_DIALOG);
        }
        return container;
    }

    private TextView createDialogTitle(String title) {
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(COLOR_PRIMARY_TEXT);
        titleView.setTypeface(Typeface.DEFAULT);
        titleView.setLineSpacing(dp(1), 1.0f);
        setTextSize(titleView, 22);
        return titleView;
    }

    private TextView createDialogMessage(String message) {
        TextView messageView = new TextView(this);
        messageView.setText(message);
        messageView.setTextColor(COLOR_SECONDARY_TEXT);
        messageView.setLineSpacing(dp(1), 1.0f);
        setTextSize(messageView, 15);
        return messageView;
    }

    private LinearLayout createDialogActions() {
        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        actions.setPadding(0, dp(18), 0, 0);
        return actions;
    }

    private CheckBox createPercentCheckBox(String text, boolean checked) {
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(text);
        checkBox.setChecked(checked);
        checkBox.setTextColor(COLOR_PRIMARY_TEXT);
        checkBox.setTypeface(Typeface.DEFAULT);
        checkBox.setGravity(Gravity.CENTER_VERTICAL);
        checkBox.setPadding(0, 0, 0, 0);
        checkBox.setButtonTintList(new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        COLOR_BLUE,
                        Color.rgb(178, 180, 188)
                }));
        setTextSize(checkBox, 18);
        return checkBox;
    }

    private void showStyledDialog(Dialog dialog, View contentView) {
        dialog.setContentView(contentView);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
        Window shownWindow = dialog.getWindow();
        if (shownWindow != null) {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(shownWindow.getAttributes());
            params.width = getResources().getDisplayMetrics().widthPixels - dp(56);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.dimAmount = 0.62f;
            shownWindow.setAttributes(params);
            shownWindow.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            shownWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void styleNumberPicker(NumberPicker picker) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            picker.setTextColor(COLOR_PRIMARY_TEXT);
        }

        try {
            Field selectorWheelPaintField = NumberPicker.class.getDeclaredField("mSelectorWheelPaint");
            selectorWheelPaintField.setAccessible(true);
            Paint paint = (Paint) selectorWheelPaintField.get(picker);
            paint.setColor(COLOR_PRIMARY_TEXT);
            paint.setTextSize(dp(28));
        } catch (Exception ex) {
            Log.d(DroidCommon.TAG, "Nao foi possivel estilizar NumberPicker: " + ex.getMessage());
        }

        setNumberPickerTextColor(picker);
    }

    private void setNumberPickerTextColor(View view) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setTextColor(COLOR_PRIMARY_TEXT);
            textView.setTypeface(Typeface.DEFAULT);
            setTextSize(textView, 28);
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                setNumberPickerTextColor(group.getChildAt(i));
            }
        }
    }

    private void restoreDefaultMessages() {
        preferences.edit()
                .putString("falaBateriaCarregada", getString(R.string.txt_fala_bateria_carregada))
                .putString("dispositivoConectado", getString(R.string.txt_dispositivo_conectado))
                .putString("dispositivoDesconectado", getString(R.string.txt_dispositivo_desconectado))
                .apply();
        refreshSummaries();
        Toast.makeText(this, R.string.mensagens_restauradas_padrao, Toast.LENGTH_SHORT).show();
    }

    private void showRestoreMessagesDialog() {
        final Dialog dialog = new Dialog(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(24), dp(22), dp(24), dp(16));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            container.setBackground(createRoundedBackground(COLOR_DIALOG, 28));
        } else {
            container.setBackgroundColor(COLOR_DIALOG);
        }

        TextView titleView = new TextView(this);
        titleView.setText(R.string.confirmar_restaurar_mensagens);
        titleView.setTextColor(COLOR_PRIMARY_TEXT);
        titleView.setTypeface(Typeface.DEFAULT);
        setTextSize(titleView, 21);
        container.addView(titleView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView messageView = new TextView(this);
        messageView.setText(R.string.descricao_confirmar_restaurar_mensagens);
        messageView.setTextColor(COLOR_SECONDARY_TEXT);
        messageView.setPadding(0, dp(8), 0, dp(18));
        messageView.setLineSpacing(dp(1), 1.0f);
        setTextSize(messageView, 15);
        container.addView(messageView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);

        TextView cancel = createDialogButton(getString(R.string.cancel));
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        actions.addView(cancel);

        TextView restore = createDialogButton(getString(R.string.acao_restaurar));
        restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restoreDefaultMessages();
                dialog.dismiss();
            }
        });
        actions.addView(restore);

        container.addView(actions, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        dialog.setContentView(container);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
        Window shownWindow = dialog.getWindow();
        if (shownWindow != null) {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(shownWindow.getAttributes());
            params.width = getResources().getDisplayMetrics().widthPixels - dp(56);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.dimAmount = 0.62f;
            shownWindow.setAttributes(params);
            shownWindow.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            shownWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private GradientDrawable createRoundedBackground(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private GradientDrawable createCircleBackground(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        return drawable;
    }

    private android.graphics.drawable.StateListDrawable createPressedBackground() {
        android.graphics.drawable.StateListDrawable drawable = new android.graphics.drawable.StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed}, createRoundedBackground(COLOR_GROUP_PRESSED, 32));
        drawable.addState(new int[]{}, createRoundedBackground(COLOR_GROUP, 32));
        return drawable;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void setTextSize(TextView textView, int dpSize) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp(dpSize));
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
