package com.compass.qq.handler;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.duer.dcs.androidapp.DcsSampleMainActivity;
import com.baidu.duer.dcs.systeminterface.IWebView;
import com.compass.qq.QDownLinkMsgHelper;
import com.compass.qq.QMsgCode;
import com.compass.qq.tts.TtsModule;
import java.nio.charset.Charset;

public class UIHandler extends Handler {

    private String TAG = UIHandler.class.getName();
    // HTML静态页面基本信息
    private static final String WEB_VIEW_HTM_STRING = "<html>" +
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
    private DcsSampleMainActivity mainActivity;
    private static UIHandler instance = new UIHandler();

    public static UIHandler getInstance() {
        return instance;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);

        switch (msg.what) {
            case QMsgCode.MSG_ARDUINO_TEXT:
                String message = new String((byte[]) msg.obj, Charset.defaultCharset());
                Log.i(TAG, String.format("receiving uplink message: %s", message));
                TtsModule.getInstance().speak(message);
                showInWebView(message);
                break;
            case QMsgCode.MSG_ARDUINO_LAMP_STATE:
                if (mainActivity != null) {
                    mainActivity.updateBreathingLight((Boolean) msg.obj);
                }
                break;
            case QMsgCode.PLAY_HONOR:
                if (mainActivity != null) {
                    mainActivity.playHonor();
                }
                break;
            case QMsgCode.PLAY_SOUND:
                if (mainActivity != null) {
                    mainActivity.playSound();
                }
                break;
            case QMsgCode.STOP_HONOR:
                if (mainActivity != null) {
                    mainActivity.stopHonor();
                }
                break;
            default:
                break;
        }
    }

    public void showInWebView(String msg) {
        Log.d(TAG, "showing in WebView: " + msg);
        if(msg.isEmpty()){
            return;
        }
        // 判断是文本还是URL
        if(msg.contains("http")){
            webView.loadUrl(msg.split(" ")[0]);
        }else{
            webView.loadData(String.format(WEB_VIEW_HTM_STRING, msg), "text/html; charset=UTF-8", null);
        }
    }

    public void speak(String msg){
        // 判断是文本还是URL
        if(msg.contains("http")){
            TtsModule.getInstance().speak(msg.split(" ")[1]);
        }else{
            TtsModule.getInstance().speak(msg);
        }
    }

    public void setWebView(IWebView webView) {
        this.webView = webView;
    }

    public void setContext(Context mContext) {
        this.mainActivity = (DcsSampleMainActivity) mContext;
    }
}
