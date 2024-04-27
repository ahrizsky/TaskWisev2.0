package com.example.taskwiserebirth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskwiserebirth.database.DatabaseChangeListener;
import com.example.taskwiserebirth.database.MongoDbRealmHelper;
import com.example.taskwiserebirth.database.TaskDatabaseManager;
import com.example.taskwiserebirth.task.Task;
import com.example.taskwiserebirth.task.TaskAdapter;
import com.example.taskwiserebirth.task.TaskPriorityCalculator;
import com.example.taskwiserebirth.utils.DialogUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.mongodb.App;
import io.realm.mongodb.User;

public class FinishedTaskFragment extends Fragment implements TaskAdapter.TaskActionListener, DatabaseChangeListener, NestedScrollView.OnScrollChangeListener {

    private TaskAdapter taskAdapter;
    private TaskDatabaseManager taskDatabaseManager;
    private DialogUtils dialogUtils;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_finished_task, container, false);

        App app = MongoDbRealmHelper.initializeRealmApp();
        User user = app.currentUser();

        MongoDbRealmHelper.addDatabaseChangeListener(this);
        taskDatabaseManager = new TaskDatabaseManager(user, requireContext());

        dialogUtils = new DialogUtils(requireActivity(), taskDatabaseManager);

        NestedScrollView nestedScrollView = rootView.findViewById(R.id.scrollFinishedTask);
        nestedScrollView.setOnScrollChangeListener(this);

        setUpUnfinishedRecyclerView(rootView);

        return rootView;
    }

    private void setUpUnfinishedRecyclerView(View rootView) {
        RecyclerView cardRecyclerView = rootView.findViewById(R.id.finishedRecyclerView);

        List<Task> tasks = new ArrayList<>();
        taskAdapter = new TaskAdapter(requireContext(), tasks, this);

        cardRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        cardRecyclerView.setAdapter(taskAdapter);

        updateUnfinishedRecyclerView();
    }

    private void updateUnfinishedRecyclerView() {
        taskDatabaseManager.fetchAllTasks(new TaskDatabaseManager.TaskFetchListener() {
            @Override
            public void onTasksFetched(List<Task> tasks) {
                List<Task> sortedTasks = TaskPriorityCalculator.sortTasksByPriority(tasks, new Date());

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        taskAdapter.setTasks(sortedTasks);
                        taskAdapter.setSelectedDate(new Date());
                    });
                }
            }
        }, true);
    }

    @Override
    public void onScrollChange(@NonNull NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        boolean scrollingUp = scrollY < oldScrollY;
        ((MainActivity) requireActivity()).toggleNavBarVisibility(scrollingUp, true);
    }

    @Override
    public void onDatabaseChange() {
        updateUnfinishedRecyclerView();
    }

    @Override
    public void onEditTask(Task task) {
        dialogUtils.showBottomSheetDialog(task);
    }

    @Override
    public void onDeleteTask(Task task) {
        taskDatabaseManager.deleteTask(task);
    }

    @Override
    public void onDoneTask(Task task) {
        taskDatabaseManager.markTaskAsFinished(task);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MongoDbRealmHelper.removeDatabaseChangeListener(this);
    }
}