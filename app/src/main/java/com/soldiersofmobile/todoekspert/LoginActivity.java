package com.soldiersofmobile.todoekspert;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.soldiersofmobile.todoekspert.api.ErrorResponse;
import com.soldiersofmobile.todoekspert.api.TodoApi;
import com.soldiersofmobile.todoekspert.api.User;

import java.io.IOException;
import java.lang.annotation.Annotation;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.username_edit_text)
    EditText usernameEditText;
    @BindView(R.id.password_edit_text)
    EditText passwordEditText;
    @BindView(R.id.sign_in_button)
    Button signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.sign_in_button)
    public void onClick() {

        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        boolean hasErrors = false;
        if (username.isEmpty()) {
            usernameEditText.setError(getString(R.string.this_field_is_required));
            hasErrors = true;
        }
        if (password.isEmpty()) {
            passwordEditText.setError(getString(R.string.this_field_is_required));
            hasErrors = true;
        }

        if (!hasErrors) {
            login(username, password);
        }
    }

    private void login(String username, String password) {


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://parseapi.back4app.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final Converter<ResponseBody, ErrorResponse> converter
                = retrofit.responseBodyConverter(ErrorResponse.class, new Annotation[0]);


        TodoApi todoApi = retrofit.create(TodoApi.class);
        Call<User> login = todoApi.getLogin(username, password);

        login.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    User body = response.body();
                    Toast.makeText(getApplicationContext(),
                            String.format("user%s: token:%s", body.username, body.sessionToken),
                            Toast.LENGTH_LONG).show();
                } else {

                    try {
                        ResponseBody responseBody = response.errorBody();
                        ErrorResponse errorResponse = converter.convert(responseBody);
                        Toast.makeText(getApplicationContext(),
                                errorResponse.error,
                                Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

                t.printStackTrace();
            }
        });




    }
}
