package com.example.cofrinhodigital;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SavingsManager sm = new SavingsManager(context);
        showNotification(context, sm);

        // Reagenda para amanhã
        if (sm.isReminderEnabled()) {
            scheduleNextAlarm(context, sm.getReminderHour(), sm.getReminderMinute());
        }
    }

    /** Exibe a notificação de lembrete. Pode ser chamado também para teste imediato. */
    public static void showNotification(Context context, SavingsManager sm) {
        String msg = String.format(
                "Você já economizou R$ %.2f de R$ %.2f. Continue assim!",
                sm.getCurrentAmount(), sm.getGoalValue());

        Uri soundUri = Uri.parse(
                "android.resource://" + context.getPackageName()
                        + "/" + R.raw.notificacao_som);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, SavingsManager.CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("💰 Cofrinho Digital")
                        .setContentText(msg)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setSound(soundUri)
                        .setVibrate(new long[]{0, 300, 200, 300})
                        .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(1001, builder.build());
        } catch (SecurityException ignored) {}
    }

    /** Agenda o próximo alarme com verificação de permissão. */
    public static void scheduleNextAlarm(Context context, int hour, int minute) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent nextIntent = new Intent(context, ReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long triggerMs = SavingsManager.nextTriggerMs(hour, minute);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pi);
            } else {
                // Fallback sem crash — pode ter pequeno atraso
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pi);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerMs, pi);
        }
    }
}