package ann.player.annplayer.interfaces;

/**
 * Created by AP on 21/05/14.
 */
public interface IPlayerModelListener {
    public void onPlayingStarted();
    public void onPlayingPaused();
    public void onPlayingResumed();
    public void onPlayingFinished();
    public void onPlayingStopped();
    public void onPositionChanged(boolean userChange);
    public void onTrackChanged();
}
