package com.rja.mediaplayer;

import android.net.Uri;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;

import java.util.ArrayList;

public class AudioQueue {

    private static AudioQueue instance;
    private ArrayList<MediaSessionCompat.QueueItem> mQueueItems = new ArrayList<>();

    private long mLastId = FIRST_ID;
    private static final long FIRST_ID = 0;

    private AudioQueue() {}

    public static void initialize() {
        instance = new AudioQueue();
    }

    public static AudioQueue getInstance() {
        if(instance == null)
            initialize();

        return instance;
    }

    public ArrayList<MediaSessionCompat.QueueItem> getQueueItems() {
        return mQueueItems;
    }

    public long add(MediaItem mediaItem) {
        mLastId++;

        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder();
        builder.setMediaId(mediaItem.getUrl())
                .setTitle(mediaItem.getTitle())
                .setSubtitle(mediaItem.getSubtitle())
                .setDescription(mediaItem.getArtist());

        if(mediaItem.getSmallImageUrl() != null)
            builder.setIconUri(Uri.parse(mediaItem.getSmallImageUrl()));
        if(mediaItem.getUrl() != null)
            builder.setMediaUri(Uri.parse(mediaItem.getUrl()));

        MediaSessionCompat.QueueItem queueItem = new MediaSessionCompat.QueueItem(builder.build(), mLastId);
        mQueueItems.add(queueItem);

        return queueItem.getQueueId();
    }

    public void add(ArrayList<MediaItem> mediaItems) {
        for(MediaItem mediaItem : mediaItems) {
            add(mediaItem);
        }
    }

    public void clear() {
        mQueueItems.clear();
        mLastId = FIRST_ID;
    }

}
