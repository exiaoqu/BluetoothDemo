package com.compass.interestpoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ezfanbi on 6/12/2018.
 *
 * 单例，兴趣点集合
 */

public enum InterestPoint {
    INSTANCE;

    Map<String, List<String>> points = null;
    InterestPoint(){
        points = new HashMap();

        List list = new ArrayList();
        list.add("牛逼！");
        list.add("吊炸天！");
        list.add("相当拽！");
        list.add("你咋不上天跟太阳肩并肩！");
        addPoint("直升机",list);

        List list2 = new ArrayList();
        list2.add("当前室内湿度为：15");
        list2.add("室内湿度为：15");
        addPoint("湿度",list2);
    }

    // add a point
    public void addPoint(String key, List list){
        points.put(key,list);
    }

    // remove specific point
    public void removePoint(String key, List list){
        points.remove(key);
    }

    public Map<String, List<String>> getPoints(){
        return points;
    }

}
