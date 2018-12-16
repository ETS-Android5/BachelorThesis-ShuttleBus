package tfg.shuttlego.activities.account;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Objects;
import tfg.shuttlego.R;
import tfg.shuttlego.activities.person.driver.DriverMain;
import tfg.shuttlego.activities.person.passenger.PassengerMain;
import tfg.shuttlego.model.events.Event;
import tfg.shuttlego.model.events.EventDispatcher;
import tfg.shuttlego.model.transfers.person.Person;
import tfg.shuttlego.model.transfers.person.TypePerson;

/**
 *
 */
public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private ProgressBar pBar;
    private RelativeLayout relative1, relative2, relative3;
    private EditText emailText, passwordText, nameText, surnameText, phoneText;
    private RadioButton driverButton, passengerButton;
    private Button next1Button, next2Button, finishButton;
    private Animation animationIn, animationOut;
    private String email, name, surname, password, type;
    private int phone;
    private TypePerson typePerson;
    private Class nextClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_main);

        inicializateView();
        inicializateAnimation();

        next1Button.setOnClickListener(this);
        next2Button.setOnClickListener(this);
        finishButton.setOnClickListener(this);
    }

    /**
     *
     */
    private void inicializateView() {
        relative1 = findViewById(R.id.relative1_form_register);
        relative2 = findViewById(R.id.relative2_form_register);
        relative3 = findViewById(R.id.relative3_form_register);

        next1Button = findViewById(R.id.btn_next1_register);
        next2Button = findViewById(R.id.btn_next2_register);
        finishButton = findViewById(R.id.btn_finish_register);

        emailText = findViewById(R.id.et_email_register);
        nameText = findViewById(R.id.et_name_register);
        surnameText = findViewById(R.id.et_surname_register);
        phoneText = findViewById(R.id.et_phone_register);
        passwordText = findViewById(R.id.et_password_register);
        driverButton = findViewById(R.id.rb1_register);
        passengerButton = findViewById(R.id.rb2_register);

        pBar = findViewById(R.id.progress);
    }//inicializateView

    /**
     *
     */
    private void inicializateAnimation() {
        animationIn = AnimationUtils.loadAnimation(RegisterActivity.this, R.anim.left_in);
        animationOut = AnimationUtils.loadAnimation(RegisterActivity.this, R.anim.left_out);
    }//inicializateAnimation

    /**
     *
     * @return
     */
    private JSONObject buildJson() {

        JSONObject json = new JSONObject();
        JSONObject user = new JSONObject();

        try {
            email = emailText.getText().toString();
            name = nameText.getText().toString();
            surname = surnameText.getText().toString();
            password = passwordText.getText().toString();

            phone = Integer.parseInt(phoneText.getText().toString());

            if (driverButton.isChecked()) type = "driver";
            else type = "passenger";

            json.put("email", email);
            json.put("name", name);
            json.put("surname", surname);
            json.put("number", phone);
            json.put("password", password);
            json.put("type", type);
            user.put("user", json);
        }
        catch (NumberFormatException e){
            throwToast("Introduzca un telefono correcto");
        }
        catch (JSONException e) {
            changeVisibility(relative3, relative1);
            throwToast("ERROR. Vuelva a intentarlo");
        }

        return user;
    }//buildJson

    /**
     *
     * @param data
     */
    private void throwEvent(JSONObject data) {

        EventDispatcher.getInstance(getApplicationContext())
        .dispatchEvent(Event.SIGNUP, data)
        .addOnCompleteListener(new OnCompleteListener<HashMap<String, String>>() {
            @Override
            public void onComplete(@NonNull Task<HashMap<String, String>> task) {

                if (!task.isSuccessful() || task.getResult() == null) {
                    changeVisibility(pBar, relative3);
                    throwToast("Error de conexion");
                }
                else if (task.getResult().containsKey("error")) {

                    changeVisibility(pBar, relative3);

                    switch (Objects.requireNonNull(task.getResult().get("error"))) {

                        case "badRequestForm":
                            throwToast("Formato de datos incorrecto");
                            break;

                        case "userAlreadyExists":
                            throwToast("Usuario ya existente");
                            break;

                        case "server":
                            throwToast("Error del servidor");
                            break;

                        default:
                            throwToast("Error desconocido: " + task.getResult().get("error"));
                            break;
                    }//switch
                }//else if
                else {

                    parserTypePerson();
                    Person user = new Person(email, password, name, surname, phone, typePerson);

                    Intent logIntent = new Intent(RegisterActivity.this, nextClass);
                    logIntent.putExtra("user", user);
                    startActivity(logIntent);
                }//else
            }

            /**
             *
             */
            private void parserTypePerson() {

                switch (type) {
                    case "passenger":
                        typePerson = TypePerson.USER;
                        nextClass = PassengerMain.class;
                        break;

                    default:
                        typePerson = TypePerson.DRIVER;
                        nextClass = DriverMain.class;
                        break;
                }//switch
            }
        });
    }//throwEvent

    /**
     *
     * @param rOut
     * @param rIn
     */
    private void doAnimation(View rOut, View rIn) {
        rOut.startAnimation(animationOut);
        rIn.startAnimation(animationIn);
    }//doAnimation

    /**
     *
     * @param vGone
     * @param vVisible
     */
    private void changeVisibility(View vGone, View vVisible) {
        vGone.setVisibility(View.GONE);
        vVisible.setVisibility(View.VISIBLE);
    }//changeVisibility

    /**
     *
     * @param msg
     */
    private void throwToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }//throwToast

    @Override
    public void onClick(View v) {

        boolean empty = false;

        switch (v.getId()) {

            case R.id.btn_next1_register:

                if (emailText.getText().toString().isEmpty()) empty = true;
                if (!driverButton.isChecked() && !passengerButton.isChecked()) empty = true;

                if (!empty) {
                    doAnimation(relative1, relative2);
                    changeVisibility(relative1, relative2);
                }
                else throwToast("Introduzca todos los datos");
                break;

            case R.id.btn_next2_register:

                if (nameText.getText().toString().isEmpty()) empty = true;
                if (surnameText.getText().toString().isEmpty()) empty = true;
                if (phoneText.getText().toString().isEmpty()) empty = true;

                if (!empty) {
                    doAnimation(relative2, relative3);
                    changeVisibility(relative2, relative3);
                }
                else throwToast("Introduzca todos los datos");
                break;

            case R.id.btn_finish_register:

                if (passwordText.getText().toString().isEmpty()) empty = true;

                if (!empty){
                    changeVisibility(relative3, pBar);
                    JSONObject user = buildJson();
                    throwEvent(user);
                }
                else throwToast("Introduzca una contraseña");
                break;

            default:
                break;
        }//switch
    }//onClick
}
