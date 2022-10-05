package com.example.aria.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aria.R;
import com.example.aria.db.entity.AudioRecord;
import com.example.aria.recyclerview.LongItemDetail;

import java.util.List;

public class AudioRecordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //**** ViewHolder *****//

    public static class AudioRecordViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleLabel, metaLabel;

        public AudioRecordViewHolder(View itemView) {
            super(itemView);

            titleLabel = itemView.findViewById(R.id.listItem_title);
            metaLabel = itemView.findViewById(R.id.listItem_meta);
        }

        public void bindTo(@NonNull AudioRecord record, boolean isSelected) {
            titleLabel.setText(record.title);
            metaLabel.setText(record.duration);

            itemView.setSelected(isSelected);
        }

        public ItemDetailsLookup.ItemDetails<Long> getItemDetails() {
            int position = getBindingAdapterPosition();
            return new LongItemDetail(position, getItemId());
        }
    }


    //****  Adapter *****//

    private final AsyncListDiffer<AudioRecord> listDiffer;
    private SelectionTracker<Long> selectionTracker;

    public AudioRecordAdapter() {
        listDiffer = new AsyncListDiffer<>(this, callback);
        this.setHasStableIds(true);
    }

    @NonNull
    @Override
    public AudioRecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_playback_list_viewholder, parent, false);
        return new AudioRecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AudioRecord record = listDiffer.getCurrentList().get(position);
        ((AudioRecordViewHolder) holder).bindTo(record, selectionTracker.isSelected(getItemId(position)));
    }

    @Override
    public int getItemCount() {
        return listDiffer.getCurrentList().size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void submitList(List<AudioRecord> recordList) {
        listDiffer.submitList(recordList);
    }

    public AudioRecord getRecordAt(int position) {
        return listDiffer.getCurrentList().get(position);
    }

    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.selectionTracker = selectionTracker;
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