package com.example.taskwiserebirth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

public class MySpinnerAdapter extends ArrayAdapter<SpinnerItem> {

    public MySpinnerAdapter(Context context, int resource, List<SpinnerItem> objects) {
        super(context, resource, objects);
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Nullable
    @Override
    public SpinnerItem getItem(int position) {
        return super.getItem(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);
        SpinnerItem currentItem = getItem(position);

        if (currentItem != null) {
            textView.setText(currentItem.getText());
        } else {
            // Set hint text for the last item
            textView.setText("Select");
        }

        return convertView;
    }
}
