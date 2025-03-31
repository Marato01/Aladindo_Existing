package com.reeman.delige.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.reeman.delige.R;
import com.reeman.delige.request.model.BaseItem;
import com.reeman.delige.request.model.Route;

import java.util.List;

public class RouteListAdapter extends RecyclerView.Adapter<RouteListAdapter.ViewHolder> {


    private int currentIndex = 0;
    private List<? extends BaseItem> list;
    private Route currentRoute;

    public Route getCurrentRoute() {
        return currentRoute;
    }

    public void setList(List<? extends BaseItem> list, int currentIndex) {
        this.list = list;
        this.currentIndex = currentIndex;
        if (list != null && !list.isEmpty()) {
            currentRoute = (Route) list.get(0);
        } else {
            currentRoute = null;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView root = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_route_item, parent, false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView textView = (TextView) holder.itemView;
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == currentIndex) {
                    textView.setTextColor(Color.parseColor("#707070"));
                    textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_route_item_normal, 0, 0, 0);
                    currentIndex = -1;
                    currentRoute = null;
                } else {
                    currentRoute = (Route) list.get(position);
                    textView.setTextColor(Color.parseColor("#008EFB"));
                    textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_route_item_selected, 0, 0, 0);
                    notifyItemChanged(currentIndex);
                    currentIndex = position;
                }
            }
        });
        textView.setText(list.get(position).name);
        if (position == currentIndex) {
            textView.setTextColor(Color.parseColor("#008EFB"));
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_route_item_selected, 0, 0, 0);
        } else {
            textView.setTextColor(Color.parseColor("#707070"));
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_route_item_normal, 0, 0, 0);
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
