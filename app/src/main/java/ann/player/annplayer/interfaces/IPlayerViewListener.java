package ann.player.annplayer.interfaces;

/**
 * Created by AP on 22/05/14.
 */
public interface IPlayerViewListener {

    void onStopPressed();
    void onPreviousTrackPressed();
    void onRewindPressed();
    void onPlayPressed();
    void onPausePressed();
    void onForwardPressed();
    void onNextTrackPressed();
    void onTrackChangeFinished(int progress);
}
