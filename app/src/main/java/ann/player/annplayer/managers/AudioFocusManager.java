package ann.player.annplayer.managers;

import android.content.Context;
import android.media.AudioManager;

import ann.player.annplayer.interfaces.IAudioFocusManagerListener;

/**
 * Created by AP on 26/05/14.
 */
public class AudioFocusManager implements AudioManager.OnAudioFocusChangeListener {

    private final AudioManager mAudioManager;
    private final Context mContext;
    private final IAudioFocusManagerListener mListener;

    public AudioFocusManager(Context ctx, IAudioFocusManagerListener listener) {
        mContext = ctx;
        mListener = listener;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    public boolean requestFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN);
    }

    public boolean abandonFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                mAudioManager.abandonAudioFocus(this);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (mListener == null) return;
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                mListener.onFocusGain();
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                mListener.onFocusLoss();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                mListener.onFocusLossTransient();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                mListener.onFocusLossTransientCanDuck();
                break;
        }
    }
}
