package com.soldiersofmobile.todoekspert;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.soldiersofmobile.todoekspert.api.ErrorResponse;
import com.soldiersofmobile.todoekspert.api.Todo;
import com.soldiersofmobile.todoekspert.api.TodoApi;
import com.soldiersofmobile.todoekspert.api.TodosResponse;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TodoListActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 123;
    private static final String TAG = TodoListActivity.class.getSimpleName();
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.todo_list_view)
    ListView todoListView;
    @BindView(R.id.content_todo_list)
    RelativeLayout contentTodoList;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    private SharedPreferences preferences;
    private String token;
    //private ArrayAdapter<Todo> adapter;
    private TodoAdapter adapter;

    class TodoAdapter extends BaseAdapter {

        List<Todo> todos = new ArrayList<>();

        public void addAll(Todo[] todoArray) {
            for (Todo todo : todoArray) {
                todos.add(todo);
            }
            notifyDataSetChanged();

        }

        @Override
        public int getCount() {
            return todos.size();
        }

        @Override
        public Todo getItem(int position) {
            return todos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false);
                view.setTag(new ViewHolder(view));
            }
            Todo todo = getItem(position);

            ViewHolder holder = (ViewHolder) view.getTag();

            holder.checkBox.setChecked(todo.done);
            holder.checkBox.setText(todo.content);

            return view;
        }

        class ViewHolder {
            @BindView(R.id.checkBox)
            CheckBox checkBox;
            @BindView(R.id.button)
            Button button;

            ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String username = preferences.getString(LoginActivity.USERNAME, "");
        token = preferences.getString(LoginActivity.TOKEN, "");

        if (username.isEmpty() || token.isEmpty()) {
            goToLogin();
            return;
        }

        setContentView(R.layout.activity_todo_list);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
//        adapter = new ArrayAdapter<Todo>(this,
//                R.layout.item_todo, R.id.checkBox);
        adapter = new TodoAdapter();
        todoListView.setAdapter(adapter);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.todo_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                Intent intent = new Intent(this, AddTodoActivity.class);
                startActivityForResult(intent, REQUEST_CODE);


                break;
            case R.id.action_refresh:
                refresh();
                break;
            case R.id.action_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.confirm_logout);
                builder.setMessage(R.string.are_you_sure);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor edit = preferences.edit();
                        edit.remove(LoginActivity.USERNAME);
                        edit.remove(LoginActivity.TOKEN);
                        edit.apply();

                        goToLogin();
                    }
                });
                builder.setNegativeButton(R.string.no, null);
                builder.setCancelable(false);
                builder.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refresh() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://parseapi.back4app.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final Converter<ResponseBody, ErrorResponse> converter
                = retrofit.responseBodyConverter(ErrorResponse.class, new Annotation[0]);


        TodoApi todoApi = retrofit.create(TodoApi.class);

        Call<TodosResponse> call = todoApi.getTodos(token);
        call.enqueue(new Callback<TodosResponse>() {
            @Override
            public void onResponse(Call<TodosResponse> call, Response<TodosResponse> response) {
                if (response.isSuccessful()) {
                    TodosResponse body = response.body();

                    adapter.addAll(body.results);

                    for (Todo result : body.results) {
                        Log.d(TAG, result.toString());
                    }


                }
            }

            @Override
            public void onFailure(Call<TodosResponse> call, Throwable t) {

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                Todo todo = (Todo) data.getSerializableExtra("todo");
                Toast.makeText(this, "OK:" + todo.content, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "CANCEL", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
