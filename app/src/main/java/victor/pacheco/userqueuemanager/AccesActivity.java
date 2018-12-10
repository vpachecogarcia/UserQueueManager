package victor.pacheco.userqueuemanager;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

import io.opencensus.tags.Tag;

public class AccesActivity extends AppCompatActivity {

    private EditText queue_code;
    private EditText username;
    private Button btn_acces;

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
        String code = queue_code.getText().toString();

        db.collection("Queues").whereEqualTo(code, true).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()){

                        Toast.makeText(getApplicationContext(),document.getId(),Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

}
