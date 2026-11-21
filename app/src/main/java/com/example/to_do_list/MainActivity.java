package com.example.to_do_list;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    EditText taskInput;
    Button addButton;
    Button logoutButton;
    ListView taskList;

    ArrayList<String> tasks;
    ArrayList<String> taskIds;
    ArrayAdapter<String> adapter;

    FirebaseAuth auth;
    CollectionReference tasksRef;
    ListenerRegistration listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        // Security check: only a logged-in user with a verified email may continue
        if (user == null || !user.isEmailVerified()) {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        taskInput = findViewById(R.id.taskInput);
        addButton = findViewById(R.id.addButton);
        logoutButton = findViewById(R.id.logoutButton);
        taskList = findViewById(R.id.taskList);

        // Each user's tasks live under their own ID
        tasksRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("tasks");

        tasks = new ArrayList<>();
        taskIds = new ArrayList<>();

        // This layout shows a tick box next to each task
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_multiple_choice,
                tasks
        );
        taskList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        taskList.setAdapter(adapter);

        // Live updates: the list refreshes whenever the cloud data changes
        listener = tasksRef.orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Could not load tasks", Toast.LENGTH_LONG).show();
                        return;
                    }
                    tasks.clear();
                    taskIds.clear();
                    ArrayList<Boolean> doneStates = new ArrayList<>();

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String text = doc.getString("text");
                        if (text != null) {
                            tasks.add(text);
                            taskIds.add(doc.getId());
                            doneStates.add(Boolean.TRUE.equals(doc.getBoolean("done")));
                        }
                    }

                    taskList.clearChoices();
                    adapter.notifyDataSetChanged();
                    for (int i = 0; i < doneStates.size(); i++) {
                        taskList.setItemChecked(i, doneStates.get(i));
                    }
                });

        // Tapping a task ticks or unticks it, and saves the change
        taskList.setOnItemClickListener((parent, view, position, id) -> {
            boolean checked = taskList.isItemChecked(position);
            tasksRef.document(taskIds.get(position))
                    .update("done", checked)
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Could not update task", Toast.LENGTH_LONG).show());
        });

        addButton.setOnClickListener(v -> {
            String task = taskInput.getText().toString().trim();

            if (!task.isEmpty()) {
                Map<String, Object> data = new HashMap<>();
                data.put("text", task);
                data.put("done", false);
                data.put("createdAt", FieldValue.serverTimestamp());

                tasksRef.add(data).addOnFailureListener(e ->
                        Toast.makeText(this, "Could not save task", Toast.LENGTH_LONG).show());

                taskInput.setText("");
            }
        });

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) {
            listener.remove();
        }
    }
}