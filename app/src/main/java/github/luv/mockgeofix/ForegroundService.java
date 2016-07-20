package github.luv.mockgeofix;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * ForegroundService is a super-simple service that MockLocationService starts
 * when the worker thread has been started and stops when the thread has stopped (but only if
 * the user ticked "foreground service" in preferences).
 *
 * This service is also started by Android on app startup when (and only when) the app was
 * previously killed by OOM/low-memory and the service was running at that time - that's why we call
 * MockLocationService.self.start() in onStartCommand (to start the worker thread if it was running
 * at the time MockGeoFix was killed by oom killer).
 *
 * Running this service has following consequences:
 *   * provides a system notification when the worker thread is running
 *   * lower oom_adj value (less likely to be killed by OOM/low-memory killer)
 *   * automatically restarts the worker thread on app startup when (and only when) the thread
 *     was running when the app was killed by OOM/low-memory killer
 */
public class ForegroundService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Thread t = new Thread() {
            @Override
            public void run() {
                // Wait for MockLocationService to be ready (it's initialized on app startup)
                // and start the worker thread
                //
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

        // when MockGeoFix app is killed by oom killer and ForegroundService is running at the time,
        // we want the killer to restart MockGeoFix app process and start this service
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        goForeground();
    }

    public void goForeground() {
        // deprecated but only API that works on everything from Gingerbread to Android N
        Notification notification = new Notification(R.drawable.ic_notification, "MockGeoFix service started",
                System.currentTimeMillis());
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
        notification.setLatestEventInfo(getApplicationContext(), "MockGeoFix",
                "Touch to open MockGeoFix.", pendingIntent);
        startForeground(MockGeoFixApp.FOREGROUND_SERVICE_NOTIFICATION_ID, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}