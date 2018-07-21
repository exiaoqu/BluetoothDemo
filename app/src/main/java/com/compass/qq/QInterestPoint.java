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
        interestPointMap.put("关闭+功能-不-别", new Action(ACTION_CODE_STOP));

        interestPointMap.put("测+温度-不-别", new Action(ACTION_CODE_TEMPERATURE));
        interestPointMap.put("室内+温度-不-别", new Action(ACTION_CODE_TEMPERATURE));

        interestPointMap.put("测+湿度-不-别", new Action(ACTION_CODE_HUMIDITY));
        interestPointMap.put("室内+湿度-不-别", new Action(ACTION_CODE_HUMIDITY));

        interestPointMap.put("测+距-不-别", new Action(ACTION_CODE_DISTANCE));
        interestPointMap.put("测+距离-不-别", new Action(ACTION_CODE_DISTANCE));
        interestPointMap.put("当前+距离-不-别", new Action(ACTION_CODE_DISTANCE));

        interestPointMap.put("盲人模式-不-别", new Action(ACTION_CODE_BLINDGUIDE));

        // DIALOG
        // 展示图片时，index=1 的元素不能为 null
        // String[] beauty = {URL_PREFIX+"/html/imgGIF", "MUSIC", "这些人都很美"};
        // interestPointMap.put("最漂亮", new Action(beauty));

        String[] dialog = {null, null, "打给哪一个？"};
        interestPointMap.put("打电话+女朋友", new Action(dialog));

        String[] dialog2 = {null, null, "我在的，没事时我都在思考人生，主人有事随时叫我"};
        interestPointMap.put("你在吗", new Action(dialog2));
        String[] dialog3 = {null, null, "上到天文地理，下到鸡毛蒜皮，六合八荒，万事皆通，万物皆明。外事不决问谷歌，内事不决问百度，啥都不决问小Q"};
        interestPointMap.put("都会+什么", new Action(dialog3));
        String[] dialog4 = {null, null, "一直吹，就不会死"};
        interestPointMap.put("吹牛+能死", new Action(dialog4));
        String[] dialog5 = {null, null, "不好意思，我喜欢女的"};
        interestPointMap.put("问你+难的", new Action(dialog5));
        String[] dialog51 = {null, null, "不好意思，我喜欢女的"};
        interestPointMap.put("问你+男的", new Action(dialog51));
        String[] dialog6 = {null, null, "好吧，我喜欢美人儿"};
        interestPointMap.put("喜欢女的", new Action(dialog6));
        // 展示图片
        String[] beauty2 = {URL_PREFIX+"/html/gifPhotoShow", Constants.MUSIC_NJY, "这些人都很美"};
        interestPointMap.put("最美的", new Action(beauty2));
        String[] beauty21 = {URL_PREFIX+"/html/gifPhotoShow", Constants.MUSIC_NJY, "这些人都很美"};
        interestPointMap.put("最漂亮", new Action(beauty21));
        // 图片
        String[] dialog7 = {URL_PREFIX+"/html/pngQingboZhang", Constants.MODEL_NRH, "最美还是娘娘！芙蓉不及美人妆，水殿风来珠翠香。谁分含啼掩秋扇，空悬明月待君王。"};
        interestPointMap.put("到底+最美", new Action(dialog7));
        String[] dialog8 = {null, null, "青波娘娘因饰演杨贵妃一夜成名，从此更是释放了天性。从那一天起，我就深深爱上了娘娘"};
        interestPointMap.put("娘娘+搬出来", new Action(dialog8));
        // 视频
        String[] dialog9 = {URL_PREFIX+"/html/mp3DancingKiller", null, "还有更释放天性的，想不想看？"};
        interestPointMap.put("释放天性", new Action(dialog9));
        String[] dialog11 = {null, null, "那就先谢谢冯飞了，我想冯飞一定会给我投票的，是不是，冯飞哥哥"};
        interestPointMap.put("不错+飞+记住你", new Action(dialog11));
        String[] dialog13 = {null, null, "这才哪跟哪呀，IOT我还没展示呢。我们之前吹得牛，总不能就这么兜回去"};
        interestPointMap.put("你们+做了这个", new Action(dialog13));
        String[] dialog14 = {null, null, "唉，兰英不批经费，巧Q难为无米之炊啊。目前我主要提供四个功能：测温，感湿，防火，测距"};
        interestPointMap.put("还有什么", new Action(dialog14));
        String[] dialog15 = {null, null, "测温感湿，就是测量当前温度和湿度，并及时有效的返回给主人。防火，就是一旦我感知有火情，及时发起警示，这个和测温感湿结合在一起，可以更精确的分析险情。报距是我特意为主人订制的。主人现在是手不离机，甚至走路也看手机，太危险了。于是我特意为主人订制这个功能，测量前方的障碍物，给主人提示，免生意外。"};
        interestPointMap.put("详细说说", new Action(dialog15));
        String[] dialog16 = {null, null, "为了这次展示，我可是开启话唠模式，大大牺牲了形象。主人一定要和青波娘娘说清楚，要不然，他嫌我唠叨，不爱我了怎么办"};
        interestPointMap.put("太贴心", new Action(dialog16));
        String[] dialog17 = {null, null, "好的，来吧"};
        interestPointMap.put("我+试试", new Action(dialog17));
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
