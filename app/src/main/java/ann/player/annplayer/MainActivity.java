package ann.player.annplayer;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import ann.player.annplayer.adapters.DrawerListAdapter;
import ann.player.annplayer.fragments.MainFragment;
import ann.player.annplayer.utils.PreferencesUtils;

public class MainActivity extends FragmentActivity {

    private String[] mDrawerTitles;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private CharSequence mTitle;
    private CharSequence mDrawerTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerTitle = getString(R.string.drawer_title);
        mDrawerTitles = getResources().getStringArray(R.array.drawer_list_titles);
        mTitle = mDrawerTitles[0];
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new DrawerListAdapter(this, mDrawerTitles));
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /**
             * Called when a drawer has settled in a completely closed state.
             */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mTitle);
            }

            /**
             * Called when a drawer has settled in a completely open state.
             */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
            }
        };

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setTitle(mTitle);

        if (savedInstanceState == null) showFolderBrowserFragment();
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
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

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

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {
        switch (position) {
            case 0:
                showFolderBrowserFragment();
                break;
            case 1:
                break;
            case 2:
                break;
        }

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mDrawerTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    private void showFolderBrowserFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment != null && fragment instanceof MainFragment) {
            return;
        }

        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, new MainFragment())
                .commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDrawerList != null) mDrawerList.setOnItemClickListener(null);
        if (mDrawerLayout != null) mDrawerLayout.setDrawerListener(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDrawerList != null) mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        if (mDrawerLayout != null) mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }
}
