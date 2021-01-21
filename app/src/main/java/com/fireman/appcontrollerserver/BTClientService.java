package com.fireman.appcontrollerserver;


import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class BTClientService extends Service {


    BluetoothSocket bluetoothSocket;
    BluetoothAdapter bluetoothAdapter;
    BTClientThread btClientThread = new BTClientThread();
    static final String TAG = "BTTEST1";

    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            String s;
            s = (String) msg.obj;

            switch(s){
                case "Ynavi":
                    startApp("jp.co.yahoo.android.apps.navi");
                    break;
                case "Amazonmusic":
                    startApp("com.amazon.mp3");
                    break;
                case "Tablog":
                    startApp("com.kakaku.tabelog");
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if( bluetoothAdapter == null ){
            Log.d(TAG, "This device doesn't support Bluetooth.");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // to do something

        btClientThread.start();


        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if(btClientThread != null){
            btClientThread.interrupt();
            btClientThread = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(bluetoothSocket != null){
            try {
                bluetoothSocket.close();
            } catch (IOException e) {}
            bluetoothSocket = null;
        }

        handler.obtainMessage(
                Constants.MESSAGE_BT,
                "DISCONNECTED - Exit BTClientThread")
                .sendToTarget();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class BTClientThread extends Thread {

        InputStream inputStream;
        OutputStream outputStrem;
        BluetoothSocket bluetoothSocket;

        public void run() {

            byte[] incomingBuff = new byte[64];

            BluetoothDevice bluetoothDevice = null;
            Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
            for(BluetoothDevice device : devices){
                if(device.getName().equals(Constants.BT_DEVICE)) {
                    bluetoothDevice = device;
                    break;
                }
            }

            if(bluetoothDevice == null){
                Log.d(TAG, "No device found.");
                return;
            }

            try {

                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(
                        Constants.BT_UUID);

                while(true) {
                    if(Thread.interrupted()){break;}
                    try {
                        bluetoothSocket.connect();

                        handler.obtainMessage(
                                Constants.MESSAGE_BT,
                                "CONNECTED " + bluetoothDevice.getName())
                                .sendToTarget();

                        inputStream = bluetoothSocket.getInputStream();
                        outputStrem = bluetoothSocket.getOutputStream();

                        while (true) {
                            if(Thread.interrupted()){break;}
                            // Send Command
                            String command = "GET:TEMP";
                            outputStrem.write(command.getBytes());
                            // Read Response
                            int incomingBytes = inputStream.read(incomingBuff);
                            byte[] buff = new byte[incomingBytes];
                            System.arraycopy(incomingBuff, 0, buff, 0, incomingBytes);
                            String s = new String(buff, StandardCharsets.UTF_8);

                            // Show Result to UI
                            handler.obtainMessage(
                                    Constants.MESSAGE_TEMP,
                                    s)
                                    .sendToTarget();
                            // Update again in a few seconds
                            Thread.sleep(3000);
                        }

                    } catch (IOException e) {
                        // connect will throw IOException immediately
                        // when it's disconnected.
                        Log.d(TAG, e.getMessage());
                    }

                    handler.obtainMessage(
                            Constants.MESSAGE_BT,
                            "DISCONNECTED")
                            .sendToTarget();

                    // Re-try after 3 sec
                    Thread.sleep(3 * 1000);
                }

            }catch (InterruptedException e){
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            if(bluetoothSocket != null){
                try {
                    bluetoothSocket.close();
                } catch (IOException e) {}
                bluetoothSocket = null;
            }

            handler.obtainMessage(
                    Constants.MESSAGE_BT,
                    "DISCONNECTED - Exit BTClientThread")
                    .sendToTarget();
        }
    }

    private void startApp(String appName){
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(appName);
        startActivity(intent);
    }
}
