package es.tfg.shuttle.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctionsException;


import org.json.JSONException;
import org.json.JSONObject;
import es.tfg.shuttle.R;
import es.tfg.shuttle.logic.events.Event;
import es.tfg.shuttle.logic.events.EventDispatcher;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button signInButton = findViewById(R.id.signin_button);

        signInButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String email = ((EditText)findViewById(R.id.email_signin_text)).getText().toString();
                String password = ((EditText)findViewById(R.id.password_signin_text)).getText().toString();

                JSONObject json = new JSONObject();
                JSONObject user = new JSONObject();

                try {

                    json.put("email", email);
                    json.put("password", password);
                    user.put("user", json);

                } catch (JSONException e) {

                    e.printStackTrace();
                }

                EventDispatcher.getInstance().dispatchEvent(Event.SIGNIN, user)
                        .addOnCompleteListener(new OnCompleteListener<String>() {

                            @Override
                            public void onComplete(@NonNull Task<String> task) {

                                if (!task.isSuccessful()) {

                                    Exception e = task.getException();

                                    if (e instanceof FirebaseFunctionsException) {

                                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                        FirebaseFunctionsException.Code code = ffe.getCode();
                                        Object details = ffe.getDetails();
                                    }
                                }
                                else {

                                    Intent welcomeIntent = new Intent(LoginActivity.this, WelcomeActivity.class);
                                    startActivity(welcomeIntent);
                                }
                            }//onComplete
                        });


            }
        });
    }
}
