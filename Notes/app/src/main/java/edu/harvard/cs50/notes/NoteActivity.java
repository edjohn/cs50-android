package edu.harvard.cs50.notes;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import static edu.harvard.cs50.notes.MainActivity.database;

public class NoteActivity extends AppCompatActivity
{
    private EditText editText;
    private int id;
    private FloatingActionButton deleteNoteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        editText = findViewById(R.id.note_edit_text);
        String contents = getIntent().getStringExtra("contents");
        id = getIntent().getIntExtra("id", 0);
        editText.setText(contents);

        deleteNoteButton = findViewById(R.id.delete_note_button);
        deleteNoteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                database.noteDao().delete(id);
                finish();
            }
        });
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        database.noteDao().save(editText.getText().toString(), id);
    }
}