package victor.pacheco.userqueuemanager;


import android.content.Intent;
import android.content.SharedPreferences;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import javax.annotation.Nullable;


public class UserQueueActivity extends AppCompatActivity {
    private TextView waiting_time;
    private Integer wt;
    private String queueId;
    private String username;
    public boolean estado_user=false;
    public boolean estado_delete=false;
    private boolean called = false;
    private Button leave;
    private Button back;
    private boolean wt_seted = false;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final int CREATE_USER = 0;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String ID = "id";
    public static final String QUEUE = "queue";
    public static final String WT_SET = "wt_seted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_queue);

        waiting_time = findViewById(R.id.waiting_time);
        leave = findViewById(R.id.btn_leave);
        back = findViewById(R.id.btn_be_back);

        loadData();

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
        else {
            notifica();
            actualiza_wt(queueId, username);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        /*if(!(username.equals("") && queueId.equals(""))) {
            startService();
        }*/
    }


    public void startService (){
        Intent serviceIntent = new Intent(this, ServiceActivity.class);
        serviceIntent.putExtra("wt", Integer.valueOf(wt));
        serviceIntent.putExtra("queueId", queueId);
        serviceIntent.putExtra("username", username);
        startService(serviceIntent);
    }

    public void stopService(){
        Intent serviceIntent = new Intent(this, ServiceActivity.class);
        stopService(serviceIntent);
    }

    public void UserWillBeBack(){
        estado_user=true;
        waiting_time.setText("You are in absent mode");
    }

    public void deleteUser(){
        estado_delete=true;
        username="";
        queueId="";
        wt_seted = false;
        this.getSharedPreferences("sharedPrefs", 0).edit().clear().apply();
        finish();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CREATE_USER:
                if (resultCode == RESULT_OK){
                    queueId = data.getStringExtra("queueId");
                    username = data.getStringExtra("username");
                    set_wt(queueId, username);
                    saveData(queueId, username, wt_seted);
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
        wt_seted = sharedPreferences.getBoolean(WT_SET, false);
    }

    public void saveData(String queueId, String username, Boolean wt_seted){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(QUEUE, queueId);
        editor.putString(ID, username);
        editor.putBoolean(WT_SET, wt_seted);
        editor.apply();
    }

    private void set_wt(final String queueId, final String username){
        db.collection("Queues").document(queueId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException e) {
                if (!wt_seted) {
                    Queue q = doc.toObject(Queue.class);
                    wt = (q.getSlot_time() * (q.getNumuser()+1 - q.getCurrent_pos()));
                    startService();
                    wt_seted = true;
                    actualiza_wt(queueId, username);
                    db.collection("Queues").document(queueId).collection("Users")
                            .whereEqualTo("usr_id", username)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot doc : task.getResult()) {
                                            db.collection("Queues").document(queueId).collection("Users")
                                                    .document(doc.getId()).update("waiting_time", wt);
                                            waiting_time.setText(wt.toString());

                                        }
                                    }
                                }
                            });
                }
            }
        });
    }

    public void notifica(){
        if (!called) {
            db.collection("Queues").document(queueId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException e) {
                    Queue q = doc.toObject(Queue.class);
                    if (q.getCurrent_user().equals(username)) {
                        waiting_time.setText("Please, come in !");
                        called = true;

                    }
                }
            });
        }
    }


    private void actualiza_wt (final String queueId, final String username) {
        // Escuchamos cambios en el campo usuario y actualizamos el waiting time de nuestro layout con el wt correspondiente
        db.collection("Queues").document(queueId).collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                db.collection("Queues").document(queueId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException e) {
                        db.collection("Queues").document(queueId).collection("Users")
                                .whereEqualTo("usr_id", username)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                                User u = doc.toObject(User.class);
                                                wt = u.getWaiting_time();
                                                if (wt == 0 && !called) {
                                                    waiting_time.setText("You are about to be called...");
                                                } else if (!called) waiting_time.setText(wt.toString());
                                            }
                                        }
                                    }
                                });
                    }
                });
            }
        });

        /*
        db.collection("Queues").document(queueId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException e) {

                db.collection("Queues").document(queueId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException e) {
                            db.collection("Queues").document(queueId).collection("Users")
                                    .whereEqualTo("usr_id", username)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                                    User u = doc.toObject(User.class);
                                                    wt_act = u.getWaiting_time();
                                                    new CountDownTimer((wt_act * 60 * 1000), 60000) {

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
                                                                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                                                                    db.collection("Queues").document(queueId).collection("Users")
                                                                                            .document(doc.getId()).update("waiting_time", wt_act);
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
                                                                                            if(q.getCurrent_user().equals(username) && !called){
                                                                                                notifica();
                                                                                                called = true;
                                                                                            }
                                                                                        }
                                                                                    });
                                                                                }
                                                                            }
                                                                        }
                                                                    });
                                                            cont++;
                                                            wt_act --;
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
                                                                                            .document(doc.getId()).update("waiting_time", 0);


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
                                            }
                                        }
                                    });
                    }
                });
            }
        });*/
    }

}
