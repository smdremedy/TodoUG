package com.soldiersofmobile.todoekspert.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.soldiersofmobile.todoekspert.App;
import com.soldiersofmobile.todoekspert.R;
import com.soldiersofmobile.todoekspert.RefreshIntentService;
import com.soldiersofmobile.todoekspert.api.ErrorResponse;
import com.soldiersofmobile.todoekspert.api.Todo;
import com.soldiersofmobile.todoekspert.api.TodoApi;
import com.soldiersofmobile.todoekspert.api.TodosResponse;
import com.soldiersofmobile.todoekspert.db.DbHelper;
import com.soldiersofmobile.todoekspert.db.TodoDao;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
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
    //private TodoAdapter adapter;
    private SimpleCursorAdapter adapter;
    private TodoDao todoDao;
    private String userId;
    private TodoApi todoApi;
    private Converter<ResponseBody, ErrorResponse> converter;

    class TodoAdapter extends BaseAdapter {

        List<Todo> todos = new ArrayList<>();

        public void addAll(Todo[] todoArray) {
            todos.clear();
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
        userId = preferences.getString(LoginActivity.USER_ID, "");

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
        //adapter = new TodoAdapter();
        todoDao = new TodoDao(new DbHelper(this));
        adapter = new SimpleCursorAdapter(this, R.layout.item_todo, null,
                FROM, TO, 0);
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == cursor.getColumnIndex(TodoDao.C_DONE)) {
                    boolean done = cursor.getInt(columnIndex) > 0;
                    CheckBox checkBox = (CheckBox) view;
                    checkBox.setChecked(done);
                    return true;
                }
                return false;
            }
        });
        todoListView.setAdapter(adapter);

        App app = (App) getApplication();
        todoApi = app.getTodoApi();
        converter = app.getConverter();

        reloadCursor();


    }

    private static final String[] FROM = {TodoDao.C_CONTENT, TodoDao.C_DONE, TodoDao.C_ID};
    private static final int[] TO = {R.id.checkBox, R.id.checkBox, R.id.button};

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

        Intent intent = new Intent(this, RefreshIntentService.class);
        startService(intent);

        Call<TodosResponse> call = todoApi.getTodos(token);
        call.enqueue(new Callback<TodosResponse>() {
            @Override
            public void onResponse(Call<TodosResponse> call, Response<TodosResponse> response) {
                if (response.isSuccessful()) {
                    TodosResponse body = response.body();

                    //adapter.addAll(body.results);

                    for (Todo result : body.results) {
                        Log.d(TAG, result.toString());
                        todoDao.insert(result, userId);
                    }

                    reloadCursor();


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

                todoDao.insert(todo, userId);

                reloadCursor();

                Toast.makeText(this, "OK:" + todo.content, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "CANCEL", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void reloadCursor() {
        Cursor cursor = todoDao.getTodosByUser(userId);
        adapter.swapCursor(cursor);
    }
}
