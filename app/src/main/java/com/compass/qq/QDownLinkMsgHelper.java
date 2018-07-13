package com.compass.qq;

import android.os.Message;
import android.util.Log;

import com.compass.qq.handler.UIHandler;
import com.compass.qq.tts.TtsModule;

public class QDownLinkMsgHelper {
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
            case QInterestPoint.ACTION_CODE_DISTANCE: //测距
            case QInterestPoint.ACTION_CODE_TEMPERATURE: //测温度
            case QInterestPoint.ACTION_CODE_HUMIDITY:
                device.sendMessage(command);
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

    /**
     * 导盲模式的定时任务
     */
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            UIHandler.getInstance().postDelayed(this, 3000);
            Log.i(QBluetoothDevice.class.getName(), "timer call in main thread");
            // Distance measurement
            device.sendMessage(QInterestPoint.ACTION_CODE_DISTANCE);
        }
    };

    private void enableBlindGuideMode() {
        Message msg = UIHandler.getInstance().obtainMessage();
        msg.what = 1;
        msg.obj = "盲人模式已开启".getBytes();
        UIHandler.getInstance().sendMessage(msg);
        UIHandler.getInstance().post(runnableCode);
    }

    public void disableBlindGuideMode() {
        TtsModule.getInstance().stop();
        UIHandler.getInstance().removeCallbacks(runnableCode);
    }
}
