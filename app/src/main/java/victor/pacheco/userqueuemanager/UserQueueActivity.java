package victor.pacheco.userqueuemanager;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import javax.annotation.Nullable;


public class UserQueueActivity extends AppCompatActivity {
    private TextView waiting_time;
    private Integer wt;
    private String queueId;
    private String username;
    private String usr_id;
    public boolean absent=false;
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
    public static final String USR_ID = "usr_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_queue);

        waiting_time = findViewById(R.id.waiting_time);
        leave = findViewById(R.id.btn_leave);
        back = findViewById(R.id.btn_be_back);
        // Cargamos los datos de nuestro usuario (username, queueId y wt_seted
        loadData();
        // Escuchamos los botones de ahora vuelvo y dejar la cola
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

        // Si no hemos registrado nuestro usuario y por tanto no pertenecemos a una cola
        if(username.equals("") && queueId.equals("")) {
            // creamos el Intent y le enviamos datos a AccesActivity esperando que esta nos devuelva otros
            Intent intent = new Intent(this, AccesActivity.class);
            intent.putExtra("queueId", queueId);
            intent.putExtra("username", username);
            intent.putExtra("user_id", usr_id);
            startActivityForResult(intent, CREATE_USER);
        }
        // De lo contrario llamamos a las funciones que se encargan de actualizar el tiempo de espera y de notificarnos si es nuestro turno
        else {
            notifica();
            actualiza_wt(queueId, username);
        }

    }

    // función para iniciar el servicio que realizará operaciones aun con la aplicación matada
    public void startService (){
        Intent serviceIntent = new Intent(this, ServiceActivity.class);
        serviceIntent.putExtra("wt", Integer.valueOf(wt));
        serviceIntent.putExtra("queueId", queueId);
        serviceIntent.putExtra("username", username);
        serviceIntent.putExtra("user_id", usr_id);
        startService(serviceIntent);
    }

    public void stopService(){
        Intent serviceIntent = new Intent(this, ServiceActivity.class);
        stopService(serviceIntent);
    }

    public void UserWillBeBack(){
        // Cuando presionamos el botón de ahora vuelvo, si estabamos ausentes nos devuelve al modo normal y viceversa
        db.collection("Queues").document(queueId).collection("Users").document(usr_id)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                User u = doc.toObject(User.class);
                if (!u.isState()){
                    db.collection("Queues").document(queueId).collection("Users")
                            .document(doc.getId()).update("state", true);
                    waiting_time.setText("You are in absent mode");
                    wt_seted = false;
                    absent = true;
                    stopService();
                }
                else {
                    db.collection("Queues").document(queueId).collection("Users")
                            .document(doc.getId()).update("state", false);
                    absent = false;
                    set_wt(queueId,username);
                }
            }
        });
    }
    // Mostramos un alertdialog si pulsa sobre dejar la cola. Si afirma, eliminamos el usuario de firestore así como las sharedPreferences
    public void deleteUser(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure that you want to leave?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Accedemos a la colección Usuario y borramos el documento de nuestro Usuario
                db.collection("Queues").document(queueId).collection("Users").document(usr_id)
                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(UserQueueActivity.this, "Thanks for using our App", Toast.LENGTH_SHORT).show();
                    }
                });
                username="";
                queueId="";
                usr_id="";
                wt_seted = false;
                getSharedPreferences("sharedPrefs", 0).edit().clear().apply();
                finish();
            }

        });
        builder.setNegativeButton(android.R.string.cancel, null);
        // Le pedimos al builder que finalmente cree el AlertDialog y lo mostramos directamente
        builder.create().show();
    }
    @Override
    //recibimos los datos de AccesActivity, y llamamos a set_wt y saveData
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CREATE_USER:
                if (resultCode == RESULT_OK){
                    queueId = data.getStringExtra("queueId");
                    username = data.getStringExtra("username");
                    usr_id= data.getStringExtra("user_id");
                    set_wt(queueId, username);
                    saveData(queueId, username, wt_seted, usr_id);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode,data);
        }
    }
    // Carga los datos queueId, username y wt_seted guardados mediante sharedPreferences
    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        queueId = sharedPreferences.getString(QUEUE,"");
        username = sharedPreferences.getString(ID,"");
        wt_seted = sharedPreferences.getBoolean(WT_SET, false);
        usr_id = sharedPreferences.getString(USR_ID, "");
    }
    // Guarda los datos queueId, username y wt_seted mediante sharedPreferences
    public void saveData(String queueId, String username, Boolean wt_seted, String usr_id){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(QUEUE, queueId);
        editor.putString(ID, username);
        editor.putBoolean(WT_SET, wt_seted);
        editor.putString(USR_ID,usr_id);
        editor.apply();
    }
    // Inicia el tiempo de espera y llama al servicio, que partirá de ese tiempo de espera
    private void set_wt(final String queueId, final String username){
        db.collection("Queues").document(queueId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException e) {
                // Si no se habia dado el wt antes (para no reiniciar el servicio y evitar bugs)
                if (!wt_seted) {
                    Queue q = doc.toObject(Queue.class);
                    // si eres el primero, te pide que entres directamente y anula que se muestre el wt por otro lado y que se te vuelva a llamar.
                    if(q.getNumuser()==0 || q.getCurrent_user().equals(username)){
                        waiting_time.setText("Please, come in !");
                        called = true;
                        wt_seted = true;
                    }
                    // Por contra, calculamos el tiempo de espera en función del número de usuarios, la posición actual y el slot time e inicamos el servicio.
                    else {
                        wt = (q.getSlot_time() * (q.getNumuser()+1 - q.getCurrent_pos()));
                        startService();
                        wt_seted = true;
                        actualiza_wt(queueId, username);
                        db.collection("Queues").document(queueId).collection("Users")
                                .document(usr_id).update("waiting_time", wt);
                        waiting_time.setText(wt.toString());
                    }

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
                    if (q.getCurrent_user().equals(username) && !absent) {
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
                db.collection("Queues").document(queueId).collection("Users").document(usr_id)
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot doc) {
                        User u = doc.toObject(User.class);
                        wt = u.getWaiting_time();
                        if (wt == 0 && !called && !absent) {
                            waiting_time.setText("You are about to be called...");
                        } else if (wt!=0 && !called && !absent) waiting_time.setText(wt.toString());
                    }
                });

            }
        });
    }
    // Detectamos si alguien sale de la lista por delante nuestro, para así actualizar el wt.
    private void cambios_posicion(){



    }

}
