package com.pili.pldroid.playerdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.PLOnBufferingUpdateListener;
import com.pili.pldroid.player.PLOnCompletionListener;
import com.pili.pldroid.player.PLOnErrorListener;
import com.pili.pldroid.player.PLOnInfoListener;
import com.pili.pldroid.player.PLOnPreparedListener;
import com.pili.pldroid.player.PLOnVideoSizeChangedListener;
import com.pili.pldroid.playerdemo.adapter.ChatFullRoomAdapter;
import com.pili.pldroid.playerdemo.adapter.ChatRoomAdapter;
import com.pili.pldroid.playerdemo.adapter.ChatRoomAdapter.ContentLongClickListener;
import com.pili.pldroid.playerdemo.adapter.ChattingListAdapter;
import com.pili.pldroid.playerdemo.utils.Config;
import com.pili.pldroid.playerdemo.utils.NavigationBarUtil;
import com.pili.pldroid.playerdemo.utils.SoftKeyBoardListener;
import com.pili.pldroid.playerdemo.utils.Utils;
import com.pili.pldroid.playerdemo.view.PlayerMenuView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import cn.jmessage.support.okhttp3.MediaType;
import cn.jmessage.support.okhttp3.internal.http.HttpHeaders;
import cn.jmessage.support.okhttp3.internal.http.HttpMethod;
import cn.jpush.im.android.api.ChatRoomManager;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetChatRoomSilencesCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.callback.GetUserInfoListCallback;
import cn.jpush.im.android.api.callback.RequestCallback;
import cn.jpush.im.android.api.event.ChatRoomMessageEvent;
import cn.jpush.im.android.api.event.ChatRoomNotificationEvent;
import cn.jpush.im.android.api.event.OfflineMessageEvent;
import cn.jpush.im.android.api.model.ChatRoomInfo;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.DeviceInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.SilenceInfo;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;

/**
 * This demo shows how to use PLMediaPlayer API playing video stream
 */
public class PLMediaPlayerActivity extends Activity {

    private static final String TAG = PLMediaPlayerActivity.class.getSimpleName();

    private SurfaceView mSurfaceView;
    private PLMediaPlayer mMediaPlayer;
    private View mLoadingView;
    private AVOptions mAVOptions;

    private TextView mStatInfoTextView;


    private int mSurfaceWidth = 0;
    private int mSurfaceHeight = 0;

    private String mVideoPath = null;
    private boolean mIsStopped = false;
    private Toast mToast = null;

    private long mLastUpdateStatTime = 0;

    private boolean mDisableLog = false;
    private Long chatRoomId = 25113167L;
    private Conversation mConv;
    private PlayerMenuView playMenuView;
    private List<Message> msgs;
    private ChatRoomAdapter chatRoomAdapter;
    private ChatFullRoomAdapter chatFullRoomAdapter;
    private ListView listview, listview_full;
    private LinearLayout lin_edit_01;
    private LinearLayout lin_edit_full;
    private FrameLayout frameLayout;
    //当前是否为全屏
    private Boolean mIsFullScreen = false;
    private Boolean isOnce = true;//是否第一次
    private EditText edit_input_01;
    private TextView text_submit;
    private DisplayMetrics dm;
    private int mVideoWidth = 0, mVideoHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //activity.getWindow().setStatusBarColor(0x00000000);
                Window window = getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
                //状态栏字颜色
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//android6.0以后可以对状态栏文字颜色和图标进行修改
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }

                //KLog.d("setAndroidNativeLightStatusBar","--setAndroidNativeLightStatusBar--"+activity);
            }
        }
        if (NavigationBarUtil.hasNavigationBar(this)) {
            NavigationBarUtil.initActivity(findViewById(android.R.id.content));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(getResources().getColor(R.color.white));
        }
        setContentView(R.layout.activity_media_player);

        dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        /**
         * 应用层可以在任意类中注册事件接收，sdk会持有这个类的强引用，上层需要注意在合适的地方解绑事件接收
         * Object receiver 消息接收类对象 int priority 定义事件接收者接收事件的优先级，默认值为0，优先级越高将越先接收到事件。（优先级只对同一个线程模式中的接收者有效）
         * JMessageClient.registerEventReceiver(Object receiver, int priority);
         */
        JMessageClient.registerEventReceiver(this);
        mVideoPath = getIntent().getStringExtra("videoPath");
        boolean isLiveStreaming = getIntent().getIntExtra("liveStreaming", 1) == 1;
        //
        initJpushIM();
        playMenuView = findViewById(R.id.playMenuView);
        listview = findViewById(R.id.listview);
        listview_full = findViewById(R.id.listview_full);
        msgs = new ArrayList<>();
        chatRoomAdapter = new ChatRoomAdapter(PLMediaPlayerActivity.this, msgs,
                new ContentLongClickListener() {
                    @Override
                    public void onContentLongClick(int position, View view) {
                        final UserInfo user = msgs.get(position).getFromUser();

                        JMessageClient.getUserInfo(user.getUserName(), new GetUserInfoCallback() {
                            @Override
                            public void gotResult(int i, String s, UserInfo userInfo) {
                                Collection<UserInfo> userInfos = new ArrayList<>();
                                userInfos.add(userInfo);
                                ChatRoomManager.addChatRoomSilence(chatRoomId, userInfos, time, new BasicCallback() {
                                    @Override
                                    public void gotResult(int responseCode, String responseMessage) {
                                        if (0 == responseCode) {
                                            Toast.makeText(PLMediaPlayerActivity.this, "禁言成功" + user.getUserName(), Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(PLMediaPlayerActivity.this, "设置禁言失败" + user.getUserName() + responseMessage, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                                ChatRoomManager.getChatRoomSilencesFromNewest(chatRoomId, 0, 200, new GetChatRoomSilencesCallback() {
                                    @Override
                                    public void gotResult(int i, String s, List<SilenceInfo> list, int i1) {
                                        Toast.makeText(PLMediaPlayerActivity.this, "禁言列表" + list.size(), Toast.LENGTH_LONG).show();

                                    }
                                });
                            }
                        });


                    }
                });
        listview.setAdapter(chatRoomAdapter);

        chatFullRoomAdapter = new ChatFullRoomAdapter(PLMediaPlayerActivity.this, msgs, dm.widthPixels);
        listview_full.setAdapter(chatFullRoomAdapter);


        edit_input_01 = findViewById(R.id.edit_input_01);
        lin_edit_01 = findViewById(R.id.lin_edit_01);
        text_submit = findViewById(R.id.text_submit);
        frameLayout = findViewById(R.id.frameLayout);
        lin_edit_full = findViewById(R.id.lin_edit_full);

        Button pauseBtn = findViewById(R.id.BtnPause);
        Button resumeBtn = findViewById(R.id.BtnResume);

        mLoadingView = findViewById(R.id.LoadingView);
        mSurfaceView = findViewById(R.id.SurfaceView);
        mSurfaceView.getHolder().addCallback(mCallback);

        mStatInfoTextView = findViewById(R.id.StatInfoTextView);

        mSurfaceWidth = getResources().getDisplayMetrics().widthPixels;
        mSurfaceHeight = getResources().getDisplayMetrics().heightPixels;

        if (isLiveStreaming) {
            pauseBtn.setEnabled(false);
            resumeBtn.setEnabled(false);
        }

        mAVOptions = new AVOptions();
        mAVOptions.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
        // 1 -> hw codec enable, 0 -> disable [recommended]
        int codec = getIntent().getIntExtra("mediaCodec", AVOptions.MEDIA_CODEC_SW_DECODE);
        mAVOptions.setInteger(AVOptions.KEY_MEDIACODEC, codec);
        mAVOptions.setInteger(AVOptions.KEY_LIVE_STREAMING, isLiveStreaming ? 1 : 0);
        boolean cache = getIntent().getBooleanExtra("cache", false);
        if (!isLiveStreaming && cache) {
            mAVOptions.setString(AVOptions.KEY_CACHE_DIR, Config.DEFAULT_CACHE_DIR);
        }
        mDisableLog = getIntent().getBooleanExtra("disable-log", false);
        mAVOptions.setInteger(AVOptions.KEY_LOG_LEVEL, mDisableLog ? 5 : 0);
        if (!isLiveStreaming) {
            int startPos = getIntent().getIntExtra("start-pos", 0);
            mAVOptions.setInteger(AVOptions.KEY_START_POSITION, startPos * 1000);
        }

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        initListener();
    }

    private String username;
    private String password;
    public static final int PAGE_MESSAGE_COUNT = 18;
    private int mOffset = PAGE_MESSAGE_COUNT;
    //初始化极光
    private void initJpushIM() {
//        JMessageClient.register("mowang2020", "123456", new BasicCallback() {
//            @Override
//            public void gotResult(int i, String s) {
//                Log.d("JMessageClient_register", i + ":" + s);
//            }
//        });//mw1607498082567
        username = "admin888888";
        password = "123456";
        JMessageClient.login(username, password, new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                Log.d("JMessageClient_login", i + ":" + s);

                ChatRoomManager.enterChatRoom(chatRoomId, new RequestCallback<Conversation>() {
                    @Override
                    public void gotResult(int i, String s, Conversation conversation) {

                        if (i == 0) {
                            if (conversation == null) {
                                mConv = Conversation.createChatRoomConversation(chatRoomId);
                            } else {
                                mConv = conversation;
                            }



                            getRoomnumber(chatRoomId);
                            //查询禁言状态
                            ChatRoomManager.getChatRoomMemberSilence(chatRoomId, username, "", new RequestCallback<SilenceInfo>() {
                                @Override
                                public void gotResult(int responseCode, String responseMessage, SilenceInfo result) {
                                    if (0 == responseCode) {
                                        StringBuilder builder = new StringBuilder();
                                        if (result != null) {
                                            edit_input_01.setHint("你已被禁言，无法发送消息");
                                            edit_input_01.clearFocus();
                                            edit_input_01.setEnabled(false);
                                            edit_input_01.setGravity(Gravity.CENTER);
                                        } else {
                                            builder.append("该用户没有被禁言");
                                        }
                                    }
                                }
                            });

                        } else if (i == 851003) { // 已经在聊天室中先退出聊天室再进入
                            getRoomnumber(chatRoomId);
                            ChatRoomManager.leaveChatRoom(chatRoomId, new BasicCallback() {
                                @Override
                                public void gotResult(int i, String s) {
                                    if (i == 0) {
                                        ChatRoomManager.enterChatRoom(chatRoomId, new RequestCallback<Conversation>() {
                                            @Override
                                            public void gotResult(int i, String s, Conversation conversation) {
                                                if (i == 0) {
                                                    if (conversation == null) {
                                                        mConv = Conversation.createChatRoomConversation(chatRoomId);
                                                    } else {
                                                        mConv = conversation;
                                                    }
                                                    getRoomnumber(chatRoomId);
                                                }
                                            }
                                        });
                                    } else if (i == 852004) {
                                        mConv = Conversation.createChatRoomConversation(chatRoomId);
//                                        msgs = mConv.get(20, mOffset);
                                        msgs.addAll(mConv.getAllMessage());
                                        chatRoomAdapter.notifyDataSetChanged();

                                    } else {
                                        Toast.makeText(PLMediaPlayerActivity.this, "进入聊天室失败" + s, Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(PLMediaPlayerActivity.this, "进入聊天室失败" + s, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });

            }
        });


    }
    /**
     * @Description  极光推送专用post
     * @Author PrinceCharmingDong
     * @Date 2020/3/4
     */
//    public static String doPostForJpush (String url, String JSONBody,String appKey,String masterKey) {
//        CloseableHttpResponse response = null;
//        CloseableHttpClient httpClient = null;
//        String responseContent = "";
//        try {
//            httpClient = HttpClients.createDefault();
//            HttpPost httpPost = new HttpPost(url);
//            httpPost.addHeader("Content-Type", "application/json");
//            httpPost.addHeader("Authorization", "Basic " + Base64.getUrlEncoder()
//                    .encodeToString((appKey+ ":" + masterKey).getBytes()));
//            httpPost.setEntity(new StringEntity(JSONBody));
//            response = httpClient.execute(httpPost);
//            HttpEntity entity = response.getEntity();
//            responseContent = EntityUtils.toString(entity, "UTF-8");
//            response.close();
//            httpClient.close();
//            System.out.println(responseContent);
//        } catch (ClientProtocolException e) {
//            log.error(e);
//        } catch (IOException e) {
//            log.error(e);
//        } finally {
//            close(response, httpClient);
//        }
//        return responseContent;


        private void initListener() {
        text_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = edit_input_01.getText().toString().trim();
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(PLMediaPlayerActivity.this, "请输入你要发送的文字", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 发送聊天室消息
                Conversation conv = JMessageClient.getChatRoomConversation(chatRoomId);
                if (null == conv) {
                    conv = Conversation.createChatRoomConversation(chatRoomId);
                }
                final Message msg = conv.createSendTextMessage(text);//实际聊天室可以支持所有类型的消息发送，demo为了简便，仅仅实现了文本类型的消息发送，其他的消息类型参考消息发送相关文档
                msg.setOnSendCompleteCallback(new BasicCallback() {
                    @Override
                    public void gotResult(int responseCode, String responseMessage) {
                        if (0 == responseCode) {
                            Log.i(TAG, responseCode + responseMessage + msg);
                            edit_input_01.setText("");
                            msgs.add(msg);
                            chatRoomAdapter.setList(msgs);
                            chatRoomAdapter.notifyDataSetChanged();
                            chatFullRoomAdapter.setList(msgs);
                            chatFullRoomAdapter.notifyDataSetChanged();
                            Log.i(TAG, msgs.size() + "消息数目");
                        } else {
                            Log.i(TAG, responseCode + responseMessage + "消息发送失败");
                        }
                    }
                });
                JMessageClient.sendMessage(msg);
            }
        });


        lin_edit_full.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lin_edit_full.setVisibility(View.GONE);
                lin_edit_01.setVisibility(View.VISIBLE);
                edit_input_01.setFocusable(true);
                edit_input_01.setFocusableInTouchMode(true);
                edit_input_01.requestFocus();

                InputMethodManager imm = (InputMethodManager) edit_input_01.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
            }
        });
        SoftKeyBoardListener.setListener(PLMediaPlayerActivity.this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {


                if (mIsFullScreen && !isOnce) {
                    lin_edit_full.setVisibility(View.GONE);
                    lin_edit_01.setVisibility(View.VISIBLE);
                    isOnce = false;
                    Log.d("9999999999", "onLayoutChange+1111111==>" + height);
                }
                if (isOnce)
                    isOnce = false;
            }

            @Override
            public void keyBoardHide(int height) {
                if (mIsFullScreen && !isOnce) {
                    lin_edit_full.setVisibility(View.VISIBLE);
                    lin_edit_01.setVisibility(View.GONE);
                    isOnce = false;
                    Log.d("9999999999", "onLayoutChange+2222222==>" + height);
                }
            }
        });


    }


    public void onEvent(OfflineMessageEvent event) {
        List<Message> newMessageList = event.getOfflineMessageList();//获取此次离线期间会话收到的新消息列表
        msgs.addAll(newMessageList);
        chatRoomAdapter.setList(msgs);
        chatRoomAdapter.notifyDataSetChanged();
        chatFullRoomAdapter.setList(msgs);
        chatFullRoomAdapter.notifyDataSetChanged();
        Log.i(TAG, newMessageList.size() + "消息数目");
    }


    public void onEventMainThread(ChatRoomMessageEvent event) {


        msgs.addAll(event.getMessages());
        chatRoomAdapter.setList(msgs);
        chatRoomAdapter.notifyDataSetChanged();
        chatFullRoomAdapter.setList(msgs);
        chatFullRoomAdapter.notifyDataSetChanged();
        getRoomnumber(chatRoomId);
        if (null == msgs || msgs.size() == 0) {
            msgs.addAll(event.getMessages());
            EventBus.getDefault().postSticky(msgs);
        } else {
            EventBus.getDefault().postSticky(event.getMessages().get(0));
        }


    }


    public void onEventMainThread(ChatRoomNotificationEvent event) {
//        final StringBuilder builder = new StringBuilder();
//        builder.append("事件id:").append(event.getEventID()).append("\n");
//        builder.append("聊天室id:").append(event.getRoomID()).append("\n");
//        builder.append("事件类型:").append(event.getType()).append("\n");
        if (event.getType().equals(ChatRoomNotificationEvent.Type.add_chatroom_silence)) {//添加禁言
            event.getTargetUserInfoList(new GetUserInfoListCallback() {
                @Override
                public void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfoList) {
//                    builder.append("目标用户:\n");
                    if (0 == responseCode) {
                        for (UserInfo userInfo : userInfoList) {
                            if (userInfo.getUserName().equals(username)) {
                                Toast.makeText(PLMediaPlayerActivity.this, userInfo.getUserName() + "已经被禁言", Toast.LENGTH_SHORT).show();
                                edit_input_01.setHint("你已被禁言，无法发送消息");
                                edit_input_01.clearFocus();
                                edit_input_01.setEnabled(false);
                                edit_input_01.setGravity(Gravity.CENTER);
                            }
                        }
                    }
                }
            });
        } else if (event.getType().equals(ChatRoomNotificationEvent.Type.del_chatroom_silence)) {//取消禁言
            event.getTargetUserInfoList(new GetUserInfoListCallback() {
                @Override
                public void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfoList) {
//                    builder.append("目标用户:\n");
                    if (0 == responseCode) {
                        for (UserInfo userInfo : userInfoList) {
                            if (userInfo.getUserName().equals(username)) {
                                Toast.makeText(PLMediaPlayerActivity.this, userInfo.getUserName() + "已经被取消禁言", Toast.LENGTH_SHORT).show();
                                edit_input_01.setHint("点击参与讨论");
                                edit_input_01.requestFocus();
                                edit_input_01.setEnabled(true);
                                edit_input_01.setGravity(Gravity.CENTER_VERTICAL);
                            }
                        }
                    }
                }
            });
        }


//        Toast.makeText(PLMediaPlayerActivity.this, builder.toString(), Toast.LENGTH_SHORT).show();
    }


    private Long time = 300000l;

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    void longClickListener(UserInfo userInfo) {
//
//    }


    private void getRoomnumber(Long roonId) {
        ChatRoomManager.getChatRoomInfos(Collections.singleton(roonId), new RequestCallback<List<ChatRoomInfo>>() {

            @Override
            public void gotResult(int i, String s, List<ChatRoomInfo> chatRoomInfos) {
//                tv_num.setText(userCount + chatRoomInfos.get(0).getTotalMemberCount() + " 观看");
                playMenuView.setNumber(chatRoomInfos.get(0).getTotalMemberCount());
               // chatRoomInfos.get(0).
            }
        });


        ChatRoomManager.getChatRoomInfos(Collections.singleton(roonId), new RequestCallback<List<ChatRoomInfo>>() {

            @Override
            public void gotResult(int i, String s, List<ChatRoomInfo> chatRoomInfos) {
                playMenuView.setNumber(chatRoomInfos.get(0).getTotalMemberCount());

            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //横屏
            full();
        } else {
            cancle_full();
        }
    }

    private double getDouble(int i) {
        return (double) i;
    }

    private void full() {
        playMenuView.setmFullScreen(true);
        //去掉系统通知栏
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //调整mFlVideoGroup布局参数
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LinearLayout
                .LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        frameLayout.setLayoutParams(params);
        frameLayout.setBackgroundColor(ContextCompat.getColor(PLMediaPlayerActivity.this, R.color.color_000000));
        playMenuView.setLayoutParams(params);
        listview.setVisibility(View.GONE);
        lin_edit_01.setVisibility(View.GONE);
        listview_full.setVisibility(View.VISIBLE);
        lin_edit_full.setVisibility(View.VISIBLE);

        setSize(dm.widthPixels);
        mIsFullScreen = true;
        Log.d("9999999999", "full+222222222");
    }


    private void cancle_full() {
        isOnce = true;
        playMenuView.setmFullScreen(false);
        listview.setVisibility(View.VISIBLE);
        lin_edit_01.setVisibility(View.VISIBLE);
        listview_full.setVisibility(View.GONE);
        lin_edit_full.setVisibility(View.GONE);
        frameLayout.setBackgroundColor(ContextCompat.getColor(PLMediaPlayerActivity.this, R.color.color_f2f2f2));
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LinearLayout
                .LayoutParams.MATCH_PARENT, getResources().getDimensionPixelOffset(R.dimen.h_200dp));
        playMenuView.setLayoutParams(params);
        setSize(getResources().getDimensionPixelOffset(R.dimen.h_200dp));
        mIsFullScreen = false;
        Log.d("9999999999", "full+111111111");
    }

    private void setSize(int count) {
        double y = getDouble(mVideoWidth) / getDouble(mVideoHeight);
        int hight = count;
        int width = (int) (y * getDouble(hight));

        FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(width, hight);
        layout.gravity = Gravity.CENTER_HORIZONTAL;
        mSurfaceView.setLayoutParams(layout);


    }


    public static int Dp2Px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(null);

        //事件接收类的解绑
        JMessageClient.unRegisterEventReceiver(this);
        ChatRoomManager.leaveChatRoom(chatRoomId, new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                Log.d("离开聊天室", i + "");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void onClickPlay(View v) {
        if (mIsStopped) {
            prepare();
        } else {
            mMediaPlayer.start();
        }
    }

    public void onClickPause(View v) {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
    }

    public void onClickResume(View v) {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    public void onClickStop(View v) {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
        mIsStopped = true;
        mMediaPlayer = null;
    }

    public void releaseWithoutStop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.setDisplay(null);
        }
    }

    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void prepare() {
        if (mMediaPlayer != null) {
            mMediaPlayer.setDisplay(mSurfaceView.getHolder());
            return;
        }

        try {
            mMediaPlayer = new PLMediaPlayer(this, mAVOptions);
            mMediaPlayer.setLooping(getIntent().getBooleanExtra("loop", false));
            mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
            // mMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
            mMediaPlayer.setOnErrorListener(mOnErrorListener);
            mMediaPlayer.setOnInfoListener(mOnInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
            // set replay if completed
            // mMediaPlayer.setLooping(true);
            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mMediaPlayer.setDataSource(mVideoPath);
            mMediaPlayer.setDisplay(mSurfaceView.getHolder());
            mMediaPlayer.prepareAsync();
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            prepare();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // release();
            releaseWithoutStop();
        }
    };

    private PLOnVideoSizeChangedListener mOnVideoSizeChangedListener = new PLOnVideoSizeChangedListener() {
        public void onVideoSizeChanged(int width, int height) {
            Log.i(TAG, "onVideoSizeChanged: width = " + width + ", height = " + height);
            // resize the display window to fit the screen
            if (width != 0 && height != 0) {
                float ratioW = (float) width / (float) mSurfaceWidth;
                float ratioH = (float) height / (float) mSurfaceHeight;
                float ratio = Math.max(ratioW, ratioH);
                width = (int) Math.ceil((float) width / ratio);
                height = (int) Math.ceil((float) height / ratio);
                FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(width, height);
                layout.gravity = Gravity.CENTER;
                mSurfaceView.setLayoutParams(layout);
            }
        }
    };

    private PLOnPreparedListener mOnPreparedListener = new PLOnPreparedListener() {
        @Override
        public void onPrepared(int preparedTime) {
            Log.i(TAG, "On Prepared ! prepared time = " + preparedTime + " ms");
            mMediaPlayer.start();
            mIsStopped = false;
            if (mVideoWidth == 0) {
                mVideoWidth = mMediaPlayer.getVideoWidth();
                mVideoHeight = mMediaPlayer.getVideoHeight();
            }
        }
    };

    private PLOnInfoListener mOnInfoListener = new PLOnInfoListener() {
        @Override
        public void onInfo(int what, int extra) {
            Log.i(TAG, "OnInfo, what = " + what + ", extra = " + extra);
            switch (what) {
                case PLOnInfoListener.MEDIA_INFO_BUFFERING_START:
                    mLoadingView.setVisibility(View.VISIBLE);
                    break;
                case PLOnInfoListener.MEDIA_INFO_BUFFERING_END:
                    mLoadingView.setVisibility(View.GONE);
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_RENDERING_START:
                    mLoadingView.setVisibility(View.GONE);
                    Utils.showToastTips(PLMediaPlayerActivity.this, "first video render time: " + extra + "ms");
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_GOP_TIME:
                    Log.i(TAG, "Gop Time: " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_AUDIO_RENDERING_START:
                    mLoadingView.setVisibility(View.GONE);
                    break;
                case PLOnInfoListener.MEDIA_INFO_SWITCHING_SW_DECODE:
                    Log.i(TAG, "Hardware decoding failure, switching software decoding!");
                    break;
                case PLOnInfoListener.MEDIA_INFO_METADATA:
                    Log.i(TAG, mMediaPlayer.getMetadata().toString());
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_BITRATE:
                case PLOnInfoListener.MEDIA_INFO_VIDEO_FPS:
                    updateStatInfo();
                    break;
                case PLOnInfoListener.MEDIA_INFO_CONNECTED:
                    Log.i(TAG, "Connected !");
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                    Log.i(TAG, "Rotation changed: " + extra);
                default:
                    break;
            }
        }
    };

    private PLOnBufferingUpdateListener mOnBufferingUpdateListener = new PLOnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(int percent) {
            Log.d(TAG, "onBufferingUpdate: " + percent + "%");
            long current = System.currentTimeMillis();
            if (current - mLastUpdateStatTime > 3000) {
                mLastUpdateStatTime = current;
                updateStatInfo();
            }
        }
    };

    /**
     * Listen the event of playing complete
     * For playing local file, it's called when reading the file EOF
     * For playing network stream, it's called when the buffered bytes played over
     * <p>
     * If setLooping(true) is called, the player will restart automatically
     * And ｀onCompletion｀ will not be called
     */
    private PLOnCompletionListener mOnCompletionListener = new PLOnCompletionListener() {
        @Override
        public void onCompletion() {
            Log.d(TAG, "Play Completed !");
            Utils.showToastTips(PLMediaPlayerActivity.this, "Play Completed !");
            finish();
        }
    };

    private PLOnErrorListener mOnErrorListener = new PLOnErrorListener() {
        @Override
        public boolean onError(int errorCode) {
            Log.e(TAG, "Error happened, errorCode = " + errorCode);
            switch (errorCode) {
                case PLOnErrorListener.ERROR_CODE_IO_ERROR:
                    /**
                     * SDK will do reconnecting automatically
                     */
                    Utils.showToastTips(PLMediaPlayerActivity.this, "IO Error !");
                    return false;
                case PLOnErrorListener.ERROR_CODE_OPEN_FAILED:
                    Utils.showToastTips(PLMediaPlayerActivity.this, "failed to open player !");
                    break;
                case PLOnErrorListener.ERROR_CODE_SEEK_FAILED:
                    Utils.showToastTips(PLMediaPlayerActivity.this, "failed to seek !");
                    break;
                default:
                    Utils.showToastTips(PLMediaPlayerActivity.this, "unknown error !");
                    break;
            }
            finish();
            return true;
        }
    };

    private void updateStatInfo() {
        long bitrate = mMediaPlayer.getVideoBitrate() / 1024;
        final String stat = bitrate + "kbps, " + mMediaPlayer.getVideoFps() + "fps";
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatInfoTextView.setText(stat);
            }
        });
    }


}
