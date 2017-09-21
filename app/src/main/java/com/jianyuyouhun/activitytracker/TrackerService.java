package com.jianyuyouhun.activitytracker;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.jianyuyouhun.jmvplib.app.broadcast.LightBroadcast;
import com.jianyuyouhun.jmvplib.app.broadcast.OnGlobalMsgReceiveListener;
import com.jianyuyouhun.jmvplib.utils.Logger;

import java.io.Serializable;

/**
 * 后台服务
 * Created by wangyu on 2017/7/5.
 */

public class TrackerService extends AccessibilityService {
    public static final String TAG = "TrackerService";
    public static final String COMMAND = "COMMAND";
    public static final String COMMAND_OPEN = "COMMAND_OPEN";
    public static final String COMMAND_CLOSE = "COMMAND_CLOSE";
    TrackerWindowManager mTrackerWindowManager;

    private OnGlobalMsgReceiveListener onGlobalMsgReceiveListener = new OnGlobalMsgReceiveListener() {
        @Override
        public void onReceiveGlobalMsg(Message msg) {
            if (msg.what == MsgWhat.RECEIVE_SERVICE_COMMAND.getValue()) {
                Intent intent = (Intent) msg.obj;
                parseCommand(intent);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        LightBroadcast.getInstance().addOnGlobalMsgReceiveListener(onGlobalMsgReceiveListener);
    }

    private void initTrackerWindowManager(){
        if(mTrackerWindowManager == null)
            mTrackerWindowManager = new TrackerWindowManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d(TAG, "onStartCommand");
        initTrackerWindowManager();

        parseCommand(intent);

        return super.onStartCommand(intent, flags, startId);
    }

    private void parseCommand(Intent intent) {
        String command = intent.getStringExtra(COMMAND);
        if(command != null) {
            if (command.equals(COMMAND_OPEN))
                mTrackerWindowManager.addView();
            else if (command.equals(COMMAND_CLOSE))
                mTrackerWindowManager.removeView();
        }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "onAccessibilityEvent: " + event.getPackageName());
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                Message message = new Message();
                message.what = MsgWhat.RECEIVE_INFO_MSG_WHAT.getValue();
                message.obj = new ActivityChangedEvent(event.getPackageName().toString(),
                        event.getClassName().toString());
                LightBroadcast.getInstance().sendMessage(message);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LightBroadcast.getInstance().removeOnGlobalMsgReceiveListener(onGlobalMsgReceiveListener);
        Log.d(TAG, "onDestroy");
    }

    public static class ActivityChangedEvent implements Serializable {
        private final String mPackageName;
        private final String mClassName;

        public ActivityChangedEvent(String packageName, String className) {
            mPackageName = packageName;
            mClassName = className;
        }

        public String getPackageName() {
            return mPackageName;
        }

        public String getClassName() {
            return mClassName;
        }
    }
}
