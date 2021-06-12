package chs.plantdiary;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import chs.plantdiary.AsyncResponse;
import chs.plantdiary.WaterPlantActivity;

class NetworkSniffTask extends AsyncTask<Void, Void, List<String>> {
    public AsyncResponse delegate = null;
    private static final String TAG ="nstask";

    private WeakReference<Context> mContextRef;

    public NetworkSniffTask(Context context){
        mContextRef = new WeakReference<Context>(context);
    }

    /* thread separat ca sa nu blocheze UI */
    @Override
    protected List<String> doInBackground(Void... voids) {
        int progressBarSteps = 0;

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
                //socket.setSoTimeout(500);

                /* Looping IP addresses starting with prefix */
                for (int i = 0; i < 255; i++) {
                    String testIp = prefix + String.valueOf(i);

                    if((i%5) == 0){
                        progressBarSteps++;
                        WaterPlantActivity.mProgressBar.setProgress(progressBarSteps);
                    }

                    InetAddress address = InetAddress.getByName(testIp);
                    boolean reachable = true;//address.isReachable(10);
                    String hostName = address.getCanonicalHostName();
                    String name = address.getHostName();

                    Socket socket = new Socket();
                    /* If the IP is reachable, it means the node exists on the network */
                    if (reachable) {
                        /* Handshake */
                        try {
                            Log.i(TAG, "Trying " + testIp + "...");

                            /* We need to check if it accepts sockets on port 6666 to see if it's a watering device */
                            //socket = new Socket(String.valueOf(testIp), 6666);
                            socket.connect(new InetSocketAddress(String.valueOf(testIp), 6666), 50);
                        }
                        catch (Exception e)
                        {
                            Log.i(TAG, e.getMessage());
                            socket.close();
                            continue;
                        }

                        Log.i(TAG, "Connected!");
                        /* get the output stream from the socket. */
                        OutputStream outputStream = socket.getOutputStream();
                        InputStream inputStream = socket.getInputStream();
                        /* create a data output stream from the output stream so we can send data through it */
                        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                        DataInputStream dataInputStream = new DataInputStream(inputStream);

                        /* Send GET to the device */
                        dataOutputStream.writeUTF("GET");
                        dataOutputStream.flush(); // send the message
                        /* dataOutputStream.close(); // close the output stream when we're done. */
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

    /* metoda POST Execution  */
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
        //result.add("Host: raspberry(192.168.1.140)---100---water date w/e");

        delegate.processFinish(result);
    }
}