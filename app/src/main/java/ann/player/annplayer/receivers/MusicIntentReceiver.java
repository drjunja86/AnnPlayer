package ann.player.annplayer.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by AP on 27/05/14.
 */
public class MusicIntentReceiver extends BroadcastReceiver {

    public static final String ACTION_AUDIO_BECOMING_NOISY = "ann.player.annplayer.utils.action.ACTION_AUDIO_BECOMING_NOISY";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(
                android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_AUDIO_BECOMING_NOISY));
        }
    }
}
