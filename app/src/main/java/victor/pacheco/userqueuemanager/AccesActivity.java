package victor.pacheco.userqueuemanager;

import android.content.Intent;

import java.util.UUID;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Date;

public class AccesActivity extends AppCompatActivity {

    private EditText queue_code;
    private EditText user_name;
    private Button btn_acces;
    private User user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String usr_id;
    private int hora;
    private int min;
    private Date acces_time;

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
        btn_acces.setEnabled(false);
        final String queueId = queue_code.getText().toString();

        Calendar calendar = Calendar.getInstance(); // contine la fecha actual.
        acces_time = calendar.getTime();
        hora = calendar.get(Calendar.HOUR_OF_DAY);
        min = calendar.get(Calendar.MINUTE);


        db.collection("Queues").whereEqualTo("queue_name", queueId)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot doc) {
                    if(doc.isEmpty()){
                        Toast.makeText(AccesActivity.this, "The Queue " + queueId + " doesn't exists.", Toast.LENGTH_LONG).show();
                        btn_acces.setEnabled(true);
                    }
                    else {
                        db.collection("Queues").document(queueId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Queue q = documentSnapshot.toObject(Queue.class);
                                Integer h = q.getHour();
                                Integer m = q.getMin();

                                if ( hora > h) {
                                    Toast.makeText(AccesActivity.this, "The Queue " + queueId + " is closed.", Toast.LENGTH_LONG).show();
                                    btn_acces.setEnabled(true);
                                }
                                else if (hora == h &&  min > m ){
                                    Toast.makeText(AccesActivity.this, "The Queue " + queueId + " is closed.", Toast.LENGTH_LONG).show();
                                    btn_acces.setEnabled(true);
                                }
                                else FindUser();
                            }
                        });
                    }
                }

                });

    }

    public void FindUser(){
        final String queueId = queue_code.getText().toString();
        final String username = user_name.getText().toString();
        final Boolean state=false;

        db.collection("Queues").document(queueId).collection("Users").whereEqualTo("usr_id ",username)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.isEmpty()){
                    CreateUser();
                }
                else {
                    Toast.makeText(AccesActivity.this, "This user already exists.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void CreateUser(){
        final String queueId = queue_code.getText().toString();
        final String username = user_name.getText().toString();
        final Boolean state = false;
        String id;

        Integer usr_pos = -1;
        if(username.equals("")){
            id=UUID.randomUUID().toString();
        }
        else{id = username;}

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
