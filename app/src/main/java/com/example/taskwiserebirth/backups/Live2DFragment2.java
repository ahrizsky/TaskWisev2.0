package com.example.taskwiserebirth.backups;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bin4rybros.demo.GLRenderer;
import com.bin4rybros.demo.LAppDelegate;
import com.example.taskwiserebirth.MainActivity;
import com.example.taskwiserebirth.R;
import com.example.taskwiserebirth.conversational.AIRandomSpeech;
import com.example.taskwiserebirth.conversational.HttpRequest;
import com.example.taskwiserebirth.conversational.SpeechRecognition;
import com.example.taskwiserebirth.conversational.SpeechSynthesis;
import com.example.taskwiserebirth.database.ConversationDbManager;
import com.example.taskwiserebirth.database.MongoDbRealmHelper;
import com.example.taskwiserebirth.database.TaskDatabaseManager;
import com.example.taskwiserebirth.database.UserDatabaseManager;
import com.example.taskwiserebirth.task.Task;
import com.example.taskwiserebirth.utils.CalendarUtils;
import com.example.taskwiserebirth.utils.DialogUtils;
import com.example.taskwiserebirth.utils.ValidValues;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.mongodb.App;
import io.realm.mongodb.User;

public class Live2DFragment2 extends Fragment implements View.OnTouchListener, SpeechRecognition.SpeechRecognitionListener {
    private GLSurfaceView glSurfaceView;
    private SpeechRecognition speechRecognition;
    private TaskDatabaseManager taskDatabaseManager;
    private ConversationDbManager conversationDbManager;
    private UserDatabaseManager userDatabaseManager;
    private User user;
    private Task finalTask;
    private boolean inTurnBasedInteraction = false;
    private boolean isUserDone = false;
    private final String TAG_SERVER_RESPONSE = "SERVER_RESPONSE";
    private final String aiName = "Mio";
    private TextView realTimeSpeechTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live2d, container, false);

        glSurfaceView = view.findViewById(R.id.gl_surface_view);
        glSurfaceView.setEGLContextClientVersion(2); // Using OpenGL ES 2.0

        GLRenderer glRenderer = new GLRenderer();
        glSurfaceView.setRenderer(glRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        glSurfaceView.setOnTouchListener(this);

        App app = MongoDbRealmHelper.initializeRealmApp();
        user = app.currentUser();

        taskDatabaseManager = new TaskDatabaseManager(user, requireContext());
        conversationDbManager = new ConversationDbManager(user);
        userDatabaseManager = new UserDatabaseManager(user, requireContext());

        ImageButton collapseBtn = view.findViewById(R.id.fullscreen_button);
        FloatingActionButton speakBtn = view.findViewById(R.id.speakBtn);

        collapseBtn.setOnClickListener(v -> ((MainActivity) requireActivity()).toggleNavBarVisibility(false, false));

        speechRecognition = new SpeechRecognition(requireContext(), speakBtn, this);
        speakBtn.setOnClickListener(v -> {
            if (speechRecognition.isListening()) {
                speechRecognition.stopSpeechRecognition();
            } else {
                speechRecognition.startSpeechRecognition();
            }
        });

        realTimeSpeechTextView = view.findViewById(R.id.realTimeSpeechTextView);
        realTimeSpeechTextView.setOnClickListener(new View.OnClickListener() {
            private boolean isExpanded = false;

            @Override
            public void onClick(View v) {
                if (isExpanded) {
                    realTimeSpeechTextView.setMaxLines(7);
                    realTimeSpeechTextView.setEllipsize(TextUtils.TruncateAt.END);
                } else {
                    realTimeSpeechTextView.setMaxLines(Integer.MAX_VALUE);
                    realTimeSpeechTextView.setEllipsize(null);
                }
                isExpanded = !isExpanded;
            }
        });

        return view;
    }

    private void performIntent(String intent, String responseText){
        Pattern taskPattern = Pattern.compile("TASK_NAME: (.+?)(?:\\nDEADLINE:|$)");
        Pattern deadlinePattern = Pattern.compile("DEADLINE: (.+)");

        String taskName = "", deadline = "";

        Matcher taskMatcher = taskPattern.matcher(responseText);
        if (taskMatcher.find()) {
            taskName = taskMatcher.group(1);
        }

        Matcher deadlineMatcher = deadlinePattern.matcher(responseText);
        if (deadlineMatcher.find()) {
            deadline = deadlineMatcher.group(1);
            // check if the deadline matches the required pattern "MM-dd-yyyy | hh:mm a"
            if (!deadline.matches("\\d{2}-\\d{2}-\\d{4} \\| \\d{2}:\\d{2} [AP]M")) {
                deadline = "No deadline";
            } else {
                // check if deadline is set to the past, if true set to default "No Deadline"
                if (!CalendarUtils.isDateAccepted(deadline)) {
                    Toast.makeText(requireContext(), "Deadline cannot be in the past so it is set to default", Toast.LENGTH_SHORT).show();
                    deadline = "No deadline";
                }
            }
        }

        if (taskName.isEmpty()) {
            Toast.makeText(requireContext(), "Task name not specified", Toast.LENGTH_LONG).show();
            return;
        }

        switch(intent) {
            case "Add Task":
                taskDatabaseManager.insertTask(setTaskFromSpeech(taskName, "No deadline"));
                SpeechSynthesis.synthesizeSpeechAsync(AIRandomSpeech.generateTaskAdded(taskName));
                return;
            case "Add Task With Deadline":
                taskDatabaseManager.insertTask(setTaskFromSpeech(taskName, deadline));
                SpeechSynthesis.synthesizeSpeechAsync(AIRandomSpeech.generateTaskAdded(taskName));
                return;
            case "Edit Task":
                editTaskThroughSpeech(taskName);
                return;
            case "Delete Task":
                deleteTaskThroughSpeech(taskName);
                return;
            default:
                Toast.makeText(requireContext(), "Failed to perform intent", Toast.LENGTH_LONG).show();
        }
    }

    private void deleteTaskThroughSpeech(String taskName) {
        taskDatabaseManager.fetchTaskByName(tasks -> {
            Task task = tasks.get(0);
            Log.d(TAG_SERVER_RESPONSE, "Task found: " + task.getTaskName());

            taskDatabaseManager.deleteTask(task);
            SpeechSynthesis.synthesizeSpeechAsync("I have successfully deleted your task");
        }, taskName);
    }

    private void editTaskThroughSpeech(String taskName) {
        taskDatabaseManager.fetchTaskByName(tasks -> {
            Task task = tasks.get(0);
            Log.d(TAG_SERVER_RESPONSE, "Task found: " + task.getTaskName());

            finalTask = task;
            isUserDone = false;

            turnBasedInteraction();
            inTurnBasedInteraction = true;
        }, taskName);
    }

    private void askQuestion( String question) {
        Toast.makeText(requireContext(), String.format("%s: %s", aiName, question), Toast.LENGTH_SHORT).show();
        SpeechSynthesis.synthesizeSpeechAsync(question);
    }

    private void turnBasedInteraction() {
        if (isUserDone) {
            return;
        }

        final String initialQuestion = "Sure, what do you want to edit?";
        final String followUpQuestion = "Got it. Is there anything else?";

        if (!inTurnBasedInteraction) {
            askQuestion(initialQuestion);
        } else {
            askQuestion(followUpQuestion);
        }
    }

    private void processResponse(String detail, String responseText) {
        Map<String, Pattern> patternMap = new HashMap<>();
        patternMap.put("Task Name", Pattern.compile("TASK_NAME: (.+)"));
        patternMap.put("Urgency", Pattern.compile("URGENCY: (.+)"));
        patternMap.put("Importance", Pattern.compile("IMPORTANCE: (.+)"));
        patternMap.put("Deadline", Pattern.compile("DEADLINE: (.+)"));
        patternMap.put("Set or Edit Recurrence", Pattern.compile("RECURRENCE: (.+)"));
        patternMap.put("Repeat Task", Pattern.compile("RECURRENCE: (.+)")); // Assuming it's the same as "Set or Edit Recurrence"
        patternMap.put("Schedule", Pattern.compile("SCHEDULE: (.+)"));
        patternMap.put("Reminder", Pattern.compile("REMINDER: (.+)"));
        patternMap.put("Notes", Pattern.compile("NOTES: (.+)"));

        Pattern pattern = patternMap.get(detail);
        if (pattern == null) {
            if (responseText.equals("DONE")) {
                inTurnBasedInteraction = false;
                isUserDone = true;
                prefilterFinalTask(finalTask);
                taskDatabaseManager.updateTask(finalTask);
                SpeechSynthesis.synthesizeSpeechAsync("I have updated your task " + finalTask.getTaskName());
                return;
            } else if (responseText.equals("NOT DONE")) {
                SpeechSynthesis.synthesizeSpeechAsync(AIRandomSpeech.generateTaskAdded("Ok! I'm listening."));
                return;
            } else {
                inTurnBasedInteraction = false;
                isUserDone = true;
                prefilterFinalTask(finalTask);
                taskDatabaseManager.updateTask(finalTask);
                Log.e("TEST", finalTask.getRecurrence());
                Log.e("TEST", finalTask.getId().toString());
                SpeechSynthesis.synthesizeSpeechAsync(AIRandomSpeech.generateTaskAdded("Sorry, I didn't get that. If you need anything else, just tell me."));
                return;
            }
        }

        Matcher matcher = pattern.matcher(responseText);
        if (matcher.find()) {
            String value = matcher.group(1);
            applyTaskDetail(detail, value);
        } else {
            Toast.makeText(requireContext(), "Couldn't determine the correct value for " + detail, Toast.LENGTH_SHORT).show();
        }
    }

    //TODO: if user edited the recurrence, tell user deadline and sched were set to default
    private void prefilterFinalTask(Task finalTask) {
        if(!finalTask.getRecurrence().equals("None")) {
            finalTask.setDeadline("No deadline");   // Recurrent tasks have no deadlines

            if(finalTask.getSchedule().equals("No schedule")) {
                finalTask.setSchedule("09:00 AM");      // default sched time
            } else {
                String schedule = finalTask.getSchedule();
                // Extracting the time part from the schedule string
                String filteredSched = schedule.substring(schedule.lastIndexOf("|") + 1).trim();
                finalTask.setSchedule(filteredSched);
            }
        }

        taskDatabaseManager.updateTask(finalTask);
    }

    private void applyTaskDetail(String detail, String value) {
        switch (detail) {
            case "Task Name":
                finalTask.setTaskName(value);
                break;
            case "Urgency":
                if (ValidValues.VALID_URGENCY_LEVELS.contains(value)) {
                    finalTask.setUrgencyLevel(value);
                } else {
                    finalTask.setUrgencyLevel("None");
                }
                break;
            case "Importance":
                if (ValidValues.VALID_IMPORTANCE_LEVELS.contains(value)) {
                    finalTask.setImportanceLevel(value);
                } else {
                    finalTask.setImportanceLevel("None");
                }
                break;
            case "Deadline":
                if (CalendarUtils.isDateAccepted(value)) {
                    finalTask.setDeadline(value);
                } else {
                    finalTask.setDeadline("No deadline");
                }
                break;
            case "Set or Edit Recurrence":
            case "Repeat Task":
                if (CalendarUtils.isRecurrenceAccepted(value)) {
                    finalTask.setRecurrence(CalendarUtils.formatRecurrence(value));
                } else {
                    finalTask.setRecurrence("None");
                }
                break;
            case "Schedule":
                if (CalendarUtils.isDateAccepted(value)) {
                    finalTask.setSchedule(value);
                } else {
                    finalTask.setSchedule("No schedule");
                }
                break;
            case "Reminder":
                finalTask.setReminder(value.equals("True"));
                break;
            case "Notes":
                finalTask.setNotes(value);
                break;
        }
    }

    private Task setTaskFromSpeech(String taskName, String deadline) {
        String urgency = DialogUtils.setAutomaticUrgency(deadline);

        Task newTask = new Task();
        newTask.setTaskName(taskName);
        newTask.setImportanceLevel("None");
        newTask.setUrgencyLevel(urgency);
        newTask.setDeadline(deadline);
        newTask.setSchedule("No schedule");
        newTask.setRecurrence("None");
        newTask.setReminder(true);
        newTask.setNotes("");
        newTask.setStatus("Unfinished");
        newTask.setDateFinished(null);

        return newTask;
    }

    @Override
    public void onSpeechRecognized(String recognizedSpeech) {
        realTimeSpeechTextView.setText(recognizedSpeech);
        HttpRequest.sendRequest(recognizedSpeech, aiName, user.getId(), inTurnBasedInteraction, new HttpRequest.HttpRequestCallback() {
            @Override
            public void onSuccess(String intent, String responseText) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (inTurnBasedInteraction) {
                        processResponse(intent, responseText);
                        turnBasedInteraction();
                    } else {
                        if (!intent.equals("null")) {
                            performIntent(intent, responseText);
                        } else {
                            conversationDbManager.insertDialogue(recognizedSpeech, false);

                            Toast.makeText(requireContext(), String.format("%s: %s", aiName, responseText), Toast.LENGTH_LONG).show();
//                            ttsManager.convertTextToSpeech(responseText);
                            SpeechSynthesis.synthesizeSpeechAsync(responseText);
                            conversationDbManager.insertDialogue(responseText, true);
                        }
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                    Log.d(TAG_SERVER_RESPONSE, errorMessage);
                });
            }
        });
    }

    @Override
    public void onPartialSpeechRecognized(String partialSpeech) {
        realTimeSpeechTextView.setVisibility(View.VISIBLE);
        realTimeSpeechTextView.setText(partialSpeech);
    }

    @Override
    public void onStart() {
        super.onStart();
        LAppDelegate.getInstance().onStart(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        glSurfaceView.onPause();
        LAppDelegate.getInstance().onPause();

    }

    @Override
    public void onStop() {
        super.onStop();
        LAppDelegate.getInstance().onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (speechRecognition != null) {
            speechRecognition.stopSpeechRecognition();
        }

//        if (ttsManager != null) {
//            ttsManager.shutdown();
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LAppDelegate.getInstance().onDestroy();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float pointX = event.getX();
        float pointY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                LAppDelegate.getInstance().onTouchBegan(pointX, pointY);
                break;
            case MotionEvent.ACTION_UP:
                LAppDelegate.getInstance().onTouchEnd(pointX, pointY);
                break;
            case MotionEvent.ACTION_MOVE:
                LAppDelegate.getInstance().onTouchMoved(pointX, pointY);
                break;
        }
        return true;
    }
}
