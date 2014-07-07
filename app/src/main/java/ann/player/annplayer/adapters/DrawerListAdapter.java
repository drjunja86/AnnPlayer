package ann.player.annplayer.adapters;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import ann.player.annplayer.R;

/**
 * Created by pisponen on 07/07/14.
 */
public class DrawerListAdapter extends BaseAdapter {
    private String[] mDrawerTitles;
    private Activity mActivity;

    public DrawerListAdapter(FragmentActivity activity, String[] drawerTitles) {
        super();
        mDrawerTitles = drawerTitles;
        mActivity = activity;
    }

    @Override
    public int getCount() {
        return mDrawerTitles.length;
    }

    @Override
    public String getItem(int position) {
        return mDrawerTitles[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mActivity == null || mDrawerTitles == null) return null;
        Holder holder;
        if (convertView == null) {
            convertView = mActivity.getLayoutInflater().inflate(R.layout.item_drawer_list, parent, false);
            holder = new Holder();
            holder.title = (TextView) convertView.findViewById(R.id.item_drawer_list_title);
            convertView.setTag(holder);
        }
        holder = (Holder) convertView.getTag();
        String title = getItem(position);
        if (!TextUtils.isEmpty(title)) holder.title.setText(title);
        return convertView;
    }

    private class Holder {
        public TextView title;
    }
}
