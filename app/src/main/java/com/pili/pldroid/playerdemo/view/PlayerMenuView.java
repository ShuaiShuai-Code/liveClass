package com.pili.pldroid.playerdemo.view;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pili.pldroid.playerdemo.R;
import com.pili.pldroid.playerdemo.utils.AnimUtils;

public class PlayerMenuView extends RelativeLayout implements View.OnClickListener {
    private Context mContext;
    private Activity activity;
    private LinearLayout mTopContainer;
    private LinearLayout mBottomContainer;
    private RelativeLayout mLeftContainer;
    private RelativeLayout mRightContainer;
    private int mVisiable = View.GONE;
    private Boolean mIsFullScreen = false;//是否横屏

    private TextView img_full, img_share, tv_watch, img_back;

    public PlayerMenuView(Context context) {
        this(context, null);
    }

    public PlayerMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        activity = ((Activity) mContext);
        initView();
    }

    public void setNumber(int number) {
        tv_watch.setText(number + "观看");
    }

    /**
     *
     * @param isHeng 是否横屏，横屏不显示一些东西
     */
    public void setHeng(boolean isHeng) {
        img_full.setVisibility(isHeng ? GONE : VISIBLE);
        img_share.setVisibility(isHeng ? GONE : VISIBLE);
        tv_watch.setVisibility(isHeng ? GONE : VISIBLE);
    }

    private void initView() {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.layout_play_menu, this);
        mTopContainer = rootView.findViewById(R.id.top_container);
        mBottomContainer = rootView.findViewById(R.id.bottom_container);
        mLeftContainer = rootView.findViewById(R.id.left_container);
        mRightContainer = rootView.findViewById(R.id.right_container);

        img_full = rootView.findViewById(R.id.img_full);
        img_share = rootView.findViewById(R.id.img_share);
        tv_watch = rootView.findViewById(R.id.tv_watch);
        img_back = rootView.findViewById(R.id.img_back);


        this.setOnClickListener(this);
        img_full.setOnClickListener(this);
        img_share.setOnClickListener(this);
        tv_watch.setOnClickListener(this);
        img_back.setOnClickListener(this);
    }

    public void setmFullScreen(boolean mIsFullScreen) {
        this.mIsFullScreen = mIsFullScreen;
    }

    @Override
    public void onClick(View v) {
        if (v == this) {
            onClickMySelf();
            return;
        }
        switch (v.getId()) {
            case R.id.img_full: {
                //Toast.makeText(mContext, "全屏", Toast.LENGTH_SHORT).show();
                if (mIsFullScreen) {//全屏
                    if (activity.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }

                } else {
                    if (activity.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }
                }


                break;
            }
            case R.id.img_share: {
                Toast.makeText(mContext, "分享", Toast.LENGTH_SHORT).show();
                //UIUtils.showToast(mContext,"bottom_container clicked");
                break;
            }
            case R.id.tv_watch: {
                //UIUtils.showToast(mContext,"left_container clicked");
                break;
            }
            case R.id.img_back: {
                if (mIsFullScreen) {
                    if (activity.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                } else {
                    ((Activity) mContext).finish();
                }
                break;
            }
        }
    }

    private void onClickMySelf() {
        setVisiable();
        setContainerVisiable();
        if (mVisiable == View.GONE) {
            setOutAnim();
        } else {
            setInAnim();
        }
    }

    private void setVisiable() {
        mVisiable = mVisiable == View.GONE ? View.VISIBLE : View.GONE;
    }

    private void setContainerVisiable() {
        //mTopContainer.setVisibility(mVisiable);
        // mBottomContainer.setVisibility(mVisiable);
        mLeftContainer.setVisibility(mVisiable);
        mRightContainer.setVisibility(mVisiable);
    }

    private void setOutAnim() {
        mTopContainer.setAnimation(AnimUtils.getTopOutAnim(mContext));
        mBottomContainer.setAnimation(AnimUtils.getBottomOutAnim(mContext));
        mLeftContainer.setAnimation(AnimUtils.getLeftOutAnim(mContext));
        mRightContainer.setAnimation(AnimUtils.getRightOutAnim(mContext));
    }

    private void setInAnim() {
        // mTopContainer.setAnimation(AnimUtils.getTopInAnim(mContext));
        // mBottomContainer.setAnimation(AnimUtils.getBottomInAnim(mContext));
        mLeftContainer.setAnimation(AnimUtils.getLeftInAnim(mContext));
        mRightContainer.setAnimation(AnimUtils.getRightInAnim(mContext));
    }

}
