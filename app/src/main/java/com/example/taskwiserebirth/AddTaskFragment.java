package com.example.taskwiserebirth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskwiserebirth.database.DatabaseChangeListener;
import com.example.taskwiserebirth.database.MongoDbRealmHelper;
import com.example.taskwiserebirth.database.TaskDatabaseManager;
import com.example.taskwiserebirth.task.Task;
import com.example.taskwiserebirth.task.TaskAdapter;
import com.example.taskwiserebirth.task.TaskPriorityCalculator;
import com.example.taskwiserebirth.utils.DialogUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.mongodb.App;
import io.realm.mongodb.User;

public class AddTaskFragment extends Fragment implements DatabaseChangeListener, NestedScrollView.OnScrollChangeListener, TaskAdapter.TaskActionListener, CalendarAdapter.OnDateSelectedListener {

    private String daysSelected = null;
    private TaskAdapter taskAdapter;
    private Date selectedDate;
    private View rootView;
    private TaskDatabaseManager taskDatabaseManager;
    private DialogUtils dialogUtils;

    public AddTaskFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_task, container, false);

        // Realm initialization
        App app = MongoDbRealmHelper.initializeRealmApp();
        User user = app.currentUser();

        MongoDbRealmHelper.addDatabaseChangeListener(this);

        taskDatabaseManager = new TaskDatabaseManager(user, requireContext());
        dialogUtils = new DialogUtils(requireActivity(), taskDatabaseManager);

        NestedScrollView nestedScrollView = rootView.findViewById(R.id.nestedScrollView);
        nestedScrollView.setOnScrollChangeListener(this);

        setUpCalendarRecyclerView(rootView);
        setUpTaskRecyclerView(rootView);

        FloatingActionButton fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(v -> dialogUtils.showBottomSheetDialog(null));

        displayTimeOfDay(rootView);

        LinearLayout todayTaskContainer = rootView.findViewById(R.id.viewAllContainer);
        todayTaskContainer.setOnClickListener(v -> {
            // Navigate to All Task Fragment
            AllTaskFragment fragmentAllTask = new AllTaskFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, fragmentAllTask);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return rootView;
    }


    private void setUpCalendarRecyclerView(View rootView) {
        RecyclerView calendarRecyclerView = rootView.findViewById(R.id.calendarRecyclerView);

        TextView currentMonth = rootView.findViewById(R.id.monthTxt);

        Calendar calendar = Calendar.getInstance();

        // Set current selected day by user
        selectedDate = calendar.getTime();

        int currentMonthIndex = calendar.get(Calendar.MONTH);
        String[] months = new DateFormatSymbols().getMonths();
        String currentMonthName = months[currentMonthIndex].toUpperCase();

        currentMonth.setText(currentMonthName);

        List<Calendar> calendarList = getDatesForCurrentMonth();

        CalendarAdapter calendarAdapter = new CalendarAdapter(calendarList, this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        calendarRecyclerView.setLayoutManager(layoutManager);

        calendarRecyclerView.setAdapter(calendarAdapter);

        int currentPosition = getCurrentDatePosition(calendarList);
        if (currentPosition != -1) {
            layoutManager.scrollToPosition(currentPosition);
        }
    }

    private void setUpTaskRecyclerView(View rootView) {
        RecyclerView cardRecyclerView = rootView.findViewById(R.id.tasksRecyclerView);

        List<Task> tasks = new ArrayList<>();
        taskAdapter = new TaskAdapter(requireContext(), tasks,this);

        cardRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        cardRecyclerView.setAdapter(taskAdapter);

        updateTaskRecyclerView();
    }

    private void updateTaskRecyclerView() {
        taskDatabaseManager.fetchSelectedDayTasks(tasks -> {

            List<Task> sortedTasks = TaskPriorityCalculator.sortTasksByPriority(tasks, new Date());

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    taskAdapter.setTasks(sortedTasks);
                    taskAdapter.setSelectedDate(selectedDate);
                });
            }
        }, selectedDate);
    }

    private void displayTimeOfDay(View rootView) {
        TextView timeOfDayTextView = rootView.findViewById(R.id.tasksText);
        ImageView timeOfDayImageView = rootView.findViewById(R.id.timeOfDayImageView);

        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        String timeOfDay;
        int drawableResId;

        if (hourOfDay >= 6 && hourOfDay < 12) {
            timeOfDay = "Morning";
            drawableResId = R.drawable.baseline_sun2;
        } else if (hourOfDay >= 12 && hourOfDay < 18) {
            timeOfDay = "Afternoon";
            drawableResId = R.drawable.baseline_sun2;
        } else if (hourOfDay >= 18) {
            timeOfDay = "Evening";
            drawableResId = R.drawable.baseline_night2;
        } else {
            timeOfDay = "Night";
            drawableResId = R.drawable.baseline_night2;
        }

        timeOfDayTextView.setText(timeOfDay);
        timeOfDayImageView.setImageResource(drawableResId);
    }

    private List<Calendar> getDatesForCurrentMonth() {
        List<Calendar> calendarList = new ArrayList<>();
        Calendar currentDate = Calendar.getInstance();

        currentDate.set(Calendar.DAY_OF_MONTH, 1);

        int daysInMonth = currentDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int day = 1; day <= daysInMonth; day++) {
            Calendar date = (Calendar) currentDate.clone();
            date.set(Calendar.DAY_OF_MONTH, day);
            calendarList.add(date);
        }

        return calendarList;
    }

    private int getCurrentDatePosition(List<Calendar> calendarList) {
        Calendar currentDate = Calendar.getInstance();
        int currentDay = currentDate.get(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < calendarList.size(); i++) {
            Calendar calendar = calendarList.get(i);
            if (calendar.get(Calendar.DAY_OF_MONTH) == currentDay) {
                return i;
            }
        }

        return -1; // Current date not found
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MongoDbRealmHelper.removeDatabaseChangeListener(this);
    }

    @Override
    public void onDatabaseChange() {
        updateTaskRecyclerView();
    }

    @Override
    public void onScrollChange(@NonNull NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        boolean scrollingUp = scrollY < oldScrollY;
        ((MainActivity) requireActivity()).toggleNavBarVisibility(scrollingUp, true);
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
    public void onDateCardSelected(Calendar date) {
        selectedDate = date.getTime();
        updateTaskRecyclerView();

        TextView todayTask = rootView.findViewById(R.id.todayTask);
        Calendar currentDate = Calendar.getInstance();

        if (date.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR)
                && date.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH)
                && date.get(Calendar.DAY_OF_MONTH) == currentDate.get(Calendar.DAY_OF_MONTH)) {
            todayTask.setText("Today's Task");
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(date.getTime());
            todayTask.setText(formattedDate);
        }
    }
}
