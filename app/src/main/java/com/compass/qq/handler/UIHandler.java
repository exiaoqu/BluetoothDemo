package com.compass.qq.handler;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.duer.dcs.androidapp.DcsSampleMainActivity;
import com.baidu.duer.dcs.systeminterface.IWebView;
import com.compass.qq.QMsgCode;
import com.compass.qq.tts.TtsModule;

import java.nio.charset.Charset;

public class UIHandler extends Handler {
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
                Log.i(UIHandler.class.getName(), String.format("receiving uplink message: %s", message));
                TtsModule.getInstance().speak(message);
                showInWebView(message);
                break;
            case QMsgCode.MSG_ARDUINO_LAMP_STATE:
                if(mainActivity != null){
                    mainActivity.updateBreathingLight((Boolean) msg.obj);
                }
                break;
            default:
                break;
        }
    }

    private void showInWebView(String message) {
        Log.d(UIHandler.class.getName(), "showing in WebView: " + message);
        webView.loadData(String.format(WEB_VIEW_HTML_FORMAT_STRING, message), "text/html; charset=UTF-8", null);
    }


    public void setWebView(IWebView webView) {
        this.webView = webView;
    }

    public void setContext(DcsSampleMainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
}
