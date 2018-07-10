package com.compass.interestpoint;

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

public class InterestPointHandler {
    private static final String TAG = "InterestPointHandler";

    private static final String ACTION_CODE_STOP = "A";
    private static final String ACTION_CODE_TEMPERATURE = "B";
    private static final String ACTION_CODE_HUMIDITY = "C";
    private static final String ACTION_CODE_DISTANCE = "D";
    private static final String ACTION_CODE_BLINDGUIDE = "E";

    private static Map<String, Action> interestPointMap = new HashMap<>();

    static {
        // ARDUINO
        interestPointMap.put("停止", new Action(ACTION_CODE_STOP));
        interestPointMap.put("停+下来", new Action(ACTION_CODE_STOP));


        interestPointMap.put("测+温度", new Action(ACTION_CODE_TEMPERATURE));
        interestPointMap.put("室内+温度", new Action(ACTION_CODE_TEMPERATURE));

        interestPointMap.put("测+湿度", new Action(ACTION_CODE_HUMIDITY));
        interestPointMap.put("室内+湿度", new Action(ACTION_CODE_HUMIDITY));

        interestPointMap.put("测+距", new Action(ACTION_CODE_DISTANCE));
        interestPointMap.put("当前+距离", new Action(ACTION_CODE_DISTANCE));

        interestPointMap.put("导盲", new Action(ACTION_CODE_BLINDGUIDE));
        interestPointMap.put("实时+测距", new Action(ACTION_CODE_BLINDGUIDE));

        // DIALOG
        String[] helicopter = {"牛逼！", "吊炸天！", "你咋不上天,跟太阳肩并肩！"};
        interestPointMap.put("直升机", new Action(helicopter));

        String[] beauty = {"就是你！"};
        interestPointMap.put("直升机", new Action(beauty));
    }

    private Map<Pair<List<String>, List<String>>, Action> filterWordMap = new HashMap<>();
    private InterestPointHandler instance = new InterestPointHandler();

    public InterestPointHandler getInstance() {
        return instance;
    }

    public Action getInterestPointAction(String text) {
        for (Map.Entry<Pair<List<String>, List<String>>, Action> entry : filterWordMap.entrySet()) {
            Pair<List<String>, List<String>> pair = entry.getKey();

            List<String> includeInterestPointList = pair.first;
            List<String> excludeInterestPointList = pair.second;

            boolean flag = true;

            for (String includeInterestPoint : includeInterestPointList) {
                if (!text.contains(includeInterestPoint)) {
                    flag = false;
                    break;
                }
            }

            if (flag) {
                for (String excludeInterestPoint : excludeInterestPointList) {
                    if (text.contains(excludeInterestPoint)) {
                        flag = false;
                        break;
                    }
                }
            }

            if (flag) {
                return entry.getValue();
            }
        }
        return null;
    }

    private InterestPointHandler() {
        for (Map.Entry<String, Action> entry : interestPointMap.entrySet()) {
            Pair<List<String>, List<String>> pair = decodeInterestPoint(entry.getKey());
            filterWordMap.put(pair, entry.getValue());
        }
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
