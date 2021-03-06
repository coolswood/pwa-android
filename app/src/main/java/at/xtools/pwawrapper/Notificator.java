package at.xtools.pwawrapper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import static androidx.core.app.NotificationCompat.DEFAULT_ALL;

public class Notificator {
    private final String CHANNEL = "Firebase channel";
    private final Context context;
    private final NotificationManagerCompat manager;
    private int nextMessageId = 1;

    public Notificator(Context context) {
        this.context = context;
        manager = NotificationManagerCompat.from(context);
        createChannel();
    }

    public void notify(String title, String text) {
        //todo разобраться откуда это вообще появляется
        if(title == null || text == null || (title.isEmpty() && text.isEmpty())) return;//заглушка, чтобы не было пустого уведомления при входе

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        Log.d("PUSH", "incomming");
        Notification show = new NotificationCompat.Builder(context, CHANNEL)
                .setContentIntent(intent)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setDefaults(DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(fromResource(R.drawable.ic_appbar))
                .build();
        manager.notify(nextMessageId++, show);
    }

    private void createChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL, "Firebase", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(true);
            channel.canShowBadge();
            manager.createNotificationChannel(channel);
        }
    }

    private Bitmap fromResource(int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
