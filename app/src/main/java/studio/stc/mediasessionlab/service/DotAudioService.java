package studio.stc.mediasessionlab.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.io.IOException;

public final class DotAudioService extends Service {

    private final static String TAG = "DotAudioService";

    private MediaSessionCompat mSession;
    private PlaybackStateCompat mPlaybackState;

    private MediaPlayer mMediaPlayer;
    private static final long PLAYBACK_ACTIONS = PlaybackStateCompat.ACTION_PAUSE
            | PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_STOP
            | PlaybackStateCompat.ACTION_PLAY_FROM_URI;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: service created.");
        mSession = new MediaSessionCompat(this, TAG);
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mSession.setCallback(sessionCallback);
        mPlaybackState = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_NONE, 0, 1.0f)
                .setActions(PLAYBACK_ACTIONS)
                .build();
        mSession.setPlaybackState(mPlaybackState);

        SetPlayer();

//        mSession.setActive(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: closing service.");
        mMediaPlayer.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind: bound with " + intent.getComponent().getPackageName());
        mBinder = new DotAudioBinder();
        return mBinder;
//        return null;
    }

    public class DotAudioBinder extends Binder{
        public MediaSessionCompat.Token getToken(){
            return DotAudioService.this.mSession.getSessionToken();
//            return null;
        }
    }

    public DotAudioBinder mBinder;

    private MediaSessionCompat.Callback sessionCallback = new MediaSessionCompat.Callback() {

        @Override
        public void onPlay() {
            Log.i(TAG, "onPlay");
            if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PAUSED) {

                mMediaPlayer.start();

                mPlaybackState = new PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                        .build();
                mSession.setPlaybackState(mPlaybackState);
            }
        }

        @Override
        public void onPause() {
            Log.i(TAG, "onPause");
            if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {

                mMediaPlayer.pause();

                mPlaybackState = new PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PAUSED, 0, 1.0f)
                        .build();
                mSession.setPlaybackState(mPlaybackState);
            }
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            Log.i(TAG, "onPlayFromUri");

            switch (mPlaybackState.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                case PlaybackStateCompat.STATE_PAUSED:
                case PlaybackStateCompat.STATE_NONE:

                    try {
                        mMediaPlayer.reset();
                        mMediaPlayer.setDataSource(DotAudioService.this, uri);
                        mMediaPlayer.prepare();//準備同步
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }

                    mPlaybackState = new PlaybackStateCompat.Builder()
                            .setState(PlaybackStateCompat.STATE_CONNECTING, 0, 1.0f)
                            .build();
                    mSession.setPlaybackState(mPlaybackState);
                    //我们可以保存当前播放音乐的信息，以便客户端刷新UI
                    mSession.setMetadata(
                            new MediaMetadataCompat.Builder()
                                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, extras.getString("title"))
                                    .build()
                    );
                    break;
                default:
                    break;
            }
        }
    };


    private void SetPlayer(){
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(onPreparedListener);
        mMediaPlayer.setOnCompletionListener(onCompletionListener);
    }

    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            mMediaPlayer.start();
            mPlaybackState = new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING,0,1.0f)
                    .build();
            mSession.setPlaybackState(mPlaybackState);
        }
    } ;

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            mPlaybackState = new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_NONE,0,1.0f)
                    .build();
            mSession.setPlaybackState(mPlaybackState);
            mMediaPlayer.reset();
        }
    };

}
