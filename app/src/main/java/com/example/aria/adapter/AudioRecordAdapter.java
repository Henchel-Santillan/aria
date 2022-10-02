package com.example.aria.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aria.R;
import com.example.aria.db.entity.AudioRecord;

import java.util.ArrayList;
import java.util.List;

public class AudioRecordAdapter extends RecyclerView.Adapter<AudioRecordAdapter.AudioRecordViewHolder> {

    //**** Click Listener Interfaces ****//

    public interface OnClickItemListener {
        void onItemClick(View view, int position);
    }

    public interface OnLongClickItemListener {
        void onItemLongClick(View view, int position);
    }

    private OnClickItemListener clickItemListener;
    private OnLongClickItemListener longClickItemListener;

    public final void setClickItemListener(OnClickItemListener clickItemListener) {
        if (clickItemListener != null)
            this.clickItemListener = clickItemListener;
    }

    public final void setLongClickItemListener(OnLongClickItemListener longClickItemListener) {
        if (longClickItemListener != null)
            this.longClickItemListener = longClickItemListener;
    }


    //**** ViewHolder *****//

    public class AudioRecordViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleLabel, metaLabel;

        public AudioRecordViewHolder(View itemView) {
            super(itemView);

            titleLabel = itemView.findViewById(R.id.listItem_title);
            metaLabel = itemView.findViewById(R.id.listItem_meta);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    clickItemListener.onItemClick(view, position);

                    // When an item is selected, navigation to PlaybackFragment occurs.
                    // At this point, the selection should be cleared.
                    longClickedPositions.clear();
                }
            });

            itemView.setOnLongClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    longClickItemListener.onItemLongClick(view, position);
                    longClickedPositions.add(position);
                    return true;
                }
                return false;
            });
        }

        public void bindTo(@NonNull AudioRecord record) {
            titleLabel.setText(record.title);
            metaLabel.setText(""); // TODO:
            // Do stuff with the duration and the dateCreated, i.e. the meta information
        }
    }


    //****  Adapter *****//

    private final AsyncListDiffer<AudioRecord> listDiffer;
    private final List<Integer> longClickedPositions;

    public AudioRecordAdapter() {
        listDiffer = new AsyncListDiffer<AudioRecord>(this, callback);
        longClickedPositions = new ArrayList<>();
    }

    @NonNull
    @Override
    public AudioRecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_playback_list_viewholder, parent, false);
        return new AudioRecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioRecordViewHolder holder, int position) {
        AudioRecord record = listDiffer.getCurrentList().get(position);
        holder.bindTo(record);
    }

    @Override
    public int getItemCount() {
        return listDiffer.getCurrentList().size();
    }

    public void submitList(List<AudioRecord> recordList) {
        listDiffer.submitList(recordList);
    }

    public List<Integer> getLongClickedPositions() {
        return longClickedPositions;
    }

    public AudioRecord getRecordAt(final int position) {
        return listDiffer.getCurrentList().get(position);
    }


    //**** Diff ItemCallback implementation ****//

    DiffUtil.ItemCallback<AudioRecord> callback = new DiffUtil.ItemCallback<AudioRecord>() {

        @Override
        public boolean areItemsTheSame(@NonNull AudioRecord oldItem, @NonNull AudioRecord newItem) {
            return oldItem.recordId == newItem.recordId;
        }

        @Override
        public boolean areContentsTheSame(@NonNull AudioRecord oldItem, @NonNull AudioRecord newItem) {
            return oldItem.title.equals(newItem.title) &&
                    oldItem.filePath.equals(newItem.filePath) &&
                    oldItem.duration.equals(newItem.duration) &&
                    oldItem.amplitudePath.equals(newItem.amplitudePath) &&
                    oldItem.dateCreated == newItem.dateCreated;
        }
    };

}