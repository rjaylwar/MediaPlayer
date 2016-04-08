package com.rja.mediaplayer;

public class MusicProvider {

//    VolleyContext mVolleyContext;
//
//    public MusicProvider(VolleyContext volleyContext) {
//        mVolleyContext = volleyContext;
//    }
//
//    public void getMediaItem(String artistName, OnCompleteListener<MediaItem> onCompleteListener) {
//        SpotifyPreviewHelper helper = new SpotifyPreviewHelper();
//        helper.startPreview(artistName, onCompleteListener);
//    }
//
//    public void getSpotifyArtistSearch(String artistName, VolleyRequestListener<SpotifyArtistSearchResponse> uiListener) {
//        try {
//            artistName = URLEncoder.encode(artistName, "UTF-8");
//        }
//        catch(UnsupportedEncodingException e) {
//            Print.log(e.toString());
//            artistName = artistName.replace(" ", "%d");
//        }
//
//        String url = String.format("https://api.spotify.com/v1/search?q=%s&type=artist", artistName);
//        Print.log("making spotify search request", url);
//        GsonVolleyRequester<SpotifyArtistSearchResponse> requester = new GsonVolleyRequester<>(mVolleyContext, SpotifyArtistSearchResponse.class);
//        requester.makeGetRequest(mVolleyContext, url, uiListener);
//    }
//
//    public void getSpotifyTrackForArtistId(String spotifyId, VolleyRequestListener<SpotifyArtistResponse> uiListener) {
//        String url = String.format("https://api.spotify.com/v1/artists/%s/top-tracks?country=US&limit=3", spotifyId);
//        Print.log("making spotify tracks request", url);
//        GsonVolleyRequester<SpotifyArtistResponse> requester = new GsonVolleyRequester<>(mVolleyContext, SpotifyArtistResponse.class);
//        requester.makeGetRequest(mVolleyContext, url, uiListener);
//    }
//
//    private class SpotifyArtistResponse implements ApiResponse {
//
//        @SerializedName("tracks")
//        private ArrayList<SpotifyTrack> mTracks;
//
//        public ArrayList<SpotifyTrack> getTracks() {
//            return mTracks != null ? mTracks : new ArrayList<SpotifyTrack>();
//        }
//
//        @Override
//        public void saveResponse(Context context) {}
//
//        class SpotifyTrack {
//
//            @SerializedName("id")
//            private String mId;
//
//            @SerializedName("name")
//            private String mName;
//
//            @SerializedName("uri")
//            private String mUri;
//
//            @SerializedName("preview_url")
//            private String mPreviewUrl;
//
//            public String getId() {
//                return mId;
//            }
//
//            public String getName() {
//                return mName;
//            }
//
//            public String getUri() {
//                return mUri;
//            }
//
//            public String getPreviewUrl() {
//                return mPreviewUrl;
//            }
//
//        }
//    }
//
//    public class SpotifyArtistSearchResponse implements ApiResponse {
//
//        @SerializedName("artists")
//        SpotifyResponse mResponse;
//
//        public ArrayList<Artist> getArtist() {
//            if(mResponse != null)
//                return mResponse.getArtists();
//            else
//                return new ArrayList<>();
//        }
//
//        public Artist getExactMatch(String artistName) {
//            if(mResponse != null && mResponse.getArtists() != null) {
//                for(Artist artist : mResponse.getArtists()) {
//                    if(artistName.toLowerCase().equalsIgnoreCase(artist.getName()))
//                        return artist;
//                }
//                return null;
//            }
//            else
//                return null;
//        }
//
//        @Override
//        public void saveResponse(Context context) { }
//
//        class SpotifyResponse {
//
//            @SerializedName("items")
//            private ArrayList<Artist> mArtists;
//
//            public ArrayList<Artist> getArtists() {
//                return mArtists;
//            }
//        }
//
//        class Artist {
//
//            @SerializedName("name")
//            private String mName;
//
//            @SerializedName("id")
//            private String mId;
//
//            public String getId() {
//                return mId;
//            }
//
//            public String getName() {
//                return mName;
//            }
//        }
//    }
//
//    public class SpotifyPreviewHelper {
//
//        private String mCurrentArtistName;
//        private OnCompleteListener<MediaItem> mCompleteListener;
//
//        private HashMap<String, SpotifyArtistResponse.SpotifyTrack> mCache = new HashMap<>();
//
//        public void startPreview(String artistName, OnCompleteListener<MediaItem> onCompleteListener) {
//            mCurrentArtistName = artistName.toLowerCase();
//            mCompleteListener = onCompleteListener;
//
//            if(mCache.containsKey(mCurrentArtistName))
//                onTrackDownloaded(mCache.get(mCurrentArtistName));
//            else
//                getArtists(artistName);
//        }
//
//        private void getArtists(final String artistName) {
//            getSpotifyArtistSearch(artistName, new VolleyRequestListener<SpotifyArtistSearchResponse>() {
//
//                @Override
//                public void onResponse(SpotifyArtistSearchResponse response) {
//                    getPreviewUrl(response.getExactMatch(artistName).getId());
//                }
//
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    Print.log("Error getting artists", error.toString());
//                    mCompleteListener.onComplete(null, error);
//                }
//
//            });
//        }
//
//        private void getPreviewUrl(String id) {
//            Print.log("Spotify id", id);
//            if(id != null) {
//                getSpotifyTrackForArtistId(id, new VolleyRequestListener<SpotifyArtistResponse>() {
//
//                    @Override
//                    public void onResponse(SpotifyArtistResponse response) {
//                        if(response != null && response.getTracks().size() > 0) {
//                            onTrackDownloaded(response.getTracks().get(0));
//                        }
//                        else {
//                            Print.log("Error getting preview url, no preview url");
//                            if(mCompleteListener != null)
//                                mCompleteListener.onComplete(null, new Exception("Error getting preview url, no preview url"));
//                        }
//                    }
//
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Print.log("Error getting preview url", error.toString());
//
//                        if(mCompleteListener != null)
//                            mCompleteListener.onComplete(null, error);
//                    }
//
//                });
//            }
//        }
//
//        private void onTrackDownloaded(SpotifyArtistResponse.SpotifyTrack track) {
//            mCache.put(mCurrentArtistName, track);
//
//            MediaItem mediaItem = new MediaItem.Builder()
//                    .setUrl(track.getPreviewUrl())
//                    .setTitle(track.getName())
//                    .setArtist(mCurrentArtistName)
//                    .build();
//
//            if(mCompleteListener != null)
//                mCompleteListener.onComplete(mediaItem, null);
//        }
//
//    }
}
