package victor.pacheco.userqueuemanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.v1beta1.StructuredQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class UserQueueActivity extends AppCompatActivity {
    private TextView waiting_time;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_queue);

        waiting_time = findViewById(R.id.waiting_time);



        db.collection("Queues").addSnapshotListener(new EventListener<QuerySnapshot>() { // actualiza la queue_set_list con
            // la lista que tenemos en firebase
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    Queue q = doc.toObject(Queue.class);
                    q.setId(doc.getId());
                    waiting_time.setText(q.getSlot_time() * q.getNumuser());
                }
            }
        });

        /* Puede que el slot time se obtenga asi:

        DocumentReference docRef = db.collection("Queues").document(idqueue);
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
        @Override
         public void onSuccess(DocumentSnapshot documentSnapshot) {
        Queue q = documentSnapshot.toObject(Queue.class);
        int slottime = q.getSlot_time;

        int wait_t= slottime*q.getNumuser;
        waiting_time.setText(wait_t);
         }
            });
         */

    }


}
