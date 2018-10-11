package com.example.darkknight.doit2;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.date.MonthAdapter;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    DatabaseHelper databaseHelper;
    ArrayAdapter<String> adapter;
    ArrayList<String> taskArrayList;
    ListView listTask;
    String setDueDate;
    String setDueTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper= new DatabaseHelper(this);
        listTask=(ListView)findViewById(R.id.listTask);
        loadTaskList();

        final AlertDialog.Builder dialogBuilder= new AlertDialog.Builder(MainActivity.this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);

        //when user taps on the floating action button display a dialog box to add new task and due date and time
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View dialogView= getLayoutInflater().inflate(R.layout.new_task_dialog, null);

                final EditText task= (EditText) dialogView.findViewById(R.id.newTask);
                final Button date= (Button) dialogView.findViewById(R.id.datePicker);
                final Button time= (Button) dialogView.findViewById(R.id.timePicker);
                Button addButton= (Button) dialogView.findViewById(R.id.addNewTask);

                dialogBuilder.setView(dialogView);
                final AlertDialog newTaskDialog= dialogBuilder.create();
                newTaskDialog.show();

                date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar now= Calendar.getInstance();
                        DatePickerDialog datePickerDialog= DatePickerDialog.newInstance(MainActivity.this
                        ,now.get(Calendar.YEAR)
                        ,now.get(Calendar.MONTH)
                        ,now.get(Calendar.DAY_OF_MONTH));
                        datePickerDialog.setTitle("Pick a Due Date");
                        datePickerDialog.show(getFragmentManager(),"DatePicker");

                    }
                });

                time.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar now= Calendar.getInstance();
                        TimePickerDialog timePickerDialog= TimePickerDialog.newInstance(MainActivity.this
                        ,now.get(Calendar.HOUR_OF_DAY)
                        ,now.get(Calendar.MINUTE)
                        ,true);
                        timePickerDialog.setTitle("Pick a Due Time");
                        timePickerDialog.show(getFragmentManager(), "TimePicker");
                    }
                });


                addButton.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onClick(View v) {
                        if (!task.getText().toString().isEmpty()){

                            String taskValue=String.valueOf(task.getText());
                            databaseHelper.insertNewTask(taskValue, setDueDate, setDueTime);

                            AlarmManager alarmManager= (AlarmManager)getSystemService(ALARM_SERVICE);
                            //getting the due date and time
                            int[] dueDateArray= splitDate(setDueDate);
                            int[] dueTimeArray= splitTime(setDueTime);
                            //creating a calendar instance and setting the due time
                            Calendar taskDue= Calendar.getInstance();
                            taskDue.setTimeInMillis(System.currentTimeMillis());
                            taskDue.set(Calendar.YEAR, dueDateArray[2]);
                            taskDue.set(Calendar.MONTH, dueDateArray[1]-1);
                            taskDue.set(Calendar.DAY_OF_MONTH, dueDateArray[0]);
                            taskDue.set(Calendar.HOUR_OF_DAY, dueTimeArray[0]-1);
                            taskDue.set(Calendar.MINUTE, dueTimeArray[1]);
                            taskDue.set(Calendar.SECOND, dueTimeArray[2]);

                            long currentTime= System.currentTimeMillis();
                            System.out.println("CURRENT TIME "+ currentTime);
                            System.out.println("FUTURE TIME" + taskDue.getTimeInMillis());
                            System.out.println("Interval" + (taskDue.getTimeInMillis()-currentTime));

                            Intent intent= new Intent("doit.app.action.DISPLAY_NOTIFICATION");
                            intent.putExtra("Task Due", taskValue);
                            PendingIntent broadcast= PendingIntent.getBroadcast(getApplicationContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                            alarmManager.set(AlarmManager.RTC_WAKEUP, taskDue.getTimeInMillis(), broadcast);
                            loadTaskList();
                            Toast.makeText(MainActivity.this, getString(R.string.task_added), Toast.LENGTH_SHORT).show();
                            newTaskDialog.dismiss();
                        }
                        else{
                            Toast.makeText(MainActivity.this, getString(R.string.add_task_error), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        });

        View parent= (View) listTask.getParent();
        ListView editTaskList= (ListView) parent.findViewById(R.id.listTask);


        listTask.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                View editDialogView= getLayoutInflater().inflate(R.layout.edit_task_dialog, null);

                View parentView= (View) listTask.getParent();
                final String oldTask= taskArrayList.get(position).toString();
                System.out.println("THIS IS THE OLD TASK: " + oldTask);

                final EditText editedTask= (EditText) editDialogView.findViewById(R.id.editText);
                final Button edit= (Button) editDialogView.findViewById(R.id.editButton);
                taskArrayList.clear();
                taskArrayList=databaseHelper.getTaskList();

                editedTask.setText(taskArrayList.get(position).toString());

                dialogBuilder.setView(editDialogView);
                final  AlertDialog editTaskDialog= dialogBuilder.create();
                editTaskDialog.show();

                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!editedTask.getText().toString().isEmpty()){
                            taskArrayList.remove(position);
                            taskArrayList.add(position, editedTask.getText().toString());
                            databaseHelper.updateTask(editedTask.getText().toString(), oldTask);
                            adapter.notifyDataSetChanged();
                            editTaskDialog.dismiss();
                            loadTaskList();
                            Toast.makeText(MainActivity.this, getString(R.string.task_edited_message), Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(MainActivity.this,getString(R.string.add_task_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                loadTaskList();
            }
        });
    }

    private void loadTaskList(){
        taskArrayList= databaseHelper.getTaskList();
        if(adapter==null){
            adapter= new ArrayAdapter<String>(this, R.layout.row, R.id.task_title, taskArrayList);
            listTask.setAdapter(adapter);
        }
        else{
            adapter.clear();
            adapter.addAll(taskArrayList);
            adapter.notifyDataSetChanged();
        }
    }

    public void deleteTask(final View view){

        View parent= (View)view.getParent();
        final TextView taskTextView= (TextView)parent.findViewById(R.id.task_title);
        final CheckBox itemCheckBox= (CheckBox)parent.findViewById(R.id.checkbox);
        /*Timer timer= new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                if(itemCheckBox.isChecked()){
                    String task= String.valueOf(taskTextView.getText());
                    System.out.println(task);
                    databaseHelper.deleteTask(task);
                }
            }
        }, 7000);*/
        if(itemCheckBox.isChecked()){
            String task= String.valueOf(taskTextView.getText());
            System.out.println(task);
            databaseHelper.deleteTask(task);
            loadTaskList();
            System.out.println("Load Task List Called!");
            itemCheckBox.setChecked(false);
        }

    }



    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.MONTH, (monthOfYear));
        calendar.set(Calendar.YEAR, year);
        SimpleDateFormat sdf= new SimpleDateFormat("dd/MM/yyyy");
        setDueDate=sdf.format(calendar.getTime());
        System.out.println("Due date set as "+ setDueDate);
        Toast.makeText(MainActivity.this, getString(R.string.duedate) +setDueDate, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        SimpleDateFormat sdf= new SimpleDateFormat("HH:mm:ss");
        setDueTime=sdf.format(calendar.getTime());
        System.out.println("Due time set as "+ setDueTime);
        Toast.makeText(MainActivity.this, getString(R.string.duetime) +setDueTime, Toast.LENGTH_SHORT).show();
        }


    public int[] splitDate(String date){
        int[] dateArray= new int[3];
        String[] array= date.split("/");
        for(int i=0; i<array.length; i++){
            dateArray[i]=Integer.parseInt(array[i]);

        }
        return dateArray;
    }

    public int[] splitTime(String time){
        int[] timeArray= new int[3];
        String[] array= time.split(":");
        for(int i=0; i<array.length; i++){
            timeArray[i]=Integer.parseInt(array[i]);

        }
        return timeArray;
    }
}
