package ann.player.annplayer.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import ann.player.annplayer.R;
import ann.player.annplayer.adapters.MainListAdapter;
import ann.player.annplayer.controllers.PlayerController;
import ann.player.annplayer.interfaces.IPlayerControllerListener;
import ann.player.annplayer.models.FolderItem;
import ann.player.annplayer.views.PlayerView;

public class MainFragment extends Fragment implements OnClickListener, LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener, IPlayerControllerListener {
	
	private ListView mList;
	private Button mButtonUp;
	private TextView mNoMusic;
	private ProgressBar mLoadingProgress;
	private MainListAdapter mListAdapter;
	private String mCurrentFolderPath = "";
	private boolean mProgressVisible = false;
	private ArrayList<String> mPrevFolders = new ArrayList<String>();
    private PlayerController mPlayerController;
    private PlayerView mPlayerView;

	static final String[] MUSIC_FILES_PROJECTION = { "*" };
	static final int LOADER_ID = 100;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_main, container, false);
		mList = (ListView) view.findViewById(R.id.main_list);
		mButtonUp = (Button) view.findViewById(R.id.button_up);
		mNoMusic = (TextView) view.findViewById(R.id.main_list_empty);
		mLoadingProgress = (ProgressBar) view.findViewById(R.id.main_list_progress);
        mPlayerView = (PlayerView) view.findViewById(R.id.player_control_frame);

		mList.setAdapter(mListAdapter);
		return view;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (mListAdapter == null) mListAdapter = new MainListAdapter();
		mListAdapter.setActivity((FragmentActivity) activity);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		if (savedInstanceState == null) {
			startListUpdate("");
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mButtonUp.setOnClickListener(null);
		mList.setOnItemClickListener(null);
        if (mPlayerController != null) mPlayerController.onPause();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mButtonUp.setOnClickListener(this);
		mList.setOnItemClickListener(this);
        if (mPlayerController != null) {
            mPlayerController.updatePlayerView(mPlayerView);
            mPlayerController.onResume();
        }
		updateView();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_up:
            onButtonUpPressed();
			break;

		default:
			break;
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
        FolderItem item = mListAdapter.getItem(position);
		if (item.isFolder) {
			mPrevFolders.add(mCurrentFolderPath);
			startListUpdate(item.path);
		} else {
			mListAdapter.setCurrentPosition(position);
            mList.smoothScrollToPosition(position);
            if (mPlayerController == null) {
                mPlayerController = new PlayerController(getActivity().getApplicationContext(), mPlayerView);
                mPlayerController.setControllerListener(this);
            }
            mPlayerController.setTrackList(mListAdapter.getTrackList(), mListAdapter.getRootFolder());
            mPlayerController.startPlayingTrackFromPosition(position - mListAdapter.getFoldersCount());
		}
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
		String folderClause = "";
		if (!TextUtils.isEmpty(mCurrentFolderPath)) folderClause = " AND " + MediaStore.Audio.Media.DATA + " LIKE '" + mCurrentFolderPath + "%'";
		return new CursorLoader(getActivity(), 
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MUSIC_FILES_PROJECTION, 
				MediaStore.Audio.Media.IS_MUSIC + " = 1" + folderClause, 
				null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mListAdapter.setData(cursor);
        mCurrentFolderPath = mListAdapter.getRootFolder();
        tryToSelectTrackFromList();
		updateLoadProgressVisibility(false);
		updateView();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) { }

	private void onButtonUpPressed() {
		startListUpdate(mPrevFolders.remove(mPrevFolders.size()-1));
	}
	
	private void updateView() {
		mButtonUp.setVisibility(mPrevFolders.size()==0?View.GONE:View.VISIBLE);
		mNoMusic.setVisibility(mListAdapter.getCount()==0?View.VISIBLE:View.GONE);
		updateLoadProgressVisibility(mProgressVisible);
	}
	
	private void startListUpdate(String folderPath) {
		updateLoadProgressVisibility(true);
        mCurrentFolderPath = folderPath;
        getLoaderManager().restartLoader(LOADER_ID, null, this);
	}
	
	private void updateLoadProgressVisibility(boolean visible) {
		mProgressVisible = visible;
		if (mLoadingProgress != null) { 
			mLoadingProgress.setVisibility(visible?View.VISIBLE:View.GONE);
		}
	}

    @Override
    public void onTrackChanged() {
        tryToSelectTrackFromList();
    }

    @Override
    public void onPlayingStopped() {
        mListAdapter.setCurrentPosition(-1);
        mPlayerController.setControllerListener(null);
        mPlayerController.release();
        mPlayerController = null;
    }

    private void tryToSelectTrackFromList() {
        if (mPlayerController == null) return;
        Log.i("MainFragment", "tryToSelectTrackFromList, mCurrentFolderPath = "+mCurrentFolderPath+"  VS   " + mPlayerController.getModel().getListRootFolder());
        if (mCurrentFolderPath == null || mPlayerController.getModel().getListRootFolder() == null) return;
        if (mCurrentFolderPath.equals(mPlayerController.getModel().getListRootFolder())) {
            //update playing track in mListAdapter
            int position = mPlayerController.getModel().getTrackId() + mListAdapter.getFoldersCount();
            mListAdapter.setCurrentPosition(position);
            mList.smoothScrollToPosition(position);
        }
    }
}
