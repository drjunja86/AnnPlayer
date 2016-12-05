package ann.player.annplayer.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import ann.player.annplayer.R;
import ann.player.annplayer.interfaces.IPlayerViewListener;
import ann.player.annplayer.models.Song;
import ann.player.annplayer.utils.FontUtils;

/**
 * Created by AP on 21/05/14.
 *
 */
public class PlayerView extends RelativeLayout implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private SeekBar mPosSeekBar;
    private ImageButton mButPrev;
    private ImageButton mButRew;
    private ImageButton mButPlay;
    private ImageButton mButPause;
    private ImageButton mButStop;
    private ImageButton mButFwd;
    private ImageButton mButNext;
    private TextView mTrackTitle;
    private TextView mTrackDuration;
    private TextView mTrackPosition;
    private IPlayerViewListener mListener;
    private static boolean mVisibility, mButtonReadyToPlay;
    private boolean mTrackInProgress;

    public PlayerView(Context context) {
        super(context);
    }

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void initViews() {
        Log.i("PlayerView", "InitViews called!");
        mPosSeekBar = (SeekBar) findViewById(R.id.player_seek_bar);
        mButPrev = (ImageButton) findViewById(R.id.player_button_prev);
        mButRew = (ImageButton) findViewById(R.id.player_button_rew);
        mButPlay = (ImageButton) findViewById(R.id.player_button_play);
        mButPause = (ImageButton) findViewById(R.id.player_button_pause);
        mButStop = (ImageButton) findViewById(R.id.player_button_stop);
        mButFwd = (ImageButton) findViewById(R.id.player_button_ff);
        mButNext = (ImageButton) findViewById(R.id.player_button_next);
        mTrackTitle = (TextView) findViewById(R.id.player_track_title);
        mTrackDuration = (TextView) findViewById(R.id.player_track_duration);
        mTrackPosition = (TextView) findViewById(R.id.player_track_position);
    }

    public void setViewListener(IPlayerViewListener listener) {
        mListener = listener;
    }

    public void onResume() {
        initViews();
        mButPrev.setOnClickListener(this);
        mButRew.setOnClickListener(this);
        mButPlay.setOnClickListener(this);
        mButPause.setOnClickListener(this);
        mButStop.setOnClickListener(this);
        mButFwd.setOnClickListener(this);
        mButNext.setOnClickListener(this);
        mPosSeekBar.setOnSeekBarChangeListener(this);
        setVisibility(mVisibility?VISIBLE:GONE);
        if(mButtonReadyToPlay) setPlatyButtonStateReadyToPlay();
        else setPlatyButtonStateReadyToPause();
    }

    public void onPause() {
        mButPrev.setOnClickListener(null);
        mButRew.setOnClickListener(null);
        mButPlay.setOnClickListener(null);
        mButPause.setOnClickListener(null);
        mButStop.setOnClickListener(null);
        mButFwd.setOnClickListener(null);
        mButNext.setOnClickListener(null);
        mPosSeekBar.setOnSeekBarChangeListener(null);
    }

    public void setStatePlaying(Song song) {
        FontUtils.setDefaultFontToText(mTrackTitle);
        mTrackTitle.setText(song.title);
        updateDuration(song.duration);
        updatePosition(0);
        setVisibility(VISIBLE);
        setPlatyButtonStateReadyToPause();
    }

    public void updatePosition(long position) {
        updatePosition(position, false);
    }

    private void updatePosition(long position, boolean bySeekBar) {
        if (bySeekBar == mTrackInProgress) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(position);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(position - TimeUnit.MINUTES.toMillis(minutes));
            mTrackPosition.setText(String.format("%02d:%02d", minutes, seconds));
        }

        if (!bySeekBar && !mTrackInProgress) mPosSeekBar.setProgress((int) position);
    }

    private void updateDuration(long duration) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration - TimeUnit.MINUTES.toMillis(minutes));
        mTrackDuration.setText(String.format("%02d:%02d", minutes, seconds));
        mPosSeekBar.setMax((int) duration);
    }

    public void setStateStopped() {
        setVisibility(GONE);
        setPlatyButtonStateReadyToPlay();
    }

    @Override
    public void setVisibility(int visibility) {
        mVisibility = visibility == VISIBLE;
        super.setVisibility(visibility);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.player_button_prev:
                if (mListener != null) mListener.onPreviousTrackPressed();
                break;
            case R.id.player_button_rew:
                if (mListener != null) mListener.onRewindPressed();
                break;
            case R.id.player_button_play:
                if (mListener != null) mListener.onPlayPressed();
                break;
            case R.id.player_button_pause:
                if (mListener != null) mListener.onPausePressed();
                break;
            case R.id.player_button_stop:
                if (mListener != null) mListener.onStopPressed();
                break;
            case R.id.player_button_ff:
                if (mListener != null) mListener.onForwardPressed();
                break;
            case R.id.player_button_next:
                if (mListener != null) mListener.onNextTrackPressed();
                break;
        }
    }

    public void setPlatyButtonStateReadyToPlay() {
        mButPlay.setVisibility(VISIBLE);
        mButPause.setVisibility(GONE);
        mButtonReadyToPlay = true;
    }

    public void setPlatyButtonStateReadyToPause() {
        mButPlay.setVisibility(GONE);
        mButPause.setVisibility(VISIBLE);
        mButtonReadyToPlay = false;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) updatePosition(progress, true);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mTrackInProgress = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mTrackInProgress = false;
        if (mListener != null) mListener.onTrackChangeFinished(seekBar.getProgress());
    }
}
