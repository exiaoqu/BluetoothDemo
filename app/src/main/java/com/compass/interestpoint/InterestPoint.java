package com.compass.interestpoint;


/**
 * Created by ezfanbi on 6/12/2018.
 *
 * 单例，兴趣点集合
 */

public enum InterestPoint {
    TEMPERATURE("小强", Constants.MODEL_ARDUINP),    // arduino处理模式
    HUMIDITY("魔镜", Constants.MODEL_DIALOGUE);      // 对话模式

    private String keyWord;
    private String model;

    InterestPoint(String keyWord, String model) {
        this.keyWord = keyWord;
        this.model = model;
    }

    public String getKeyWord(){
        return keyWord;
    }

    public String getModel(){
        return model;
    }

}
