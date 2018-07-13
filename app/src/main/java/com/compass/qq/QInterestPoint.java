package com.compass.qq;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by EXIAOQU on 2018/7/9.
 * Interest Point Handler
 */
public class QInterestPoint {
    private final String TAG = "QInterestPoint";

    private Map<Pair<List<String>, List<String>>, Action> filterWordMap = new HashMap<>();
    private static Map<String, Action> interestPointMap = new HashMap<>();

    private static final String URL_PREFIX = "http://47.94.250.178:8080";

    public static final String ACTION_CODE_STOP = "ST";
    public static final String ACTION_CODE_TEMPERATURE = "TE";  // 温度
    public static final String ACTION_CODE_HUMIDITY = "HU";     // 湿度
    public static final String ACTION_CODE_DISTANCE = "DI";     // 距离
    public static final String ACTION_CODE_BLINDGUIDE = "BL";   // 盲人
    public static final String ACTION_CODE_FIRE_ALARM = "FI";   // 火警

    static {
        // ARDUINO
        interestPointMap.put("停止-不-别", new Action(ACTION_CODE_STOP));
        interestPointMap.put("停+下来-不-别", new Action(ACTION_CODE_STOP));

        interestPointMap.put("测+温度-不-别", new Action(ACTION_CODE_TEMPERATURE));
        interestPointMap.put("室内+温度-不-别", new Action(ACTION_CODE_TEMPERATURE));

        interestPointMap.put("测+湿度-不-别", new Action(ACTION_CODE_HUMIDITY));
        interestPointMap.put("室内+湿度-不-别", new Action(ACTION_CODE_HUMIDITY));

        interestPointMap.put("测+距-不-别", new Action(ACTION_CODE_DISTANCE));
        interestPointMap.put("测+距离-不-别", new Action(ACTION_CODE_DISTANCE));
        interestPointMap.put("当前+距离-不-别", new Action(ACTION_CODE_DISTANCE));

        interestPointMap.put("盲人模式-不-别", new Action(ACTION_CODE_BLINDGUIDE));
        interestPointMap.put("实时+测距-不-别", new Action(ACTION_CODE_BLINDGUIDE));

        // DIALOG
        /**
         * /html/imgPNG
         * /html/imgGIF
         * /html/imgVIDEO
         */
        String[] helicopter = {"", "牛逼！", "吊炸天！", "你咋不上天,跟太阳肩并肩！"};
        interestPointMap.put("直升机", new Action(helicopter));

        String[] beauty = {URL_PREFIX+"/html/imgGIF", "是你是你还有你！","是你！","快看看有没有你！"};
        interestPointMap.put("最漂亮", new Action(beauty));

        String[] video = {URL_PREFIX+"/html/imgVIDEO", "在这呢","快看"};
        interestPointMap.put("我们的视频", new Action(video));
    }

    private static QInterestPoint instance = new QInterestPoint();
    public static QInterestPoint getInstance() {
        return instance;
    }

    private QInterestPoint() {
        for (Map.Entry<String, Action> entry : interestPointMap.entrySet()) {
            Pair<List<String>, List<String>> pair = decodeInterestPoint(entry.getKey());
            filterWordMap.put(pair, entry.getValue());
        }
    }

    public Action getInterestPointAction(String words) {
        Log.d(TAG, "getInterestPointAction(), words:[" + words + "]");

        for (Map.Entry<Pair<List<String>, List<String>>, Action> entry : filterWordMap.entrySet()) {
            Pair<List<String>, List<String>> pair = entry.getKey();

            List<String> includeInterestPointList = pair.first;
            List<String> excludeInterestPointList = pair.second;

            boolean flag = true;
            for (String excludeInterestPoint : excludeInterestPointList) {
                if (words.contains(excludeInterestPoint)) {
                    flag = false;
                    break;
                }
            }

            if(!flag){
               continue;
            }

            for (String includeInterestPoint : includeInterestPointList) {
                if (!words.contains(includeInterestPoint)) {
                    flag = false;
                    break;
                }
            }

            if (flag) {
                return entry.getValue();
            }
        }
        return null;
    }

    private Pair<List<String>, List<String>> decodeInterestPoint(String interestPoint) {
        List<String> includeInterestPointList = new ArrayList<>();
        List<String> excludeInterestPointList = new ArrayList<>();

        String[] segments = interestPoint.split("\\+");
        for (String segment : segments) {
            String[] words = segment.split("-");
            includeInterestPointList.add(words[0]);
            for (int i = 1; i < words.length; i++) {
                excludeInterestPointList.add(words[i]);
            }
        }

        return new Pair<>(includeInterestPointList, excludeInterestPointList);
    }

    public static class Action {
        public static final int ACTION_TYPE_ARDUINO = 0;
        public static final int ACTION_TYPE_DIALOG = 1;

        private int actionType;
        private String actionCode;
        private List<String> actionTextList = new ArrayList<>();

        Action(String actionCode) {
            actionType = ACTION_TYPE_ARDUINO;
            this.actionCode = actionCode;
        }

        Action(String[] actionArray) {
            actionType = ACTION_TYPE_DIALOG;
            actionTextList = Arrays.asList(actionArray);
        }

        public int getActionType() {
            return actionType;
        }

        public String getActionCode() {
            return actionCode;
        }

        public List<String> getActionTextList() {
            return actionTextList;
        }
    }
}
