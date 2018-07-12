package com.compass.qq.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.compass.qq.QBluetoothDevice;

/**
 * Created by ezfanbi on 6/27/2018.
 * Bluetooth Communication Service
 */
public class BluetoothService extends Service {
    private static final String TAG = "BluetoothService";
    private BluetoothThread bluetoothThread = new BluetoothThread();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate()");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand()");
        bluetoothThread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        bluetoothThread.interrupt();
        super.onDestroy();
    }

    private class BluetoothThread extends Thread {
        @Override
        public void run() {
            Log.i(TAG, "BEGIN BluetoothThread");
            setName("BluetoothThread");
            QBluetoothDevice.getInstance().startListening();
        }
    }
}
