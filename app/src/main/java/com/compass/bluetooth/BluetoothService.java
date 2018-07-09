package com.compass.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.compass.interestpoint.Constants;
import com.compass.tts.BluetoothHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


/**
 * Created by ezfanbi on 6/27/2018.
 * Bluetooth Communication Service
 */
public class BluetoothService extends Service {
    private static final String TAG = "BluetoothService";

    private static final UUID UUID_SERIAL_PORT_SERVICE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String TARGET_DEVICE_ADDRESS = "AB:03:56:78:C1:3A"; // 目标蓝牙设备

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

    private void handleUplinkData(String words) {

        Log.i(TAG, String.format("receiving from Arduino: %s", words));

        String[] args = words.split("_");
        if (args.length > 1) {
            String command = args[0].toUpperCase();
            double data = Double.valueOf(args[1]);

            // 不同的模式展示不同的Text
            String showText = null;

            switch (command) {
                case Constants.COMMEAND_DISTANCE:
                    showText = "距离" + (double) Math.round(data) / 100 + "米";
                    break;
                case Constants.COMMEAND_HUMIDITY:
                    break;
                case Constants.COMMEAND_TEMPERATURE:
                    break;
            }

            if (showText != null && !showText.isEmpty()) {
                Message msg = BluetoothHandler.getInstance().obtainMessage();
                // Message msg = new Message();
                msg.what = Constants.WEBSHOW_TEXT;
                msg.obj = showText.getBytes();
                BluetoothHandler.getInstance().sendMessage(msg);
            }
        }
    }

    private class BluetoothThread extends Thread {

        @Override
        public void run() {
            Log.i(TAG, "BEGIN BluetoothThread");
            setName("BluetoothThread");

            BluetoothSocket socket = null;
            while (true) {

                // 从绑定的设备列表中查找目标设备
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                adapter.enable();

                Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
                for (BluetoothDevice device : bondedDevices) {
                    if (TARGET_DEVICE_ADDRESS.equals(device.getAddress())) {
                        BluetoothDevice targetDevice = adapter.getRemoteDevice(TARGET_DEVICE_ADDRESS);
                        socket = getConnectedSocket(targetDevice);
                    }
                }

                if (socket != null) {
                    handleConnectedSocket(socket);

                    try {
                        socket.close();
                    } catch (IOException ex) {
                        Log.e(TAG, "close() failed", ex);
                    }

                    continue;
                }

                // sleep 5 seconds
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Log.i(TAG, "Thread.sleep() interrupted", e);
                }
            }

        }

        private BluetoothSocket getConnectedSocket(BluetoothDevice targetDevice) {
            BluetoothSocket socket;

            try {
                socket = targetDevice.createRfcommSocketToServiceRecord(UUID_SERIAL_PORT_SERVICE);
            } catch (IOException e) {
                Log.e(TAG, "createRfcommSocketToServiceRecord() failed", e);
                return null;
            }

            try {
                socket.connect();
            } catch (IOException e) {
                Log.e(TAG, "connect() failed", e);
                closeConnectedSocket(socket);
                return null;
            }

            return socket;
        }

        private void handleConnectedSocket(BluetoothSocket socket) {
            InputStream is;
            OutputStream os;
            try {
                is = socket.getInputStream();
                os = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "get streams failed", e);
                closeConnectedSocket(socket);
                return;
            }

            BluetoothHandler.getInstance().setOutputStream(os);

            BufferedReader bf = new BufferedReader(new InputStreamReader(is));

            // 持续监听
            while (true) {
                try {
                    String words = bf.readLine();
                    // 直接将 words 交给情景模型 SituationalModule 处理
                    if (words != null && words.length() > 0) {
                        handleUplinkData(words);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "connection lost", e);

                    BluetoothHandler.getInstance().setOutputStream(null);
                    closeConnectedSocket(socket);
                    break;
                }
            }
        }

        private void closeConnectedSocket(BluetoothSocket socket) {
            try {
                socket.close();
            } catch (IOException ex) {
                Log.e(TAG, "close() failed", ex);
            }
        }

    }
}
