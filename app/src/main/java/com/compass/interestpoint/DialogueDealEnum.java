package com.compass.interestpoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ezfanbi on 7/3/2018.
 */

public enum DialogueDealEnum {
    INSTANCE;

    Map<String, List<String>> points = null;
    DialogueDealEnum(){
        points = new HashMap();

        List list = new ArrayList();
        list.add("牛逼！");
        list.add("吊炸天！");
        list.add("你咋不上天,跟太阳肩并肩！");
        addPoint("直升机",list);

        List list2 = new ArrayList();
        list2.add("就是你！");
        addPoint("最漂亮",list2);
    }

    public void addPoint(String key, List list){
        points.put(key,list);
    }

    public void removePoint(String key){
        points.remove(key);
    }

    public Map<String, List<String>> getPoints(){
        return points;
    }
}
