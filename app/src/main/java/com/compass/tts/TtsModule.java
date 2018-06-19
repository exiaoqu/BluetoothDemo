package com.compass.tts;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

//import com.compass.tts.control.InitConfig;
import com.compass.tts.listener.UiMessageListener;
//import com.compass.tts.util.AutoCheck;

import java.io.File;
//import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ezfanbi on 6/15/2018.
 * 语音合成，语音播放
 *
 */

public class TtsModule extends AppCompatActivity {
    private static final String TEXT = "默认语音，请在代码中修改合成文本!\n";
    //    private static final String WELCOME = "欢迎使用爱立信Compass智能语音电子导盲系统!\n\n";
    private static final String TAG = "MainActivity";

    // ================== 初始化参数设置开始 ==========================
    protected String appId = "11189849";
    protected String appKey = "yoLqWgEbzNgKKprVT7iSh9YK";
    protected String secretKey = "cf7fb720d813411714ec85a5429ea75f";

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    private TtsMode ttsMode = TtsMode.ONLINE;

    // ================选择TtsMode.ONLINE  不需要设置以下参数; 选择TtsMode.MIX 需要设置下面2个离线资源文件的路径
    private static final String TEMP_DIR = "/sdcard/baiduTTS"; // 重要！请手动将assets目录下的3个dat 文件复制到该目录

    // 请确保该PATH下有这个文件
    private static final String TEXT_FILENAME = TEMP_DIR + "/" + "bd_etts_text.dat";

    // 请确保该PATH下有这个文件 ，m15是离线男声
    private static final String MODEL_FILENAME =
            TEMP_DIR + "/" + "bd_etts_common_speech_as_mand_eng_high_am_v3.0.0_20170516.dat";

    // ===============初始化参数设置完毕，更多合成参数请至getParams()方法中设置 =================
    protected SpeechSynthesizer mSpeechSynthesizer;

    // =========== 以下为UI部分 ==================================================
    //    private Button mSpeak;
    //    private Button mRecord;
    //    private TextView mShowText;
    protected Handler mainHandler;

    // 初始化
    public TtsModule(){
        //        initPermission();
        initTTs();
    }

    // 下面是android 6.0以上的动态授权
    // android 6.0 以上需要动态申请权限
    //    private void initPermission() {
    //        String[] permissions = {
    //                Manifest.permission.INTERNET,
    //                Manifest.permission.ACCESS_NETWORK_STATE,
    //                Manifest.permission.MODIFY_AUDIO_SETTINGS,
    //                Manifest.permission.WRITE_EXTERNAL_STORAGE,
    //                Manifest.permission.WRITE_SETTINGS,
    //                Manifest.permission.READ_PHONE_STATE,
    //                Manifest.permission.ACCESS_WIFI_STATE,
    //                Manifest.permission.CHANGE_WIFI_STATE
    //        };
    //
    //        ArrayList<String> toApplyList = new ArrayList<String>();
    //
    //        for (String perm : permissions) {
    //            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
    //                toApplyList.add(perm);
    //                // 进入到这里代表没有权限.
    //            }
    //        }
    //        String[] tmpList = new String[toApplyList.size()];
    //        if (!toApplyList.isEmpty()) {
    //            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
    //        }
    //    }

    /**
     * 注意此处为了说明流程，故意在UI线程中调用。
     * 实际集成中，该方法一定在新线程中调用，并且该线程不能结束。具体可以参考NonBlockSyntherizer的写法
     */
    private void initTTs() {
        LoggerProxy.printable(true); // 日志打印在logcat中
        boolean isMix = ttsMode.equals(TtsMode.MIX);
        boolean isSuccess;
        if (isMix) {
            // 检查2个离线资源是否可读
            isSuccess = checkOfflineResources();
            if (!isSuccess) {
                return;
            } else {
                Log.i(TAG,"离线资源存在并且可读, 目录：" + TEMP_DIR);
            }
        }
        // 日志更新在UI中，可以换成MessageListener，在logcat中查看日志
        SpeechSynthesizerListener listener = new UiMessageListener(mainHandler);

        // 1. 获取实例
        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        mSpeechSynthesizer.setContext(this);

        // 2. 设置listener
        mSpeechSynthesizer.setSpeechSynthesizerListener(listener);

        // 3. 设置appId，appKey.secretKey
        int result = mSpeechSynthesizer.setAppId(appId);
        checkResult(result, "setAppId");
        result = mSpeechSynthesizer.setApiKey(appKey, secretKey);
        checkResult(result, "setApiKey");

        // 4. 支持离线的话，需要设置离线模型
        if (isMix) {
            // 检查离线授权文件是否下载成功，离线授权文件联网时SDK自动下载管理，有效期3年，3年后的最后一个月自动更新。
            isSuccess = checkAuth();
            if (!isSuccess) {
                return;
            }
            // 文本模型文件路径 (离线引擎使用)， 注意TEXT_FILENAME必须存在并且可读
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, TEXT_FILENAME);
            // 声学模型文件路径 (离线引擎使用)， 注意TEXT_FILENAME必须存在并且可读
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, MODEL_FILENAME);
        }

        // 5. 以下setParam 参数选填。不填写则默认值生效
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "5");
        // 设置合成的语速，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");

        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        mSpeechSynthesizer.setAudioStreamType(AudioManager.MODE_IN_CALL);

        // x. 额外 ： 自动so文件是否复制正确及上面设置的参数
        Map<String, String> params = new HashMap<>();
        // 复制下上面的 mSpeechSynthesizer.setParam参数
        // 上线时请删除AutoCheck的调用
        if (isMix) {
            params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, TEXT_FILENAME);
            params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, MODEL_FILENAME);
        }
        //        InitConfig initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);
        //        AutoCheck.getInstance(getApplicationContext()).check(initConfig, new Handler() {
        //            @Override
        //            /**
        //             * 开新线程检查，成功后回调
        //             */
        //            public void handleMessage(Message msg) {
        //                if (msg.what == 100) {
        //                    AutoCheck autoCheck = (AutoCheck) msg.obj;
        //                    synchronized (autoCheck) {
        //                        String message = autoCheck.obtainDebugMessage();
        //                        print(message); // 可以用下面一行替代，在logcat中查看代码
        //                        // Log.w("AutoCheckMessage", message);
        //                    }
        //                }
        //            }
        //
        //        });

        // 6. 初始化
        result = mSpeechSynthesizer.initTts(ttsMode);
        checkResult(result, "initTts");

        //7. 初始化显示
        speak(TEXT);
    }


    /**
     * 检查appId ak sk 是否填写正确，另外检查官网应用内设置的包名是否与运行时的包名一致。本demo的包名定义在build.gradle文件中
     *
     * @return
     */
    private boolean checkAuth() {
        AuthInfo authInfo = mSpeechSynthesizer.auth(ttsMode);
        if (!authInfo.isSuccess()) {
            // 离线授权需要网站上的应用填写包名。本demo的包名是com.baidu.tts.sample，定义在build.gradle中
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            Log.i(TAG,"【error】鉴权失败 errorMsg=" + errorMsg);
            return false;
        } else {
            Log.i(TAG,"验证通过，离线正式授权文件存在。");
            return true;
        }
    }

    /**
     * 检查 TEXT_FILENAME, MODEL_FILENAME 这2个文件是否存在，不存在请自行从assets目录里手动复制
     *
     * @return
     */
    private boolean checkOfflineResources() {
        String[] filenames = {TEXT_FILENAME, MODEL_FILENAME};
        for (String path : filenames) {
            File f = new File(path);
            if (!f.canRead()) {
                Log.i(TAG,"[ERROR] 文件不存在或者不可读取，请从assets目录复制同名文件到：" + path);
                Log.i(TAG,"[ERROR] 初始化失败！！！");
                return false;
            }
        }
        return true;
    }


    private void checkResult(int result, String method) {
        if (result != 0) {
            Log.i(TAG,"error code :" + result + " method:" + method + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }

    //    private void speak() {
    //        speak(TEXT);
    //    }

    public void speak(String text) {
        if (mSpeechSynthesizer == null) {
            // print("[ERROR], 初始化失败");
            return;
        }
        int result = mSpeechSynthesizer.speak(text);
        //        mShowText.setText("");
        //        print(text);
        checkResult(result, "speak");
    }

    //    private void print(String message) {
    //        Log.i(TAG, message);
    //        mShowText.append(message + "\n");
    //    }

    @Override
    protected void onDestroy() {
        if (mSpeechSynthesizer != null) {
            mSpeechSynthesizer.stop();
            mSpeechSynthesizer.release();
            mSpeechSynthesizer = null;
        }
        super.onDestroy();
    }
}
