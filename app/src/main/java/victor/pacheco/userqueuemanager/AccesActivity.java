package victor.pacheco.userqueuemanager;

import android.content.Intent;



import java.util.UUID;
import android.icu.text.TimeZoneFormat;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Timer;

import javax.annotation.Nullable;

public class AccesActivity extends AppCompatActivity {

    private EditText queue_code;
    private EditText user_name;
    private Button btn_acces;
    private User user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String usr_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acces);

        //Obtenemos referencias de los objetos de la pantalla
        queue_code = findViewById(R.id.queue_code);
        user_name = findViewById(R.id.username);
        btn_acces = findViewById(R.id.btn_acces);


        btn_acces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                check();
            }
        });


    }
    // Función para inicializar el campo current_user con el primer Usuario que entre en la cola
    public void check(){
        final String queueId = queue_code.getText().toString();
        final String username = user_name.getText().toString();
        final Boolean state = false;


        db.collection("Queues").addSnapshotListener(new EventListener<QuerySnapshot>() { // actualiza la queue_set_list con
            // la lista que tenemos en firebase
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                Boolean colaencontrada = false;
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    Queue q = doc.toObject(Queue.class);
                    q.setId(doc.getId());
                    if(q.getId().equals(queueId)){
                        colaencontrada=true;
                    }
                }
                if(colaencontrada==false){
                    Toast.makeText(AccesActivity.this, "The queueId doesn't exist.", Toast.LENGTH_SHORT).show();
                }
                else{
                    FindUser();
                }

            }
        });





    }

    public void FindUser(){
        final String queueId = queue_code.getText().toString();
        final String username = user_name.getText().toString();
        final Boolean state=false;


        db.collection("Queues").document(queueId).collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() { // actualiza la queue_set_list con
            // la lista que tenemos en firebase
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                Boolean userencontrado = false;
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    User u = doc.toObject(User.class);

                    if(u.getUsr_id().equals(username)){
                        userencontrado=true;
                        Toast.makeText(AccesActivity.this, "This user already exists.", Toast.LENGTH_SHORT).show();

                    }
                }
                if(userencontrado==false){

                        CreateUser();

                }

            }
        });
    }

    public void CreateUser(){
        final String queueId = queue_code.getText().toString();
        final String username = user_name.getText().toString();
        final Boolean state = false;
        String id;
        Calendar calendar = Calendar.getInstance(); // contine la fecha actual.
        String acces_time = DateFormat.getTimeInstance(DateFormat.DEFAULT).format(calendar.getTime()); // contiene la hora actual

        Integer usr_pos = -1;
        if(username.equals("")){

            id=UUID.randomUUID().toString();

        }
        else{id=username;}

        user = new User(id, 0, state.equals(("true")),acces_time, usr_pos);

        final String id2=id;
        db.collection("Queues").document(queueId).collection("Users")
                .add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                usr_id= documentReference.getId();
                Intent data = new Intent();
                data.putExtra("queueId", queueId);
                data.putExtra("username", id2);
                data.putExtra("user_id", usr_id);
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }


}
