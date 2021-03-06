package com.example.rmatos.simpletodo.fragments;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatDelegate;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rmatos.simpletodo.R;
import com.example.rmatos.simpletodo.Task;
import com.example.rmatos.simpletodo.TaskStore;
import com.example.rmatos.simpletodo.receivers.AlarmReceiver;
import com.example.rmatos.simpletodo.receivers.NotificationReceiver;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by RMatos on 05/08/2017.
 */

public class TaskFragment extends Fragment {
    private static final String ARG_TASK_ID = "task_ID";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";

    private static final int REQUEST_DUE_DATE = 0;
    private static final int REQUEST_DUE_TIME = 1;
    private static final int REQUEST_REMINDER_DATE = 2;
    private static final int REQUEST_REMINDER_TIME = 3;

    private static final String DATE_FORMAT_DATE = "EEE, dd MMM yyyy";
    private static final String DATE_FORMAT_TIME = "HH:mm";

    private Task mTask;
    private EditText mTitleField;
    private EditText mNotesField;
    private TextView mDueDateField;
    private TextView mDueTimeField;
    private TextView mReminderDateField;
    private TextView mReminderTimeField;
    private ImageView mReminderTypeImageView;
    private ImageView mReminderDeleteImageView;

    //Returns this fragment to be created by another acitivty.
    //Ensures any activity can call this fragment
    public static TaskFragment newInstance(UUID taskID) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_TASK_ID, taskID);

        TaskFragment fragment = new TaskFragment();
        fragment.setArguments(args);
        return fragment;
    }


    //When fragment is launched automatically create template Task class
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Gets task by using taskID passed in by calling activity
        UUID taskID = (UUID) getArguments().getSerializable(ARG_TASK_ID);
        mTask = TaskStore.get(getActivity()).getTask(taskID);

        //Needed for vector graphics
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    //Pushes updates to DB
    @Override
    public void onPause() {
        super.onPause();

        TaskStore.get(getActivity()).updateTask(mTask);

        if (mReminderDateField.getText() != null && mReminderTimeField.getText() != null) {

            if (mTask.getReminderType() == Task.ReminderType.ALARM) {
                AlarmReceiver.setAlarms(getContext());
            } else if (mTask.getReminderType() == Task.ReminderType.NOTIFICATION) {
                NotificationReceiver.setNotifications(getContext());
            }
        } else {
            if (mTask.getReminderType() == Task.ReminderType.ALARM)
                AlarmReceiver.cancelAlarm(getContext(), mTask);
            else if (mTask.getReminderType() == Task.ReminderType.NOTIFICATION)
                NotificationReceiver.cancelAlarm(getContext(), mTask);

            mTask.setReminder(null);
            mTask.setReminderType(Task.ReminderType.NONE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task, container, false);

        DateFormat dateFormat = new DateFormat();

        mTitleField = (EditText) view.findViewById(R.id.task_title);
        mTitleField.setText(mTask.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                mTask.setTitle(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        mNotesField = (EditText) view.findViewById(R.id.task_notes);
        mNotesField.setText(mTask.getNote());
        mNotesField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mTask.setNote(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        mDueDateField = (TextView) view.findViewById(R.id.task_dueDate);
        if (mTask.getDueDate() != null)
            mDueDateField.setText(dateFormat.format(DATE_FORMAT_DATE, mTask.getDueDate()));
        mDueDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDatePicker(mTask.getDueDate(), REQUEST_DUE_DATE);
            }
        });

        mDueTimeField = (TextView) view.findViewById(R.id.task_dueTime);
        if (mTask.getDueDate() != null)
            mDueTimeField.setText(dateFormat.format(DATE_FORMAT_TIME, mTask.getDueDate()));
        else
            mDueTimeField.setVisibility(View.GONE);
        mDueTimeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTimePicker(mTask.getDueDate(), REQUEST_DUE_TIME);
            }
        });

        mReminderDateField = (TextView) view.findViewById(R.id.task_reminder_date);
        if (mTask.getReminder() != null)
            mReminderDateField.setText(dateFormat.format(DATE_FORMAT_DATE, mTask.getReminder()));
        mReminderDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDatePicker(mTask.getDueDate(), REQUEST_REMINDER_DATE);
            }
        });

        mReminderTimeField = (TextView) view.findViewById(R.id.task_reminder_time);
        if (mTask.getReminder() != null && !(mTask.getReminder().getHours() == 0 && mTask.getReminder().getMinutes() == 0))
            mReminderTimeField.setText(dateFormat.format(DATE_FORMAT_TIME, mTask.getReminder()));
        else
            mReminderTimeField.setVisibility(View.GONE);
        mReminderTimeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTimePicker(mTask.getReminder(), REQUEST_REMINDER_TIME);
            }
        });

        mReminderTypeImageView = (ImageView) view.findViewById(R.id.task_reminder_type);
        if (mTask.getReminderType() == Task.ReminderType.ALARM) {
            mReminderTypeImageView.setImageResource(R.drawable.ic_alarm);
        } else if (mTask.getReminderType() == Task.ReminderType.NOTIFICATION) {
            mReminderTypeImageView.setImageResource(R.drawable.ic_notification);
        } else {
            mReminderTypeImageView.setVisibility(View.GONE);
        }
        mReminderTypeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTask.getReminderType() == Task.ReminderType.ALARM) {
                    mTask.setReminderType(Task.ReminderType.NOTIFICATION);
                    mReminderTypeImageView.setImageResource(R.drawable.ic_notification);
                } else if (mTask.getReminderType() == Task.ReminderType.NOTIFICATION) {
                    mTask.setReminderType(Task.ReminderType.ALARM);
                    mReminderTypeImageView.setImageResource(R.drawable.ic_alarm);
                }
            }
        });

        mReminderDeleteImageView = (ImageView) view.findViewById(R.id.task_reminder_delete);
        if (mReminderTimeField.getVisibility() == View.VISIBLE) {
            mReminderDeleteImageView.setVisibility(View.VISIBLE);
        } else {
            mReminderDeleteImageView.setVisibility(View.GONE);
        }
        mReminderDeleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mReminderDateField.setText(null);
                mReminderTimeField.setText(null);
                mReminderTimeField.setVisibility(View.GONE);
                mReminderTypeImageView.setVisibility(View.GONE);
                mReminderDeleteImageView.setVisibility(View.GONE);
                mTask.setReminder(null);
                mTask.setReminderType(Task.ReminderType.NONE);
                AlarmReceiver.cancelAlarm(getContext(), mTask);
                NotificationReceiver.cancelAlarm(getContext(), mTask);
            }
        });



        return view;

    }

    private void createDatePicker(Date initialDate, int requestCode) {
        //IF initialDate isnt set, set it to current date time
        if (initialDate == null)
            initialDate = new Date();
        FragmentManager manager = getFragmentManager();
        DatePickerFragment dialog = DatePickerFragment.newInstance(initialDate);
        dialog.setTargetFragment(TaskFragment.this, requestCode);
        dialog.show(manager, DIALOG_DATE);
    }

    private void createTimePicker(Date initialTime, int requestCode) {
        //IF initialDate isnt set, set it to current date time
        if (initialTime == null)
            initialTime = new Date();
        FragmentManager manager = getFragmentManager();
        TimePickerFragment dialog = TimePickerFragment.newInstance(initialTime);
        dialog.setTargetFragment(TaskFragment.this, requestCode);
        dialog.show(manager, DIALOG_TIME);
    }

    //Returned dates & times from dialog boxes
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        DateFormat dateFormat = new DateFormat();

        if (requestCode == REQUEST_DUE_DATE)
        {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mTask.setDueDate(date);
            mDueDateField.setText(dateFormat.format(DATE_FORMAT_DATE, date));
            mDueTimeField.setVisibility(View.VISIBLE);
        }
        else if (requestCode == REQUEST_DUE_TIME)
        {
            Date date = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mTask.setDueDate(date);
            mDueTimeField.setText(dateFormat.format(DATE_FORMAT_TIME, date));
        }
        else if (requestCode == REQUEST_REMINDER_DATE)
        {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mTask.setReminder(date);
            mReminderDateField.setText(dateFormat.format(DATE_FORMAT_DATE, date));
            mReminderTimeField.setVisibility(View.VISIBLE);
            mReminderTypeImageView.setVisibility(View.VISIBLE);
            mReminderDeleteImageView.setVisibility(View.VISIBLE);
            if (mTask.getReminderType() == Task.ReminderType.NONE) {
                mReminderTypeImageView.setImageResource(R.drawable.ic_notification);
                mTask.setReminderType(Task.ReminderType.NOTIFICATION);
            }
        }
        else if (requestCode == REQUEST_REMINDER_TIME)
        {
            Date date = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mTask.setReminder(date);
            mReminderTimeField.setText(dateFormat.format(DATE_FORMAT_TIME, date));
        }



    }
}
