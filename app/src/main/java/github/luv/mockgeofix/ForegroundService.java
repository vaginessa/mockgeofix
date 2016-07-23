package github.luv.mockgeofix;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

/**
 * ForegroundService is a super-simple service that MockLocationService starts
 * when the worker thread has been started and stops when the thread has stopped (but only if
 * the user ticked "foreground service" in preferences).
 *
 * This service is also started by Android on app startup when (and only when) the app was
 * previously killed by OOM/low-memory killer and the service was running at that time - that's why
 * we call MockLocationService.start() in onStartCommand (to start the worker thread if it
 * was running at the time MockGeoFix was killed by oom killer).
 *
 * Running this service has following consequences:
 *   * provides a system notification when the worker thread is running
 *   * lower oom_adj value (less likely to be killed by OOM/low-memory killer)
 *   * hints OOM/low-memory killer to restart MockGeoFix when resources are available again
 *   * automatically restarts the worker thread on app startup when (and only when) the thread
 *     was running when the app was killed by OOM/low-memory killer
 */
public class ForegroundService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bindService(new Intent(getApplicationContext(),MockLocationService.class),
                new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder binder) {
                        MockLocationService service = ((MockLocationService.Binder)binder).getService();
                        // MockLocationService.start is idempotent (ie if the worker is already running
                        // it does nothing)
                        service.start();
                        unbindService(this);
                    }
                    @Override
                    public void onServiceDisconnected(ComponentName name) {}
                },
                Context.BIND_AUTO_CREATE
        );
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
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext());
        String portStr = pref.getString("listen_port", "5554");

        // deprecated but only API that works on everything from Gingerbread to Android N
        Notification notification = new Notification(R.drawable.ic_notification,
                getString(R.string.notification_ticker),
                System.currentTimeMillis());
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
        notification.setLatestEventInfo(getApplicationContext(),
                String.format(getString(R.string.notification_title), portStr),
                getString(R.string.notification_text),
                pendingIntent);
        startForeground(MockGeoFixApp.FOREGROUND_SERVICE_NOTIFICATION_ID, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}