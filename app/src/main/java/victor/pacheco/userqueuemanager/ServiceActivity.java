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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import static victor.pacheco.userqueuemanager.NotificationActivity.CHANNEL_ID;

public class ServiceActivity extends Service {

    private Integer wt;
    private Integer pos;
    private Integer slot;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String queueId;
    private String username;
    private String usr_id;
    private PendingIntent pendingIntent;
    private boolean called = false;

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
        usr_id = intent.getStringExtra("user_id");

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
        obten_datos();
        actualiza_wt();

        return START_NOT_STICKY; // Determina que sucede cuando el sistema mata nuestro servicio. Non Stiky significa que no hacemos nada cuando esto sucede.
    }

    private void obten_datos() {
        db.collection("Queues").document(queueId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Queue q = documentSnapshot.toObject(Queue.class);
                slot = q.getSlot_time();
            }
        });
        db.collection("Queues").document(queueId).collection("Users").document(usr_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User u = documentSnapshot.toObject(User.class);
                if (u.getUsr_pos() == -1){
                    obten_datos();
                }
                else cambios_pos();
            }

        });
    }

    private void cambios_pos() {
        db.collection("Queues").document(queueId).collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable final FirebaseFirestoreException e) {
                db.collection("Queues").document(queueId).collection("Users").document(usr_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User u = documentSnapshot.toObject(User.class);
                        Integer new_pos = u.getUsr_pos();
                        if(new_pos != pos){
                            pos = new_pos;
                           wt = wt -slot;
                           if (wt>=1){
                           }
                           else {
                               db.collection("Queues").document(queueId).collection("Users").document(usr_id)
                                       .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                   @Override
                                   public void onSuccess(DocumentSnapshot doc) {
                                       db.collection("Queues").document(queueId).collection("Users")
                                               .document(doc.getId()).update("waiting_time", 0);
                                   }
                               });
                           }
                        }
                    }
                });
            }
        });
    }


    private void actualiza_wt() {
        //Iniciamos un temporizador desde el tiempo de espera establecido hasta 0. A cada minuto, actualizamos el tiempo de espera en firestore.

        new CountDownTimer((wt * 60 * 1000), 60000) {

            @Override
            public void onTick(long l) {
                //Actualizamos el tiempo de espera en firestore
                db.collection("Queues").document(queueId).collection("Users")
                        .document(usr_id).update("waiting_time", ServiceActivity.this.wt).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ServiceActivity.this, "Something went wrong. Please contact with the manager", Toast.LENGTH_SHORT).show();
                    }
                });

                wt--;
            }

            @Override
            // Cuando acaba la cuenta atrás, realiza las siguientes operaciones
            public void onFinish() {
                // Entramos en la colección de nuestro Usuario y ponemos el tiempo de espera a 0
                db.collection("Queues").document(queueId).collection("Users").document(usr_id)
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot doc) {
                        db.collection("Queues").document(queueId).collection("Users")
                                .document(doc.getId()).update("waiting_time", 0);
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
