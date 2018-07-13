package com.compass.qq;

import android.os.Message;
import android.util.Log;

import com.compass.qq.handler.UIHandler;

/**
 * listen to message from hardware and deal it
 */
public class QUpLinkMsgListener implements QMessageListener {

    private String TAG = QUpLinkMsgListener.class.getName();

    public void receiveMsg(String arduinoData) {
        Log.i(TAG, String.format("receiving from Arduino: %s", arduinoData));

        String[] args = arduinoData.split("_");
        String command = args[0].toUpperCase();

        // 不同的模式展示不同的Text
        String showText = null;
        try{
            switch (command) {
                case QInterestPoint.ACTION_CODE_DISTANCE:
                    showText = (double) Math.round(Double.valueOf(args[1])) / 100 + "米";
                    break;
                case QInterestPoint.ACTION_CODE_HUMIDITY:
                    showText = (int) Math.round(Double.valueOf(args[1]))+"%";
                    break;
                case QInterestPoint.ACTION_CODE_TEMPERATURE:
                    showText = Double.valueOf(args[1]) + "℃";
                    break;
                case QInterestPoint.ACTION_CODE_FIRE_ALARM:
                    QDownLinkMsgHelper.getInstance().disableBlindGuideMode();
                    showText = "火警警报";
                    break;
                default:
                    break;
            }
        }catch(Exception ex){
            Log.e(TAG, "exception was thrown when call receiveMsg()");
            ex.printStackTrace();
        }

        if (null != showText) {
            Message msg = UIHandler.getInstance().obtainMessage();
            msg.what = QMsgCode.MSG_ARDUINO_TEXT;
            msg.obj = showText.getBytes();
            UIHandler.getInstance().sendMessage(msg);
        }
    }
}
