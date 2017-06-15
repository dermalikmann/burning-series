package de.m4lik.burningseries.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import de.m4lik.burningseries.ActivityComponent;
import de.m4lik.burningseries.R;
import de.m4lik.burningseries.ui.TabletShowActivity;
import de.m4lik.burningseries.ui.base.DialogBase;
import de.m4lik.burningseries.ui.listitems.PlayerChooserListItem;
import de.m4lik.burningseries.ui.viewAdapters.PlayerChooserListAdapter;
import de.m4lik.burningseries.util.AndroidUtility;
import de.m4lik.burningseries.util.Settings;

/**
 * Created by malik on 08.06.17.
 */

public class PlayerChooserDialog extends DialogBase {

    public static PlayerChooserDialog newInstance(Integer linkID, Boolean isSupported) {

        Bundle args = new Bundle();
        args.putInt("linkID", linkID);
        args.putBoolean("supported", isSupported);
        PlayerChooserDialog dialog = new PlayerChooserDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    protected void injectComponent(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        List<PlayerChooserListItem> players = new ArrayList<>();
        if (getArguments().getBoolean("supported")) {
            players.add(new PlayerChooserListItem("Interner Player", "internal",
                    Settings.of(getActivity()).isDarkTheme() ?
                            R.drawable.ic_ondemand_video_white : R.drawable.ic_ondemand_video));

            players.add(new PlayerChooserListItem("Externer Player", "external",
                    Settings.of(getActivity()).isDarkTheme() ?
                            R.drawable.ic_live_tv_white : R.drawable.ic_live_tv));

            players.add(new PlayerChooserListItem("In-App Browser", "appbrowser",
                    Settings.of(getActivity()).isDarkTheme() ?
                            R.drawable.ic_open_in_browser_white : R.drawable.ic_open_in_browser));
        }

        players.add(new PlayerChooserListItem("Im Browser öffnen", "browser",
                Settings.of(getActivity()).isDarkTheme() ?
                        R.drawable.ic_public_white : R.drawable.ic_public));

        return DialogBuilder.start(getActivity())
                .title(getString(R.string.choose_player_title))
                .adapter(new PlayerChooserListAdapter(getActivity(), players), (dialog2, id) -> {
                    if (Settings.of(getActivity()).alarmOnMobile() &&
                            AndroidUtility.isOnMobile(getActivity())) {
                        MobileDataAlertDialog dialog = MobileDataAlertDialog
                                .newInstance(getArguments().getInt("linkID"), players.get(id).getType());
                        if (getTargetFragment() != null)
                            dialog.setTargetFragment(getTargetFragment(), 0);
                        dialog.show(getActivity().getSupportFragmentManager(), null);
                    } else {
                        Intent i = new Intent();
                        i.putExtra("linkID", getArguments().getInt("linkID"));
                        i.putExtra("playerType", players.get(id).getType());

                        if (getTargetFragment() != null)
                            getTargetFragment().onActivityResult(
                                    getTargetRequestCode(), Activity.RESULT_OK, i);
                        else
                            ((TabletShowActivity) getActivity())
                                    .onDialogCallback(0, Activity.RESULT_OK, i);
                    }
                })
                .cancelable()
                .negative()
                .build();
    }
}
