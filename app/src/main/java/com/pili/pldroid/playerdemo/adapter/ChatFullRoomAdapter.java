package com.pili.pldroid.playerdemo.adapter;

import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.pili.pldroid.playerdemo.R;

import java.util.List;

import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.model.Message;


/***
 * 知识库适配器
 *
 * @author LinYuLing
 * @Updatedate 2014-09-25
 */
public class ChatFullRoomAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    /***
     * 图片加载工具类
     */
    /***
     * 上下文变量
     */
    private Activity context;
    /***
     * 数据列表
     */
    private List<Message> list;
    /***
     * 图片宽度
     */
    private int width;


    public ChatFullRoomAdapter(Activity context, List<Message> list,
                               int width) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.list = list;
        this.width = width;
    }

    public void setList(List<Message> chatRoom) {
        this.list.addAll(chatRoom);
        notifyDataSetChanged();
    }
    public void setChatRoom(Message chatRoom) {
        this.list.add(chatRoom);
        notifyDataSetChanged();
    }


    public void clearList() {
        this.list.clear();
    }

    public Message getItemData(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(final int position, View convertView, ViewGroup arg2) {
        // TODO Auto-generated method stub

        convertView = inflater.inflate(R.layout.chat_full_room_item, null);
        TextView video_title = convertView.findViewById(R.id.tv_username_messtge);
        Message chatRoom = list.get(position);
        String usNameMessage = "<font color='#36EAFF'>" + chatRoom.getFromUser().getNickname() + ":</font>" + ((TextContent) chatRoom.getContent()).getText();
        video_title.setText(Html.fromHtml(usNameMessage));
        return convertView;
    }

}
