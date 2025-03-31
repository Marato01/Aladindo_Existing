package com.reeman.delige.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.reeman.delige.R;

import java.util.List;

public class BroadcastItemAdapter extends RecyclerView.Adapter<BroadcastItemAdapter.ViewHolder> {
    private List<String> textContentList;
    private final int type;
    public static int TYPE_OBSTACLE_PROMPT = 0;
    public static int TYPE_CRUISE_LOOP_BROADCAST = 1;
    public static int TYPE_RECYCLE_LOOP_BROADCAST = 2;
    public static int TYPE_RECYCLE_PLACE_RECYCLABLES_BROADCAST = 3;
    public static int TYPE_RECYCLE_RECYCLE_COMPLETE_BROADCAST = 4;
    public static int TYPE_BIRTHDAY_PICK_MEAL_BROADCAST = 5;
    public static int TYPE_BIRTHDAY_PICK_MEAL_COMPLETE_BROADCAST = 6;
    public static int TYPE_DELIVERY_ARRIVAL_BROADCAST = 7;
    public static int TYPE_MULTI_DELIVERY_ARRIVAL_BROADCAST = 8;
    private List<Integer> checkedList;

    public BroadcastItemAdapter(int type) {
        this.type = type;
    }

    public void setCheckedList(List<Integer> list) {
        this.checkedList = list;
    }

    public void setTextContentList(List<String> textContentList) {
        this.textContentList = textContentList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BroadcastItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewGroup root = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_broadcast_item, parent, false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull BroadcastItemAdapter.ViewHolder holder, int position) {
        String text = textContentList.get(position);
        holder.tvFileName.setText(text);
        if (type == TYPE_OBSTACLE_PROMPT || type == TYPE_CRUISE_LOOP_BROADCAST || type == TYPE_RECYCLE_LOOP_BROADCAST) {
            holder.cbCheckStatus.setVisibility(View.VISIBLE);
            holder.cbCheckStatus.setChecked(checkedList.contains(position));
            holder.cbCheckStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        if (!checkedList.contains(position)) {
                            checkedList.add(position);
                            if (listener != null) listener.onCheckChange(type, checkedList);
                        }
                    } else {
                        int target = -1;
                        for (int i = 0; i < checkedList.size(); i++) {
                            if (checkedList.get(i) == position) {
                                target = i;
                                break;
                            }
                        }
                        if (target != -1) {
                            checkedList.remove(target);
                            if (listener != null) listener.onCheckChange(type, checkedList);
                        }
                    }
                }
            });
            holder.tvFileName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.cbCheckStatus.setChecked(!holder.cbCheckStatus.isChecked());
                }
            });
        } else {
            holder.rbCheckStatus.setVisibility(View.VISIBLE);
            holder.rbCheckStatus.setChecked(checkedList.contains(position));

            holder.tvFileName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!checkedList.isEmpty() && checkedList.get(0) == position) {
                        checkedList.clear();
                    } else {
                        checkedList.clear();
                        checkedList.add(position);
                    }
                    if (listener != null) listener.onCheckChange(type, checkedList);
                    notifyDataSetChanged();
                }
            });
        }
        holder.ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    checkedList.remove((Integer) position);
                    listener.onDeleteBroadcastItem(type, position, text);
                }
            }
        });
        holder.ibAudition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onAudition(type, position, v);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return textContentList == null ? 0 : textContentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final View root;
        private final TextView tvFileName;
        private final ImageButton ibDelete;
        private final ImageButton ibAudition;
        private final CheckBox cbCheckStatus;
        private final RadioButton rbCheckStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.root = itemView;
            cbCheckStatus = this.itemView.findViewById(R.id.cb_check_status);
            rbCheckStatus = this.itemView.findViewById(R.id.rb_check_status);
            tvFileName = this.itemView.findViewById(R.id.tv_spinner_item);
            ibAudition = this.itemView.findViewById(R.id.ib_audition);
            ibDelete = this.itemView.findViewById(R.id.ib_delete);
        }
    }

    private onItemDeleteListener listener;

    public void setListener(onItemDeleteListener listener) {
        this.listener = listener;
    }

    public interface onItemDeleteListener {
        void onDeleteBroadcastItem(int type, int position, String text);

        void onAudition(int type, int position, View v);

        void onCheckChange(int type, List<Integer> list);
    }
}
