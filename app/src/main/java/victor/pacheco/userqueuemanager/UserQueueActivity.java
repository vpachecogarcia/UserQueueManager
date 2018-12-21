package victor.pacheco.userqueuemanager;

import android.app.Notification;

import android.os.CountDownTimer;

import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import javax.annotation.Nullable;


import static victor.pacheco.userqueuemanager.NotificationActivity.CHANNEL_ID;

public class UserQueueActivity extends AppCompatActivity {
    private TextView waiting_time;
    private String queueId;
    private String username;
    private TextView minutes_label;

    private NotificationManagerCompat notificationManager;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_queue);

        waiting_time = findViewById(R.id.waiting_time);
        minutes_label = findViewById(R.id.minutes_label);
        queueId = getIntent().getStringExtra("queueId");
        username = getIntent().getStringExtra("username");
        notificationManager = NotificationManagerCompat.from(this);

        // Obtenemos el id de nuestro usuario y le añadimos el waiting time

        db.collection("Queues").document(queueId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException e) {
                Queue q = doc.toObject(Queue.class);
                final Integer wt = ((q.getSlot_time()* q.getNumuser())-q.getSlot_time());
                new CountDownTimer((wt*60*1000), 60000) {
                    Integer cont = -1;
                    @Override
                    public void onTick(long l) {
                        db.collection("Queues").document(queueId).collection("Users")
                                .whereEqualTo("usr_id", username)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            // Hace falta este for ??
                                            Integer wt_act = wt - cont;
                                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                                db.collection("Queues").document(queueId).collection("Users")
                                                        .document(doc.getId()).update("waiting_time",wt);
                                                waiting_time.setText((wt_act).toString());
                                            }
                                        }
                                    }
                                });
                        cont++;
                    }

                    @Override
                    public void onFinish() {
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
                                                        .document(doc.getId()).update("waiting_time",wt);
                                                waiting_time.setText("Just a few minutes...");
                                                minutes_label.setText("");
                                                notifica();
                                            }
                                        }
                                    }
                                });
                    }
                }.start();
            }
        });
    }

    public void notifica(){
        db.collection("Queues").document(queueId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException e) {
                Queue q = doc.toObject(Queue.class);
                if (q.getCurrent_user().equals(username)) {
                    Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_your_turn)
                            .setContentText("It's your turn, please come in. Thanks for your waiting.")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                            .build();
                    notificationManager.notify(1,notification);
                    waiting_time.setText("Please, come in !");
                    minutes_label.setText("");

                }
            }
        });
    }

}
