package com.soldiersofmobile.todoekspert.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.soldiersofmobile.todoekspert.App;
import com.soldiersofmobile.todoekspert.R;
import com.soldiersofmobile.todoekspert.api.ErrorResponse;
import com.soldiersofmobile.todoekspert.api.Todo;
import com.soldiersofmobile.todoekspert.api.TodoApi;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Converter;

public class AddTodoActivity extends AppCompatActivity {

    @BindView(R.id.content_edit_text)
    EditText contentEditText;
    @BindView(R.id.done_checkbox)
    CheckBox doneCheckbox;
    @BindView(R.id.add_button)
    Button addButton;
    @BindView(R.id.activity_add_todo)
    RelativeLayout activityAddTodo;
    private TodoApi todoApi;
    private Converter<ResponseBody, ErrorResponse> converter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo);
        ButterKnife.bind(this);

        App app = (App) getApplication();
        todoApi = app.getTodoApi();
        converter = app.getConverter();
    }

    @OnClick(R.id.add_button)
    public void onClick() {

        //TODO
        //wyciagnac content z ET, sprawdzic czy nie pusty!, ustawić error jesli pusty
        String content = contentEditText.getText().toString();
        boolean isDone = doneCheckbox.isChecked();

        if (content.isEmpty()) {
            contentEditText.setError(getString(R.string.this_field_is_required));
        } else {
            Todo todo = new Todo(content, isDone);

            sendToServer(todo);
            Intent intent = new Intent();
            intent.putExtra("content", content);
            intent.putExtra("done", isDone);
            intent.putExtra("todo", todo);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private void sendToServer(Todo todo) {

    }
}
