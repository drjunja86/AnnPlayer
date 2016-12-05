package ann.player.annplayer.adapters;

import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import ann.player.annplayer.R;
import ann.player.annplayer.models.FolderItem;
import ann.player.annplayer.models.Song;
import ann.player.annplayer.utils.FontUtils;

public class MainListAdapter extends BaseAdapter {

	private static final String TAG = MainListAdapter.class.getSimpleName();
	private WeakReference<FragmentActivity> mActivity;
	private FolderItem[] mFolderContent;
	private FolderItem[] mSongList;
	private int mCurrentPos = -1;
	private int mFoldersCount = 0;
	private String mRootFolder;

	public MainListAdapter() {
		super();
	}
	
	public void setActivity(FragmentActivity activity) {
		mActivity = new WeakReference<FragmentActivity>(activity);
	}

	@Override
	public int getCount() {
		return mFolderContent == null?0:mFolderContent.length;
	}

	@Override
	public FolderItem getItem(int position) {
		return mFolderContent == null?null:mFolderContent[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (activity() == null || mFolderContent == null) return null;
		Holder holder;
		if (convertView == null) {
			convertView = activity().getLayoutInflater().inflate(R.layout.item_main_list, parent, false);
			holder = new Holder();
			holder.title = (TextView) convertView.findViewById(R.id.item_main_list_title);
			convertView.setTag(holder);
		}
		holder = (Holder) convertView.getTag();
		FolderItem item = getItem(position);
		if (item != null) {
            FontUtils.setDefaultFontToText(holder.title);
            holder.title.setText(item.isFolder?item.folderName:item.song.title);
			if (position == mCurrentPos) holder.title.setSelected(true);
			else holder.title.setSelected(false);
			holder.title.setCompoundDrawablesWithIntrinsicBounds(activity().getResources().getDrawable(
							item.isFolder?R.drawable.music_folder:R.drawable.play_icon),
					null, null, null);
			convertView.setBackground(
					position==mCurrentPos?activity().getResources().getDrawable(R.drawable.list_item_background_pressed)
							:null);

		}
		return convertView;
	}
	
	private FragmentActivity activity() {
		return mActivity.get();
	}
	
	private class Holder{
		public TextView title;
	}
	
	public void setCurrentPosition(int pos) {
		mCurrentPos = pos;
		notifyDataSetChanged();
	}
	
	public void setData(Cursor data) {
		mFolderContent = null;
		mCurrentPos = -1;
        mSongList = null;
        mRootFolder = null;
        mFoldersCount = 0;
		if (data != null) {
	        if (data.moveToFirst()) {
	        	ArrayList<FolderItem> items = new ArrayList<FolderItem>();
	        	ArrayList<String> folders = new ArrayList<String>();
	        	// files
	            do {
	            	FolderItem item = new FolderItem();
	            	Song song = new Song();
                    song.artist = data.getString(data.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    song.trackName = data.getString(data.getColumnIndex(MediaStore.Audio.Media.TITLE));
	            	String fineName = data.getString(data.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
	            	song.duration = data.getInt(data.getColumnIndex(MediaStore.Audio.Media.DURATION));
	            	song.id = data.getInt(data.getColumnIndex(MediaStore.Audio.Media._ID));
	            	if (MediaStore.UNKNOWN_STRING.equals(song.artist)) song.title = "";
	            	else song.title = song.artist + " - ";
	            	if (TextUtils.isEmpty(song.trackName)) song.title += fineName;
	            	else song.title += song.trackName;
	            	item.song = song;
	            	
	            	item.path = data.getString(data.getColumnIndex(MediaStore.Audio.Media.DATA));
	            	items.add(item);
	            	
	            	String folder = item.path.substring(0, item.path.indexOf(fineName));
	            	if (!folders.contains(folder)) {
	            		folders.add(folder);
	            		if (mRootFolder == null) mRootFolder = folder;
	            		else mRootFolder = getSharedPart(mRootFolder, folder);
	            		Log.i(TAG, "rootFolder: "+mRootFolder);
	            	}
	            } while (data.moveToNext());

                mSongList = new FolderItem[items.size()];
                items.toArray(mSongList);

	            // folders
	            for (int i = 0; i < folders.size(); i++) {
	            	FolderItem item = new FolderItem();
	            	item.isFolder = true;
	            	item.path = folders.get(i);
	            	if (!item.path.equals(mRootFolder)) {
	            		item.folderName = item.path.substring(mRootFolder.length(), item.path.length()-1);
	            		if (!item.folderName.contains("/")) {
                            items.add(0, item);
                            mFoldersCount++;
                        }
	            	}
				}
	            
	            mFolderContent = new FolderItem[items.size()];
	            items.toArray(mFolderContent);
	            notifyDataSetChanged();
	        }
		}
	}
	
	private String getSharedPart(String str1, String str2) {
		int minLength = Math.min(str1.length(),str2.length());
		int sharedLength = 0;
		for (int i = 1; i <= minLength; i++) {
			if (!str1.startsWith(str2.substring(0, i))) {
				break;
			}
			sharedLength = i;
		}
		if (sharedLength == 0) return "";
		return str1.substring(0, sharedLength);
	}

    public FolderItem[] getTrackList() {
        return mSongList;
    }

    public int getFoldersCount() {
        return mFoldersCount;
    }

    public String getRootFolder() {
        return mRootFolder;
    }

}
