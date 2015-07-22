// Copyright 2015 Google Inc. All Rights Reserved.

package com.google.android.gms.cast.samples.democastplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.gms.cast.MediaQueueItem;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Activity to display and manage a Cast media queue.
 */
public class QueueActivity extends ActionBarActivity {

    private static final String TAG = "QueueActivity";

    public static final String EXTRA_ADAPTER_MODE = "extra_adapter_mode";
    public static final String EXTRA_CURRENT_ITEM_ID = "extra_current_item_id";
    public static final String EXTRA_ITEM_IDS = "extra_item_ids";
    public static final String EXTRA_ITEMS = "extra_items";
    public static final String EXTRA_REPEAT_MODE = "extra_repeat_mode";
    public static final String EXTRA_START_INDEX = "extra_start_index";

    private Button mCancelButton;
    private Button mOkButton;
    private QueueItemAdapter mQueueAdapter;
    private QueueListFragment mQueueListFragment;
    private Spinner mRepeatModeSpinner;
    private int mMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.queue_activity);
        mMode = getIntent().getExtras().getInt(EXTRA_ADAPTER_MODE);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        switch (mMode) {
            case QueueItemAdapter.MODE_LOAD:
                actionBar.setTitle(R.string.queue_activity_load_queue_title);
                break;
            case QueueItemAdapter.MODE_INSERT:
                actionBar.setTitle(R.string.queue_activity_insert_queue_title);
                break;
            case QueueItemAdapter.MODE_UPDATE:
                actionBar.setTitle(R.string.queue_activity_update_queue_title);
                break;
            case QueueItemAdapter.MODE_REMOVE:
                actionBar.setTitle(R.string.queue_activity_remove_queue_title);
                break;
            case QueueItemAdapter.MODE_JUMP:
                actionBar.setTitle(R.string.queue_activity_jump_queue_title);
                break;
        }
        if (mMode == QueueItemAdapter.MODE_LOAD) {
            findViewById(R.id.repeat_mode_row).setVisibility(View.VISIBLE);
        }
        mCancelButton = (Button) findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mRepeatModeSpinner = (Spinner) findViewById(R.id.repeat_mode_spinner);

        mOkButton = (Button) findViewById(R.id.ok_button);
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mMode) {
                    case QueueItemAdapter.MODE_LOAD:
                        performLoad();
                        break;
                    case QueueItemAdapter.MODE_INSERT:
                        performInsert();
                        break;
                    case QueueItemAdapter.MODE_UPDATE:
                        performUpdate();
                        break;
                    case QueueItemAdapter.MODE_REMOVE:
                        performRemove();
                    case QueueItemAdapter.MODE_JUMP:
                        performJump();
                        break;
                }
            }
        });
        mQueueListFragment = (QueueListFragment) getSupportFragmentManager().findFragmentById(
                R.id.list_fragment);
        mQueueAdapter = new QueueItemAdapter(this, android.R.layout.simple_spinner_item, mMode,
                mQueueListFragment);
        if (mMode != QueueItemAdapter.MODE_LOAD) {
            updateAdapterWithMediaStatus(mQueueAdapter, getIntent());
        }
        mQueueListFragment.setListAdapter(mQueueAdapter);
        mQueueListFragment.getListView().setItemsCanFocus(true);
    }

    private void updateAdapterWithMediaStatus(final QueueItemAdapter adapter, final Intent intent) {

        mRepeatModeSpinner.setSelection(intent.getIntExtra(EXTRA_REPEAT_MODE, 0));

        if (intent.hasExtra(EXTRA_ITEMS)) {
            String[] itemStrings = intent.getStringArrayExtra(EXTRA_ITEMS);
            MediaQueueItem[] items = new MediaQueueItem[itemStrings.length];
            for (int i = 0; i < itemStrings.length; ++i) {
                try {
                    items[i] = new MediaQueueItem.Builder(new JSONObject(itemStrings[i])).build();
                } catch (JSONException e) {
                    Log.w(TAG, "Failed to parse MediaQueueItem from JSON: " + e.toString());
                }
            }
            adapter.addAll(items);
        }
        adapter.setCurrentItemId(intent.getIntExtra(EXTRA_CURRENT_ITEM_ID, 0));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed () {
        setResult(Activity.RESULT_CANCELED, new Intent());
        finish();
    }

    private void performLoad() {
        int count = mQueueAdapter.getCount();
        if (count <= 0) {
            return;
        }
        String[] itemStrings = new String[count];
        for (int i = 0; i < count; ++i) {
            MediaQueueItem item = mQueueAdapter.getItem(i);
            itemStrings[i] = item.toJson().toString();
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ITEMS, itemStrings);
        intent.putExtra(EXTRA_REPEAT_MODE, mRepeatModeSpinner.getSelectedItemPosition());
        intent.putExtra(EXTRA_START_INDEX, mQueueAdapter.getStartIndex());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void performInsert() {
        MediaQueueItem[] insertedItems = mQueueListFragment.getInsertedItems();
        int count = insertedItems.length;
        if (count <= 0) {
            return;
        }

        String[] itemStrings = new String[count];
        for (int i = 0; i < count; ++i) {
            itemStrings[i] = insertedItems[i].toJson().toString();
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ITEMS, itemStrings);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void performUpdate() {
        int count = mQueueAdapter.getCount();
        if (count <= 0) {
            return;
        }
        String[] itemStrings = new String[count];
        for (int i = 0; i < count; ++i) {
            MediaQueueItem item = mQueueAdapter.getItem(i);
            itemStrings[i] = item.toJson().toString();
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ITEMS, itemStrings);
        intent.putExtra(EXTRA_REPEAT_MODE, mRepeatModeSpinner.getSelectedItemPosition());
        intent.putExtra(EXTRA_CURRENT_ITEM_ID, mQueueAdapter.getCurrentItemId());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void performRemove() {
        int[] itemIdsToRemove = mQueueAdapter.getCheckedItemIds();
        if (itemIdsToRemove.length <= 0) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ITEM_IDS, itemIdsToRemove);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void performJump() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CURRENT_ITEM_ID, mQueueAdapter.getCurrentItemId());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
