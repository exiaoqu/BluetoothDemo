package com.compass.qq;

import android.os.Message;
import android.util.Log;

import com.compass.qq.handler.UIHandler;
import com.compass.qq.tts.TtsModule;

public class QDownLinkMsgHelper {
    private String TAG = QDownLinkMsgHelper.class.getName();
    private QBluetoothDevice device = QBluetoothDevice.getInstance();
    private static QDownLinkMsgHelper instance = new QDownLinkMsgHelper();

    public static QDownLinkMsgHelper getInstance() {
        return instance;
    }

    /**
     * 接收DcsFramework发送过来的指令, 下发给硬件
     */
    public void handleDirective(String command) {

        switch (command) {
            case QInterestPoint.ACTION_CODE_INTEEMITTENT_LAMP: // 间歇灯
            case QInterestPoint.ACTION_CODE_DISTANCE: //测距
            case QInterestPoint.ACTION_CODE_TEMPERATURE: //测温度
            case QInterestPoint.ACTION_CODE_HUMIDITY: // 湿度
                device.sendMessage(command+"#");
                break;
            case QInterestPoint.ACTION_CODE_BLINDGUIDE://导盲模式，需要开启线程 持续发送命令
                enableBlindGuideMode();
                break;
            case QInterestPoint.ACTION_CODE_STOP://停止板子所有行为，停止展示
                disableBlindGuideMode();
                TtsModule.getInstance().speak("已停止");
                break;
            default:
                break;
        }
    }

    private final double referenceDistance = 2.0;
    private double distance = 1.7;
    public void setDistance(double distance){
        this.distance = distance;
    }
    private int triggerBuzzer(){
        if(distance <= referenceDistance ){
            return (int)Math.round(distance*10/referenceDistance);
        }
        return -1;
    }

    /**
     * 导盲模式的定时任务
     */
    private Runnable runnableCode = new Runnable() {
        int sendCommendAccount = 0;
        int triggerBuzzerAccount = 0;
        int i = 0;
        @Override
        public void run() {
            UIHandler.getInstance().postDelayed(this, 100);
            Log.i(TAG, "timer call in main thread");

            // 发送命令计数
            if(sendCommendAccount < 30){
                sendCommendAccount++;
            }else{
                // Distance measurement
                device.sendMessage(QInterestPoint.ACTION_CODE_DISTANCE);
                sendCommendAccount = 0;
            }

            // 触发蜂鸣器计数
            triggerBuzzerAccount = triggerBuzzer();
            if(i < triggerBuzzerAccount){
                i++;
            }else if(-1 != triggerBuzzerAccount){
                // 处理蜂鸣器
                sendMessage(QMsgCode.PLAY_SOUND, null);
                i = 0;
            }

        }
    };

    private void sendMessage(int what, Object obj){
        Message msg = UIHandler.getInstance().obtainMessage();
        msg.what = what;
        msg.obj = obj;
        UIHandler.getInstance().sendMessage(msg);
    }

    private void enableBlindGuideMode() {
//        Message msg = UIHandler.getInstance().obtainMessage();
//        msg.what = QMsgCode.MSG_ARDUINO_TEXT;
//        msg.obj = "盲人模式已开启".getBytes();
//        UIHandler.getInstance().sendMessage(msg);
        sendMessage(QMsgCode.MSG_ARDUINO_TEXT,"盲人模式已开启".getBytes());
        UIHandler.getInstance().post(runnableCode);
    }

    public void disableBlindGuideMode() {
        TtsModule.getInstance().stop();
        Log.d(TAG, "removeCallbacks(), remove the runnableCode.");
        UIHandler.getInstance().removeCallbacks(runnableCode);
    }
}
