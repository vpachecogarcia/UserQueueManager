package victor.pacheco.userqueuemanager;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class AccesActivity extends AppCompatActivity {

    private EditText queue_code;
    private EditText user_name;
    private Button btn_acces;
    private User user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

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
        Boolean state = false;
        user = new User(username, 0, state.equals(("true")));
        db.collection("Queues").document(queueId).collection("Users").add(user);

        // Una vez hemos accedido a la cola y actualizado datos en Firestore, abrimos UserQueueActivity
        Intent data = new Intent();
        data.putExtra("queueId", queueId);
        data.putExtra("username", username);
        setResult(RESULT_OK, data);
        finish();

    }



}
