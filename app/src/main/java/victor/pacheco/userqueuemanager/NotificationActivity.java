package victor.pacheco.userqueuemanager;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class NotificationActivity extends Application {

    public static final String CHANNEL_ID = "notificationChanel";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChanels();
    }

    private void createNotificationChanels(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "notificationChanel",NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notificación de cambio de turno");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

}
