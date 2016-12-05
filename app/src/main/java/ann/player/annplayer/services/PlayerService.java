package ann.player.annplayer.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;

import ann.player.annplayer.MainActivity;
import ann.player.annplayer.models.Song;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_PLAY = "ann.player.annplayer.services.action.PLAY";
    private static final String ACTION_PAUSE = "ann.player.annplayer.services.action.PAUSE";
    private static final String ACTION_RESUME = "ann.player.annplayer.services.action.ACTION_RESUME";
    private static final String ACTION_TRACK_CHANGED = "ann.player.annplayer.services.action.TRACK_CHANGED";
    private static final String ACTION_SEEK_TO = "ann.player.annplayer.services.action.SEEK_TO";
    private static final String ACTION_START_DUCKING = "ann.player.annplayer.services.action.ACTION_START_DUCKING";
    private static final String ACTION_STOP_DUCKING = "ann.player.annplayer.services.action.ACTION_STOP_DUCKING";

    public static final String ACTION_POST_PROGRESS = "ann.player.annplayer.services.action.POST_PROGRESS";
    public static final String ACTION_PLAY_FINISHED = "ann.player.annplayer.services.action.PLAY_FINISHED";

    private static final String EXTRA_PARAM_TIME = "ann.player.annplayer.services.extra.EXTRA_PARAM_TIME";
    private static final String EXTRA_PARAM_SONG_ID = "ann.player.annplayer.services.extra.EXTRA_PARAM_SONG_ID";
    private static final String EXTRA_PARAM_SONG_TITLE = "ann.player.annplayer.services.extra.EXTRA_PARAM_SONG_TITLE";
    public static final String EXTRA_PARAM_POSITION = "ann.player.annplayer.services.extra.EXTRA_PARAM_POSITION";

    private static final String TAG = PlayerService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 55;

    private MediaPlayer mMediaPlayer = null;
    private String mCurentSongTitle;
    private static boolean mIsPlaying;
    private Thread mProgressThread;

    /**
     * Starts this service to perform action ACTION_PLAY with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPlay(Context context, Song song) {
        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction(ACTION_PLAY);
        intent.putExtra(EXTRA_PARAM_SONG_ID, song.id);
        intent.putExtra(EXTRA_PARAM_SONG_TITLE, song.title);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action ACTION_RESUME with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionResume(Context context) {
        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction(ACTION_RESUME);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action ACTION_PAUSE with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPause(Context context) {
        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction(ACTION_PAUSE);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action ACTION_TRACK_CHANGED with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionTrackChanged(Context context, Song song) {
        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction(ACTION_TRACK_CHANGED);
        intent.putExtra(EXTRA_PARAM_SONG_ID, song.id);
        intent.putExtra(EXTRA_PARAM_SONG_TITLE, song.title);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action ACTION_START_DUCKING with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionStartDucking(Context context) {
        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction(ACTION_START_DUCKING);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action ACTION_STOP_DUCKING with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionStopDucking(Context context) {
        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction(ACTION_STOP_DUCKING);
        context.startService(intent);
    }

    /**
     * Stops the service.
     *
     * @see IntentService
     */
    public static void stopPlaying(Context context) {
        Intent intent = new Intent(context, PlayerService.class);
        context.stopService(intent);
    }

    /**
     * Starts this service to perform action ACTION_SEEK_TO with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionSeekTo(Context context, int seekTime) {
        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction(ACTION_SEEK_TO);
        intent.putExtra(EXTRA_PARAM_TIME, seekTime);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            final int songId;
            final String songTitle;
            if (ACTION_PLAY.equals(action)) {
                songId = intent.getIntExtra(EXTRA_PARAM_SONG_ID, -1);
                songTitle = intent.getStringExtra(EXTRA_PARAM_SONG_TITLE);
                handleActionPlay(songId, songTitle);
            } else if (ACTION_PAUSE.equals(action)) {
                handleActionPause();
            } else if (ACTION_RESUME.equals(action)) {
                handleActionResume();
            } else if (ACTION_START_DUCKING.equals(action)) {
                handleActionStartDucking();
            } else if (ACTION_STOP_DUCKING.equals(action)) {
                handleActionStopDucking();
            } else if (ACTION_TRACK_CHANGED.equals(action)) {
                songId = intent.getIntExtra(EXTRA_PARAM_SONG_ID, -1);
                songTitle = intent.getStringExtra(EXTRA_PARAM_SONG_TITLE);
                handleActionTrackChanged(songId, songTitle);
            } else if (ACTION_SEEK_TO.equals(action)) {
                final int seekTime = intent.getIntExtra(EXTRA_PARAM_TIME, 0);
                handleActionSeekTo(seekTime);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Handle action ACTION_PLAY in the provided background thread with the provided
     * parameters.
     */
    private void handleActionPlay(int songId, String songTitle) {
        Log.i(TAG, "Received PLAY action, songId = "+songId+"  songTitle = "+songTitle);
        mMediaPlayer = new MediaPlayer();
        preparePlayerToPlay(songId, songTitle);
    }

    /**
     * Handle action ACTION_PAUSE in the provided background thread with the provided
     * parameters.
     */
    private void handleActionPause() {
        Log.i(TAG, "Received ACTION_PAUSE action");
        if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
    }

    /**
     * Handle action ACTION_RESUME in the provided background thread with the provided
     * parameters.
     */
    private void handleActionResume() {
        Log.i(TAG, "Received ACTION_RESUME action");
        if (!mMediaPlayer.isPlaying()) mMediaPlayer.start();
    }

    /**
     * Handle action ACTION_TRACK_CHANGED in the provided background thread with the provided
     * parameters.
     */
    private void handleActionTrackChanged(int songId, String songTitle) {
        Log.i(TAG, "Received TRACK_CHANGE action, songId = "+songId+"  songTitle = "+songTitle);
        resetPlayer();
        preparePlayerToPlay(songId, songTitle);
    }

    /**
     * Handle action ACTION_START_DUCKING in the provided background thread with the provided
     * parameters.
     */
    private void handleActionStartDucking() {
        Log.i(TAG, "Received ACTION_START_DUCKING action");
        mMediaPlayer.setVolume(0.3f, 0.3f);
    }

    /**
     * Handle action ACTION_STOP_DUCKING in the provided background thread with the provided
     * parameters.
     */
    private void handleActionStopDucking() {
        Log.i(TAG, "Received ACTION_STOP_DUCKING action");
        mMediaPlayer.setVolume(1.0f, 1.0f);
    }

    private void resetPlayer() {
        mProgressThread = null;
        mIsPlaying = false;
        mMediaPlayer.stop();
        mMediaPlayer.reset();
    }

    private void preparePlayerToPlay(int songId, String songTitle) {
        mCurentSongTitle = songTitle;
        Uri contentUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId);
        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(getApplicationContext(), contentUri);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.w(TAG, "Player was not prepared!");
            e.printStackTrace();
        }
    }

    /**
     * Handle action ACTION_SEEK_TO in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSeekTo(int seekTime) {
        Log.i(TAG, "Received SEEK_TO action, seekTime = "+seekTime);
        if (mIsPlaying) mMediaPlayer.seekTo(seekTime);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i(TAG, "Player prepared. Starting to play");
        mMediaPlayer.start();
        mIsPlaying = true;
        createNotification();
        createProgressThread();
    }

    private void createProgressThread() {
        Runnable _progressUpdater = new Runnable() {
            @Override
            public void run() {
                while (mMediaPlayer != null && mIsPlaying) {
                    try {
                        Intent postIntent = new Intent(ACTION_POST_PROGRESS);
                        postIntent.putExtra(EXTRA_PARAM_POSITION, mMediaPlayer.getCurrentPosition());
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(postIntent);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        mProgressThread = new Thread(_progressUpdater);
        mProgressThread.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "MediaPlayer error: "+what+" with extra: "+extra);
        // The MediaPlayer has moved to the Error state, must be reset!
        return false;
    }

    private void createNotification() {
        Context ctx = getApplicationContext();
        Intent notificationIntent = new Intent(ctx, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx,
                NOTIFICATION_ID, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = new Notification.Builder(ctx)
                .setContentIntent(contentIntent)
                .setContentTitle(mCurentSongTitle)
//                .setContentText(mCurentSongTitle)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(android.R.drawable.ic_media_play)
//                .setLargeIcon(android.R.drawable.ic_media_play)
                .build();
        startForeground(NOTIFICATION_ID, notification);

    }

    @Override
    public void onDestroy() {
        Log.w(TAG, "Service DESTROYED!");
        stopForeground(true);
        mIsPlaying = false;
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i(TAG, "Track playing is complete");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(ACTION_PLAY_FINISHED));
    }

    public static boolean isPlaying() {
        return  mIsPlaying;
    }

}
