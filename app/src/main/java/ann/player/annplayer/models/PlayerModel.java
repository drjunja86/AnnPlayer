package ann.player.annplayer.models;

import android.util.Log;

import java.util.ArrayList;

import ann.player.annplayer.interfaces.IPlayerModelListener;

public class PlayerModel {

    private int mTrackId;
    private boolean mIsPlaying = false; // shows that audio is playing or paused
    private boolean mListRepeat = false;
    private boolean mIsPaused = false; // shows that audio paused
    private int mTrackDuration = 0;
    private String mListRootFolder;
    private int mPosition = 0;
    private FolderItem[] mTrackList;
    private IPlayerModelListener mListener;
    private ArrayList<Integer> mStoredShuffleList;
    private ArrayList<Integer> mStoredShuffleListOnePlaylist;
    private static final int MAX_SHUFFLE_LIST_SIZE = 500;

	public PlayerModel() {
        mTrackId = 0;
	}

    public void setModelListener(IPlayerModelListener listener) {
        mListener = listener;
    }

	public void play() {
        mIsPlaying = true;
        mIsPaused = false;
        if (mStoredShuffleList == null) {
            mStoredShuffleList = new ArrayList<Integer>();
            mStoredShuffleListOnePlaylist = new ArrayList<Integer>();
        } else {
            mStoredShuffleList.clear();
            mStoredShuffleListOnePlaylist.clear();
        }
        if (mTrackList == null || mTrackId >= mTrackList.length) {
            stop();
            return;
        }

        if (mListener != null) mListener.onPlayingStarted();
	}

	public void resume() {
        if (!mIsPaused) return;
        mIsPaused = false;
        if (mListener != null) mListener.onPlayingResumed();
	}

    public void pause() {
        if (mIsPaused) return;
        mIsPaused = true;
        if (mListener != null) mListener.onPlayingPaused();
    }

	public void stop() {
        mIsPaused = mIsPlaying = false;
        mStoredShuffleList.clear();
        mStoredShuffleListOnePlaylist.clear();
        if (mListener != null) mListener.onPlayingStopped();
	}

    public void finish() {
        mIsPaused = mIsPlaying = false;
        if (mListener != null) mListener.onPlayingFinished();
    }

	public boolean isPlaying() {
		return mIsPlaying;
	}

    public int getTrackId() {
        return mTrackId;
    }

    /**
     * This method is called when one track is changed with another. In this case
     * play() method will not be called;
     * @param trackId track id
     */
    public void setTrackId(int trackId) {
        mTrackId = trackId;
        if (mTrackId >= mTrackList.length) {
            if (mListRepeat) mTrackId = 0;
            else {
                mTrackId = mTrackList.length - 1;
                return;
            }
        } else if (mTrackId < 0) {
            if (mListRepeat) mTrackId = mTrackList.length - 1;
            else {
                mTrackId = 0;
                return;
            }
        }

        mIsPaused = false;
        mTrackDuration = mTrackList[mTrackId].song.duration;
        mPosition = 0;
        if (mListener != null) mListener.onTrackChanged();
    }

    public void setTrackList(FolderItem[] trackList, String listRootFolder) {
        mTrackList = trackList;
        mListRootFolder = listRootFolder;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position, boolean userChange) {
        if (position < 0) position = 0;
        else if (position > mTrackDuration) position = mTrackDuration;
        mPosition = position;
        if (mListener != null) mListener.onPositionChanged(userChange);
    }

    public Song getCurrentSong() {
        return mTrackList[mTrackId].song;
    }

    public String getListRootFolder() {
        return mListRootFolder;
    }

    public void setNextTrack(boolean fromUser, boolean repeatPlaylist, boolean shuffle) {
        mListRepeat = repeatPlaylist;
        if (shuffle) {
            if (!mListRepeat && mStoredShuffleListOnePlaylist.size() >= mTrackList.length - 1) {
                if (!fromUser) stop();
                return;
            }
            mStoredShuffleList.add(mTrackId);
            mStoredShuffleListOnePlaylist.add(mTrackId);
            if (mStoredShuffleList.size() > MAX_SHUFFLE_LIST_SIZE) mStoredShuffleList.remove(0);

            int nextTrack;
            if (mStoredShuffleListOnePlaylist.size() >= mTrackList.length && mListRepeat) {
                mStoredShuffleListOnePlaylist.clear();
            }

            do {
                nextTrack = (int) Math.round(Math.random()*(mTrackList.length - 1));
                Log.i("PlayerModel", "nextTrack = "+nextTrack);
                Log.i("PlayerModel", "mStoredShuffleListOnePlaylist.contains(nextTrack) = "+mStoredShuffleListOnePlaylist.contains(nextTrack));
            } while (mStoredShuffleListOnePlaylist.contains(nextTrack));
            setTrackId(nextTrack);
        } else {
            setTrackId(++mTrackId);
            if (!fromUser && mTrackId == mTrackList.length - 1) // means the end of the list
                stop();
        }
    }

    public void setPrevTrack(boolean repeatPlaylist, boolean shuffle) {
        mListRepeat = repeatPlaylist;
        if (shuffle) {
            if (mStoredShuffleListOnePlaylist.contains(mTrackId))
                mStoredShuffleListOnePlaylist.remove(mStoredShuffleListOnePlaylist.indexOf(mTrackId));
            if (mStoredShuffleList.size() > 0)
                setTrackId(mStoredShuffleList.remove(mStoredShuffleList.size() - 1));
        } else {
            setTrackId(--mTrackId);
        }
    }

    public boolean isPaused() {
        return mIsPaused;
    }
}
