package chs.plantdiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

    private RelativeLayout waterRL;// wateringdate_layout moisture_layout ipaddress_layout
    private RelativeLayout moistureRL;
    private RelativeLayout ipaddressRL;

    private ArrayList<String> mPlantNames;
    private ArrayList<String> mPlantWateredDates;
    private ArrayList<String> mPlantMoistureLevel;

    private TextView wateringDateTv;
    private TextView moistureLevelTv;
    private TextView ipAddressTv;

    private Button scanDevicesButton;
    private Button refreshButton;
    private List<Plants> mPlants; //lista cu toate entry-urile de plante din baza de date

    private List<String> devices;
    private List<String> moistureLevelDevices;
    private List<String> wateredDateDevices;

    private int spinnerPosition = -1;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waterplants);

        wateringDateTv = findViewById(R.id.watering_date_data);
        wateringDateTv.setText("N/A");// trebuie sa ia valoarea din ce e in baza de date cand aleg din spinner

        moistureLevelTv =findViewById(R.id.moisture_data);
        moistureLevelTv.setText("N/A");

        ipAddressTv = findViewById(R.id.ipaddress_data);
        ipAddressTv.setText("N/A");

        scanDevicesButton = (Button) findViewById(R.id.scan_devices_button);
        refreshButton = (Button) findViewById(R.id.refresh_button);

        mPlantNames = new ArrayList<String>(); //store all plant name
        mPlantWateredDates = new ArrayList<String>(); //store all plant data
        mPlantMoistureLevel = new ArrayList<String>();
        devices = new ArrayList<String>();
        moistureLevelDevices = new ArrayList<String>();
        wateredDateDevices = new ArrayList<String>();

        waterRL = findViewById(R.id.wateringdate_layout);
        moistureRL = findViewById(R.id.moisture_layout);
        ipaddressRL = findViewById(R.id.ipaddress_layout);
        waterRL.setVisibility(View.INVISIBLE);
        moistureRL.setVisibility(View.INVISIBLE);
        ipaddressRL.setVisibility(View.INVISIBLE);

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        String uId = currentFirebaseUser.getUid().toString();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads/" + uId + "/");
        mStorage= FirebaseStorage.getInstance();

        Spinner plantsSpinner = (Spinner) findViewById(R.id.plants_spinner);

        // ia date din baza de date
        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //la fiecare fetch sa curete listele (in caz ca se sterg date)
                mPlantNames.clear();
                mPlantNames.add(0, "Select a plant"); //incepe indexul la 1
                mPlantWateredDates.clear(); // la restul incepe indexul la 0
                mPlantMoistureLevel.clear();

                // aici baga key value din real time database
                for(DataSnapshot postSnapshot : snapshot.getChildren()){
                    String plantName;
                    String plantWaterDate;
                    String plantMoistureLevel;

                    // ia valoarea din key pentru campul plantName si date
                    plantName = postSnapshot.child("plantName").getValue(String.class);
                    plantWaterDate = postSnapshot.child("date").getValue(String.class);
                    plantMoistureLevel = postSnapshot.child("moistureLevel").getValue(String.class);

                    Log.d("TAG", "name " + plantName + " date " + plantWaterDate);

                    mPlantNames.add(plantName);
                    mPlantWateredDates.add(plantWaterDate);
                    mPlantMoistureLevel.add(plantMoistureLevel);
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
                    setSpinnerPosition(0);
                }
                else{
                    setSpinnerPosition(position);
                    //on selecting a spinner
                    waterRL.setVisibility(View.VISIBLE);
                    moistureRL.setVisibility(View.VISIBLE);
                    ipaddressRL.setVisibility(View.VISIBLE);

                    String item = parent.getItemAtPosition(position).toString();

                    //show selected spinner item - debug purpose
                    //Toast.makeText(parent.getContext(), "Selected " + item, Toast.LENGTH_SHORT).show();

                    // update fields with what is in the database ->TO DO  and when reads from raspi -> it needs to be actualized TO DO
                    wateringDateTv.setText(mPlantWateredDates.get(position-1));
                    moistureLevelTv.setText(mPlantMoistureLevel.get(position-1));
                    devices.add(0, "for debug purpose"); //to delete
                    ipAddressTv.setText(devices.get(0));
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
                scanNetwork();//ez csak elinditja
                // maybe put a progress bar? tbd
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
        //send WTR to raspi
    }

    @Override
    public void processFinish(List<String> result) {
        Log.i("ProcessFinish", "ENTERED PROCESSFINISH IN WATER ACTIVITY");
        //devices.clear();

        //devices contains all the device ip addresses that are connected
        for(String s: result){
            Log.i("ProcessFinish", "info " + s);
            String aux =  s.substring(s.indexOf("(")+1, s.indexOf(")"));
            devices.add(aux);

            String[] splitString = s.split("---");
            // splitString[0] contains the first part with Host + device name + ip address
            moistureLevelDevices.add(splitString[1]);
            wateredDateDevices.add(splitString[2]);
        }

        Log.i("DEVICES", "");
        if(devices.isEmpty()){
            Toast.makeText(WaterPlantActivity.this, "No device connected", Toast.LENGTH_SHORT).show();

        } else {
            // debug purpose
            for (String s : devices) {
                Log.i("DEVICES", "device trimmed: " + s);
            }

            // put actual data in textviews
            wateringDateTv.setText(wateredDateDevices.get(0));
            moistureLevelTv.setText(moistureLevelDevices.get(0));
            ipAddressTv.setText(devices.get(0));
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

    private List<String> last_results;

    public NetworkSniffTask(Context context){
        mContextRef = new WeakReference<Context>(context);
    }

    /* Needs to be done in a different thread than GUI in order to not block it */
    @Override
    protected List<String> doInBackground(Void... voids) {
        Log.d(TAG, "Let's sniff the network");
        List<String> result = new ArrayList<String>();
        try {
            Context context = mContextRef.get();

            if (context != null) {
                /* This is all just to get the ip address and it's prefix in order to scan the network */
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                WifiInfo connectionInfo = wm.getConnectionInfo();
                int ipAddress = connectionInfo.getIpAddress();
                String ipString = Formatter.formatIpAddress(ipAddress);


                Log.d(TAG, "activeNetwork: " + String.valueOf(activeNetwork));
                Log.d(TAG, "ipString: " + String.valueOf(ipString));

                String prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1);
                Log.d(TAG, "prefix: " + prefix);

                /* End of IP prefix calculation */

                /* Looping IP addresses starting with prefix */
                for (int i = 0; i < 255; i++) {
                    String testIp = prefix + String.valueOf(i);

                    InetAddress address = InetAddress.getByName(testIp);
                    boolean reachable = address.isReachable(10);
                    String hostName = address.getCanonicalHostName();
                    String name = address.getHostName();

                    Socket socket;

                    /* If the IP is reachable, it means the node exists on the network */
                    if (reachable) {
                        /* Handshake */
                        try {
                            /* We need to check if it accepts sockets on port 6666 to see if it's a watering device */
                            socket = new Socket(String.valueOf(testIp), 6666);
                        }
                        catch (Exception e)
                        {
                            continue;
                        }

                        Log.i(TAG, "Connected!");
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
                        Log.i(TAG, "GET request sent! ");

                        /* Get device response */
                        byte[] response_bytes = new byte[1024];

                        /* Get actual response length */
                        dataInputStream.read(response_bytes);
                        int n;
                        for(n = 0; n < 1024; n++)
                        {
                            if(response_bytes[n] == 0x00)
                            {
                                break;
                            }
                        }

                        /* Copy only actual response */
                        byte[] parsed_response_bytes = new byte[n];

                        for(int ix = 0; ix < n; ix++)
                        {
                            parsed_response_bytes[ix] = response_bytes[ix];
                        }

                        /* Convert byte array of response to String */
                        String response = new String(parsed_response_bytes, StandardCharsets.US_ASCII);

                        /* Optional send an ACK (Acknowledge), doesn't do anything since server doesn't care */
                        Log.i(TAG, "Answer: " + response); // response <- contains the data that the raspi sent to the app
                        dataOutputStream.writeUTF("ACK");

                        socket.close();

                        /* Add all the data that we need to this String list, because this will be passed to the main thread */
                        /* Here IP, DeviceName, LastWateringDate, HumidityLevel should be added */
                        /* Later in POST Execution function the devices should be added to a list with all this information */
                        /* Devices should be added into a database in order to not rescan the network all the time */
                        /* There should be a button for scanning the network */
                        /* Each device from the list should refresh their info periodically or with a button assigned to them */
                        /* Device name should be changeable */
                        result.add("Host: " + String.valueOf(name) + "(" + String.valueOf(testIp) + ")" + "---" + response);

                        Log.i(TAG, "Host: " + String.valueOf(name) + "(" + String.valueOf(testIp) + ") is reachable!");
                    }

                }
            }

        } catch (Throwable t) {
            Log.e(TAG, "Well that's not good.", t);
        }

        Log.i(TAG, "RETURNING.........................................");

        return result;
    }

    /* Thread POST Execution function */
    @Override
    protected void onPostExecute(List<String> result)
    {

        Log.i("POSTEXEC", "ENTERED POST EXEC");
        for(String r:result)
        {
            Log.i("POSTEXEC", "ADDED: " + r);

        }

        delegate.processFinish(result);
    }
}