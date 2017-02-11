package com.soldiersofmobile.todoekspert.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.soldiersofmobile.todoekspert.App;
import com.soldiersofmobile.todoekspert.R;
import com.soldiersofmobile.todoekspert.api.ErrorResponse;
import com.soldiersofmobile.todoekspert.api.ParseAcl;
import com.soldiersofmobile.todoekspert.api.Todo;
import com.soldiersofmobile.todoekspert.api.TodoApi;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;

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
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo);
        ButterKnife.bind(this);

        App app = (App) getApplication();
        todoApi = app.getTodoApi();
        converter = app.getConverter();

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        userId = preferences.getString(LoginActivity.USER_ID, "");
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

        }
    }

    private void sendToServer(final Todo todo) {

        todo.ACL = new HashMap<>();
        todo.ACL.put(userId, new ParseAcl());

        Call<Todo> todoCall = todoApi.postTodo(todo);
        todoCall.enqueue(new Callback<Todo>() {
            @Override
            public void onResponse(Call<Todo> call, Response<Todo> response) {
                if(response.isSuccessful()) {
                    todo.objectId = response.body().objectId;
                    Intent intent = new Intent();
                    intent.putExtra("todo", todo);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Todo> call, Throwable t) {

            }
        });

    }
}
