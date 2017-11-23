
/*
The MIT License (MIT)

Copyright (c) 2015-2017 HyperTrack (http://hypertrack.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package io.hypertrack.sendeta.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.hypertrack.lib.HyperTrackMapAdapter;
import com.hypertrack.lib.HyperTrackMapFragment;
import com.hypertrack.lib.internal.common.util.HTTextUtils;

import java.util.List;

import io.hypertrack.sendeta.R;
import io.hypertrack.sendeta.presenter.ITrackPresenter;
import io.hypertrack.sendeta.presenter.TrackPresenter;

public class Track extends BaseActivity implements TrackView {

    public static final String KEY_TRACK_DEEPLINK = "track_deeplink";
    public static final String KEY_ACTION_ID_LIST = "action_id_list";
    public static final String KEY_LOOKUP_ID = "1234";
    public static final String KEY_COLLECTION_ID = "collection_id";

    private Intent intent;
    private Button retryButton;
    private TrackPresenter trackPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        trackPresenter = new ITrackPresenter(this);

        initializeUI();

        intent = getIntent();
        if (!processIntentParams(intent)) {
            displayLoader(false);
            finish();
        }
    }

    private void initializeUI() {
        // Initialize HyperTrackMapFragment, adapter and callback
        HyperTrackMapFragment hyperTrackMapFragment = (HyperTrackMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        hyperTrackMapFragment.setHTMapAdapter(new TrackMapAdapter(this));

        // Initialize UI buttons
        retryButton = (Button) findViewById(R.id.retryButton);
    }

    private boolean processIntentParams(Intent intent) {
        // Check if Intent has a valid TASK_ID_LIST extra
        if (intent != null) {
            String lookupId = intent.getStringExtra(KEY_LOOKUP_ID);
            String collectionId = intent.getStringExtra(KEY_COLLECTION_ID);
            if(!HTTextUtils.isEmpty(collectionId))
            {
                if (intent.getBooleanExtra(KEY_TRACK_DEEPLINK, false)) {
                    // Add lookupId being tracked by this user
                    trackPresenter.trackAction(collectionId,null);
                }
                return true;
            }
            if (!HTTextUtils.isEmpty(lookupId)) {
                if (intent.getBooleanExtra(KEY_TRACK_DEEPLINK, false)) {
                    // Add lookupId being tracked by this user
                    trackPresenter.trackAction(null,lookupId);
                }
                return true;

            } else if (intent.hasExtra(KEY_ACTION_ID_LIST)) {
                List<String> actionIdList = intent.getStringArrayListExtra(KEY_ACTION_ID_LIST);

                // Check if a valid TASK_ID_LIST is available
                if (actionIdList != null && !actionIdList.isEmpty()) {
                    if (intent.getBooleanExtra(KEY_TRACK_DEEPLINK, false)) {
                        // Add TaskId being tracked by this user
                        trackPresenter.trackAction(actionIdList);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void showRetryButton(boolean showRetryButton) {
        if (showRetryButton) {
            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    displayLoader(true);
                    processIntentParams(intent);
                }
            });
            retryButton.setEnabled(true);
            retryButton.setVisibility(View.VISIBLE);

        } else {
            retryButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void showLoader(boolean toggle) {
        displayLoader(toggle);
    }

    @Override
    public void showTrackingDetail() {
        displayLoader(false);
    }

    @Override
    public void showError() {
        showRetryButton(true);
        displayLoader(false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        trackPresenter.removeTrackingAction();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        trackPresenter.destroy();
    }

    /**
     * Implementation for HyperTrackMapAdapter specifying the UI customizations for Live-tracking view
     */
    class TrackMapAdapter extends HyperTrackMapAdapter {
        TrackMapAdapter(Context mContext) {
            super(mContext);
        }

        @Override
        public boolean showTrailingPolyline() {
            return true;
        }
    }
}
