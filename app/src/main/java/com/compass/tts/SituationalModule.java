package com.compass.tts;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.duer.dcs.systeminterface.IWebView;
import com.compass.interestpoint.ArduinoDealEnum;
import com.compass.interestpoint.Constants;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by ezfanbi on 7/3/2018.
 * <p>
 * 主要功能：
 * 1、处理 DcsFramework 发来的指令；
 * 2、与 BluetoothService 通信，收、发数据；
 * 3、数据处理，并展示
 */
public class SituationalModule {

    private final String TAG = "SituationalModule";

    // 静态页面展示，包含html的基本信息
    private final String htmlPrefix = "<html><head><meta charset=\"utf-8\"><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"><script src=\"http://duer.bdstatic.com/saiya/dcsview/main.e239b3.js\"></script><style></style></head><body>\n" +
            "<div id=\"display\">" +
            "<section data-from=\"server\" class=\"head p-box-out\">" +
            "<div class=\"avatar\"></div>" +
            "<div class=\"bubble-container\">" +
            "<div class=\"bubble p-border text\">" +
            "<div class=\"text-content text\"> ";
    private final String htmlSuffix = "</div></div></div></section></div>\n";

    //
    private IWebView webView;
    //    private Handler mHandler = new Handler();
    private Handler mHandler;

    private OutputStream outputStream;

    public boolean isBreathingLightGreen() {
        return (this.outputStream != null);
    }

    public synchronized void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public synchronized void clearOutputStream() {
        this.outputStream = null;
    }

    private synchronized void sendDownlinkMessage(String message) {
        try {
            if (outputStream != null) {
                outputStream.write(message.getBytes());
            }
        } catch (IOException e) {
            Log.e(TAG, "send downlink message failed", e);
        }
    }

    // 构造
    private static SituationalModule instance = new SituationalModule();

    private SituationalModule() {
    }

    public void setWebView(IWebView webView) {
        this.webView = webView;
    }

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public static SituationalModule getInstance() {
        return instance;
    }

    /**
     * 接收 DcsFramework 发送过来的指令
     */
    private String continuedCommand = null;

    public void dealCommand(String command) {

        switch (command) {
            case Constants.COMMEAND_DISTANCE://测距
            case Constants.COMMEAND_HUMIDITY://测湿度
            case Constants.COMMEAND_TEMPERATURE://测温度
                sendDownlinkMessage(command);
                break;
            case Constants.COMMEAND_BLIND://导盲模式，需要开启线程 持续发送命令
                startBlind();
                break;
            case Constants.COMMEAND_STOP://停止板子所有行为，停止展示
                stop();
                speak("已停止");
                break;
        }//end switch
    }

    /**
     * 开启导盲模式,
     */
    private void startBlind() {
        show(ArduinoDealEnum.BLIND.getKeyWord() + "已开启");
        this.continuedCommand = ArduinoDealEnum.DISTANCE.getCommand();
        startContinueSend();
    }

    /**
     * Create the Handler object (on the main thread by default)
     */
    private boolean isContinueSend = false;
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(this, 2000);

            Log.d("Handlers", "Called on main thread");
            sendDownlinkMessage(continuedCommand);
        }
    };

    private void startContinueSend() {
        // Start the initial runnable task by posting through the handler
        if (!isContinueSend) {
            mHandler.post(runnableCode);
            isContinueSend = true;
        }
    }

    private void stopContinueSend() {
        // Removes pending code execution
        if (isContinueSend) {
            mHandler.removeCallbacks(runnableCode);
            isContinueSend = false;
        }
    }


    /**
     * 实时处理板子发过来的数据
     */
    public void dealData(String words) {
        String[] commandAndData = words.split("_");
        Log.i(TAG, String.format("receiving from Arduino: %s", words));
        if(commandAndData.length > 1) {
            double data = Double.valueOf(commandAndData[1]);

            // 不同的模式展示不同的Text
            String showText = null;

            switch (commandAndData[0].toUpperCase()) {
                case Constants.COMMEAND_DISTANCE:
                    showText = (double) Math.round(data) / 100 + "米";
                    break;
                case Constants.COMMEAND_HUMIDITY:
                    break;
                case Constants.COMMEAND_TEMPERATURE:
                    break;
            }

            show(showText);
        }

    }

    private void show(String showText) {
//        mHandler.obtainMessage(Constants.WEBSHOW_TEXT,showText).sendToTarget();
//        mHandler.obtainMessage(Constants.WEBSHOW_TEXT,showText.getBytes()).sendToTarget();
        speak(showText);

        Message msg = new Message();
        msg.what = Constants.WEBSHOW_TEXT;
        msg.obj = showText.getBytes();
        mHandler.sendMessage(msg);
    }

    public void showView(String interestedText) {
        Log.d(TAG, "showView: " + interestedText);
        String html = htmlPrefix + interestedText + htmlSuffix;
        webView.loadData(html, "text/html; charset=UTF-8", null);
    }

    private void speak(String words) {
        TtsModule.getInstance().speak(words);
    }

    public void stop() {
        stopContinueSend();
        TtsModule.getInstance().stop();
    }

}
