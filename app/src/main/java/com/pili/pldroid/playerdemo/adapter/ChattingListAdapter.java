package com.pili.pldroid.playerdemo.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.pili.pldroid.playerdemo.NimApplication;
import com.pili.pldroid.playerdemo.R;
import com.pili.pldroid.playerdemo.utils.TimeFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;

import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.enums.MessageDirect;
import cn.jpush.im.android.api.enums.MessageStatus;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.api.options.MessageSendingOptions;
import cn.jpush.im.api.BasicCallback;

public class ChattingListAdapter extends BaseAdapter {
    public static final int PAGE_MESSAGE_COUNT = 18;
    //文本
    private final int TYPE_SEND_TXT = 0;
    private final int TYPE_RECEIVE_TXT = 1;

    // 图片
    private final int TYPE_SEND_IMAGE = 2;
    private final int TYPE_RECEIVER_IMAGE = 3;

    //文件
    private final int TYPE_SEND_FILE = 4;
    private final int TYPE_RECEIVE_FILE = 5;
    // 语音
    private final int TYPE_SEND_VOICE = 6;
    private final int TYPE_RECEIVER_VOICE = 7;
    // 位置
    private final int TYPE_SEND_LOCATION = 8;
    private final int TYPE_RECEIVER_LOCATION = 9;
    //群成员变动
    private final int TYPE_GROUP_CHANGE = 10;
    //视频
    private final int TYPE_SEND_VIDEO = 11;
    private final int TYPE_RECEIVE_VIDEO = 12;

    //自定义消息
    private final int TYPE_CUSTOM_TXT = 13;

    private Context mContext;
    private Activity mActivity;
    private int mWidth;
    private LayoutInflater mInflater;
    private Conversation mConv;
    private List<Message> mMsgList = new ArrayList<Message>();//所有消息列表
    private ContentLongClickListener mLongClickListener;
    private int mOffset = PAGE_MESSAGE_COUNT;

    public ChattingListAdapter(Activity context, Conversation conv, ContentLongClickListener longClickListener) {
        this.mContext = context;
        mActivity = context;
        DisplayMetrics dm = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mWidth = dm.widthPixels;
        mInflater = LayoutInflater.from(mContext);
        this.mConv = conv;
        this.mMsgList = mConv.getMessagesFromNewest(0, mOffset);
        mLongClickListener = longClickListener;


    }
    public void setList(List<Message> chatRoom) {
        this.mMsgList.addAll(chatRoom);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMsgList.size();
    }

    @Override
    public Object getItem(int i) {
        return mMsgList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        final Message msg = mMsgList.get(position);
        final UserInfo userInfo = msg.getFromUser();
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = getItemViewType(position) == TYPE_SEND_TXT ?
                    mInflater.inflate(R.layout.jmui_chat_item_send_text, null) :
                    mInflater.inflate(R.layout.jmui_chat_item_receive_text, null);//默认纯文本
            holder.msgTime = (TextView) convertView.findViewById(R.id.jmui_send_time_txt);
            holder.headIcon = (ImageView) convertView.findViewById(R.id.jmui_avatar_iv);
            holder.displayName = (TextView) convertView.findViewById(R.id.jmui_display_name_tv);
            holder.txtContent = (TextView) convertView.findViewById(R.id.jmui_msg_content);
            holder.sendingIv = (ImageView) convertView.findViewById(R.id.jmui_sending_iv);
            holder.resend = (ImageButton) convertView.findViewById(R.id.jmui_fail_resend_ib);
            holder.text_receipt = (TextView) convertView.findViewById(R.id.text_receipt);
            //默认纯文本
            holder.ll_businessCard = (LinearLayout) convertView.findViewById(R.id.ll_businessCard);
            holder.business_head = (ImageView) convertView.findViewById(R.id.business_head);
            holder.tv_nickUser = (TextView) convertView.findViewById(R.id.tv_nickUser);
            holder.tv_userName = (TextView) convertView.findViewById(R.id.tv_userName);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        long nowDate = msg.getCreateTime();
        if (mOffset == 18) {
            if (position == 0 || position % 18 == 0) {
                TimeFormat timeFormat = new TimeFormat(mContext, nowDate);
                holder.msgTime.setText(timeFormat.getDetailTime());
                holder.msgTime.setVisibility(View.VISIBLE);
            } else {
                long lastDate = mMsgList.get(position - 1).getCreateTime();
                // 如果两条消息之间的间隔超过五分钟则显示时间
                if (nowDate - lastDate > 300000) {
                    TimeFormat timeFormat = new TimeFormat(mContext, nowDate);
                    holder.msgTime.setText(timeFormat.getDetailTime());
                    holder.msgTime.setVisibility(View.VISIBLE);
                } else {
                    holder.msgTime.setVisibility(View.GONE);
                }
            }
        } else {
            if (position == 0 || position == mOffset
                    || (position - mOffset) % 18 == 0) {
                TimeFormat timeFormat = new TimeFormat(mContext, nowDate);

                holder.msgTime.setText(timeFormat.getDetailTime());
                holder.msgTime.setVisibility(View.VISIBLE);
            } else {
                long lastDate = mMsgList.get(position - 1).getCreateTime();
                // 如果两条消息之间的间隔超过五分钟则显示时间
                if (nowDate - lastDate > 300000) {
                    TimeFormat timeFormat = new TimeFormat(mContext, nowDate);
                    holder.msgTime.setText(timeFormat.getDetailTime());
                    holder.msgTime.setVisibility(View.VISIBLE);
                } else {
                    holder.msgTime.setVisibility(View.GONE);
                }
            }
        }

        //显示头像
        if (holder.headIcon != null) {
            if (userInfo != null && !TextUtils.isEmpty(userInfo.getAvatar())) {
                userInfo.getAvatarBitmap(new GetAvatarBitmapCallback() {
                    @Override
                    public void gotResult(int status, String desc, Bitmap bitmap) {
                        if (status == 0) {
                            holder.headIcon.setImageBitmap(bitmap);
                        } else {
                            holder.headIcon.setImageResource(R.drawable.jmui_head_icon);
                        }
                    }
                });
            } else {
                holder.headIcon.setImageResource(R.drawable.jmui_head_icon);
            }

            // 点击头像跳转到个人信息界面
            holder.headIcon.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Bundle bundle = new Bundle();
                    //测试 BeeTest1000000148   正式 B1000000148
//                    int langthStr = BuildConfig.IS_SHOW_LOG ? 14 : 8;
//                    if (msg.getDirect() == MessageDirect.send) {
//                        String targetID = JMessageClient.getMyInfo().getUserName();
//                        bundle.putString("uid", targetID.substring(langthStr));
//                        new IntentToOther(mActivity, ExpertDetailNewActivity.class, bundle);
//                    } else {
//                        //别人
//                        String targetID = userInfo.getUserName();
//                        bundle.putString("uid", targetID.substring(langthStr));
//                        new IntentToOther(mActivity, ExpertDetailNewActivity.class, bundle);
//                    }


//                        if (msg.getDirect() == MessageDirect.send) {
//                        intent.putExtra(NimApplication.TARGET_ID, JMessageClient.getMyInfo().getUserName());
//                        intent.setClass(mContext, PersonalActivity.class);
//                        mContext.startActivity(intent);
//                    } else {
//                        String targetID = userInfo.getUserName();
//                        intent.putExtra(NimApplication.TARGET_ID, targetID);
//                        intent.putExtra(NimApplication.TARGET_APP_KEY, userInfo.getAppKey());
//                        intent.putExtra(NimApplication.GROUP_ID, mGroupId);
//                        if (userInfo.isFriend()) {
//                            intent.setClass(mContext, FriendInfoActivity.class);
//                        } else {
//                            intent.setClass(mContext, GroupNotFriendActivity.class);
//                        }
//                        ((Activity) mContext).startActivityForResult(intent,
//                                NimApplication.REQUEST_CODE_FRIEND_INFO);
//                    }
                }
            });

            holder.headIcon.setTag(position);
            holder.headIcon.setOnLongClickListener(mLongClickListener);
        }
        return convertView;
    }



    public static abstract class ContentLongClickListener implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View v) {
            onContentLongClick((Integer) v.getTag(), v);
            return true;
        }

        public abstract void onContentLongClick(int position, View view);
    }

    public static class ViewHolder {
        public TextView msgTime;
        public ImageView headIcon;
        public ImageView ivDocument;
        public TextView displayName;
        public TextView txtContent;
        public ImageView picture;
        public TextView progressTv;
        public ImageButton resend;
        public TextView voiceLength;
        public ImageView voice;
        public ImageView readStatus;
        public TextView location;
        public TextView groupChange;
        public ImageView sendingIv;
        public LinearLayout contentLl;
        public TextView sizeTv;
        public LinearLayout videoPlay;
        public TextView alreadySend;
        public View locationView;
        public LinearLayout ll_businessCard;
        public ImageView business_head;
        public TextView tv_nickUser;
        public TextView tv_userName;
        public TextView text_receipt;
        public TextView fileLoad;
    }

}