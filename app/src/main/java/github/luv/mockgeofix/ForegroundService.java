package github.luv.mockgeofix;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class ForegroundService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // deprecated but only API that works on everything from Gingerbread to Android N
        Notification notification = new Notification(R.drawable.ic_notification, "MockGeoFix service started",
                System.currentTimeMillis());
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
        notification.setLatestEventInfo(getApplicationContext(), "MockGeoFix",
                "Touch to open MockGeoFix.", pendingIntent);
        startForeground(MockGeoFixApp.FOREGROUND_SERVICE_NOTIFICATION_ID, notification);

        Thread t = new Thread() {
            @Override
            public void run() {
                // while(true) loop is just the usual java boilerplate
                // we are just calling "MockLocationService.initialized.await()" and making sure
                // InterruptedException is handled correctly
                while (true) {
                    try { MockLocationService.selfInitialized.await(); break; }
                    catch (InterruptedException ignored) {}
                }
                MockLocationService.self.start();
            }
        };
        t.start();

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}