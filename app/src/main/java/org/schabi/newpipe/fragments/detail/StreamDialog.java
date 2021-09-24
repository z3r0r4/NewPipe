package org.schabi.newpipe.fragments.detail;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.schabi.newpipe.R;
import org.schabi.newpipe.database.LocalItem;
import org.schabi.newpipe.database.playlist.PlaylistStreamEntry;
import org.schabi.newpipe.database.stream.StreamStatisticsEntry;
import org.schabi.newpipe.error.ErrorInfo;
import org.schabi.newpipe.error.UserAction;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.info_list.InfoItemDialog;
import org.schabi.newpipe.player.helper.PlayerHolder;
import org.schabi.newpipe.player.playqueue.PlayQueue;
import org.schabi.newpipe.player.playqueue.SinglePlayQueue;
import org.schabi.newpipe.util.Localization;
import org.schabi.newpipe.util.NavigationHelper;
import org.schabi.newpipe.util.StreamDialogEntry;
import org.schabi.newpipe.util.external_communication.KoreUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.preference.PreferenceManager;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

class StreamDialog {
    //USE BASESLISTFRAGMETN CLASS INSTEAD?
    public static final int StatisticsPlaylistFragment = 1;
    public static final int LocalPlaylistFragment = 2;
    public static final int PlaylistFragment = 3;
    public static final int FeedFragment = 4;

    private static void showStreamDialog(final StreamStatisticsEntry item,
                                  final int FragmentType,
                                  final Activity activity) {
        final Context context = activity.getApplicationContext();
        if (context == null || context.getResources() == null) {
            return;
        }
        final StreamInfoItem infoItem = item.toStreamInfoItem();

        final ArrayList<StreamDialogEntry> entries = new ArrayList<>();

        if (PlayerHolder.getInstance().isPlayerOpen()) {
            entries.add(StreamDialogEntry.enqueue);

            if (PlayerHolder.getInstance().getQueueSize() > 1) {
                entries.add(StreamDialogEntry.enqueue_next);
            }
        }


        switch (FragmentType) {
            case StatisticsPlaylistFragment:
                if (infoItem.getStreamType() == StreamType.AUDIO_STREAM) {
                    entries.addAll(Arrays.asList(
                            StreamDialogEntry.start_here_on_background,
                            StreamDialogEntry.delete,
                            StreamDialogEntry.append_playlist,
                            StreamDialogEntry.share
                    ));
                } else  {
                    entries.addAll(Arrays.asList(
                            StreamDialogEntry.start_here_on_background,
                            StreamDialogEntry.start_here_on_popup,
                            StreamDialogEntry.delete,
                            StreamDialogEntry.append_playlist,
                            StreamDialogEntry.share
                    ));
                }
                break;
            case LocalPlaylistFragment:
                if (infoItem.getStreamType() == StreamType.AUDIO_STREAM) {
                    entries.addAll(Arrays.asList(
                            StreamDialogEntry.start_here_on_background,
                            StreamDialogEntry.set_as_playlist_thumbnail,
                            StreamDialogEntry.delete,
                            StreamDialogEntry.append_playlist,
                            StreamDialogEntry.share
                    ));
                } else {
                    entries.addAll(Arrays.asList(
                            StreamDialogEntry.start_here_on_background,
                            StreamDialogEntry.start_here_on_popup,
                            StreamDialogEntry.set_as_playlist_thumbnail,
                            StreamDialogEntry.delete,
                            StreamDialogEntry.append_playlist,
                            StreamDialogEntry.share
                    ));
                }
                break;
            case PlaylistFragment:
                if (infoItem.getStreamType() == StreamType.AUDIO_STREAM) {
                    entries.addAll(Arrays.asList(
                            StreamDialogEntry.start_here_on_background,
                            StreamDialogEntry.append_playlist,
                            StreamDialogEntry.share
                    ));
                } else  {
                    entries.addAll(Arrays.asList(
                            StreamDialogEntry.start_here_on_background,
                            StreamDialogEntry.start_here_on_popup,
                            StreamDialogEntry.append_playlist,
                            StreamDialogEntry.share
                    ));
                }
                break;
            case FeedFragment:
                if (infoItem.getStreamType() == StreamType.AUDIO_STREAM) {
                    entries.addAll(Arrays.asList(
                                    StreamDialogEntry.start_here_on_background,
                                    StreamDialogEntry.append_playlist,
                                    StreamDialogEntry.share,
                                    StreamDialogEntry.open_in_browser
                            ));
                } else {
                    entries.addAll(Arrays.asList(
                                    StreamDialogEntry.start_here_on_background,
                                    StreamDialogEntry.start_here_on_popup,
                                    StreamDialogEntry.append_playlist,
                                    StreamDialogEntry.share,
                                    StreamDialogEntry.open_in_browser
                            ));
                }
                break;
        }

        if (FragmentType == StatisticsPlaylistFragment ||
            FragmentType == LocalPlaylistFragment ||
            FragmentType == PlaylistFragment) {
                entries.add(StreamDialogEntry.open_in_browser);
                if (KoreUtils.shouldShowPlayWithKodi(context, infoItem.getServiceId())) {
                    entries.add(StreamDialogEntry.play_with_kodi);
                }
                StreamDialogEntry.start_here_on_background.setCustomAction((fragment, infoItemDuplicate) ->
                        NavigationHelper.playOnBackgroundPlayer(context,
                                getPlayQueueStartingAt(item, FragmentType), true));
        }

        switch (FragmentType) {
            case StatisticsPlaylistFragment:
                StreamDialogEntry.delete.setCustomAction((fragment, infoItemDuplicate) ->
                        StatisticsPlaylistFragment.deleteEntry(Math.max(itemListAdapter.getItemsList().indexOf(item), 0)));
                break;
            case LocalPlaylistFragment:
                StreamDialogEntry.set_as_playlist_thumbnail.setCustomAction(
                        (fragment, infoItemDuplicate) ->
                                LocalPlaylistFragment.changeThumbnailUrl(item.getStreamEntity().getThumbnailUrl()));
                StreamDialogEntry.delete.setCustomAction((fragment, infoItemDuplicate) ->
                        LocalPlaylistFragment.deleteItem(item));
                break;
            case PlaylistFragment:

                break;
            case FeedFragment:
                // show "mark as watched" only when watch history is enabled
                boolean isWatchHistoryEnabled = PreferenceManager
                        .getDefaultSharedPreferences(context)
                        .getBoolean(context.getString(R.string.enable_watch_history_key),
                                            false);
                if (infoItem.getStreamType() != StreamType.AUDIO_LIVE_STREAM &&
                        infoItem.getStreamType() != StreamType.LIVE_STREAM &&
                        isWatchHistoryEnabled) {
                    entries.add(StreamDialogEntry.mark_as_watched);
                }
                break;
        }

        if (!isNullOrEmpty(infoItem.getUploaderUrl())) { //prev only in playlistfragment
            entries.add(StreamDialogEntry.show_channel_details);
        }

        StreamDialogEntry.setEnabledEntries(entries);

        new InfoItemDialog(activity, infoItem, StreamDialogEntry.getCommands(context),
                (dialog, which) -> StreamDialogEntry.clickOn(which, this, infoItem)).show();
    }

    private static PlayQueue getPlayQueueStartingAt(final PlaylistStreamEntry infoItem) {
        return getPlayQueue(Math.max(itemListAdapter.getItemsList().indexOf(infoItem), 0));
    }

    private PlayQueue getPlayQueueStartingAt(final StreamInfoItem infoItem) {
        return getPlayQueue(Math.max(infoListAdapter.getItemsList().indexOf(infoItem), 0));
    }

    private PlayQueue getPlayQueueStartingAt(final StreamStatisticsEntry infoItem) {
        return getPlayQueue(Math.max(itemListAdapter.getItemsList().indexOf(infoItem), 0));
    }
}
