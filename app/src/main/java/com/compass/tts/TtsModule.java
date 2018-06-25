package com.compass.tts;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ezfanbi on 6/15/2018.
 * 语音合成，播放
 *
 */
public class TtsModule implements SpeechSynthesizerListener{
    private static final String TAG = "TtsModule";

    // ================== 初始化参数设置开始 ==========================
    private String appId = "11189849";
    private final String appKey = "yoLqWgEbzNgKKprVT7iSh9YK";
    private final String secretKey = "cf7fb720d813411714ec85a5429ea75f";
    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    // 离线时只支持2种发音, 离线时只有普通女声和普通男声。即无特别男声、度逍遥和度丫丫
    private TtsMode ttsMode = TtsMode.ONLINE;

    private String mSampleDirPath;
    private final String SAMPLE_DIR_NAME = "baiduTTS";
    // m15 离线男声 / f7 离线女声 / yyjw 度逍遥 / as 度丫丫
    private final String SPEECH_FEMALE_MODEL_NAME = "bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat";
    private final String SPEECH_YYJW_MODEL_NAME = "bd_etts_common_speech_yyjw_mand_eng_high_am-mix_v3.0.0_20170512.dat";
    private final String TEXT_MODEL_NAME = "bd_etts_text.dat";

    private String MODEL_FILENAME;
    private String TEXT_FILENAME;
    private String YYJW_MODEL_NAME;
    private SpeechSynthesizer mSpeechSynthesizer=null;
    private Context context;
    // volatile保证了多线程访问时instance变量的可见性，避免了instance初始化时其他变量属性还没赋值完时，被另外线程调用
    private static volatile TtsModule instance;

    private TtsModule(Context context)
    {
        this.context = context;
        initTTs();
    }
    public static synchronized void initializeInstance(Context context) {
        if (null == instance) {
            instance = new TtsModule(context);
        }
    }
    public static TtsModule getInstance() {
        return instance;
    }

    /**
     * 可以测试离线合成功能，首次使用请联网
     * 其中initTTS方法需要在新线程调用，否则引起UI阻塞
     * 纯在线请修改代码里ttsMode为TtsMode.ONLINE， 没有纯离线
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
                Log.i(TAG,"离线资源存在并且可读, 目录：" + mSampleDirPath);
            }
        }

        // 1. 获取实例
        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        mSpeechSynthesizer.setContext(context);

        // 2. 设置appId，appKey.secretKey
        int result = mSpeechSynthesizer.setAppId(appId);
        checkResult(result, "setAppId");
        result = mSpeechSynthesizer.setApiKey(appKey, secretKey);
        checkResult(result, "setApiKey");

        // 3. 支持离线的话，需要设置离线模型
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

        // 4. 以下setParam 参数选填。不填写则默认值生效
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "3");
        // 设置合成的音量，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "5");
        // 设置合成的语速，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);

        // 5. 额外 ： 自动so文件是否复制正确及上面设置的参数
        Map<String, String> params = new HashMap<>();
        if (isMix) {
            params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, TEXT_FILENAME);
            params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, MODEL_FILENAME);
        }

        // 6. 初始化
        result = mSpeechSynthesizer.initTts(ttsMode);
        checkResult(result, "initTts");
    }

    /**
     * 检查appId ak sk 是否填写正确，另外检查官网应用内设置的包名是否与运行时的包名一致。本demo的包名定义在build.gradle文件中
     *
     * @return
     */
    private boolean checkAuth() {
        AuthInfo authInfo = mSpeechSynthesizer.auth(ttsMode);
        if (!authInfo.isSuccess()) {
            // 离线授权需要网站上的应用填写包名
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            Log.e(TAG,"【error】鉴权失败 errorMsg=" + errorMsg);
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
        if (mSampleDirPath == null) {
            String sdcardPath = Environment.getExternalStorageDirectory().toString();
            mSampleDirPath = sdcardPath + "/" + SAMPLE_DIR_NAME;
        }
        File file = new File(mSampleDirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        TEXT_FILENAME = mSampleDirPath + "/" + TEXT_MODEL_NAME;
        MODEL_FILENAME = mSampleDirPath + "/" + SPEECH_FEMALE_MODEL_NAME;
        YYJW_MODEL_NAME = mSampleDirPath + "/" + SPEECH_YYJW_MODEL_NAME;
        copyFromAssetsToSdcard(false, SPEECH_FEMALE_MODEL_NAME, MODEL_FILENAME);
        copyFromAssetsToSdcard(false, SPEECH_YYJW_MODEL_NAME, YYJW_MODEL_NAME);
        copyFromAssetsToSdcard(false, TEXT_MODEL_NAME, TEXT_FILENAME);
        String[] filenames = {TEXT_FILENAME, MODEL_FILENAME};
        for (String path : filenames) {
            File f = new File(path);
            if (!f.canRead()) {
                Log.e(TAG,"[ERROR] 初始化失败！！！文件不存在或者不可读取，请从assets目录复制同名文件到：" + path);
                return false;
            }
        }
        return true;
    }

    public void speak(String msg) {
        /* 以下参数每次合成时都可以修改
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
         *  设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "5"); 设置合成的音量，0-9 ，默认 5
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5"); 设置合成的语速，0-9 ，默认 5
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5"); 设置合成的语调，0-9 ，默认 5
         *
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
         *  MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
         *  MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
         *  MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
         *  MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
         */

        if (mSpeechSynthesizer == null) {
            Log.e(TAG,"[ERROR] 初始化失败");
            return;
        }
        int result = mSpeechSynthesizer.speak(msg);

        checkResult(result, "speak");
    }

//    public void stop() {
//        Log.i(TAG,"停止合成引擎 按钮已经点击");
//        int result = mSpeechSynthesizer.stop();
//        checkResult(result, "stop");
//    }

//    private void print(String message) {
//        Log.i(TAG, message);
//    }

    private void checkResult(int result, String method) {
        if (result != 0) {
            Log.e(TAG,"error code :" + result + " method:" + method + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }

    protected void onDestroy() {
        if (mSpeechSynthesizer != null) {
            mSpeechSynthesizer.stop();
            mSpeechSynthesizer.release();
            mSpeechSynthesizer = null;
            Log.i(TAG,"释放资源成功");
        }
    }

    /**
     * 将工程需要的资源文件拷贝到SD卡中使用（授权文件为临时授权文件，请注册正式授权）
     *
     * @param isCover 是否覆盖已存在的目标文件
     * @param source
     * @param dest
     */
    private void copyFromAssetsToSdcard(boolean isCover, String source, String dest) {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = context.getAssets().open(source);
                String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void onSynthesizeStart(String s) {}
    @Override
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {}
    @Override
    public void onSynthesizeFinish(String s) {}
    @Override
    public void onSpeechStart(String s) {}
    @Override
    public void onSpeechProgressChanged(String s, int i) {}
    @Override
    public void onSpeechFinish(String s) {}
    @Override
    public void onError(String s, SpeechError speechError) {}
}