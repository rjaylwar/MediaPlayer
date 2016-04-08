package com.rja.mediaplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MediaPlayerService extends MediaBrowserServiceCompat implements Playback.Callback {

    private static final int STOP_DELAY = 30000;
    private static final int FF_RW_TIME_SKIP = 10000;
    public static final String ACTION_CMD = "com.rja.etaThetaTau.action.CMD";
    public static final String CMD_EXTRA = "command_extra";

    public static final String PLAY = "play";
    public static final String PAUSE = "pause";
    public static final String STOP = "stop";
    public static final String CMD_STOP_CASTING = "stop_casting";
    public static final java.lang.String EXTRA_CONNECTED_CAST = "extra_connected_cast";
    public static final String MEDIA_ID_ROOT = "media_player_service_root_";

    private MediaSessionCompat mSession;
    private MediaNotificationManager mMediaNotificationManager;
    private Playback mPlayback;
//    private ArrayList<MediaSessionCompat.QueueItem> mQueueItems = new ArrayList<>();

    private boolean mServiceStarted;

    private final DelayedStopHandler mDelayedStopHandler = new DelayedStopHandler(this);
    private int mCurrentIndexOnQueue = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mSession = new MediaSessionCompat(this, MediaPlayerService.class.getCanonicalName());
        setSessionToken(mSession.getSessionToken());
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mSession.setCallback(mCallback);

        try {
            //TODO figure out a way to set the intent manually...
            mMediaNotificationManager = new MediaNotificationManager(this);
        } catch(RemoteException e) {
            e.printStackTrace();
        }

        mPlayback = new Playback(this);
        mPlayback.setState(PlaybackStateCompat.STATE_NONE);
        mPlayback.setCallback(this);
        mPlayback.start();

        updatePlaybackState(null);
    }

    MediaSessionCompat.Callback mCallback = new MediaSessionCompat.Callback() {

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);

            if(mPlayback != null)
                mPlayback.seekTo((int) pos);
        }

        @Override
        public void onStop() {
            super.onStop();
            stopMedia(null);
        }

        @Override
        public void onRewind() {
            super.onRewind();

            if(mPlayback != null)
                mPlayback.seekTo(mPlayback.getCurrentStreamPosition() - FF_RW_TIME_SKIP);
        }

        @Override
        public void onFastForward() {
            super.onFastForward();

            if(mPlayback != null)
                mPlayback.seekTo(mPlayback.getCurrentStreamPosition() + FF_RW_TIME_SKIP);
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();

            skipToPreviousMedia();
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();

            skipToNextMedia();
        }

        @Override
        public void onPause() {
            super.onPause();

            pauseMedia();
        }

        @Override
        public void onSkipToQueueItem(long id) {
            super.onSkipToQueueItem(id);

            skipToQueueItem(id);
        }

        @Override
        public void onPlay() {
            super.onPlay();
            playMedia();
        }

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            return super.onMediaButtonEvent(mediaButtonEvent);
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            super.onPlayFromUri(uri, extras);

            //TODO...
        }
    };

    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO figure out if these media session calls are necessary...
        mSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED, 0, 1)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
                .build());

        String action = intent.getAction();
        String command = intent.getStringExtra(CMD_EXTRA);
        if (action != null && command != null && ACTION_CMD.equals(action)) {
            switch(command) {
                case PAUSE :
                    pauseMedia();
                    break;
                case PLAY :
                    if(mPlayback != null)
                        playMedia();
                    break;
                case STOP :
                    stopMedia(null);
            }
        } else {
            // Try to handle the intent as a media button event wrapped by MediaButtonReceiver
            MediaButtonReceiver.handleIntent(mSession, intent);
        }

        // Reset the delay handler to enqueue a message to stop the service if
        // nothing is playing.
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(String parentId, Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }

    public void prepareMedia() {
        mSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_BUFFERING, 0, 0)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
                .build());
    }

    public void playMedia() {
        Print.log("handlePlayRequest: mState=" + mPlayback.getState());

//        mSession.setPlaybackState(new PlaybackStateCompat.Builder()
//                .setState(PlaybackStateCompat.STATE_PLAYING, mPlayback.getCurrentStreamPosition(), 1)
//                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
//                .build());

        mDelayedStopHandler.removeCallbacksAndMessages(null);
        if (!mServiceStarted) {
            Print.log("Starting music service");
            // The MusicService needs to keep running even after the calling MediaBrowser
            // is disconnected. Call startService(Intent) and then stopSelf(..) when we no longer
            // need to play media.
            startService(new Intent(getApplicationContext(), MediaPlayerService.class));
            mServiceStarted = true;
        }

        if (!mSession.isActive()) {
            mSession.setActive(true);
        }

        if (isIndexPlayable(mCurrentIndexOnQueue, getAudioQueue())) {
            updateMetadata();
            mPlayback.play(getAudioQueue().get(mCurrentIndexOnQueue));
        }
    }

    private void updateMetadata() {
        //TODO... queue items -> metadata transformer
        MediaSessionCompat.QueueItem queueItem = getAudioQueue().get(mCurrentIndexOnQueue);

        if(queueItem != null && queueItem.getDescription() != null) {

            MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

            String title = queueItem.getDescription().getTitle() != null ? queueItem.getDescription().getTitle().toString() : "Audio";
            String subtitle = queueItem.getDescription().getSubtitle() != null ? queueItem.getDescription().getSubtitle().toString() : "";

            metadataBuilder
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, subtitle)
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mPlayback != null ? mPlayback.getCurrentDuration() : -1);

            if(queueItem.getDescription().getExtras() != null) {
                MediaItem item = queueItem.getDescription().getExtras().getParcelable(MediaItem.MEDIA_ITEM_EXTRA);

                if(item != null)
                    metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, item.getArtist())
                            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, item.getAlbum())
                            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, item.getLargeImageUrl());
            }

            mSession.setMetadata(metadataBuilder.build());
        }

    }

    public void pauseMedia() {
        if(mPlayback != null && mPlayback.isPlaying()) {
            mPlayback.pause();

//            mSession.setPlaybackState(new PlaybackStateCompat.Builder()
//                    .setState(PlaybackStateCompat.STATE_PAUSED, mPlayback.getCurrentStreamPosition(), 0)
//                    .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
//                    .build());

            mDelayedStopHandler.removeCallbacksAndMessages(null);
            mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        }
    }

    private void skipToNextMedia() {
        //TODO...
        if(mPlayback != null) {
//            if(mPlayback.isPlaying())
//                mPlayback.pause();

            mCurrentIndexOnQueue++;
            if(isIndexPlayable(mCurrentIndexOnQueue, getAudioQueue())) {
                updateMetadata();
                mPlayback.play(getAudioQueue().get(mCurrentIndexOnQueue));
            }
        }
    }

    private void skipToPreviousMedia() {
        //TODO...
        if(mPlayback != null) {
//            if(mPlayback.isPlaying())
//                mPlayback.pause();

            mCurrentIndexOnQueue--;
            if(isIndexPlayable(mCurrentIndexOnQueue, getAudioQueue())) {
                updateMetadata();
                mPlayback.play(getAudioQueue().get(mCurrentIndexOnQueue));
            }
        }
    }

    private void skipToQueueItem(long id) {
        int index = indexOfIdInQueue(getAudioQueue(), id);
        if(index >= 0) {

            if(mPlayback != null) {
//                if(mPlayback.isPlaying())
//                    mPlayback.pause();

                mCurrentIndexOnQueue = index;
                if(isIndexPlayable(mCurrentIndexOnQueue, getAudioQueue())) {
                    updateMetadata();
                    mPlayback.play(getAudioQueue().get(mCurrentIndexOnQueue));
                }
            }
        }

    }

    private void stopMedia(String withError) {
        Print.log( "handleStopRequest: mState=" + mPlayback.getState() + " error=", withError);
        mPlayback.stop(true);
        mSession.setActive(false);

        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);

        updatePlaybackState(withError);

        // service is no longer necessary. Will be started again if needed.
        stopSelf();
        mServiceStarted = false;
    }

    @Override
    public void onCompletion() {
        if (getAudioQueue() != null && !getAudioQueue().isEmpty()) {
            // In this sample, we restart the playing queue when it gets to the end:
            mCurrentIndexOnQueue++;
            if (mCurrentIndexOnQueue >= getAudioQueue().size()) {
                mCurrentIndexOnQueue = 0;
            }
            playMedia();
        }
        else
            stopMedia(null);
    }

    @Override
    public void onPlaybackStatusChanged(int state) {
        updatePlaybackState(null);
    }

    @Override
    public void onError(String error) {
        updatePlaybackState(error);
    }

    private void updatePlaybackState(String error) {
        Print.log("updatePlaybackState, playback state=" + mPlayback.getState());
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (mPlayback != null && mPlayback.isConnected()) {
            position = mPlayback.getCurrentStreamPosition();
        }

        //noinspection ResourceType
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions());

//        setCustomAction(stateBuilder);
        int state = mPlayback.getState();

        // If there is an error message, send it to the playback state:
        if (error != null) {
            // Error states are really only supposed to be used for errors that cause playback to
            // stop unexpectedly and persist until the user takes action to fix it.
            stateBuilder.setErrorMessage(error);
            state = PlaybackStateCompat.STATE_ERROR;
        }

        //noinspection ResourceType
        stateBuilder.setState(state, position, 1.0f, SystemClock.elapsedRealtime());

        // Set the activeQueueItemId if the current index is valid.
        if (isIndexPlayable(mCurrentIndexOnQueue, getAudioQueue())) {
            MediaSessionCompat.QueueItem item = getAudioQueue().get(mCurrentIndexOnQueue);
            stateBuilder.setActiveQueueItemId(item.getQueueId());
        }

        mSession.setPlaybackState(stateBuilder.build());

        if(state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_PAUSED) {
            if(mMediaNotificationManager != null)
                mMediaNotificationManager.startNotification();
        }
    }

//    private void setCustomAction(PlaybackState.Builder stateBuilder) {
//        MediaMetadataCompat currentMusic = getCurrentPlayingMusic();
//        if (currentMusic != null) {
//            // Set appropriate "Favorite" icon on Custom action:
//            String musicId = currentMusic.getString(MediaMetadata.METADATA_KEY_MEDIA_ID);
//            int favoriteIcon = R.drawable.ic_star_off;
//            if (mMusicProvider.isFavorite(musicId)) {
//                favoriteIcon = R.drawable.ic_star_on;
//            }
//            Print.log("updatePlaybackState, setting Favorite custom action of music ",
//                    musicId, " current favorite=", mMusicProvider.isFavorite(musicId));
//            stateBuilder.addCustomAction(CUSTOM_ACTION_THUMBS_UP, getString(R.string.favorite),
//                    favoriteIcon);
//        }
//    }

    private long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH;
        if (getAudioQueue() == null || getAudioQueue().isEmpty()) {
            return actions;
        }
        if (mPlayback.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        }
        if (mCurrentIndexOnQueue > 0) {
            actions |= PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
        }
        if (mCurrentIndexOnQueue < getAudioQueue().size() - 1) {
            actions |= PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        }
        return actions;
    }

    @Override
    public void onDestroy() {
        Print.log("MediaPlayerService onDestroy");
        // Service is being killed, so make sure we release our resources
        stopMedia(null);

        if(mMediaNotificationManager != null)
            mMediaNotificationManager.stopNotification();

        mDelayedStopHandler.removeCallbacksAndMessages(null);
        // Always release the MediaSession to clean up resources
        // and notify associated MediaController(s).
        mSession.release();
    }

    private ArrayList<MediaSessionCompat.QueueItem> getAudioQueue() {
        return AudioQueue.getInstance().getQueueItems();
    }

    /**
     * A simple handler that stops the service if playback is not active (playing)
     */
    private static class DelayedStopHandler extends Handler {
        private final WeakReference<MediaPlayerService> mWeakReference;

        private DelayedStopHandler(MediaPlayerService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MediaPlayerService service = mWeakReference.get();
            if(service != null && service.mPlayback != null) {
                if(service.mPlayback.isPlaying()) {
                    Print.log("Ignoring delayed stop since the media player is in use.");
                    return;
                }
                Print.log("Stopping service with delay handler.");
                service.stopSelf();
            }
        }
    }

    /**
     * Static helper classes
     */
    public static boolean isIndexPlayable(int index, ArrayList queue) {
        return (queue != null && index >= 0 && index < queue.size());
    }

    public static int indexOfIdInQueue(ArrayList<MediaSessionCompat.QueueItem> queueItems, long id) {
        if(queueItems != null) {
            for(int i = 0; i < queueItems.size(); i++) {
                MediaSessionCompat.QueueItem item = queueItems.get(i);
                if(item.getQueueId() == id)
                    return i;
            }
        }

        return -1;
    }
}
