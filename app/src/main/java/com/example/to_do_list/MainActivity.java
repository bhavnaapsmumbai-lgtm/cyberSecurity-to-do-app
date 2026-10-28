package com.example.to_do_list;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import java.util.ArrayList;

public class MainActivity extends Activity {

    EditText taskInput;
    Button addButton;
    ListView taskList;

    ArrayList<String> tasks;
    ArrayAdapter<String> adapter;

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        taskInput = findViewById(R.id.taskInput);
        addButton = findViewById(R.id.addButton);
        taskList = findViewById(R.id.taskList);

        tasks = new ArrayList<>();

        preferences = getSharedPreferences("ToDoData", MODE_PRIVATE);

        loadTasks();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                tasks
        );

        taskList.setAdapter(adapter);

        addButton.setOnClickListener(v -> {

            String task = taskInput.getText().toString();

            if (!task.isEmpty()) {

                tasks.add(task);

                saveTasks();

                adapter.notifyDataSetChanged();

                taskInput.setText("");
            }

        });
    }

    void saveTasks() {

        String allTasks = "";

        for (int i = 0; i < tasks.size(); i++) {
            allTasks = allTasks + tasks.get(i) + "\n";
        }

        preferences.edit().putString("tasks", allTasks).apply();
    }

    void loadTasks() {

        String allTasks = preferences.getString("tasks", "");

        if (!allTasks.isEmpty()) {

            String[] savedTasks = allTasks.split("\n");

            for (int i = 0; i < savedTasks.length; i++) {
                tasks.add(savedTasks[i]);
            }

        }
    }
}