package com.jianyuyouhun.activitytracker.app;

import com.jianyuyouhun.activitytracker.BuildConfig;
import com.jianyuyouhun.jmvplib.app.JApp;
import com.jianyuyouhun.jmvplib.mvp.BaseJModel;

import java.util.List;

/**
 * application
 * Created by wangyu on 2017/7/5.
 */

public class App extends JApp {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void initModels(List<BaseJModel> models) {

    }

    @Override
    public boolean setIsDebug() {
        return BuildConfig.DEBUG;
    }
}
