package ann.player.annplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by AP on 27/05/14.
 */
public class PreferencesUtils {

    private static final String PREFERENCE_REPEAT_PLAYLIST = "ann.player.annplayer.utils.PREFERENCE_REPEAT_PLAYLIST";
    private static final String PREFERENCE_REPEAT_SONG = "ann.player.annplayer.utils.PREFERENCE_REPEAT_SONG";
    private static final String PREFERENCE_SHUFFLE = "ann.player.annplayer.utils.PREFERENCE_SHUFFLE";
    private static final String PREFERENCE_FILE = "ann.player.annplayer.utils.PREFERENCE_FILE";

    public static boolean getRepeatPlaylist(Context context) {
        return getSettings(context).getBoolean(PREFERENCE_REPEAT_PLAYLIST, false);
    }

    public static boolean getRepeatSong(Context context) {
        return getSettings(context).getBoolean(PREFERENCE_REPEAT_SONG, false);
    }

    public static boolean getShuffle(Context context) {
        return getSettings(context).getBoolean(PREFERENCE_SHUFFLE, false);
    }

    public static void setShuffle(Context context, boolean value) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(PREFERENCE_SHUFFLE, value);
        editor.commit();
    }

    public static void setRepeatSong(Context context, boolean value) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(PREFERENCE_REPEAT_SONG, value);
        editor.commit();
    }

    public static void setRepeatPlaylist(Context context, boolean value) {
        SharedPreferences.Editor editor = getSettings(context).edit();
        editor.putBoolean(PREFERENCE_REPEAT_PLAYLIST, value);
        editor.commit();
    }

    private static SharedPreferences getSettings(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_FILE, 0);
        return  settings;
    }

}
