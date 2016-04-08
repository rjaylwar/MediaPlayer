package com.rja.mediaplayer;

import android.media.session.PlaybackState;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;

/**
 * Created by rjaylward on 4/8/16 for Bandsintown
 */
public class MediaButtonSet {

    private View mPlayPause;

    private int mPlayResId;
    private int mPauseResId;

    private View mNext;
    private View mPrev;
    private View mFF;
    private View mRW;
    private View mStop;

    private MediaControllerCompat.TransportControls mTransportControls;
    private PlaybackStateCompat mPlaybackState;

    private MediaButtonSet(View playPause, int playResId, int pauseResId, View next, View prev, View FF, View RW, View stop,
                           MediaControllerCompat.TransportControls transportControls, PlaybackStateCompat playbackState) {

        mPlayPause = playPause;
        mNext = next;
        mPrev = prev;
        mFF = FF;
        mRW = RW;
        mStop = stop;
        mTransportControls = transportControls;
        mPlaybackState = playbackState;

        mPlayResId = playResId;
        mPauseResId = pauseResId;

        setUp();
    }

    public void updateState(PlaybackStateCompat playbackState) {
        mPlaybackState = playbackState;
        enableButtons(mPlaybackState);
    }

    public void updateControls(MediaControllerCompat.TransportControls transportControls) {
        mTransportControls = transportControls;
    }

    private void setUp() {
        if(mPlayPause != null)
            mPlayPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int state = mPlaybackState == null ?
                            PlaybackStateCompat.STATE_NONE : mPlaybackState.getState();

                    if (state == PlaybackStateCompat.STATE_PAUSED ||
                            state == PlaybackStateCompat.STATE_STOPPED ||
                            state == PlaybackStateCompat.STATE_NONE) {
                        playMedia();
                    } else if (state == PlaybackStateCompat.STATE_PLAYING) {
                        pauseMedia();
                    }
                }
            });
        if(mNext != null)
            mNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    skipToNext();
                }
            });
        if(mPrev != null)
            mPrev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    skipToPrevious();
                }
            });
        if(mFF != null)
            mFF.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fastForward();
                }
            });
        if(mRW != null)
            mRW.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rewind();
                }
            });
        if(mStop != null)
            mStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopMedia();
                }
            });

        if(mPlaybackState != null)
            enableButtons(mPlaybackState);
    }

    private void enableButtons(PlaybackStateCompat state) {
        boolean enablePlay = false;
        StringBuilder statusBuilder = new StringBuilder();
        switch (state.getState()) {
            case PlaybackState.STATE_PLAYING:
                statusBuilder.append("playing");
                enablePlay = false;
                break;
            case PlaybackState.STATE_PAUSED:
                statusBuilder.append("paused");
                enablePlay = true;
                break;
            case PlaybackState.STATE_STOPPED:
                statusBuilder.append("ended");
                enablePlay = true;
                break;
            case PlaybackState.STATE_ERROR:
                statusBuilder.append("error: ").append(state.getErrorMessage());
                break;
            case PlaybackState.STATE_BUFFERING:
                statusBuilder.append("buffering");
                break;
            case PlaybackState.STATE_NONE:
                statusBuilder.append("none");
                enablePlay = false;
                break;
            case PlaybackState.STATE_CONNECTING:
                statusBuilder.append("connecting");
                break;
            default:
                statusBuilder.append(mPlaybackState);
        }

        Print.log(statusBuilder.toString());

        if (enablePlay) {
            mPlayPause.setBackground(ContextCompat.getDrawable(mPlayPause.getContext(), mPlayResId > 0 ? mPlayResId : android.R.drawable.ic_media_play));
        } else {
            mPlayPause.setBackground(ContextCompat.getDrawable(mPlayPause.getContext(), mPauseResId > 0 ? mPauseResId : android.R.drawable.ic_media_pause));
        }

        mPrev.setEnabled((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0);
        mNext.setEnabled((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0);
        mFF.setEnabled((state.getActions() & PlaybackStateCompat.ACTION_FAST_FORWARD) != 0);
        mRW.setEnabled((state.getActions() & PlaybackStateCompat.ACTION_REWIND) != 0);
        mStop.setEnabled((state.getActions() & PlaybackStateCompat.ACTION_STOP) != 0);
    }

    private void playMedia() {
        if(mTransportControls != null) {
            mTransportControls.play();
        }
    }

    private void pauseMedia() {
        if(mTransportControls != null) {
            mTransportControls.pause();
        }
    }

    private void skipToPrevious() {
        if(mTransportControls != null) {
            mTransportControls.skipToPrevious();
        }
    }

    private void skipToNext() {
        if(mTransportControls != null) {
            mTransportControls.skipToNext();
        }
    }

    private void fastForward() {
        if(mTransportControls != null) {
            mTransportControls.fastForward();
        }
    }

    private void rewind() {
        if(mTransportControls != null) {
            mTransportControls.rewind();
        }
    }

    private void stopMedia() {
        if(mTransportControls != null) {
            mTransportControls.stop();
        }
    }

    public static class Builder {

        private View mPlayPause;
        private View mNext;
        private View mPrev;
        private View mFF;
        private View mRW;
        private View mStop;

        private int mPlayResId;
        private int mPauseResId;

        private MediaControllerCompat.TransportControls mTransportControls;
        private PlaybackStateCompat mPlaybackState;

        public Builder(MediaControllerCompat mediaControllerCompat) {
            if(mediaControllerCompat != null) {
                mTransportControls = mediaControllerCompat.getTransportControls();
                mPlaybackState = mediaControllerCompat.getPlaybackState();
            }
        }


        public MediaButtonSet build() {
            return new MediaButtonSet(mPlayPause, mPlayResId, mPauseResId, mNext, mPrev, mFF, mRW, mStop, mTransportControls, mPlaybackState);
        }

        public Builder setPlayPause(View playPause) {
            mPlayPause = playPause;
            return this;
        }

        public Builder setNext(View next) {
            mNext = next;
            return this;
        }

        public Builder setPrev(View prev) {
            mPrev = prev;
            return this;
        }

        public Builder setFF(View FF) {
            mFF = FF;
            return this;
        }

        public Builder setRW(View RW) {
            mRW = RW;
            return this;
        }

        public Builder setStop(View stop) {
            mStop = stop;
            return this;
        }

//        public Builder setTransportControls(MediaControllerCompat.TransportControls transportControls) {
//            mTransportControls = transportControls;
//            return this;
//        }

//        public Builder setPlaybackState(PlaybackStateCompat playbackState) {
//            mPlaybackState = playbackState;
//            return this;
//        }

        public Builder setPauseResId(int pauseResId) {
            mPauseResId = pauseResId;
            return this;
        }

        public Builder setPlayResId(int playResId) {
            mPlayResId = playResId;
            return this;
        }

    }
}
