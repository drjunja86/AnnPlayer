package ann.player.annplayer.utils;

import android.graphics.Typeface;
import android.widget.TextView;

/**
 * Created by AP on 29/10/14.
 *
 */
public class FontUtils {

    public static void setDefaultFontToText(TextView textView){
        Typeface font = Typeface.createFromAsset(textView.getContext().getAssets(), "DroidSans.ttf");
        textView.setTypeface(font);
    }
}
