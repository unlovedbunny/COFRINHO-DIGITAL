package com.example.cofrinhodigital;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SavingsManager {

    private static final String PREFS_NAME        = "cofrinho_prefs";
    private static final String KEY_GOAL_NAME     = "goal_name";
    private static final String KEY_GOAL_VALUE    = "goal_value";
    private static final String KEY_DEADLINE_DAYS = "deadline_days";
    private static final String KEY_MODALITY      = "modality";
    private static final String KEY_CURRENT_AMOUNT = "current_amount";
    private static final String KEY_HISTORY       = "history";
    private static final String KEY_REMINDER_ENABLED = "reminder_enabled";
    private static final String KEY_REMINDER_HOUR    = "reminder_hour";
    private static final String KEY_REMINDER_MINUTE  = "reminder_minute";
    private static final String KEY_START_DATE    = "start_date";
    private static final String KEY_CHANNEL_VERSION = "channel_version";

    // Aumente este número sempre que mudar o canal (som, nome, etc.)
    private static final int CHANNEL_VERSION = 2;

    public static final String CHANNEL_ID = "cofrinho_channel_v2"; // novo ID força recriação

    private final SharedPreferences prefs;
    private final Context context;

    public SavingsManager(Context context) {
        this.context = context;
        this.prefs   = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        ensureNotificationChannel();
    }

    // ── Meta ──────────────────────────────────────────────────────────────────

    public void saveGoal(String name, double value, int days, String modality) {
        prefs.edit()
                .putString(KEY_GOAL_NAME, name)
                .putFloat(KEY_GOAL_VALUE, (float) value)
                .putInt(KEY_DEADLINE_DAYS, days)
                .putString(KEY_MODALITY, modality)
                .putFloat(KEY_CURRENT_AMOUNT, 0f)
                .putString(KEY_HISTORY, "[]")
                .putLong(KEY_START_DATE, System.currentTimeMillis())
                .apply();
    }

    public boolean hasGoal()          { return prefs.contains(KEY_GOAL_NAME); }
    public String getGoalName()       { return prefs.getString(KEY_GOAL_NAME, ""); }
    public double getGoalValue()      { return prefs.getFloat(KEY_GOAL_VALUE, 0f); }
    public int    getDeadlineDays()   { return prefs.getInt(KEY_DEADLINE_DAYS, 30); }
    public String getModality()       { return prefs.getString(KEY_MODALITY, ""); }
    public double getCurrentAmount()  { return prefs.getFloat(KEY_CURRENT_AMOUNT, 0f); }

    public int getDaysRemaining() {
        long start   = prefs.getLong(KEY_START_DATE, System.currentTimeMillis());
        long diffMs  = System.currentTimeMillis() - start;
        int  elapsed = (int)(diffMs / (1000 * 60 * 60 * 24));
        return Math.max(0, getDeadlineDays() - elapsed);
    }

    public float getProgress() {
        double goal = getGoalValue();
        if (goal <= 0) return 0f;
        return (float) Math.min(1.0, getCurrentAmount() / goal);
    }

    // ── Depósitos ─────────────────────────────────────────────────────────────

    public void addDeposit(double amount) {
        float current = (float) getCurrentAmount();
        prefs.edit().putFloat(KEY_CURRENT_AMOUNT, current + (float) amount).apply();
        saveToHistory(amount);
    }

    private void saveToHistory(double amount) {
        try {
            JSONArray arr    = getHistoryJson();
            JSONObject entry = new JSONObject();
            entry.put("amount", amount);
            entry.put("date", new SimpleDateFormat("dd/MM/yyyy HH:mm",
                    Locale.getDefault()).format(new Date()));
            arr.put(entry);
            prefs.edit().putString(KEY_HISTORY, arr.toString()).apply();
        } catch (JSONException e) { e.printStackTrace(); }
    }

    public List<String> getHistoryList() {
        List<String> list = new ArrayList<>();
        try {
            JSONArray arr = getHistoryJson();
            for (int i = arr.length() - 1; i >= 0; i--) {
                JSONObject obj = arr.getJSONObject(i);
                list.add(String.format(Locale.getDefault(),
                        "R$ %.2f  —  %s",
                        obj.getDouble("amount"), obj.getString("date")));
            }
        } catch (JSONException e) { e.printStackTrace(); }
        return list;
    }

    private JSONArray getHistoryJson() {
        try { return new JSONArray(prefs.getString(KEY_HISTORY, "[]")); }
        catch (JSONException e) { return new JSONArray(); }
    }

    public void resetGoal() { prefs.edit().clear().apply(); }

    // ── Lembretes ─────────────────────────────────────────────────────────────

    public void setReminder(boolean enabled, int hour, int minute) {
        prefs.edit()
                .putBoolean(KEY_REMINDER_ENABLED, enabled)
                .putInt(KEY_REMINDER_HOUR, hour)
                .putInt(KEY_REMINDER_MINUTE, minute)
                .apply();

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent   = new Intent(context, ReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (!enabled) {
            am.cancel(pi);
            return;
        }

        ReminderReceiver.scheduleNextAlarm(context, hour, minute);
    }

    public static long nextTriggerMs(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return cal.getTimeInMillis();
    }

    public boolean isReminderEnabled() { return prefs.getBoolean(KEY_REMINDER_ENABLED, false); }
    public int getReminderHour()        { return prefs.getInt(KEY_REMINDER_HOUR, 9); }
    public int getReminderMinute()      { return prefs.getInt(KEY_REMINDER_MINUTE, 0); }

    /** Retorna true se o app pode agendar alarmes exatos (Android 12+) */
    public boolean canScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            return am.canScheduleExactAlarms();
        }
        return true;
    }

    // ── Canal de notificação ──────────────────────────────────────────────────

    /**
     * Cria (ou recria) o canal de notificação com som.
     * Usa CHANNEL_VERSION para forçar recriação quando o canal muda.
     */
    private void ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        int savedVersion = prefs.getInt(KEY_CHANNEL_VERSION, -1);
        if (savedVersion == CHANNEL_VERSION) return; // já está atualizado

        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Remove canal antigo, se existir
        nm.deleteNotificationChannel("cofrinho_channel");
        nm.deleteNotificationChannel("cofrinho_channel_v2");

        Uri soundUri = Uri.parse(
                "android.resource://" + context.getPackageName()
                        + "/" + R.raw.notificacao_som);

        AudioAttributes audioAttr = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID, "Cofrinho Digital",
                NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("Lembretes de economia");
        ch.setSound(soundUri, audioAttr);
        ch.enableVibration(true);
        nm.createNotificationChannel(ch);

        prefs.edit().putInt(KEY_CHANNEL_VERSION, CHANNEL_VERSION).apply();
    }
}