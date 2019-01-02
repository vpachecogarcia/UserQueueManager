package victor.pacheco.userqueuemanager;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import static victor.pacheco.userqueuemanager.NotificationActivity.CHANNEL_ID;

public class ServiceActivity extends Service {

    private Integer wt;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String queueId;
    private String username;
    private PendingIntent pendingIntent;
    private boolean called = false;
    public boolean estado_user=false;
    public boolean estado_delete=false;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {   // Será llamado cada vez que iniciamos el servicio

        //Obtenemos datos de UserQueueActivity
        queueId = intent.getStringExtra("queueId");
        username = intent.getStringExtra("username");
        wt = intent.getIntExtra("wt", -1);

        // Cremos una notificación de la clase NotificationActivity
        Intent notificationIntent = new Intent(this, UserQueueActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        String notification_text = "An alert will notify your turn.";
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle("Queue Manager Service")
                .setContentText(notification_text)
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build();
        // Iniciamos la notificación
        startForeground(1, notification);
        //Alertará si llega el turno del usuario
        turn_alert();
        // Actualiza el tiempo de espera
        actualiza_wt();

        return START_NOT_STICKY; // Determina que sucede cuando el sistema mata nuestro servicio. Non Stiky significa que no hacemos nada cuando esto sucede.

    }


    private void actualiza_wt() {
        //Iniciamos un temporizador desde el tiempo de espera establecido hasta 0. A cada minuto, actualizamos el tiempo de espera en firestore.
        new CountDownTimer((wt * 60 * 1000), 60000) {

            @Override
            public void onTick(long l) {
                // Accedemos ala colección Usuario cuyo id es el nuestro.
                db.collection("Queues").document(queueId).collection("Users")
                        .whereEqualTo("usr_id", username)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    // Hace falta este for ??
                                    for (QueryDocumentSnapshot doc : task.getResult()) {
                                        //Actualizamos el tiempo de espera en firestore
                                        db.collection("Queues").document(queueId).collection("Users")
                                                .document(doc.getId()).update("waiting_time", ServiceActivity.this.wt);

                                    }
                                }
                            }
                        });
                wt--;
            }

            @Override
            // Cuando acaba la cuenta atrás, realiza las siguientes operaciones
            public void onFinish() {
                // Entramos en la colección de nuestro Usuario y ponemos el tiempo de espera a 0
                db.collection("Queues")
                        .document(queueId)
                        .collection("Users")
                        .whereEqualTo("usr_id", username)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    // Hace falta este for ??
                                    for (QueryDocumentSnapshot doc : task.getResult()) {
                                        db.collection("Queues").document(queueId).collection("Users")
                                                .document(doc.getId()).update("waiting_time", 0);

                                    }
                                }
                            }
                        });
            }
        }.start();
    }
    // Escuchamos  cambios en nuestra cola, y cuando el current_user es el nuestro, notificamos al usuario.
    public void turn_alert() {
        db.collection("Queues").document(queueId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException e) {
                Queue q = doc.toObject(Queue.class);
                if (q.getCurrent_user().equals(username) && !called) {
                    String notification_text = "It's your turn. Please, come in.";
                    Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                            .setContentTitle("Queue Manager Service")
                            .setContentText(notification_text)
                            .setSmallIcon(R.drawable.ic_android)
                            .setContentIntent(pendingIntent)
                            .build();
                    startForeground(1, notification);
                    called = true;
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {  // No es necesario, por lo que lo q retornamos null
        return null;
    }
}
