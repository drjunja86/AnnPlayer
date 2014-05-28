package ann.player.annplayer.interfaces;

/**
 * Created by AP on 26/05/14.
 */
public interface IAudioFocusManagerListener {
    void onFocusGain();
    void onFocusLoss();
    void onFocusLossTransient();
    void onFocusLossTransientCanDuck();
}
