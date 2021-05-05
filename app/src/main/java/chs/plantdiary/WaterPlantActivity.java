package chs.plantdiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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
    private ValueEventListener mDBListener;

    private ArrayList<String> mPlantNames;
    private ArrayList<String> mPlantDates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waterplants);

        //mFirebaseAuth = FirebaseAuth.getInstance();

        //FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser(); //returneaza user-ul curent

        // creates a new folder in uploads for each user
        //mStorageRef = FirebaseStorage.getInstance().getReference("uploads/" + uId + "/");
        //mDataBaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        mPlantNames = new ArrayList<String>(); //store all plant name
        mPlantDates = new ArrayList<String>(); //store all plant data

       /* add all plant names to mPlantNames list */

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        String uId = currentFirebaseUser.getUid().toString();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads/" + uId + "/");
        mStorage= FirebaseStorage.getInstance();

        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //la fiecare fetch sa curete listele (in caz ca se sterg date)
                mPlantNames.clear();
                mPlantDates.clear();

                // aici baga key value din real time database
                for(DataSnapshot postSnapshot : snapshot.getChildren()){
                    String plantName;
                    String plantWaterDate;

                    // ia valoarea din key pentru campul plantName si date
                    plantName = postSnapshot.child("plantName").getValue(String.class);
                    plantWaterDate = postSnapshot.child("date").getValue(String.class);

                    Log.d("TAG", "name " + plantName + " date " + plantWaterDate);

                    mPlantNames.add(plantName);
                    mPlantDates.add(plantWaterDate);
                }
                //mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WaterPlantActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

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