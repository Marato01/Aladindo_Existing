package com.reeman.delige.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.reeman.delige.R;

import java.util.List;

public class TableGroupAdapter extends RecyclerView.Adapter<TableGroupAdapter.TableGroupViewHolder> {

    private int itemCount = 0;
    private int selectedIndex = 0;

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
        notifyDataSetChanged();
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
        notifyDataSetChanged();
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    @NonNull
    @Override
    public TableGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_table_group, parent, false);
        return new TableGroupViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull TableGroupViewHolder holder, int position) {

    }

    @Override
    public void onBindViewHolder(@NonNull TableGroupViewHolder holder, int position, @NonNull List<Object> payloads) {
        Context context = holder.itemView.getContext();
        if (selectedIndex == position) {
            holder.tvTableGroup.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.bg_table_group_selected, context.getTheme()));
        } else {
            holder.tvTableGroup.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.bg_table_group_normal, context.getTheme()));
        }
        holder.tvTableGroup.setText(String.valueOf(position + 1));
        holder.tvTableGroup.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            setSelectedIndex(adapterPosition);
            if (mOnTableGroupItemClickListener != null) {
                mOnTableGroupItemClickListener.onTableGroupItemClick(adapterPosition, v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    static class TableGroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvTableGroup;

        public TableGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTableGroup = itemView.findViewById(R.id.tv_table_group);
        }
    }

    private OnTableGroupItemClickListener mOnTableGroupItemClickListener;

    public void setOnTableGroupItemClickListener(OnTableGroupItemClickListener onTableGroupItemClickListener) {
        mOnTableGroupItemClickListener = onTableGroupItemClickListener;
    }

    public interface OnTableGroupItemClickListener {
        void onTableGroupItemClick(int tableGroup, View view);
    }
}
