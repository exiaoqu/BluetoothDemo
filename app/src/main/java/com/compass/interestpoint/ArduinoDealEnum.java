package com.compass.interestpoint;

/**
 * Created by ezfanbi on 6/28/2018.
 *
 * 与板子通信，发送命令
 */
public enum ArduinoDealEnum {
    STOP("停止", Constants.COMMEAND_STOP),
    TEMPERATURE("温度", Constants.COMMEAND_TEMPERATURE),  // 测量温度
    HUMIDITY("湿度", Constants.COMMEAND_HUMIDITY),        // 测量湿度
    DISTANCE("距离", Constants.COMMEAND_DISTANCE),        // 测量距离
    BLIND("导盲", Constants.COMMEAND_BLIND);              // 导盲模式

    private String keyWord;
    private String command;

    ArduinoDealEnum(String keyWord, String command) {
        this.keyWord = keyWord;
        this.command = command;
    }

    public String getKeyWord(){
        return keyWord;
    }

    public String getCommand(){
        return command;
    }
}
