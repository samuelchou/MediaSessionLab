package studio.stc.mediasessionlab.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import studio.stc.mediasessionlab.MainActivity;
import studio.stc.mediasessionlab.R;
import studio.stc.mediasessionlab.utils.NotificationUtil;

public class MusicService extends MediaBrowserServiceCompat {

    private final static String TAG = "MusicService";
    public static final String MEDIA_ID_ROOT = "__ROOT__";

    private final static String SERVICE_CHANNEL_ID = "musicService";
    private final static String SERVICE_CHANNEL_NAME = "Player";
    private final static int SERVICE_PENDING_INTENT_ID = 1308;
    private final static int SERVICE_NOTIFICATION_ID = 2120;
    private final static int SERVICE_NOTIFICATION_PICTURE_RESOURCE = R.mipmap.ic_launcher_round;

    private MediaSessionCompat mSession;
    private PlaybackStateCompat mPlaybackState;

    private MediaPlayer mMediaPlayer;
    private Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        mPlaybackState = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_NONE, 0, 1.0f)
                .build();

        mSession = new MediaSessionCompat(this, TAG);
        mSession.setCallback(sessionCallback);//设置回调
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mSession.setPlaybackState(mPlaybackState);

        //设置token后会触发MediaBrowserCompat.ConnectionCallback的回调方法
        //表示MediaBrowser与MediaBrowserService连接成功
        setSessionToken(mSession.getSessionToken());

        SetMediaPlayer();

        SetForegroundNotification();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mSession != null) {
            mSession.release();
            mSession = null;
        }
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String s, int i, @Nullable Bundle bundle) {
        Log.e(TAG, "onGetRoot-----------");
        return new BrowserRoot(MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull String s, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.e(TAG, "onLoadChildren--------");
        //将信息从当前线程中移除，允许后续调用sendResult方法
        result.detach();

        //我们模拟获取数据的过程，真实情况应该是异步从网络或本地读取数据
        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "" + R.raw.payphone)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "公共電話")
                .build();
        ArrayList<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        mediaItems.add(createMediaItem(metadata));

        //向Browser发送数据
        result.sendResult(mediaItems);
    }

    private MediaBrowserCompat.MediaItem createMediaItem(MediaMetadataCompat metadata) {
        return new MediaBrowserCompat.MediaItem(
                metadata.getDescription(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        );
    }

    private void SetMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(PreparedListener);
        mMediaPlayer.setOnCompletionListener(CompletionListener);
    }

    /**
     * 响应控制器指令的回调
     */
    private android.support.v4.media.session.MediaSessionCompat.Callback sessionCallback = new MediaSessionCompat.Callback() {
        /**
         * 响应MediaController.getTransportControls().play
         */
        @Override
        public void onPlay() {
            Log.e(TAG, "onPlay");
            if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PAUSED) {
                mMediaPlayer.start();
                mPlaybackState = new PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                        .build();
                mSession.setPlaybackState(mPlaybackState);
            }
        }

        /**
         * 响应MediaController.getTransportControls().onPause
         */
        @Override
        public void onPause() {
            Log.e(TAG, "onPause");
            if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
                mMediaPlayer.pause();
                mPlaybackState = new PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PAUSED, 0, 1.0f)
                        .build();
                mSession.setPlaybackState(mPlaybackState);
            }
        }

        /**
         * 响应MediaController.getTransportControls().playFromUri
         */
        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            Log.e(TAG, "onPlayFromUri");
            try {
                switch (mPlaybackState.getState()) {
                    case PlaybackStateCompat.STATE_PLAYING:
                    case PlaybackStateCompat.STATE_PAUSED:
                    case PlaybackStateCompat.STATE_NONE:
                        mMediaPlayer.reset();
                        mMediaPlayer.setDataSource(MusicService.this, uri);
                        mMediaPlayer.prepare();//准备同步
                        mPlaybackState = new PlaybackStateCompat.Builder()
                                .setState(PlaybackStateCompat.STATE_CONNECTING, 0, 1.0f)
                                .build();
                        mSession.setPlaybackState(mPlaybackState);
                        //我们可以保存当前播放音乐的信息，以便客户端刷新UI
                        mSession.setMetadata(new MediaMetadataCompat.Builder()
                                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, extras.getString("title"))
                                .build()
                        );

                        break;
                    default:
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPlayFromSearch(String query, Bundle extras) {
        }
    };

    /**
     * 监听MediaPlayer.prepare()
     */
    private MediaPlayer.OnPreparedListener PreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            mMediaPlayer.start();
            mPlaybackState = new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                    .build();
            mSession.setPlaybackState(mPlaybackState);
        }
    };

    /**
     * 监听播放结束的事件
     */
    private MediaPlayer.OnCompletionListener CompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            mPlaybackState = new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_NONE, 0, 1.0f)
                    .build();
            mSession.setPlaybackState(mPlaybackState);
            mMediaPlayer.reset();
        }
    };

    private void SetForegroundNotification() {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = NotificationUtil.getNotificationChannel(mContext, SERVICE_CHANNEL_ID, SERVICE_CHANNEL_NAME);
            builder = new NotificationCompat.Builder(mContext, channel.getId());
        } else {
            builder = new NotificationCompat.Builder(mContext).setPriority(NotificationCompat.PRIORITY_DEFAULT);
        }
        //設定動作為開啟指定活動
        Intent mainActIntent = new Intent(mContext, MainActivity.class);
        PendingIntent action = NotificationUtil.createPendingIntent(NotificationUtil.IntentOpenType.START_ACTIVITY, mContext, mainActIntent, SERVICE_PENDING_INTENT_ID);
        PendingIntent pi = PendingIntent.getActivity(mContext, 99 /*request code*/,
                mainActIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mSession.setSessionActivity(pi);

        //設定通知內容
        MediaStyle mediaStyle = new MediaStyle()
                .setMediaSession(mSession.getSessionToken())
                .setShowActionsInCompactView(0) //顯示按鈕
                .setShowCancelButton(true) // Add a cancel button
                .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(mContext,
                        PlaybackStateCompat.ACTION_STOP))
                ;
        builder.setSmallIcon(SERVICE_NOTIFICATION_PICTURE_RESOURCE)
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(mSession.getController().getSessionActivity())
                // Add media control buttons that invoke intents in your media service
                // Add a pause button
                .addAction(new NotificationCompat.Action(
                        R.drawable.ic_pause_black_24dp, "pause",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(mContext,
                                PlaybackStateCompat.ACTION_PLAY_PAUSE)))
                // Apply the media style template
                .setStyle(mediaStyle)
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(mContext, PlaybackStateCompat.ACTION_STOP))
                .build();
//        NotificationUtil.SendNotification(mContext, builder, SERVICE_NOTIFICATION_ID);
        startForeground(SERVICE_NOTIFICATION_ID, builder.build());
    }


}
