package studio.stc.mediasessionlab;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import studio.stc.mediasessionlab.service.DotAudioService;
import studio.stc.mediasessionlab.service.MusicService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private List<MediaBrowserCompat.MediaItem> list;

    private MediaBrowserCompat mBrowser;
    private MediaControllerCompat mController;

    private Button btnPlay;
    ServiceConnection serviceConnection;
    Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list = new ArrayList<>();

        btnPlay = findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mController != null) HandlePlayEvent();
            }
        });

        mBrowser = new MediaBrowserCompat(
                this,
                new ComponentName(this, MusicService.class),//綁定瀏覽器服務
                browserConnectionCallback,//设置连接回调
                null
        );
//        serviceIntent = new Intent(MainActivity.this, DotAudioService.class);
//        serviceConnection = new ServiceConnection() {
//            @Override
//            public void onServiceConnected(ComponentName name, IBinder service) {
//                Log.i(TAG, "onServiceConnected: connected!");
//                DotAudioService.DotAudioBinder binder = (DotAudioService.DotAudioBinder) service;
//                try {
//                    Log.i(TAG, "onServiceConnected: trying to create");
//                    mController = new MediaControllerCompat(MainActivity.this, binder.getToken());
//                    mController.registerCallback(controllerCallback);
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onServiceDisconnected(ComponentName name) {
//                mController.unregisterCallback(controllerCallback);
//                Log.i(TAG, "onServiceDisconnected: disconnected.");
//            }
//        };
//        bindService(serviceIntent, serviceConnection, Service.BIND_AUTO_CREATE);
        mBrowser.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBrowser.disconnect();
//        unbindService(serviceConnection);
    }

    /**
     * 處理播放按鈕事件
     */
    private void HandlePlayEvent() {
        switch (mController.getPlaybackState().getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                Log.i(TAG, "HandlePlayEvent: pause");
                mController.getTransportControls().pause();
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                Log.i(TAG, "HandlePlayEvent: start play.");
                mController.getTransportControls().play();
                break;
            default:
                Log.i(TAG, "HandlePlayEvent: play from null.");
//                mController.getTransportControls().playFromSearch("", null);
                String uriStr = "android.resource://" + getPackageName() + "/" + R.raw.payphone;
                mController.getTransportControls().playFromUri(Uri.parse(uriStr), null);
                break;
        }
    }

    // https://www.jianshu.com/p/a6c2a3ed842d
    //connect → onConnected → subscribe → onChildrenLoaded
    /**
     * 连接状态的回调接口，连接成功时会调用onConnected()方法
     */
    private MediaBrowserCompat.ConnectionCallback browserConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    Log.e(TAG, "onConnected------");
                    //必须在确保连接成功的前提下执行订阅的操作
                    if (mBrowser.isConnected()) {
                        //mediaId即为MediaBrowserService.onGetRoot的返回值
                        //若Service允许客户端连接，则返回结果不为null，其值为数据内容层次结构的根ID
                        //若拒绝连接，则返回null
                        String mediaId = mBrowser.getRoot();

                        //Browser通过订阅的方式向Service请求数据，发起订阅请求需要两个参数，其一为mediaId
                        //而如果该mediaId已经被其他Browser实例订阅，则需要在订阅之前取消mediaId的订阅者
                        //虽然订阅一个 已被订阅的mediaId 时会取代原Browser的订阅回调，但却无法触发onChildrenLoaded回调

                        //ps：虽然基本的概念是这样的，但是Google在官方demo中有这么一段注释...
                        // This is temporary: A bug is being fixed that will make subscribe
                        // consistently call onChildrenLoaded initially, no matter if it is replacing an existing
                        // subscriber or not. Currently this only happens if the mediaID has no previous
                        // subscriber or if the media content changes on the service side, so we need to
                        // unsubscribe first.
                        //大概的意思就是现在这里还有BUG，即只要发送订阅请求就会触发onChildrenLoaded回调
                        //所以无论怎样我们发起订阅请求之前都需要先取消订阅
                        mBrowser.unsubscribe(mediaId);
                        //之前说到订阅的方法还需要一个参数，即设置订阅回调SubscriptionCallback
                        //当Service获取数据后会将数据发送回来，此时会触发SubscriptionCallback.onChildrenLoaded回调
                        mBrowser.subscribe(mediaId, browserSubscriptionCallback);

                        try {
                            mController = new MediaControllerCompat(MainActivity.this, mBrowser.getSessionToken());
                            mController.registerCallback(controllerCallback);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onConnectionFailed() {
                    Log.e(TAG, "连接失败！");
                }
            };
    /**
     * 向媒体浏览器服务(MediaBrowserService)发起数据订阅请求的回调接口
     */
    private final MediaBrowserCompat.SubscriptionCallback browserSubscriptionCallback =
            new MediaBrowserCompat.SubscriptionCallback() {
                @Override
                public void onChildrenLoaded(@NonNull String parentId,
                                             @NonNull List<MediaBrowserCompat.MediaItem> children) {
                    Log.e(TAG, "onChildrenLoaded------");
                    //children 即为Service发送回来的媒体数据集合
                    for (MediaBrowserCompat.MediaItem item : children) {
                        Log.e(TAG, item.getDescription().getTitle().toString());
                        list.add(item);
                    }
                    //在onChildrenLoaded可以执行刷新列表UI的操作
//                    demoAdapter.notifyDataSetChanged();
                }
            };

    /**
     * 媒体控制器控制播放过程中的回调接口，可以用来根据播放状态更新UI
     */
    private final MediaControllerCompat.Callback controllerCallback =
            new MediaControllerCompat.Callback() {
                /***
                 * 音乐播放状态改变的回调
                 */
                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    // 在這裡設定播放器狀態回調
                    switch (state.getState()) {
                        case PlaybackStateCompat.STATE_NONE:
                            btnPlay.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
                            break;
                        case PlaybackStateCompat.STATE_PAUSED:
                            btnPlay.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
                            break;
                        case PlaybackStateCompat.STATE_PLAYING:
                            btnPlay.setBackgroundResource(R.drawable.ic_pause_black_24dp);
                            break;
                    }
                }

                /**
                 * 播放音乐改变的回调
                 */
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
//                    textTitle.setText(metadata.getDescription().getTitle());
                }
            };

}
