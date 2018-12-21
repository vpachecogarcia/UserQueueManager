package victor.pacheco.userqueuemanager;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
    private EditText username;
    private Button btn_acces;
    private User user;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acces);

        //Obtenemos referencias de los objetos de la pantalla
        queue_code = findViewById(R.id.queue_code);
        username = findViewById(R.id.username);
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
        final String code = queue_code.getText().toString();
        final String name = username.getText().toString();
        Boolean state = false;
        user = new User(name, 0, state.equals(("true")));

        db.collection("Queues").document(code).collection("Users").add(user).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                // Tan solo queremos que espere a haber añadido el usuario a la colección antes de cambiar de actividad
            }
        });

        // Una vez hemos accedido a la cola y actualizado datos en Firestore, abrimos UserQueueActivity
        Intent intent = new Intent(getApplicationContext(), UserQueueActivity.class);
        intent.putExtra("queueId", code);
        intent.putExtra("username", name);
        startActivity(intent);
    }

}
