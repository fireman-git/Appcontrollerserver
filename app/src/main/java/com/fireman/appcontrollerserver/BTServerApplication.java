package com.fireman.appcontrollerserver;

import android.app.Application;

public class BTServerApplication extends Application {
    private String tempValue = "25";

    public synchronized String getTempValue(){
        return tempValue;
    }

    public synchronized void setTempValue(String s){
        tempValue =s;
    }
}
