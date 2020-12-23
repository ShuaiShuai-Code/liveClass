package com.pili.pldroid.playerdemo.utils;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.pili.pldroid.playerdemo.R;

public class AnimUtils {
    public static Animation getTopInAnim(Context context){
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.play_top_in);
        return  animation;
    }
    public static Animation getTopOutAnim(Context context){
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.play_top_out);
        return  animation;
    }
    public static Animation getBottomInAnim(Context context){
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.play_bottom_in);
        return  animation;
    }
    public static Animation getBottomOutAnim(Context context){
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.play_bottom_out);
        return  animation;
    }
    public static Animation getLeftInAnim(Context context){
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.play_left_in);
        return animation;
    }
    public static Animation getLeftOutAnim(Context context){
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.play_left_out);
        return animation;
    }
    public static Animation getRightInAnim(Context context){
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.play_right_in);
        return animation;
    }
    public static Animation getRightOutAnim(Context context){
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.play_right_out);
        return animation;
    }

}
