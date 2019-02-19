package tfg.shuttlego.activities.person.driver;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import tfg.shuttlego.R;
import tfg.shuttlego.activities.route.RouteMain;
import tfg.shuttlego.model.event.Event;
import tfg.shuttlego.model.event.EventDispatcher;
import tfg.shuttlego.model.transfer.person.Person;

/**
 *
 */
public class DriverMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private ProgressBar driveMainProgress;
    private LinearLayout driveMainLinear;
    private NavigationView navigationView;
    private Person user;
    private EditText limitArea, limitPassengers;
    private AutoCompleteTextView origin;
    private Button createRoute;
    private ArrayList<String> originList;
    private ArrayList<HashMap<?, ?>> originMap;
    private String idOrigin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_main);
        user = (Person)Objects.requireNonNull(getIntent().getExtras()).getSerializable("user");
        incializateView();
        setProgressBar();
        setMenuDrawer();
        setCredencials();
        throwEventGerAllOrigins();
        listeners();
    }//onCreate

    /**
     *
     */
    private void incializateView() {

        driveMainProgress = findViewById(R.id.progress);
        driveMainLinear = findViewById(R.id.driver_main_content_linear4);
        origin = findViewById(R.id.driver_main_content_autoComplete);
        limitArea = findViewById(R.id.driver_main_content_limit);
        limitPassengers = findViewById(R.id.driver_main_content_passengers);
        createRoute = findViewById(R.id.driver_main_content_button);
    }//incializateView

    /**
     *
     */
    private void setProgressBar () {

        driveMainProgress.setVisibility(View.VISIBLE);
        driveMainLinear.setVisibility(View.GONE);
    }//setProgressBar

    /**
     *
     */
    private void removeProgressBar () {

        driveMainProgress.setVisibility(View.GONE);
        driveMainLinear.setVisibility(View.VISIBLE);
    }//removeProgressBar

    /**
     *
     */
    private void setMenuDrawer() {

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        DrawerLayout drawer = findViewById(R.id.driver_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }//setMenuDrawer

    /**
     *
     */
    private void setCredencials() {

        View hView =  navigationView.getHeaderView(0);
        TextView nav_name_text = hView.findViewById(R.id.menu_nav_header_name);
        TextView nav_email_text = hView.findViewById(R.id.menu_nav_header_email);
        nav_name_text.setText(user.getName() + " " + user.getSurname());
        nav_email_text.setText(user.getEmail());
    }//setCredencials

    /**
     *
     */
    private void throwEventGerAllOrigins(){

        EventDispatcher.getInstance(getApplicationContext())
        .dispatchEvent(Event.GETORIGINS, null)
        .addOnCompleteListener(new OnCompleteListener<HashMap<String, String>>() {
            @Override
            public void onComplete(@NonNull Task<HashMap<String, String>> task) {

                if (!task.isSuccessful() || task.getResult() == null) throwToast(R.string.errConexion);
                else if (task.getResult().containsKey("error")) throwToast(R.string.errServer);
                else {

                    HashMap<?, ?> result = task.getResult();
                    originMap = (ArrayList<HashMap<?, ?>>) result.get("origins");
                    originList = new ArrayList<String>();

                    for (HashMap<?, ?> l : originMap) originList.add((String) l.get("name"));

                    setAutoCompleteTextView();
                    removeProgressBar();

                }//else
            }//onComplete
        });
    }//throwEventGerAllOrigins

    /**
     *
     */
    private void setAutoCompleteTextView() {

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, originList);
        origin.setThreshold(1);
        origin.setAdapter(adapter);
    }//setAutoCompleteTextView

    /**
     *
     * @param origin
     */
    private void throwEventGetRouteByName(JSONObject origin) {

        EventDispatcher.getInstance(getApplicationContext())
        .dispatchEvent(Event.GETORIGINBYNAME, origin )
        .addOnCompleteListener(new OnCompleteListener<HashMap<String, String>>() {
            @Override
            public void onComplete(@NonNull Task<HashMap<String, String>> task) {

                if (!task.isSuccessful() || task.getResult() == null) throwToast(R.string.errConexion);
                else if (task.getResult().containsKey("error")) throwToast(R.string.errServer);
                else {

                    idOrigin = task.getResult().get("id");
                    throwEventCreteRoute(buildJson());
                }//else
            }//onComplete
        });
    }//throwEventCreteRoute


    /**
     *
     * @param route
     */
    private void throwEventCreteRoute(JSONObject route) {

        EventDispatcher.getInstance(getApplicationContext())
        .dispatchEvent(Event.CREATEROUTE, route )
        .addOnCompleteListener(new OnCompleteListener<HashMap<String, String>>() {
            @Override
            public void onComplete(@NonNull Task<HashMap<String, String>> task) {

                if (!task.isSuccessful() || task.getResult() == null) throwToast(R.string.errConexion);
                else if (task.getResult().containsKey("error")) throwToast(R.string.errServer);
                else {


                    String idRoute = task.getResult().get("route");
                    Intent logIntent = new Intent(DriverMain.this, RouteMain.class);
                    logIntent.putExtra("route", idRoute);
                    startActivity(logIntent);

                }//else
            }//onComplete
        });
    }//throwEventCreteRoute

    /**
     *
     * @return
     */
    private JSONObject buildJson() {

        JSONObject json = new JSONObject();
        JSONObject userJson = new JSONObject();
        JSONObject routeJson = new JSONObject();

        try {

            int codePostal = Integer.parseInt(limitArea.getText().toString());
            int passengers = Integer.parseInt(limitPassengers.getText().toString());

            userJson.put("email", user.getEmail());
            userJson.put("password", user.getPassword());
            routeJson.put("max", passengers);
            routeJson.put("origin", idOrigin);
            routeJson.put("destination", codePostal);

            json.put("user", userJson);
            json.put("route", routeJson);

        } catch (JSONException e) { throwToast(R.string.err);}

        return json;
    }//buildJson

    /**
     *
     */
    private void listeners() {

        createRoute.setOnClickListener(this);
    }//listeners

    /**
     *
     * @param msg
     */
    private void throwToast(int msg) { Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show(); }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = findViewById(R.id.driver_main);

        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
        else super.onBackPressed();
    }//onBackPressed

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.nav_settings_admin) { }
        else if (id == R.id.nav_signout_admin) { }

        DrawerLayout drawer = findViewById(R.id.driver_main);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }//onNavigationItemSelected

    @Override
    public void onClick(View v) {

        boolean empty = false;

        switch (v.getId()) {

            case R.id.driver_main_content_button:
                if (origin.getText().toString().isEmpty()) empty = true;
                if (limitArea.getText().toString().isEmpty()) empty = true;
                if (limitPassengers.getText().toString().isEmpty()) empty = true;

                if (!empty) {

                    setProgressBar();
                    try { throwEventGetRouteByName(new JSONObject().put("origin", new JSONObject().put("name", origin.getText().toString()))); }
                    catch (JSONException e) { throwToast(R.string.err);}
                }
                else throwToast(R.string.errDataEmpty);
                break;
        }
    }
}
