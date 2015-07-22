// Copyright 2015 Google Inc. All Rights Reserved.

package com.google.android.gms.cast.samples.democastplayer;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaQueueItem;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

class QueueItemAdapter extends ArrayAdapter<MediaQueueItem> {
    private int mCurrentItemId;
    private int mStartIndex;
    private int mMode;
    private Set<Integer> mCheckedItemIds;
    private Listener mListener;

    public static final int MODE_UNKNOWN = 0;
    public static final int MODE_LOAD = 1;
    public static final int MODE_INSERT = 2;
    public static final int MODE_UPDATE = 3;
    public static final int MODE_REMOVE = 4;
    public static final int MODE_REORDER = 5;
    public static final int MODE_JUMP = 6;

    public interface Listener {
        void onSelectedItemChanged(int position);
        void onItemClicked(int position);
    }

    public QueueItemAdapter(Context context, int resource, int mode, Listener listener) {
        super(context, resource);
        mMode = mode;
        mCurrentItemId = 0;
        mStartIndex = 0;
        mCheckedItemIds = new HashSet<Integer>();
        mListener = listener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            view = inflater.inflate(R.layout.queue_item, parent, false);
        }

        RadioButton radioButton = (RadioButton) view.findViewById(R.id.radio_button);
        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentItemId = getItem(position).getItemId();
                mStartIndex = position;
                mListener.onSelectedItemChanged(position);
            }
        });
        if (((mCurrentItemId == 0) && (position == mStartIndex))
                || ((mCurrentItemId != 0) && (mCurrentItemId == getItem(position).getItemId()))) {
            radioButton.setChecked(true);
        }

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.check_box);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer itemId = getItem(position).getItemId();
                if (mCheckedItemIds.contains(itemId)) {
                    mCheckedItemIds.remove(itemId);
                } else {
                    mCheckedItemIds.add(itemId);
                }
            }
        });

        if (mMode == MODE_LOAD) {
            checkBox.setVisibility(View.GONE);
            radioButton.setVisibility(View.VISIBLE);
        } else if (mMode == MODE_REMOVE) {
            checkBox.setVisibility(View.VISIBLE);
            radioButton.setVisibility(View.GONE);
        } else if (mMode == MODE_JUMP) {
            checkBox.setVisibility(View.GONE);
            radioButton.setVisibility(View.VISIBLE);
        } else {
            checkBox.setVisibility(View.GONE);
            radioButton.setVisibility(View.GONE);
        }

        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        TextView textView = (TextView) view.findViewById(R.id.text);

        MediaQueueItem item = getItem(position);
        MediaInfo mediaInfo = item.getMedia();
        MediaMetadata metadata = mediaInfo.getMetadata();
        if (metadata == null) {
            textView.setText(R.string.unsupported_item);
            imageView.setImageBitmap(null);
        } else {
            switch (metadata.getMediaType()) {
                case MediaMetadata.MEDIA_TYPE_PHOTO:
                    imageView.setImageResource(R.drawable.type_image);
                    break;
                case MediaMetadata.MEDIA_TYPE_MUSIC_TRACK:
                    imageView.setImageResource(R.drawable.type_audio);
                    break;
                case MediaMetadata.MEDIA_TYPE_MOVIE:
                case MediaMetadata.MEDIA_TYPE_TV_SHOW:
                    imageView.setImageResource(R.drawable.type_video);
                    break;
                default:
                    imageView.setImageBitmap(null);
            }

            textView.setText(metadata.getString(MediaMetadata.KEY_TITLE));
        }

        if (mMode == MODE_LOAD || mMode == MODE_INSERT || mMode == MODE_UPDATE) {
            view.setFocusable(true);
            view.setClickable(true);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onItemClicked(position);
                }
            });
        }

        return view;
    }

    @Override
    public void clear() {
        super.clear();
        mCurrentItemId = 0;
        mStartIndex = -1;
        mCheckedItemIds.clear();
    }

    public int[] getCheckedItemIds() {
        int count = mCheckedItemIds.size();
        Integer[] tmpArray = mCheckedItemIds.toArray(new Integer[count]);
        int[] itemIdsArray = new int[count];
        for (int i = 0; i < count; ++i) {
            itemIdsArray[i] = tmpArray[i];
        }
        return itemIdsArray;
    }

    public int getCurrentItemId() {
        return mCurrentItemId;
    }

    public void setCurrentItemId(int currentItemId) {
        mCurrentItemId = currentItemId;
    }

    public int getStartIndex() {
        return mStartIndex;
    }
}
