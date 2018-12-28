package victor.pacheco.userqueuemanager;

import android.app.Notification;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;

import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    public boolean estado_user=false;
    public boolean estado_delete=false;
    private Button leave;
    private Button back;


    private NotificationManagerCompat notificationManager;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final int CREATE_USER = 0;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String ID = "id";
    public static final String QUEUE = "queue";
    public String id_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_queue);

        waiting_time = findViewById(R.id.waiting_time);
        minutes_label = findViewById(R.id.minutes_label);
        leave = findViewById(R.id.btn_leave);
        back = findViewById(R.id.btn_be_back);
        notificationManager = NotificationManagerCompat.from(this);

        loadData();
        Toast.makeText(this,username+ "" + queueId, Toast.LENGTH_LONG).show();
        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteUser();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserWillBeBack();
            }
        });


        if(username.equals("") && queueId.equals("")) {

            Intent intent = new Intent(this, AccesActivity.class);
            intent.putExtra("queueId", queueId);
            intent.putExtra("username", username);
            startActivityForResult(intent, CREATE_USER);
        }
        else actualiza_wt(queueId, username);


    }

    public void UserWillBeBack(){
        estado_user=true;
        waiting_time.setText("You are in absent mode");
        minutes_label.setText("");
    }

    public void deleteUser(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure that you want to leave?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                estado_delete=true;

                username="";
                queueId="";
                getSharedPreferences("sharedPrefs", 0).edit().clear().apply();
                finish();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        // Le pedimos al builder que finalmente cree el AlertDialog y lo mostramos directamente
        builder.create().show();


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CREATE_USER:
                if (resultCode == RESULT_OK){
                    queueId = data.getStringExtra("queueId");
                    username = data.getStringExtra("username");
                    actualiza_wt(queueId, username);
                    saveData(queueId, username);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode,data);
        }
    }

    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        queueId = sharedPreferences.getString(QUEUE,"");
        username = sharedPreferences.getString(ID,"");
        Toast.makeText(this,username+ "" + queueId, Toast.LENGTH_LONG).show();

    }

    public void saveData(String queueId, String username){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(QUEUE, queueId);
        editor.putString(ID, username);
        editor.apply();
    }

    private void actualiza_wt(final String queueId, final String username) {
        // Obtenemos el id de nuestro usuario y le añadimos el waiting time
        db.collection("Queues").document(queueId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException e) {
                 Queue q = doc.toObject(Queue.class);
                final Integer wt = ((q.getSlot_time() * q.getNumuser()) - q.getSlot_time());


                new CountDownTimer((wt * 60 * 1000), 60000) {
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
                                                        .document(doc.getId()).update("waiting_time", wt);
                                                if(estado_user==true){
                                                    db.collection("Queues").document(queueId).collection("Users").document(doc.getId()).update("state", true);
                                                }
                                                if(estado_delete==true){
                                                    db.collection("Queues").document(queueId).collection("Users").document(doc.getId()).delete();

                                                }
                                                waiting_time.setText((wt_act).toString());

                                                db.collection("Queues").document(queueId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        Queue q = documentSnapshot.toObject(Queue.class);
                                                        if(q.getCurrent_user().equals(username)){
                                                            notifica();
                                                        }
                                                    }
                                                });





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
                                                        .document(doc.getId()).update("waiting_time", wt);


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
