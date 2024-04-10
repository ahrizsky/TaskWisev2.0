package com.example.taskwiserebirth.database;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskwiserebirth.R;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskViewHolder> {

    Context context;
    List<Task> tasks;

    public TaskAdapter(Context context, List<Task> tasks) {
        this.context = context;
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TaskViewHolder(LayoutInflater.from(context).inflate(R.layout.item_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.taskName.setText(tasks.get(position).getTaskName());
        holder.deadline.setText(tasks.get(position).getDeadline());
        holder.priority.setText(tasks.get(position).getPriorityLevel()); // TODO: replace with priority
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }
}
