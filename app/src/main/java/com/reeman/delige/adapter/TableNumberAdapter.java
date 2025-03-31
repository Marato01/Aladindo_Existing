package com.reeman.delige.adapter;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.BaseAdapter;

import com.reeman.delige.R;
import com.reeman.delige.activities.MainActivity;
import com.reeman.delige.constants.Constants;
import com.reeman.delige.request.model.BaseItem;
import com.reeman.delige.widgets.TableNumberView;

import java.util.ArrayList;
import java.util.List;

public class TableNumberAdapter extends BaseAdapter {

    private List<? extends BaseItem> list;
    private List<String> recycleTables;

    public List<String> getRecycleTables() {
        return recycleTables;
    }

    public TableNumberAdapter(List<? extends BaseItem> list) {
        this.list = list;
    }

    public TableNumberAdapter(OnTableNumberClickListener listener) {
        this.listener = listener;
        recycleTables = new ArrayList<>();
    }

    public void setList(List<? extends BaseItem> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void resetSelected() {
        recycleTables.clear();
    }

    public void setSelect(List<String> list) {
        recycleTables.clear();
        if (list != null) {
            recycleTables.addAll(list);
        }
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TableNumberView tableNumberView;
        int currentModeIndex = ((MainActivity) listener).getCurrentModeIndex();
        if (convertView == null) {
            tableNumberView = new TableNumberView(parent.getContext());
            tableNumberView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int currentModeIndex = ((MainActivity) listener).getCurrentModeIndex();
                    if (currentModeIndex == Constants.MODE_RECYCLE_2 || currentModeIndex == Constants.MODE_MULTI_DELIVERY) {
                        String content = tableNumberView.getText();
                        int index = recycleTables.indexOf(content);
                        if (index != -1) {
                            tableNumberView.select(false);
                            recycleTables.remove(index);
                            tableNumberView.setBackgroundResource(R.drawable.bg_table_number_normal);
                            tableNumberView.setTextColor(Color.parseColor("#FF666666"));
                            listener.onTableNumberRemove(recycleTables, index);
                        } else {
                            tableNumberView.select(true);
                            recycleTables.add(content);
                            tableNumberView.setBackgroundResource(R.drawable.bg_table_number_selected);
                            tableNumberView.setTextColor(Color.WHITE);
                            listener.onTableNumberAdd(recycleTables, recycleTables.size() - 1);
                        }
                    } else {
                        tableNumberView.animate()
                                .setDuration(100)
                                .setInterpolator(new AccelerateInterpolator())
                                .scaleX(1.1f)
                                .scaleY(1.1f)
                                .withLayer()
                                .withStartAction(() -> {
                                    tableNumberView.setBackgroundResource(R.drawable.bg_table_number_selected);
                                    tableNumberView.setTextColor(Color.WHITE);
                                })
                                .withEndAction(() -> tableNumberView
                                        .animate()
                                        .setDuration(100)
                                        .scaleX(1)
                                        .scaleY(1)
                                        .withEndAction(() -> {
                                            tableNumberView.setBackgroundResource(R.drawable.bg_table_number_normal);
                                            tableNumberView.setTextColor(Color.parseColor("#FF666666"));
                                        })
                                        .start())
                                .start();
                        listener.onTableNumberClick(((TableNumberView) v).getText());
                    }
                }
            });
        } else {
            tableNumberView = (TableNumberView) convertView;
        }
        String name = list.get(position).name;
        if (currentModeIndex == Constants.MODE_RECYCLE_2 || currentModeIndex == Constants.MODE_MULTI_DELIVERY) {
            boolean contains = recycleTables.contains(name);
            tableNumberView.select(contains);
            if (contains) {
                tableNumberView.setTextColor(Color.WHITE);
                tableNumberView.setBackgroundResource(R.drawable.bg_table_number_selected);
            } else {
                tableNumberView.setTextColor(Color.parseColor("#FF666666"));
                tableNumberView.setBackgroundResource(R.drawable.bg_table_number_normal);
            }
        } else {
            tableNumberView.select(false);
            tableNumberView.setTextColor(Color.parseColor("#FF666666"));
            tableNumberView.setBackgroundResource(R.drawable.bg_table_number_normal);
        }
        tableNumberView.setText(name);
        if (name.length() > 10) {
            tableNumberView.setTextSize(18);
        } else {
            tableNumberView.setTextSize(24);
        }
        return tableNumberView;
    }


    private OnTableNumberClickListener listener;

    public interface OnTableNumberClickListener {
        void onTableNumberClick(String s);

        void onTableNumberAdd(List<String> tables, int position);

        void onTableNumberRemove(List<String> tables, int position);
    }
}
