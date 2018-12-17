package victor.pacheco.userqueuemanager;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import io.opencensus.tags.Tag;

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

    public void check(){
        final String code = queue_code.getText().toString();
        final String name = username.getText().toString();
        Boolean state = false;
        user = new User(name, 0, state.equals(("true")));

        db.collection("Queues").document(code).collection("Users").add(user);

        db.collection("Queues").document(code).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                Queue q = documentSnapshot.toObject(Queue.class);
                if (q.getNumuser() == 1){
                    db.collection("Queues").document(code).update("current_user", name);
                }
            }
        });

        Intent intent = new Intent(getApplicationContext(), UserQueueActivity.class);
        intent.putExtra("queueId", code);
        intent.putExtra("username", name);
        startActivity(intent);
    }

}
