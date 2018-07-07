/*
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.duer.dcs.androidapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.duer.dcs.R;
import com.baidu.duer.dcs.androidsystemimpl.PlatformFactoryImpl;
import com.baidu.duer.dcs.androidsystemimpl.webview.BaseWebView;
import com.baidu.duer.dcs.devicemodule.screen.ScreenDeviceModule;
import com.baidu.duer.dcs.devicemodule.screen.message.RenderVoiceInputTextPayload;
import com.baidu.duer.dcs.devicemodule.voiceinput.VoiceInputDeviceModule;
import com.baidu.duer.dcs.framework.DcsFramework;
import com.baidu.duer.dcs.framework.DeviceModuleFactory;
import com.baidu.duer.dcs.http.HttpConfig;
import com.baidu.duer.dcs.oauth.api.IOauth;
import com.baidu.duer.dcs.oauth.api.OauthImpl;
import com.baidu.duer.dcs.systeminterface.IPlatformFactory;
import com.baidu.duer.dcs.systeminterface.IWakeUp;
import com.baidu.duer.dcs.util.CommonUtil;
import com.baidu.duer.dcs.util.LogUtil;
import com.baidu.duer.dcs.util.NetWorkUtil;
import com.baidu.duer.dcs.wakeup.WakeUp;
import com.compass.bluetooth.BluetoothService;
import com.compass.interestpoint.Constants;
import com.compass.tts.SituationalModule;
import com.compass.tts.TtsModule;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;

/**
 * 主界面 activity
 */
public class DcsSampleMainActivity extends Activity implements View.OnClickListener {
    public static final String TAG = "DcsDemoActivity";

    private ImageView innerBreathingLightImageView;
    private ImageView outerBreathingLightImageView;
    private LinearLayout webViewLinearLayout;
    private BaseWebView webView;
    private TextView voiceInputTextView;
    private Button voiceButton;

    private DcsFramework dcsFramework;
    private DeviceModuleFactory deviceModuleFactory;
    private IPlatformFactory platformFactory;
    private boolean isStopListenReceiving;
    private String mHtmlUrl;
    private WakeUp wakeUp;

    private boolean isBreathingLightGreen = false;

    // 目标蓝牙设备
    private String targetDeviceAddress = "AB:03:56:78:C1:3A";
    private BluetoothAdapter mBtAdapter = null;
    // communicate services
    private BluetoothService mBluetoothService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dcs_sample_activity_main);
        initView();
        initOauth();
        initFramework();

    }

    @Override
    public void onStart() {
        super.onStart();
        // 语音合成模块
        initPermission();
        TtsModule.getInstance().setContext(this);

        // 蓝牙模块
//        mBluetoothService = BluetoothService.getInstance();
//        int stateConnect =  mBluetoothService.connectDevice(targetDeviceAddress);
//        if(BluetoothService.RESULT_CONNECT_Null == stateConnect){
//            showExitDialog(getResources().getString(R.string.dialog_title),getResources().getString(R.string.dialog_message));
//        }

        // 初始化 SituationalModule 处理模型
        SituationalModule.getInstance().setWebView(webView);
        SituationalModule.getInstance().setHandler(mHandler);
    }

    // 消息弹出框
    private void showExitDialog(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton(getResources().getString(R.string.dialog_confirm), null).show();
    }

    // Handler
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constants.WEBSHOW_TEXT:
                    String message = new String((byte[]) msg.obj, Charset.defaultCharset());
                    Log.i("Handler","收到消息["+message+"].");
                    SituationalModule.getInstance().showView(message);
                    break;
                case Constants.WEBSHOW_TEXT_FIGURE:
                    //                    byte[] writeBuf = (byte[]) msg.obj;
                    //                    String writeMessage = new String((byte[]) msg.obj);

                    break;
            }
        }
    };

    private void initView() {
        innerBreathingLightImageView = findViewById(R.id.innerBreathingLightImageView);
        outerBreathingLightImageView = findViewById(R.id.outerBreathingLightImageView);

        // 返回数据WebView显示框
        webViewLinearLayout = findViewById(R.id.webViewLinearLayout);

        // 语音识别显示框
        voiceInputTextView = findViewById(R.id.voiceInputTextView);

        voiceButton = findViewById(R.id.voiceButton);
        voiceButton.setOnClickListener(this);

        createBreathingLight();

        createWebView();
    }

    private void createBreathingLight() {
        GradientDrawable innerGradientDrawable = new GradientDrawable();
        GradientDrawable outerGradientDrawable = new GradientDrawable();

        // 设置边框类型为椭圆
        innerGradientDrawable.setShape(GradientDrawable.OVAL);
        outerGradientDrawable.setShape(GradientDrawable.OVAL);

        isBreathingLightGreen = (new Random()).nextBoolean();
        if (isBreathingLightGreen) {
            // 设置边框的厚度和颜色
            outerGradientDrawable.setStroke(2, Color.rgb(0, 100, 0)); // DARKGREEN
            // 填充背景颜色
            innerGradientDrawable.setColor(Color.rgb(0, 100, 0)); // DARKGREEN
            outerGradientDrawable.setColor(Color.rgb(173, 255, 47)); // GREENYELLOW
        } else {
            // 设置边框的厚度和颜色
            outerGradientDrawable.setStroke(2, Color.RED);
            // 填充背景颜色
            innerGradientDrawable.setColor(Color.RED);
            outerGradientDrawable.setColor(Color.rgb(255, 165, 0)); // ORANGE
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            innerBreathingLightImageView.setBackgroundDrawable(innerGradientDrawable);
            outerBreathingLightImageView.setBackgroundDrawable(outerGradientDrawable);
        } else {
            innerBreathingLightImageView.setBackground(innerGradientDrawable);
            outerBreathingLightImageView.setBackground(outerGradientDrawable);
        }

        //  3.设置动画
        // 外圆从和内圆等大的位置开始缩放,这样好处是他俩的包裹父布局的大小能确定为外圆的大小
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                8 / 18, 1.0f,
                8 / 18, 1.0f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.5f);
        scaleAnimation.setRepeatCount(AnimationSet.INFINITE);
        alphaAnimation.setRepeatCount(AnimationSet.INFINITE);

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setInterpolator(new DecelerateInterpolator());
        animationSet.setFillAfter(false);
        animationSet.setDuration(1555);

        outerBreathingLightImageView.startAnimation(animationSet);
    }

    private void createWebView() {
        webView = new BaseWebView(DcsSampleMainActivity.this.getApplicationContext());
        webView.setWebViewClientListen(new BaseWebView.WebViewClientListener() {
            @Override
            public BaseWebView.LoadingWebStatus shouldOverrideUrlLoading(WebView view, String url) {
                // 不再拦截用户点击
                return BaseWebView.LoadingWebStatus.STATUS_FALSE;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!url.equals(mHtmlUrl) && !"about:blank".equals(mHtmlUrl)) {
                    platformFactory.getWebView().linkClicked(url);
                }

                mHtmlUrl = url;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            }
        });

        webViewLinearLayout.addView(webView);
    }

    private void initFramework() {
        platformFactory = new PlatformFactoryImpl(this);
        platformFactory.setWebView(webView);
        dcsFramework = new DcsFramework(platformFactory);
        deviceModuleFactory = dcsFramework.getDeviceModuleFactory();

        deviceModuleFactory.createVoiceOutputDeviceModule();
        deviceModuleFactory.createVoiceInputDeviceModule();
        deviceModuleFactory.getVoiceInputDeviceModule().addVoiceInputListener(
                new VoiceInputDeviceModule.IVoiceInputListener() {
                    @Override
                    public void onStartRecord() {
                        LogUtil.e(TAG, "onStartRecord");
                        startRecording();
                    }

                    @Override
                    public void onFinishRecord() {
                        LogUtil.e(TAG, "onFinishRecord");
                        stopRecording();
                    }

                    @Override
                    public void onSucceed(int statusCode) {
                        LogUtil.e(TAG, "onSucceed-statusCode:" + statusCode);
                        if (statusCode != 200) {
                            stopRecording();
                            Toast.makeText(DcsSampleMainActivity.this,
                                    getResources().getString(R.string.voice_err_msg),
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onFailed(String errorMessage) {
                        LogUtil.e(TAG, "onFailed-errorMessage:" + errorMessage);
                        stopRecording();
                        Toast.makeText(DcsSampleMainActivity.this,
                                getResources().getString(R.string.voice_err_msg),
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                });

//        deviceModuleFactory.createAlertsDeviceModule();
//        deviceModuleFactory.createAudioPlayerDeviceModule();
//        deviceModuleFactory.getAudioPlayerDeviceModule().addAudioPlayListener(
//                new IMediaPlayer.SimpleMediaPlayerListener() {
//                    @Override
//                    public void onPaused() {
//                        super.onPaused();
//                        pauseOrPlayButton.setText(getResources().getString(R.string.audio_paused));
//                        isPause = true;
//                    }
//
//                    @Override
//                    public void onPlaying() {
//                        super.onPlaying();
//                        pauseOrPlayButton.setText(getResources().getString(R.string.audio_playing));
//                        isPause = false;
//                    }
//
//                    @Override
//                    public void onCompletion() {
//                        super.onCompletion();
//                        pauseOrPlayButton.setText(getResources().getString(R.string.audio_default));
//                        isPause = false;
//                    }
//
//                    @Override
//                    public void onStopped() {
//                        super.onStopped();
//                        pauseOrPlayButton.setText(getResources().getString(R.string.audio_default));
//                        isPause = true;
//                    }
//                });

        deviceModuleFactory.createSystemDeviceModule();
        deviceModuleFactory.createSpeakControllerDeviceModule();
        deviceModuleFactory.createPlaybackControllerDeviceModule();
        deviceModuleFactory.createScreenDeviceModule();
        deviceModuleFactory.getScreenDeviceModule().addRenderVoiceInputTextListener(new ScreenDeviceModule.IRenderVoiceInputTextListener() {
            @Override
            public void onRenderVoiceInputText(RenderVoiceInputTextPayload payload) {
                LogUtil.e("=========payload", payload.text);
                voiceInputTextView.setText(payload.text);
            }
        });
        // init唤醒
        wakeUp = new WakeUp(platformFactory.getWakeUp(), platformFactory.getAudioRecord());
        wakeUp.addWakeUpListener(wakeUpListener);
        // 开始录音，监听是否说了唤醒词
        wakeUp.startWakeUp();
    }

    private IWakeUp.IWakeUpListener wakeUpListener = new IWakeUp.IWakeUpListener() {
        @Override
        public void onWakeUpSucceed() {
            Toast.makeText(DcsSampleMainActivity.this, getResources().getString(R.string.wakeup_succeed), Toast.LENGTH_SHORT).show();
            voiceButton.performClick();
        }
    };

    private void doUserActivity() {
        deviceModuleFactory.getSystemProvider().userActivity();
    }

    private void initOauth() {
        IOauth baiduOauth = new OauthImpl();
        HttpConfig.setAccessToken(baiduOauth.getAccessToken());
    }

    private void stopRecording() {
        wakeUp.startWakeUp();
        isStopListenReceiving = false;
        voiceButton.setText(getResources().getString(R.string.stop_record));
    }

    private void startRecording() {
        // 停止所有语音播报
        SituationalModule.getInstance().stop();
        wakeUp.stopWakeUp();
        isStopListenReceiving = true;
        deviceModuleFactory.getSystemProvider().userActivity();
        voiceButton.setText(getResources().getString(R.string.start_record));
        voiceInputTextView.setText("");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.voiceButton:
                if (!NetWorkUtil.isNetworkConnected(this)) {
                    Toast.makeText(this,
                            getResources().getString(R.string.err_net_msg),
                            Toast.LENGTH_SHORT).show();
                    wakeUp.startWakeUp();
                    return;
                }
                if (CommonUtil.isFastDoubleClick()) {
                    return;
                }
                if (TextUtils.isEmpty(HttpConfig.getAccessToken())) {
                    startActivity(new Intent(DcsSampleMainActivity.this, DcsSampleOAuthActivity.class));
                    finish();
                    return;
                }
                if (!dcsFramework.getDcsClient().isConnected()) {
                    dcsFramework.getDcsClient().startConnect();
                    return;
                }
                if (isStopListenReceiving) {
                    platformFactory.getVoiceInput().stopRecord();
                    isStopListenReceiving = false;
                    return;
                }
                isStopListenReceiving = true;
                platformFactory.getVoiceInput().startRecord();
                doUserActivity();
                break;
            default:
                break;
        }
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
        };

        ArrayList<String> toApplyList = new ArrayList<>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 先remove listener  停止唤醒,释放资源
        wakeUp.removeWakeUpListener(wakeUpListener);
        wakeUp.stopWakeUp();
        wakeUp.releaseWakeUp();

        TtsModule.onDestroy();
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
        if (mBluetoothService != null) {
            mBluetoothService.stop();
        }

        if (dcsFramework != null) {
            dcsFramework.release();
        }
        webView.setWebViewClientListen(null);
        webViewLinearLayout.removeView(webView);
        webView.removeAllViews();
        webView.destroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // WebView浏览的网页可回退
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}