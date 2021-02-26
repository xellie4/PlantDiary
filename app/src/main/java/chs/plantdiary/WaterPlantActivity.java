package chs.plantdiary;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class WaterPlantActivity extends AppCompatActivity {

    private FirebaseStorage mStorageRef;
    private DatabaseReference mDataBaseRef;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseStorage mStorage;
    private DatabaseReference mDatabaseRef;

    //private ValueEventListener mDBListener;

    private ArrayList<String> mPlantNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waterplants);

        //mFirebaseAuth = FirebaseAuth.getInstance();

        //FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser(); //returneaza user-ul curent

        // creates a new folder in uploads for each user
        //mStorageRef = FirebaseStorage.getInstance().getReference("uploads/" + uId + "/");
        //mDataBaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        mPlantNames = new ArrayList<String>();

       /* add all plant names to mPlantNames list */

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        String uId = currentFirebaseUser.getUid().toString();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads/" + uId + "/");
        mStorage= FirebaseStorage.getInstance();

        Spinner plantsSpinner = findViewById(R.id.plants_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, mPlantNames);

        plantsSpinner.setAdapter(adapter);

        plantsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(this, "Normal click at position: " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}