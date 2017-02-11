package com.soldiersofmobile.todoekspert;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.soldiersofmobile.todoekspert.activities.LoginActivity;
import com.soldiersofmobile.todoekspert.activities.TodoListActivity;
import com.soldiersofmobile.todoekspert.api.Todo;
import com.soldiersofmobile.todoekspert.api.TodoApi;
import com.soldiersofmobile.todoekspert.api.TodosResponse;
import com.soldiersofmobile.todoekspert.db.DbHelper;
import com.soldiersofmobile.todoekspert.db.TodoDao;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RefreshIntentService extends IntentService {

    public static final String REFRESH_ACTION
            = "com.soldiersofmobile.todoekspert.REFRESH";

    public RefreshIntentService() {
        super(RefreshIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        App app = (App) getApplication();
        TodoApi todoApi = app.getTodoApi();
        TodoDao todoDao = new TodoDao(new DbHelper(this));
        SharedPreferences preferences
                = PreferenceManager.getDefaultSharedPreferences(this);
        String token = preferences.getString(LoginActivity.TOKEN, "");
        String userId = preferences.getString(LoginActivity.USER_ID, "");

        Call<TodosResponse> call = todoApi.getTodos(token);
        try {
            Response<TodosResponse> response = call.execute();
            if (response.isSuccessful()) {
                TodosResponse body = response.body();


                for (Todo result : body.results) {
                    todoDao.insert(result, userId);
                }
                Intent broadcast = new Intent(REFRESH_ACTION);
                sendBroadcast(broadcast);

                NotificationManager notificationManager
                        = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                builder.setContentTitle("Todos refreshed");
                builder.setContentText("Added:" + body.results.length);
                builder.setSmallIcon(R.mipmap.ic_launcher);
                builder.setAutoCancel(true);

                Intent activityIntent = new Intent(this, TodoListActivity.class);
                activityIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                PendingIntent pendingIntent
                        = PendingIntent.getActivity(this, 1, activityIntent, 0);
                builder.setContentIntent(pendingIntent);

                notificationManager.notify(1, builder.build());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
