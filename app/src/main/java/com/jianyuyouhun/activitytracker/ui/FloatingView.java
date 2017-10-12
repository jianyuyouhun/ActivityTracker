package com.jianyuyouhun.activitytracker.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jianyuyouhun.activitytracker.R;
import com.jianyuyouhun.activitytracker.app.CacheKey;
import com.jianyuyouhun.activitytracker.util.SimpleAnimatorListener;
import com.jianyuyouhun.activitytracker.app.MsgWhat;
import com.jianyuyouhun.activitytracker.app.TrackerService;
import com.jianyuyouhun.inject.ViewInjector;
import com.jianyuyouhun.inject.annotation.FindViewById;
import com.jianyuyouhun.jmvplib.app.broadcast.LightBroadcast;
import com.jianyuyouhun.jmvplib.app.broadcast.OnGlobalMsgReceiveListener;
import com.jianyuyouhun.jmvplib.mvp.model.CacheModel;
import com.jianyuyouhun.jmvplib.utils.injecter.model.Model;
import com.jianyuyouhun.jmvplib.utils.injecter.model.ModelInjector;

/**
 *
 * Created by wangyu on 2017/7/5.
 */

public class FloatingView extends LinearLayout implements OnGlobalMsgReceiveListener {

    @FindViewById(R.id.tv_package_name)
    private TextView mTvPackageName;
    @FindViewById(R.id.tv_class_name)
    private TextView mTvClassName;
    @FindViewById(R.id.iv_close)
    private ImageView mIvClose;
    @FindViewById(R.id.tv_toggle)
    private TextView mToggleBtn;
    @FindViewById(R.id.package_layout)
    private LinearLayout mPackageLayout;

    @Model
    private CacheModel mCacheModel;

    private int expandX;
    private int expandY;
    private int collapseX;
    private int collapseY;

    private boolean isExpand = true;//是否展开
    private boolean isDoingAnimator = false;

    private final Context mContext;
    private final WindowManager mWindowManager;

    public FloatingView(Context context) {
        this(context, null);
    }

    public FloatingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        initView();
    }

    private void initView() {
        ModelInjector.injectModel(this);
        inflate(mContext, R.layout.layout_floating, this);
        ViewInjector.inject(this, this);
        registerListener();
        initPoint();
    }

    private void initPoint() {
        expandX = mCacheModel.getInt(CacheKey.LAST_E_X);
        expandY = mCacheModel.getInt(CacheKey.LAST_E_Y);
        collapseX = mCacheModel.getInt(CacheKey.LAST_C_X);
        collapseY = mCacheModel.getInt(CacheKey.LAST_C_Y);
    }

    private void registerListener() {
        mIvClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "关闭悬浮框", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext, TrackerService.class);
                intent.putExtra(TrackerService.COMMAND, TrackerService.COMMAND_CLOSE);
                Message message = Message.obtain();
                message.what = MsgWhat.RECEIVE_SERVICE_COMMAND.getValue();
                message.obj = intent;
                LightBroadcast.getInstance().sendMessage(message);
            }
        });
        mToggleBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExpand) {
                    collapse();
                } else {
                    expand();
                }
            }
        });
    }

    AnimatorSet animSet = null;

    /**
     * 折叠
     */
    private void collapse() {
        if (animSet != null) {
            return;
        }
        animSet = new AnimatorSet();
        isDoingAnimator = true;
        final WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) this.getLayoutParams();
        expandX = layoutParams.x;
        expandY = layoutParams.y;
        int changeX = expandX;
        int changeY = expandY;
        ObjectAnimator packageAnimator = ObjectAnimator.ofFloat(mPackageLayout, "translationX", 0, -mPackageLayout.getWidth());
        ValueAnimator xAnimator = ValueAnimator.ofInt(changeX, collapseX);
        xAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                layoutParams.x = (Integer) animation.getAnimatedValue();
                mWindowManager.updateViewLayout(FloatingView.this, layoutParams);
            }
        });
        ValueAnimator yAnimator = ValueAnimator.ofInt(changeY, collapseY);
        yAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                layoutParams.y = (Integer) animation.getAnimatedValue();
                mWindowManager.updateViewLayout(FloatingView.this, layoutParams);
            }
        });
        animSet.playTogether(packageAnimator, xAnimator, yAnimator);
        animSet.addListener(new SimpleAnimatorListener(){
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animSet = null;
                isDoingAnimator = false;
                isExpand = false;
                mPackageLayout.setVisibility(GONE);
                mToggleBtn.setText("开");
            }
        });
        animSet.setDuration(500);
        animSet.start();

    }

    /**
     * 展开
     */
    private void expand() {
        if (animSet != null) {
            return;
        }
        animSet = new AnimatorSet();
        isDoingAnimator = true;
        mPackageLayout.setVisibility(VISIBLE);
        final WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) this.getLayoutParams();
        collapseX = layoutParams.x;
        collapseY = layoutParams.y;
        int changedX = layoutParams.x;
        int changedY = layoutParams.y;
        ObjectAnimator packageAnimator = ObjectAnimator.ofFloat(mPackageLayout, "translationX", -mPackageLayout.getWidth(), 0);
        ValueAnimator xAnimator = ValueAnimator.ofInt(changedX, expandX);
        xAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                layoutParams.x = (Integer) animation.getAnimatedValue();
                mWindowManager.updateViewLayout(FloatingView.this, layoutParams);
            }
        });
        ValueAnimator yAnimator = ValueAnimator.ofInt(changedY, expandY);
        yAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                layoutParams.y = (Integer) animation.getAnimatedValue();
                mWindowManager.updateViewLayout(FloatingView.this, layoutParams);
            }
        });
        animSet.playTogether(packageAnimator, xAnimator, yAnimator);
        animSet.addListener(new SimpleAnimatorListener(){
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animSet = null;
                isDoingAnimator = false;
                isExpand = true;
                mToggleBtn.setText("收");
            }
        });
        animSet.setDuration(500);
        animSet.start();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        LightBroadcast.getInstance().addOnGlobalMsgReceiveListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mCacheModel.putInt(CacheKey.LAST_E_X, expandX);
        mCacheModel.putInt(CacheKey.LAST_E_Y, expandY);
        mCacheModel.putInt(CacheKey.LAST_C_X, collapseX);
        mCacheModel.putInt(CacheKey.LAST_C_Y, collapseY);
        LightBroadcast.getInstance().removeOnGlobalMsgReceiveListener(this);
    }

    Point preP, curP;
    Point dispatchP, disCurP;
    private boolean isInterceptTouchEvent = false;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dispatchP = new Point((int)ev.getRawX(), (int)ev.getRawY());
                break;
            case MotionEvent.ACTION_MOVE:
                disCurP = new Point((int)ev.getRawX(), (int)ev.getRawY());
//                Logger.d("wy", "disCurP " + disCurP.x + "   "+ disCurP.y);
                int range = (int) Math.sqrt(Math.pow(dispatchP.x - disCurP.x, 2) +  Math.pow(dispatchP.y - disCurP.y, 2));
                if (range < 50) {
                    isInterceptTouchEvent = false;
                } else {
                    isInterceptTouchEvent = true;
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isDoingAnimator) {//处于动画效果时没有触摸事件
            return super.onTouchEvent(event);
        }
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                preP = new Point((int)event.getRawX(), (int)event.getRawY());
                break;

            case MotionEvent.ACTION_MOVE:
                curP = new Point((int)event.getRawX(), (int)event.getRawY());
                if (preP == null) {
                    preP = curP;
                }
//                Logger.d("wy", "curP " + curP.x + "   "+ curP.y);
                int dx = curP.x - preP.x,
                        dy = curP.y - preP.y;

                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) this.getLayoutParams();
                if (isExpand) {//展开状态才会更新x位置
                    layoutParams.x += dx;
                }
                layoutParams.y += dy;
                mWindowManager.updateViewLayout(this, layoutParams);
                if (isExpand) {
                    expandX = layoutParams.x;
                    expandY = layoutParams.y;
                } else {
                    collapseX = layoutParams.x;
                    collapseY = layoutParams.y;
                }
                preP = curP;
                break;
        }
        return false;
    }

    @Override
    public void onReceiveGlobalMsg(Message msg) {
        if (msg.what == MsgWhat.RECEIVE_INFO_MSG_WHAT.getValue()) {
            TrackerService.ActivityChangedEvent event = (TrackerService.ActivityChangedEvent) msg.obj;
            String packageName = event.getPackageName(),
                    className = event.getClassName();

            mTvPackageName.setText(packageName);
            mTvClassName.setText(
                    className.startsWith(packageName)?
                            className.substring(packageName.length()):
                            className
            );
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isInterceptTouchEvent;
    }
}
