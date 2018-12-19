package victor.pacheco.userqueuemanager;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.v1beta1.StructuredQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class UserQueueActivity extends AppCompatActivity {
    private TextView waiting_time;
    private String queueId;
    private String username;


    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_queue);

        waiting_time = findViewById(R.id.waiting_time);
        queueId = getIntent().getStringExtra("queueId");
        username = getIntent().getStringExtra("username");

        // Miramos que usuario tiene el mismo nombre en firebase que mi nombre, obtenemos su id y le añadimos el waiting time
        // de momento se añade un valor constante hay que obtener el slot time y el numuser de su cola y multiplicarlos
        // Comento como obtendria yo el Slot time pero creo que esta mal

        db.collection("Queues").document(queueId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException e) {
                Queue q = doc.toObject(Queue.class);
                final Integer wt = q.getSlot_time()* q.getNumuser();
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
                                                .document(doc.getId()).update("waiting_time",wt);
                                        waiting_time.setText(wt.toString());
                                    }
                                }
                            }
                        });
            }
        });


        /*db.collection("Queues").addSnapshotListener(new EventListener<QuerySnapshot>() { // actualiza la queue_set_list con
            // la lista que tenemos en firebase
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    Queue q = doc.toObject(Queue.class);
                    q.setId(doc.getId());
                    waiting_time.setText(q.getSlot_time() * q.getNumuser());
                }
            }
        });*/

       /* Habrá que hacer que cuando el usuario le de al botón atrás, salga un alert de si está seguro
        que quiere salir de la cola, pq sino no podrá entrar al usuario que habia creado (follón). Será el
        mismo código que el utilizado para el botón salir de la cola
        */
    }


}
