package org.tb.sundtektvinput.ui.setup;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;

import org.tb.sundtektvinput.R;
import org.tb.sundtektvinput.util.SettingsHelper;

import java.util.List;
import java.util.regex.Pattern;

import static android.support.v17.leanback.widget.GuidedAction.ACTION_ID_CANCEL;
import static android.support.v17.leanback.widget.GuidedAction.ACTION_ID_CONTINUE;
import static android.support.v17.leanback.widget.GuidedAction.ACTION_ID_CURRENT;

public class GuideIpFragment extends GuideBaseFragment {


    private static final int ACTION_EDIT_IP = 333;

    private static final Pattern IP_PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    private String ip;

    @Override
    @NonNull
    public GuidanceStylist.Guidance onCreateGuidance(@NonNull Bundle savedInstanceState) {
        String title = "Enter IP";
        String description = "Enter the IP Address of your streamingserver";
        //  Drawable icon = getActivity().getDrawable(R.drawable.ic_launcher);
        return new GuidanceStylist.Guidance(title, description, breadcrumb, null);
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        ip = new SettingsHelper(getActivity()).loadIp();

        actions.add(new GuidedAction.Builder(getActivity())
                .id(ACTION_EDIT_IP)
                .title( validateIp(ip) ? ip : "Enter IP Address")
                .editTitle(ip)
                .description(validateIp(ip) ? getString(R.string.ip_valid) : getString(R.string.ip_not_valid))
                .editDescription("IP Address")
                .editable(true)
                .build()
        );

        actions.add(new GuidedAction.Builder(getActivity().getApplicationContext())
                .id(ACTION_ID_CANCEL)
                .title("Cancel")
                .build());
    }

    @Override
    public void onCreateButtonActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        actions.add(new GuidedAction.Builder(getActivity())
                .enabled(validateIp(ip))
                .clickAction(GuidedAction.ACTION_ID_CONTINUE)
                .build()
        );
    }


    public static boolean validateIp(final String ip) {
        return IP_PATTERN.matcher(ip).matches();
    }

    @Override
    public long onGuidedActionEditedAndProceed(GuidedAction action) {
        if (action.getId() == ACTION_EDIT_IP) {
            CharSequence ip = action.getEditTitle();
            if (validateIp(ip.toString())) {
                new SettingsHelper(getActivity()).saveIp(ip.toString());
                findButtonActionById(GuidedAction.ACTION_ID_CONTINUE).setEnabled(true);
                findActionById(ACTION_EDIT_IP).setTitle(ip);
                action.setDescription(getString(R.string.ip_valid));
            } else {
                action.setDescription(getString(R.string.ip_not_valid));
                findButtonActionById(GuidedAction.ACTION_ID_CONTINUE).setEnabled(false);
            }
            findActionById(ACTION_EDIT_IP).setTitle(ip);
            notifyButtonActionChanged(findButtonActionPositionById(GuidedAction.ACTION_ID_CONTINUE));


        }
        return ACTION_ID_CURRENT;
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        FragmentManager fm = getFragmentManager();

        if (action.getId() == ACTION_ID_CONTINUE) {
            GuidedStepFragment.add(fm, new GuideFirstFragment());
        }
        if (action.getId() == ACTION_ID_CANCEL) {
            finishGuidedStepFragments();
        }
    }


}