package com.example.cofrinhodigital;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SavingsManager sm = new SavingsManager(context);
        String msg = String.format("Você já economizou R$ %.2f de R$ %.2f. Continue assim!",
                sm.getCurrentAmount(), sm.getGoalValue());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, SavingsManager.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Cofrinho Digital")
                .setContentText(msg)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(1001, builder.build());
        } catch (SecurityException ignored) {}
    }
}