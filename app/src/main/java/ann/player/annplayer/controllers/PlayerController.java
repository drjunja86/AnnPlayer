package ann.player.annplayer.controllers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import ann.player.annplayer.interfaces.IAudioFocusManagerListener;
import ann.player.annplayer.interfaces.IPlayerControllerListener;
import ann.player.annplayer.interfaces.IPlayerModelListener;
import ann.player.annplayer.interfaces.IPlayerViewListener;
import ann.player.annplayer.models.FolderItem;
import ann.player.annplayer.models.PlayerModel;
import ann.player.annplayer.services.PlayerService;
import ann.player.annplayer.managers.AudioFocusManager;
import ann.player.annplayer.receivers.MusicIntentReceiver;
import ann.player.annplayer.utils.PreferencesUtils;
import ann.player.annplayer.views.PlayerView;

/**
 * Created by AP on 22/05/14.
 */
public class PlayerController implements IPlayerModelListener, IPlayerViewListener, IAudioFocusManagerListener {

    private static final int POSITION_DELTA = 10000;
    private final Context mContext;
    private PlayerView mView;
    private PlayerModel mModel;
    private IPlayerControllerListener mListener;
    private Receiver mBroadCastReceiver;
    private boolean mPausedBecauseOfFocusLost, mDucking;
    private AudioFocusManager mAudioManager;

    private static final String TAG = PlayerController.class.getSimpleName();

    public PlayerController(Context context, PlayerView view) {
        mContext = context;
        mModel = new PlayerModel();
        mModel.setModelListener(this);
        mView = view;
        mView.setViewListener(this);
        mView.onResume(); // when we are in constructor, application is in resumed phase

        mBroadCastReceiver = new Receiver();
        mAudioManager = new AudioFocusManager(mContext, this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayerService.ACTION_PLAY_FINISHED);
        intentFilter.addAction(PlayerService.ACTION_POST_PROGRESS);
        intentFilter.addAction(MusicIntentReceiver.ACTION_AUDIO_BECOMING_NOISY);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mBroadCastReceiver, intentFilter);
    }

    public void release() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBroadCastReceiver);
        mBroadCastReceiver = null;
        if (mView != null) mView.setViewListener(null);
        if (mModel != null) mModel.setModelListener(null);
        mView = null;
        mModel = null;
        if (mAudioManager != null) mAudioManager.abandonFocus();
        mAudioManager = null;
    }

    public void setControllerListener(IPlayerControllerListener listener) {
        mListener = listener;
    }

    public void onPause() {
        mView.onPause();
    }

    public void onResume() {
        mView.onResume();
    }

    public void updatePlayerView(PlayerView view) {
        if (mView != null) mView.setViewListener(null);
        mView = view;
        mView.setViewListener(this);
        mView.onResume();
        mView.setStatePlaying(mModel.getCurrentSong());
        if (mModel.isPaused()) mView.setPlatyButtonStateReadyToPlay();
        else mView.setPlatyButtonStateReadyToPause();
    }

    public void startPlayingTrackFromPosition(int position) {
        mModel.setTrackId(position);
        if (!mModel.isPlaying()) mModel.play();
    }

    public void setTrackList(FolderItem[] trackList, String listRootFolder) {
        mModel.setTrackList(trackList, listRootFolder);
    }

    @Override
    public void onPlayingStarted() {
        // send action to music service with song
        if (mAudioManager.requestFocus()) PlayerService.startActionPlay(mContext, mModel.getCurrentSong());
        else mModel.stop();
    }

    @Override
    public void onPlayingPaused() {
        // send action to music service
        PlayerService.startActionPause(mContext);
        mView.setPlatyButtonStateReadyToPlay();
    }

    @Override
    public void onPlayingResumed() {
        // send action to music service
        PlayerService.startActionResume(mContext);
        mView.setPlatyButtonStateReadyToPause();
    }

    @Override
    public void onPlayingFinished() {
        if (PreferencesUtils.getRepeatSong(mContext)) mModel.setTrackId(mModel.getTrackId());
        else mModel.setNextTrack(false, PreferencesUtils.getRepeatPlaylist(mContext), PreferencesUtils.getShuffle(mContext));
    }

    @Override
    public void onPlayingStopped() {
        mView.setStateStopped();
        PlayerService.stopPlaying(mContext);
        if (!mAudioManager.abandonFocus()) Log.e(TAG, "Focus cannot be abandoned!");
        mPausedBecauseOfFocusLost = false;
        mDucking = false;
        mListener.onPlayingStopped();
    }

    @Override
    public void onPositionChanged(boolean userChange) {
        mView.updatePosition(mModel.getPosition());
        if (userChange) PlayerService.startActionSeekTo(mContext, mModel.getPosition());
    }

    @Override
    public void onTrackChanged() {
        mView.setStatePlaying(mModel.getCurrentSong());
        // send action to music service with model
        if (mListener != null) mListener.onTrackChanged();
        if (PlayerService.isPlaying()) PlayerService.startActionTrackChanged(mContext, mModel.getCurrentSong());
    }

    @Override
    public void onStopPressed() {
        mModel.stop();
    }

    @Override
    public void onPreviousTrackPressed() {
        mModel.setPrevTrack(PreferencesUtils.getRepeatPlaylist(mContext), PreferencesUtils.getShuffle(mContext));
    }

    @Override
    public void onRewindPressed() {
        mModel.setPosition(mModel.getPosition() - POSITION_DELTA, true);
    }

    @Override
    public void onPlayPressed() {
        mModel.resume();
    }

    @Override
    public void onPausePressed() {
        mModel.pause();
    }

    @Override
    public void onForwardPressed() {
        mModel.setPosition(mModel.getPosition() + POSITION_DELTA, true);
    }

    @Override
    public void onTrackChangeFinished(int progress) {
        mModel.setPosition(progress, true);
    }

    @Override
    public void onNextTrackPressed() {
        mModel.setNextTrack(true, PreferencesUtils.getRepeatPlaylist(mContext), PreferencesUtils.getShuffle(mContext));
    }

    public PlayerModel getModel() {
        return mModel;
    }

    @Override
    public void onFocusGain() {
        Log.i(TAG, "onFocusGain");
        if (mPausedBecauseOfFocusLost) {
            mPausedBecauseOfFocusLost = false;
            onPlayPressed();
        } else if (mDucking) {
            mDucking = false;
            PlayerService.startActionStopDucking(mContext);
        }
    }

    @Override
    public void onFocusLoss() {
        Log.i(TAG, "onFocusLoss");
        onStopPressed();
    }

    @Override
    public void onFocusLossTransient() {
        Log.i(TAG, "onFocusLossTransient");
        if (mModel.isPlaying()) {
            onPausePressed();
            mPausedBecauseOfFocusLost = true;
        }
    }

    @Override
    public void onFocusLossTransientCanDuck() {
        Log.i(TAG, "onFocusLossTransientCanDuck");
        if (mModel.isPlaying()) {
            //
            mDucking = true;
            PlayerService.startActionStartDucking(mContext);
        }
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (PlayerService.ACTION_PLAY_FINISHED.equals(action)) {
                mModel.finish();
            } else if (PlayerService.ACTION_POST_PROGRESS.equals(action)) {
                mModel.setPosition(intent.getIntExtra(PlayerService.EXTRA_PARAM_POSITION, 0), false);
            } else if (MusicIntentReceiver.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
                if (mModel.isPlaying()) onPausePressed();
            }
        }
    }
}
