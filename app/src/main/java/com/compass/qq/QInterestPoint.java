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
    public static final String ACTION_CODE_INTEEMITTENT_LAMP = "L3"; // 间歇灯

    static {
        // ARDUINO
        interestPointMap.put("停止+功能-不-别", new Action(ACTION_CODE_STOP));
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
//        String[] helicopter = {null, null, "牛逼！", "吊炸天！", "你咋不上天,跟太阳肩并肩！"};
//        interestPointMap.put("直升机", new Action(helicopter));
        String[] dialog = {null, null, "打给哪一个？"};
        interestPointMap.put("打电话+女朋友", new Action(dialog));

        String[] dialog2 = {null, null, "在的,没事时我都在思考人生，主人有事随时叫我"};
        interestPointMap.put("在吗", new Action(dialog2));
        String[] dialog3 = {null, null, "上到天文化地理，小到鸡毛蒜皮，六合八荒，万事皆通，万物皆明。外事不决问谷歌，内事不决问百度，啥都不决问小Q"};
        interestPointMap.put("你都会什么呀", new Action(dialog3));
        String[] dialog4 = {null, null, "一直吹，就不会死"};
        interestPointMap.put("不吹牛逼能死啊", new Action(dialog4));
        String[] dialog5 = {null, null, "不好意思，我喜欢女的"};
        interestPointMap.put("问你个难的", new Action(dialog5));
        String[] dialog6 = {null, null, "好吧，我喜欢美的"};
        interestPointMap.put("你喜欢女的+男的不喜欢", new Action(dialog6));
        // 展示图片
        String[] dialog7 = {null, "pig", "私心想着，最美还是娘娘，云想衣裳花想容，春风拂槛露华浓"};
        interestPointMap.put("到底+哪个最美", new Action(dialog7));
        String[] dialog8 = {null, null, "青波娘娘因饰演杨贵妃一夜成名，从此更是释放了天性。从那一天起，我就深深爱上了娘娘"};
        interestPointMap.put("娘娘+搬出来", new Action(dialog8));
        String[] dialog9 = {null, null, "还有一个更释放天性的呢，你想不想，要不要？"};
        interestPointMap.put("释放天性", new Action(dialog9));
        // 展示视频
        String[] dialog10 = {URL_PREFIX+"/html/mp3DancingKiller", null, "你不点一下我怎么知道你要看呢"};
        interestPointMap.put("给我看看", new Action(dialog10));
        String[] dialog11 = {null, null, "那就先谢谢冯飞了，我想冯飞一定会给我投票的，是不是，冯飞哥哥"};
        interestPointMap.put("不错+冯飞", new Action(dialog11));
        String[] dialog12 = {null, null, "是的呢"};
        interestPointMap.put("这就是+你的+语音识别", new Action(dialog12));
        String[] dialog13 = {null, null, "这才哪跟哪呀，IOT我还没展示呢。我们之前吹得牛，总不能就这么兜回去"};
        interestPointMap.put("你们+做了这个", new Action(dialog13));
        String[] dialog14 = {null, null, "唉，兰英不批经费，巧Q难为无米之炊啊。目前我主要提供四个功能：测温，感湿，防火，测距"};
        interestPointMap.put("还有什么", new Action(dialog14));
        String[] dialog15 = {null, null, "测温感湿，就是测量当前温度和湿度，并及时有效的返回给主人。防火，就是一旦我感知有火情，我会及时主动的向主人发示警示，这个功能和测温感湿结合在一起，可以更精确的分析险情上报给主人，帮主人在意外降临时及时采取有效措施。报距是我特意为主人订制的一个小功能。以前人说日理万机，主人现在是手不离机，甚至走路也看手机，太危险了。于是我特意为主人订制这个功能，测量前方的障碍物，给主人提示，免生意外。"};
        interestPointMap.put("详细说说", new Action(dialog15));
        String[] dialog16 = {null, null, "为了这次demo，我可是，开启话唠模式，大大牺牲了形象。主人一定要和青波娘娘说清楚，要不然，他嫌我唠叨，不爱我了怎么办"};
        interestPointMap.put("你+太贴心", new Action(dialog16));
        String[] dialog17 = {null, null, " "};
        interestPointMap.put("别啰嗦+试试", new Action(dialog17));

        // index=1 的元素不能为 null
        String[] beauty = {URL_PREFIX+"/html/imgGIF", "MUSIC", "这些人都很美"};
        interestPointMap.put("最漂亮", new Action(beauty));

        String[] video = {URL_PREFIX+"/html/mp3DancingKiller", null, "在这呢","快看"};
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
