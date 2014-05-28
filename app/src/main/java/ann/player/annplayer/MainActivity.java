package ann.player.annplayer;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import ann.player.annplayer.fragments.MainFragment;
import ann.player.annplayer.utils.PreferencesUtils;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        if (savedInstanceState == null) {
        	getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem menuItem = menu.findItem(R.id.action_shuffle);
        menuItem.setChecked(PreferencesUtils.getShuffle(this));
        menuItem = menu.findItem(R.id.action_repeat_playlist);
        menuItem.setChecked(PreferencesUtils.getRepeatPlaylist(this));
        menuItem = menu.findItem(R.id.action_repeat_song);
        menuItem.setChecked(PreferencesUtils.getRepeatSong(this));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_repeat_playlist:
                PreferencesUtils.setRepeatPlaylist(this, !item.isChecked());
                item.setChecked(!item.isChecked());
                return true;
            case R.id.action_repeat_song:
                PreferencesUtils.setRepeatSong(this, !item.isChecked());
                item.setChecked(!item.isChecked());
                return true;
            case R.id.action_shuffle:
                PreferencesUtils.setShuffle(this, !item.isChecked());
                item.setChecked(!item.isChecked());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
