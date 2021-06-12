package chs.plantdiary;

import android.content.Context;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

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

import static chs.plantdiary.WaterPlantActivity.devices;

class RefreshDataTask extends AsyncTask<Void, Void, List<String>> {
    public AsyncResponse delegate = null;
    //private static final String TAG ="nstask";
    private WeakReference<Context> mContextRef;

    public RefreshDataTask(Context context){
        mContextRef = new WeakReference<Context>(context);
    }

    /* thread separat ca sa nu blocheze UI */
    @Override
    protected List<String> doInBackground(Void... voids) {
        Log.d("DEVICE", " " + devices.get(0));

        //String ipAddress = deviceIp;
        String ipAddress = devices.get(0);
        List<String> result = new ArrayList<String>();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            Context context = mContextRef.get();

            if (context != null) {
                /* Looping IP addresses starting with prefix */

                InetAddress address = InetAddress.getByName(ipAddress);
                boolean reachable = true;//address.isReachable(10);
                String hostName = address.getCanonicalHostName();
                String name = address.getHostName();

                Socket socket = new Socket();
                /* If the IP is reachable, it means the node exists on the network */
                if (reachable) {
                    /* Handshake */
                    try {
                        Log.i("TAG", "Trying " + ipAddress + "...");

                        /* We need to check if it accepts sockets on port 6666 to see if it's a watering device */
                        //socket = new Socket(String.valueOf(testIp), 6666);
                        socket.connect(new InetSocketAddress(String.valueOf(ipAddress), 6666), 50);
                    }
                    catch (Exception e)
                    {
                        Log.i("TAG", e.getMessage());
                        socket.close();
                    }

                    Log.i("TAG", "Connected!");
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
                    Log.i("TAG", "GET request sent! ");

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
                    Log.i("TAG", "Answer: " + response); // response <- contains the data that the raspi sent to the app
                    dataOutputStream.writeUTF("ACK");

                    socket.close();

                    result.add("Host: " + String.valueOf(name) + "(" + String.valueOf(ipAddress) + ")" + "---" + response);

                    Log.i("TAG", "Host: " + String.valueOf(name) + "(" + String.valueOf(ipAddress) + ") is reachable!");
                }
            }
        } catch (Throwable t) {
            Log.e("TAG", "Well that's not good.", t);
        }

        return result;
    }

    /* metoda POST Execution  */
    @Override
    protected void onPostExecute(List<String> result)
    {

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