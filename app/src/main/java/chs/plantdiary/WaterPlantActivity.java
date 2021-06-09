package chs.plantdiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import android.net.*;
import android.provider.SyncStateContract;
import android.text.Layout;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Array;

import javax.net.ssl.ManagerFactoryParameters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class WaterPlantActivity extends AppCompatActivity implements AsyncResponse{

    private FirebaseStorage mStorageRef;
    private DatabaseReference mDataBaseRef;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseStorage mStorage;
    private DatabaseReference mDatabaseRef;
    private ValueEventListener mDBListener;

    /* declaratie toate relativelayout din layout */
    private RelativeLayout waterRL;
    private RelativeLayout moistureRL;
    private RelativeLayout ipaddressRL;
    private RelativeLayout sunRLData;
    private RelativeLayout waterRLData;
    private RelativeLayout tempRLData;
    private RelativeLayout fertilizerRLData;
    private RelativeLayout soilRLData;

    private ArrayList<String> mPlantNames;
    private ArrayList<String> mPlantWateredDates;
    private ArrayList<String> mPlantMoistureLevel;

    private TextView wateringDateTv;
    private TextView moistureLevelTv;
    private TextView ipAddressTv;
    private TextView sunData;
    private TextView waterData;
    private TextView tempData;
    private TextView fertilizerData;
    private TextView soilData;

    private Button scanDevicesButton;
    private Button refreshButton;

    private List<String> devices;
    private List<String> moistureLevelDevices;
    private List<String> wateredDateDevices;
    private List<String> allPlantEntriesForUser = new ArrayList<String>();

    public static int spinnerPosition = -1;

    private String lastWateredDate;
    private String moistureMeterLevel;
    private String sunlightInfoData;
    private String waterInfoData;
    private String roomTempInfoData;
    private String fertilizerInfoData;
    private String soilMixtureInfoData;

    private Spinner plantsSpinner;
    static ProgressBar mProgressBar;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waterplants);

        wateringDateTv = findViewById(R.id.watering_date_data);
        moistureLevelTv =findViewById(R.id.moisture_data);
        ipAddressTv = findViewById(R.id.ipaddress_data);
        sunData = findViewById(R.id.sunData);
        waterData = findViewById(R.id.waterData);
        tempData = findViewById(R.id.tempData);
        fertilizerData = findViewById(R.id.fertilizerData);
        soilData = findViewById(R.id.soilData);

        scanDevicesButton = (Button) findViewById(R.id.scan_devices_button);
        refreshButton = (Button) findViewById(R.id.refresh_button);

        mPlantNames = new ArrayList<String>(); //store all plant name
        mPlantWateredDates = new ArrayList<String>(); //store all plant data
        mPlantMoistureLevel = new ArrayList<String>();
        devices = new ArrayList<String>();
        moistureLevelDevices = new ArrayList<String>();
        wateredDateDevices = new ArrayList<String>();
        allPlantEntriesForUser = new ArrayList<String>();

        waterRL = findViewById(R.id.wateringdate_layout);
        moistureRL = findViewById(R.id.moisture_layout);
        ipaddressRL = findViewById(R.id.ipaddress_layout);
        sunRLData = findViewById(R.id.sunLayout);
        waterRLData = findViewById(R.id.waterLayout);
        tempRLData = findViewById(R.id.tempLayout);
        fertilizerRLData = findViewById(R.id.fertilizerLayout);
        soilRLData = findViewById(R.id.soilLayout);

        waterRL.setVisibility(View.INVISIBLE);
        moistureRL.setVisibility(View.INVISIBLE);
        ipaddressRL.setVisibility(View.INVISIBLE);
        sunRLData.setVisibility(View.INVISIBLE);
        waterRLData.setVisibility(View.INVISIBLE);
        tempRLData.setVisibility(View.INVISIBLE);
        fertilizerRLData.setVisibility(View.INVISIBLE);
        soilRLData.setVisibility(View.INVISIBLE);

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        String uId = currentFirebaseUser.getUid().toString();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads/" + uId + "/");
        mStorage= FirebaseStorage.getInstance();

        plantsSpinner = (Spinner) findViewById(R.id.plants_spinner);

        mProgressBar = findViewById(R.id.progressbar);
        mProgressBar.setVisibility(View.INVISIBLE);
        mProgressBar.setProgressTintList(ColorStateList.valueOf(Color.WHITE));

        // ia date din baza de date
        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //la fiecare fetch sa curete listele (in caz ca se sterg date)
                mPlantNames.clear();
                allPlantEntriesForUser.clear();
                mPlantNames.add(0, "Select a plant"); // numele plantelor incep de pe indexul 1

                // aici baga key value din real time database
                for(DataSnapshot postSnapshot : snapshot.getChildren()){
                    // contine practic toate plantele
                    allPlantEntriesForUser.add(postSnapshot.getKey().toString()); //practic adauga random id-ul plantei in forma de string

                    String plantName;

                    // ia valoarea din key pentru campul plantName si date
                    plantName = postSnapshot.child("plantName").getValue(String.class);

                    Log.d("TAG", "name " + plantName);

                    mPlantNames.add(plantName);

                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(WaterPlantActivity.this, android.R.layout.simple_spinner_item, mPlantNames);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                plantsSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WaterPlantActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        plantsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(parent.getItemAtPosition(position).equals("Select a plant")){
                    // do nothing - title for spinner
                    waterRL.setVisibility(View.INVISIBLE);
                    moistureRL.setVisibility(View.INVISIBLE);
                    ipaddressRL.setVisibility(View.INVISIBLE);
                    sunRLData.setVisibility(View.INVISIBLE);
                    waterRLData.setVisibility(View.INVISIBLE);
                    tempRLData.setVisibility(View.INVISIBLE);
                    fertilizerRLData.setVisibility(View.INVISIBLE);
                    soilRLData.setVisibility(View.INVISIBLE);
                    setSpinnerPosition(0);
                    Log.i("POS INAINTE DE ELSE", "" + position);
                }
                else{
                    plantsSpinner.setSelection(position);
                    setSpinnerPosition(position);
                    onPositionReadAndSetValues(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // buton de scanare network
        scanDevicesButton.setOnClickListener(new View.OnClickListener() {
            //@RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if(spinnerPosition < 1){
                    Toast.makeText(WaterPlantActivity.this, "Please select a plant!", Toast.LENGTH_SHORT).show();
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(0);
                    scanNetwork();//ez csak elinditja
                }
            }
        });

        //actualizare date ce sunt trimise de raspi
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshData();
            }
        });
    }

    void onPositionReadAndSetValues(int position){
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uId = currentFirebaseUser.getUid().toString();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("uploads/" + uId + "/" + allPlantEntriesForUser.get(position-1) + "/");
        mDBListener = databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lastWateredDate = snapshot.child("date").getValue().toString();
                moistureMeterLevel = snapshot.child("moistureLevel").getValue().toString();
                sunlightInfoData = snapshot.child("sun").getValue().toString();
                waterInfoData = snapshot.child("water").getValue().toString();
                roomTempInfoData = snapshot.child("temp").getValue().toString();
                fertilizerInfoData = snapshot.child("fertilizer").getValue().toString();
                soilMixtureInfoData = snapshot.child("soil").getValue().toString();

                wateringDateTv.setText(lastWateredDate);
                moistureLevelTv.setText(moistureMeterLevel);
                sunData.setText(sunlightInfoData);
                waterData.setText(waterInfoData);
                tempData.setText(roomTempInfoData);
                fertilizerData.setText(fertilizerInfoData);
                soilData.setText(soilMixtureInfoData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //System.out.println("The read failed: " + databaseError.getCode());
                Toast.makeText(WaterPlantActivity.this, "nu mere", Toast.LENGTH_SHORT).show();
            }
        });

        waterRL.setVisibility(View.VISIBLE);
        moistureRL.setVisibility(View.VISIBLE);
        ipaddressRL.setVisibility(View.VISIBLE);
        sunRLData.setVisibility(View.VISIBLE);
        waterRLData.setVisibility(View.VISIBLE);
        tempRLData.setVisibility(View.VISIBLE);
        fertilizerRLData.setVisibility(View.VISIBLE);
        soilRLData.setVisibility(View.VISIBLE);

        //String item = parent.getItemAtPosition(position).toString();

        //show selected spinner item - debug purpose
        //Toast.makeText(parent.getContext(), "Selected " + item, Toast.LENGTH_SHORT).show();
        if(devices.isEmpty()){
            ipAddressTv.setText("No device scanned yet");
        } else {
            ipAddressTv.setText(devices.get(0));
        }

        // update fields with what is in the database ->TO DO  and when reads from raspi -> it needs to be actualized TO DO
        //wateringDateTv.setText(mPlantWateredDates.get(position - 1));
        //moistureLevelTv.setText(mPlantMoistureLevel.get(position - 1));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabaseRef.removeEventListener(mDBListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case 9:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                }  else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    Toast toast = Toast.makeText(this.getApplicationContext(), "WHY U NO PERMISSION", Toast.LENGTH_LONG);

                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void scanNetwork() {
        Context context = this.getApplicationContext();
        WifiManager wifiManager = (WifiManager)
                context.getSystemService(Context.WIFI_SERVICE);

        NetworkSniffTask nst = new NetworkSniffTask(getApplicationContext());
        nst.delegate = this;
        nst.execute();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void refreshData(){
        try {
            String device_ipaddress = devices.get(0);
            InetAddress address = InetAddress.getByName(device_ipaddress);
            String hostName = address.getCanonicalHostName();
            String name = address.getHostName();

            Socket socket;
            List<String> result = new ArrayList<String>();

            try {
                Log.i("TRY", device_ipaddress + " is reachable");

                /* We need to check if it accepts sockets on port 6666 to see if it's a watering device */
                socket = new Socket(device_ipaddress, 6666);

                Log.i("INFO", "Connected!");
                // get the output stream from the socket.
                OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = socket.getInputStream();
                // create a data output stream from the output stream so we can send data through it
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                DataInputStream dataInputStream = new DataInputStream(inputStream);

                // Send GET to the device
                dataOutputStream.writeUTF("GET");
                dataOutputStream.flush(); // send the message
                //dataOutputStream.close(); // close the output stream when we're done.
                Log.i("Info", "GET request sent! ");

                /* Get device response */
                byte[] response_bytes = new byte[1024];

                /* Get actual response length */
                dataInputStream.read(response_bytes);
                int n;
                for (n = 0; n < 1024; n++) {
                    if (response_bytes[n] == 0x00) {
                        break;
                    }
                }

                /* Copy only actual response */
                byte[] parsed_response_bytes = new byte[n];

                for (int ix = 0; ix < n; ix++) {
                    parsed_response_bytes[ix] = response_bytes[ix];
                }

                /* Convert byte array of response to String */
                String response = new String(parsed_response_bytes, StandardCharsets.US_ASCII);

                /* Optional send an ACK (Acknowledge), doesn't do anything since server doesn't care */
                Log.i("INFO", "Answer: " + response); // response <- contains the data that the raspi sent to the app
                dataOutputStream.writeUTF("ACK");

                socket.close();

                Log.i("INFO", "Host: " + String.valueOf(name) + "(" + device_ipaddress + ") is reachable!");
                result.add("Host: " + String.valueOf(name) + "(" + device_ipaddress + ")" + "---" + response);

                processFinish(result);
            } catch (Exception e) {
                Log.i("TRY", device_ipaddress + " is not reachable");
            }

        } catch (Exception e){
            Log.i("TRY",  "nu bun");
        }
    }

    @Override
    public void processFinish(List<String> result) {
        Log.i("ProcessFinish", "ENTERED PROCESSFINISH IN WATER ACTIVITY");
        //devices.clear();
        moistureLevelDevices.clear();
        wateredDateDevices.clear();
        devices.clear();

        //devices contains all the device ip addresses that are connected
        for(String s: result){
            Log.i("ProcessFinish", "info " + s);
            String aux =  s.substring(s.indexOf("(")+1, s.indexOf(")"));
            devices.add(aux);

            String[] splitString = s.split("---");
            // splitString[0] contains the first part with Host + device name + ip address
            moistureLevelDevices.add(splitString[1] + "%");
            wateredDateDevices.add(splitString[2]);
        }

        Log.i("SPINNERPOZITION", " " + getSpinnerPosition());

        Log.i("DEVICES", "");
        if(devices.isEmpty()){
            Toast.makeText(WaterPlantActivity.this, "No device connected", Toast.LENGTH_SHORT).show();

            wateringDateTv.setText("No device connected");
            moistureLevelTv.setText("No device connected");
            ipAddressTv.setText("No device connected");
        } else {
            // debug purpose
            for (String s : devices) {
                Log.i("DEVICES", "device trimmed: " + s);
            }

            // put actual data in textviews - cred ca ar fi mai ok sa scriu in db si de acolo in spinner sa citeasca valoarea si avem un flag, daca flag = 1 a citit mesaj nou -> actualizeaza textview din db
            // sau sa fie mai rapid il afisam direct aici dar il scriem in bd si atunci la urm initializare o sa citeasca direct din bd dar acuma poate mere mai repede daca scriu direct din arraylist, nu si din bd
            int pos = getSpinnerPosition();
            plantsSpinner.setSelection(pos);

            wateringDateTv.setText(wateredDateDevices.get(0)); // ii mereu 0 ca avem un singur raspi care trimite date
            moistureLevelTv.setText(moistureLevelDevices.get(0)); // ii mereu 0 ca avem un singur raspi care trimite date
            ipAddressTv.setText(devices.get(0)); // la devices ii mereu 0 ca avem un singur device

            //salveaza in baza de date valorile noi

            FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
            String uId = currentFirebaseUser.getUid().toString();

            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("uploads/" + uId + "/" + allPlantEntriesForUser.get(pos-1) + "/");
            databaseRef.child("date").setValue(wateredDateDevices.get(0));
            databaseRef.child("moistureLevel").setValue(moistureLevelDevices.get(0))
                .addOnSuccessListener(new OnSuccessListener < Void > () {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(WaterPlantActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                }
            );

            plantsSpinner.setSelection(pos);
            onPositionReadAndSetValues(pos);
            Log.d("INFO AFTER WRITING IN DB", "info");
        }
    }

    public int getSpinnerPosition(){
        return spinnerPosition;
    }

    public void setSpinnerPosition(int spinnerPosition){
        this.spinnerPosition = spinnerPosition;
    }
}

class NetworkSniffTask extends AsyncTask<Void, Void, List<String>> {
    public AsyncResponse delegate = null;
    private static final String TAG ="nstask";

    private WeakReference<Context> mContextRef;

    public NetworkSniffTask(Context context){
        mContextRef = new WeakReference<Context>(context);
    }

    /* Needs to be done in a different thread than GUI in order to not block it */
    @Override
    protected List<String> doInBackground(Void... voids) {
        Log.d(TAG, "Let's sniff the network");
        List<String> result = new ArrayList<String>();
        //in for sa punem WaterPlantActivity.mProgressBar.setProgress(x); cu if-uri gen i e 125 gen 50% => set 50 si la 25% si la 75% 25 75
        return result;
    }

    /* Thread POST Execution function */
    @Override
    protected void onPostExecute(List<String> result)
    {
        WaterPlantActivity.mProgressBar.setProgress(100);
        WaterPlantActivity.mProgressBar.setVisibility(View.INVISIBLE);
        Log.i("POSTEXEC", "ENTERED POST EXEC");
        for(String r:result)
        {
            Log.i("POSTEXEC", "ADDED: " + r);

        }

        //for testing purposes
        result.add("Host: raspberry(192.168.1.140)---100---water date w/e");

        delegate.processFinish(result);
    }
}