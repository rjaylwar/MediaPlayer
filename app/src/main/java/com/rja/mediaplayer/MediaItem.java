package com.rja.mediaplayer;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaItem implements Parcelable {

    public static final String MEDIA_ITEM_EXTRA = "media_item_extra";

    private String mUrl;
    private String mArtist;
    private String mAlbum;
    private String mTitle;
    private String mSubtitle;
    private String mSmallImageUrl;
    private String mLargeImageUrl;

    protected MediaItem(Parcel in) {
        mUrl = in.readString();
        mArtist = in.readString();
        mAlbum = in.readString();
        mTitle = in.readString();
        mSubtitle = in.readString();
        mSmallImageUrl = in.readString();
        mLargeImageUrl = in.readString();
    }

    public static final Creator<MediaItem> CREATOR = new Creator<MediaItem>() {

        @Override
        public MediaItem createFromParcel(Parcel in) {
            return new MediaItem(in);
        }

        @Override
        public MediaItem[] newArray(int size) {
            return new MediaItem[size];
        }
    };

    public String getUrl() {
        return mUrl;
    }

    public String getArtist() {
        return mArtist;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSubtitle() {
        return mSubtitle;
    }

    public String getSmallImageUrl() {
        return mSmallImageUrl;
    }

    public String getLargeImageUrl() {
        return mLargeImageUrl;
    }

    public String getAlbum() {
        return mAlbum;
    }

    private MediaItem(String url, String artist, String album, String title, String subtitle, String smallImageUrl, String largeImageUrl) {
        mUrl = url;
        mArtist = artist;
        mAlbum = album;
        mTitle = title;
        mSubtitle = subtitle;
        mSmallImageUrl = smallImageUrl;
        mLargeImageUrl = largeImageUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUrl);
        dest.writeString(mArtist);
        dest.writeString(mAlbum);
        dest.writeString(mTitle);
        dest.writeString(mSubtitle);
        dest.writeString(mSmallImageUrl);
        dest.writeString(mLargeImageUrl);
    }

    public static class Builder {

        private String mUrl;
        private String mArtist;
        private String mAlbum;
        private String mTitle;
        private String mSubtitle;
        private String mSmallImageUrl;
        private String mLargeImageUrl;

        public Builder setUrl(String url) {
            mUrl = url;
            return this;
        }

        public Builder setArtist(String artist) {
            mArtist = artist;
            return this;
        }

        public Builder setAlbum(String album) {
            mAlbum = album;
            return this;
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setSubtitle(String subtitle) {
            mSubtitle = subtitle;
            return this;
        }

        public Builder setSmallImageUrl(String smallImageUrl) {
            mSmallImageUrl = smallImageUrl;
            return this;
        }

        public Builder setLargeImageUrl(String largeImageUrl) {
            mLargeImageUrl = largeImageUrl;
            return this;
        }

        public MediaItem build() {
            return new MediaItem(mUrl, mArtist, mAlbum, mTitle, mSubtitle, mSmallImageUrl, mLargeImageUrl);
        }
    }
}
