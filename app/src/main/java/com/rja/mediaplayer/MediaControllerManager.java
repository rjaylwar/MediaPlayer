package com.rja.mediaplayer;

import android.content.ComponentName;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

/**
 * Created by rjaylward on 4/8/16 for Bandsintown
 */
public class MediaControllerManager {

    MediaBrowserCompat mMediaBrowser;
    MediaControllerCompat mMediaController;
    MediaControllerCompat.TransportControls mTransportControls;
    AppCompatActivity mActivity;
    PlaybackStateCompat mPlaybackState;
    MediaButtonSet mMediaButtonSet;

    OnQueueChangedListener mQueueChangedListener;
    OnMediaControllerConnected mMediaControllerListener;

    public MediaControllerManager(AppCompatActivity activity, MediaButtonSet mediaButtonSet, OnQueueChangedListener queueChangedListener, OnMediaControllerConnected mediaControllerListener) {
        mQueueChangedListener = queueChangedListener;
        mMediaButtonSet = mediaButtonSet;
        mMediaControllerListener = mediaControllerListener;
        mMediaBrowser = new MediaBrowserCompat(activity, new ComponentName(activity, MediaPlayerService.class), mConnectionCallback, null);
        mActivity = activity;
    }

    public void onItemSelected(MediaSessionCompat.QueueItem item) {
        onItemSelected(item.getQueueId());
//        mTransportControls.playFromUri();
    }

    public void onItemSelected(long id) {
        if(mTransportControls != null) {
            mTransportControls.skipToQueueItem(id);
//        mTransportControls.playFromUri();
        }
    }

    @SuppressWarnings("FieldCanBeLocal")
    private MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    if (mMediaBrowser.getSessionToken() == null) {
                        throw new IllegalArgumentException("No Session token");
                    }

                    Print.log("onConnected: session token ", mMediaBrowser.getSessionToken());

                    try {
                        mMediaController = new MediaControllerCompat(mActivity, mMediaBrowser.getSessionToken());
                        mActivity.setSupportMediaController(mMediaController);
                        if(mMediaControllerListener != null)
                            mMediaControllerListener.onMediaControllerConnected(mMediaController);
                    } catch(RemoteException e) {
                        e.printStackTrace();
                        //TODO handle this exception
                    }

                    mTransportControls = mMediaController.getTransportControls();
                    mMediaController.registerCallback(mSessionCallback);

                    mPlaybackState = mMediaController.getPlaybackState();

                    mMediaButtonSet.updateControls(mTransportControls);
                    mMediaButtonSet.updateState(mPlaybackState);

                    List<MediaSessionCompat.QueueItem> queue = mMediaController.getQueue();

                    if(mQueueChangedListener != null)
                        mQueueChangedListener.onQueueViewShouldUpdate(queue);

                    onThePlaybackStateChanged(mPlaybackState);
                }

                @Override
                public void onConnectionFailed() {
                    Print.log("onConnectionFailed");

                    mMediaControllerListener.onMediaControllerDisconected();
                }

                @Override
                public void onConnectionSuspended() {
                    Print.log("onConnectionSuspended");
                    mMediaController.unregisterCallback(mSessionCallback);
                    mTransportControls = null;
                    mMediaController = null;
                    mActivity.setSupportMediaController(null);
                }
            };

    private MediaControllerCompat.Callback mSessionCallback = new MediaControllerCompat.Callback() {

        @Override
        public void onSessionDestroyed() {
            Print.log("Session destroyed. Need to fetch a new Media Session");
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            if (state == null) {
                return;
            }
            Print.log("Received playback state change to state ", state.getState());
            mPlaybackState = state;
            onThePlaybackStateChanged(state);
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            Print.log("onQueueChanged ", queue);
            if(mQueueChangedListener != null)
                mQueueChangedListener.onQueueChanged(queue);
        }
    };

    private void onThePlaybackStateChanged(PlaybackStateCompat state) {
        Print.log("onPlaybackStateChanged ", state);

        mMediaButtonSet.updateState(state);
    }

    public interface OnQueueChangedListener {
        void onQueueChanged(List<MediaSessionCompat.QueueItem> queue);
        void onQueueViewShouldUpdate(List<MediaSessionCompat.QueueItem> queue);
    }

//    if (queue != null) {
//        mQueueAdapter.clear();
//        mQueueAdapter.notifyDataSetInvalidated();
//        mQueueAdapter.addAll(queue);
//        mQueueAdapter.notifyDataSetChanged();
//    }

    public interface OnMediaControllerConnected {
        void onMediaControllerConnected(MediaControllerCompat mediaControllerCompat);
        void onMediaControllerDisconected();
    }

    public void connect() {
        Print.log("Media browser connect is being called");
        mMediaBrowser.connect();
    }

    public void disconnect() {
        Print.log("Media browser disconnect is being called");
        mMediaBrowser.disconnect();
    }

    public MediaBrowserCompat getMediaBrowser() {
        return mMediaBrowser;
    }
}
