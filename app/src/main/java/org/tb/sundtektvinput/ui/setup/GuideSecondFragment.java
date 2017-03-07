package org.tb.sundtektvinput.ui.setup;

import android.app.FragmentManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.util.Log;
import android.widget.Toast;

import com.google.android.media.tv.companionlibrary.model.Channel;

import org.tb.sundtektvinput.R;
import org.tb.sundtektvinput.parser.SundtekJsonParser;
import org.tb.sundtektvinput.util.SettingsHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.support.v17.leanback.widget.GuidedAction.CHECKBOX_CHECK_SET_ID;
import static com.google.ads.interactivemedia.v3.impl.w.c.loaded;

public class GuideSecondFragment extends GuideBaseFragment {

    ArrayList<String> selectedChannels = new ArrayList<>();
    HashMap<String, Channel> allChannels;
    HashMap<String, Channel> selectedChannelMap;

    @NonNull
    public GuidanceStylist.Guidance onCreateGuidance(@NonNull Bundle savedInstanceState) {
        String title = "Channels Found";
        String description = "Please select the channels you want to import";
        Drawable icon = getActivity().getDrawable(R.drawable.ic_launcher);
        return new GuidanceStylist.Guidance(title, description, breadcrumb, icon);
    }




    @Override
    public void onCreateButtonActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        actions.add(new GuidedAction.Builder(getActivity().getApplicationContext())
                .id(12)
                .title("Continue")
                .build());

        actions.add(new GuidedAction.Builder(getActivity().getApplicationContext())
                .id(13)
                .title("Back")
                .build());
    }


    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        allChannels = getChannels();
        selectedChannels = new SettingsHelper(getActivity().getApplicationContext()).loadSelectedChannelsMap(getString(R.string.selectedChannelsFile));
        selectedChannelMap = new HashMap<>();
        Log.d("LOADED", loaded.toString());
        String id;
        for (Channel channel : allChannels.values()) {
            id = String.valueOf(channel.getOriginalNetworkId());

            if(selectedChannels.contains(id)){
                selectedChannelMap.put(id, channel);
            }
            actions.add(
                    new GuidedAction.Builder(getActivity().getApplicationContext())
                            .checkSetId(CHECKBOX_CHECK_SET_ID)
                            .description(id)
                            .title(channel.getDisplayName())
                            .checked(selectedChannels.contains(id))
                            .build());
        }


    }


    HashMap<String, Channel> getChannels() {
        ArrayList<Channel> resp = new ArrayList<>();
        try {
            resp = new getChannelsAsync().execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        allChannels = new HashMap<>();
        for (Channel channel : resp)
            allChannels.put(String.valueOf(channel.getOriginalNetworkId()), channel);

        return allChannels;
    }

    private class getChannelsAsync extends AsyncTask<Void, Void, ArrayList<Channel>> {

        @Override
        protected ArrayList<Channel> doInBackground(Void... arg0) {
            return new SundtekJsonParser().getChannels();
        }

        @Override
        protected void onPostExecute(ArrayList<Channel> channels) {
            super.onPostExecute(channels);
//            allChannels = new HashMap<>();
//            for (Channel channel : channels)
//                allChannels.put(String.valueOf(channel.getOriginalNetworkId()), channel);
        }
    }


    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        FragmentManager fm = getFragmentManager();
        Log.d("ACTIONID", "" + action.getId());
        if (action.isChecked()) {
            selectedChannels.add(action.getDescription().toString());
            selectedChannelMap.put((String) action.getDescription(), allChannels.get(action.getDescription()));
            Log.d("ACTIONID", action.getTitle() + " in list " + selectedChannels.contains(action.getDescription()));
        } else {
            if (selectedChannels.contains(action.getDescription())) {
                selectedChannels.remove(action.getDescription());
                selectedChannelMap.remove(action.getDescription());
                Log.d("ACTIONID", action.getTitle() + " in list " + selectedChannels.contains(action.getDescription()));
            }
        }

        if (action.getId() == 12) {
            Toast.makeText(getActivity().getApplicationContext(), "Next", Toast.LENGTH_LONG).show();
            new SettingsHelper(getActivity().getApplicationContext())
                    .saveChannelIds(selectedChannels, getString(R.string.selectedChannelsFile));
            Bundle args = new Bundle();
            args.putSerializable("channels",selectedChannelMap);
            GuideBaseFragment fragment = new GuideThirdFragment();
            fragment.setArguments(args);
            GuidedStepFragment.add(fm, fragment);


        }
        if (action.getId() == 13) {
            Toast.makeText(getActivity().getApplicationContext(), "Next", Toast.LENGTH_LONG).show();
            getFragmentManager().popBackStack();
        }
    }
}