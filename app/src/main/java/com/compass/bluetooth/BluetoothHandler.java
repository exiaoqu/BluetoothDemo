package com.compass.bluetooth;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.duer.dcs.systeminterface.IWebView;
import com.compass.interestpoint.InterestPointHandler;
import com.compass.tts.TtsModule;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Created by exiaoqu on 2018/7/9.
 * Bluetooth Communication Handler
 */
public class BluetoothHandler extends Handler {
    private static final String TAG = "BluetoothHandler";

    // HTML静态页面基本信息
    private static final String WEB_VIEW_HTML_FORMAT_STRING = "<html>" +
            "<head>" +
            "    <meta charset=\"utf-8\">" +
            "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
            "    <script src=\"http://duer.bdstatic.com/saiya/dcsview/main.e239b3.js\"></script>" +
            "    <style></style>" +
            "</head>" +
            "<body>" +
            "<div id=\"display\">" +
            "    <section data-from=\"server\" class=\"head p-box-out\">" +
            "        <div class=\"avatar\"></div>" +
            "        <div class=\"bubble-container\">" +
            "            <div class=\"bubble p-border text\">" +
            "                <div class=\"text-content text\">%s</div>" +
            "            </div>" +
            "        </div>" +
            "    </section>" +
            "</div>" +
            "</body>" +
            "</html>";


    private IWebView webView;
    private OutputStream outputStream;

    private static BluetoothHandler instance = new BluetoothHandler();
    public static BluetoothHandler getInstance() {
        return instance;
    }
    public void setWebView(IWebView webView) {
        this.webView = webView;
    }

    @Override
    public void handleMessage(Message message) {
        super.handleMessage(message);
        handleUplinkMessage(message);
    }

    private void handleUplinkMessage(Message msg) {
        switch (msg.what) {
            case 1:
                String message = new String((byte[]) msg.obj, Charset.defaultCharset());
                Log.i(TAG, String.format("receiving uplink message: %s", message));

                // 展示
                TtsModule.getInstance().speak(message);
                showInWebView(message);
                break;
            case 2:
                // byte[] writeBuf = (byte[]) msg.obj;
                // String writeMessage = new String((byte[]) msg.obj);
                break;
        }
    }

    private void showInWebView(String message) {
        Log.d(TAG, "showing in WebView: " + message);
        webView.loadData(String.format(WEB_VIEW_HTML_FORMAT_STRING, message), "text/html; charset=UTF-8", null);
    }

    public synchronized void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public synchronized boolean isBreathingLightGreen() {
        return this.outputStream != null;
    }

    /**
     * 给硬件发 message
     */
    private synchronized void handleDownlinkMessage(String message) {
        if (outputStream == null) {
            Log.i(TAG, "send downlink message failed! bluetooth socket closed");
            return;
        }

        try {
            outputStream.write(message.getBytes());
        } catch (IOException e) {
            Log.e(TAG, "send downlink message failed", e);
        }
    }

    /**
     * 接收 DcsFramework 发送过来的指令
     */
    public void sendDownlinkCommand(String command) {

        switch (command) {
            case InterestPointHandler.ACTION_CODE_DISTANCE: //测距
            case InterestPointHandler.ACTION_CODE_HUMIDITY: //测湿度
            case InterestPointHandler.ACTION_CODE_TEMPERATURE: //测温度
                handleDownlinkMessage(command);
                break;
            case InterestPointHandler.ACTION_CODE_BLINDGUIDE://导盲模式，需要开启线程 持续发送命令
                enableBlindGuideMode();
                break;
            case InterestPointHandler.ACTION_CODE_STOP://停止板子所有行为，停止展示
                disableBlindGuideMode();
                TtsModule.getInstance().speak("已停止");
                break;
        } //end switch
    }

    /**
     * 盲人模式的定时任务
     */
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            BluetoothHandler.this.postDelayed(this, 3000);
            Log.i(TAG, "timer call in main thread");

            // Distance measurement
            handleDownlinkMessage(InterestPointHandler.ACTION_CODE_DISTANCE);
        }
    };

    private void enableBlindGuideMode() {
        showInWebView("盲人模式已开启");
        BluetoothHandler.this.post(runnableCode);
    }

    public void disableBlindGuideMode() {
        TtsModule.getInstance().stop();
        BluetoothHandler.this.removeCallbacks(runnableCode);
    }

}
