package tfg.shuttlego.activities.route.routeMain;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Objects;
import tfg.shuttlego.R;
import tfg.shuttlego.model.event.Event;
import tfg.shuttlego.model.event.EventDispatcher;
import tfg.shuttlego.model.session.Session;
import tfg.shuttlego.model.transfer.person.Person;

public abstract class RouteMain extends AppCompatActivity {

    protected LinearLayout routeMainLinearDriver, routeMainLinearPhone;
    protected Button routeMainRemoveButton, routeMainCloseButton, routeMainBeginButton;
    protected TextView routeMainOrigin, routeMainLimit, routeMainPassengerMax, routeMainPassengerCurrent, routeMainDriver, routeMainPhone;
    protected ImageView routeMainImage;
    protected NavigationView routeMainNavigation;
    protected DrawerLayout routeMainDrawer;

    private LinearLayout routeMainLinear;
    private ProgressBar routeMainProgress;

    protected String routeMainIdRoute;
    protected Person user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_main);

        routeMainIdRoute = (String)Objects.requireNonNull(getIntent().getExtras()).getSerializable("route");
        user = Session.getInstance(getApplicationContext()).getUser();

        inicializateView();
        setProgressBar();
        setMenuDrawer();
        setCredencials();
        throwEventGetRoute(buildJson(routeMainIdRoute));

        listeners();
    }

    /**
     * Inicializate the componentes of this view
     */
    private void inicializateView() {

        routeMainLinear = findViewById(R.id.route_main_linear);
        routeMainProgress = findViewById(R.id.route_main_progress);

        routeMainDrawer = findViewById(R.id.route_main_drawer);
        routeMainNavigation = findViewById(R.id.route_main_nav);

        routeMainRemoveButton = findViewById(R.id.route_main_delete_btn);
        routeMainCloseButton = findViewById(R.id.route_main_close_btn);
        routeMainBeginButton = findViewById(R.id.route_main_begin_btn);

        routeMainOrigin = findViewById(R.id.route_main_origin);
        routeMainLimit = findViewById(R.id.route_main_limit);
        routeMainPassengerMax = findViewById(R.id.route_main_passengers_max);
        routeMainPassengerCurrent = findViewById(R.id.route_main_passengers);
        routeMainDriver = findViewById(R.id.route_main_driver);
        routeMainPhone = findViewById(R.id.route_main_phone);

        routeMainLinearDriver = findViewById(R.id.route_main_linear_driver);
        routeMainLinearPhone = findViewById(R.id.route_main_linear_phone);

        routeMainImage = findViewById(R.id.route_main_ic_destiny);
    }

    /**
     * Show the progress bar component visible and put invisble the rest of the view
     */
    protected void setProgressBar () {

        routeMainProgress.setVisibility(View.VISIBLE);
        routeMainLinear.setVisibility(View.GONE);
    }

    /**
     * Show the view visible and put invisble progress bar component
     */
    protected void removeProgressBar () {

        routeMainProgress.setVisibility(View.GONE);
        routeMainLinear.setVisibility(View.VISIBLE);
    }

    /**
     * Inicializate the components to put the menu in the view
     */
    private void setMenuDrawer() {

        Toolbar toolbar = findViewById(R.id.route_main_toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, routeMainDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        routeMainDrawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    /**
     * Put the personal data about the current user
     */
    private void setCredencials() {

        View hView =  routeMainNavigation.getHeaderView(0);

        TextView nav_name_text = hView.findViewById(R.id.menu_nav_header_name);
        TextView nav_email_text = hView.findViewById(R.id.menu_nav_header_email);

        String complete_name = user.getName() + " " + user.getSurname();
        nav_name_text.setText(complete_name);
        nav_email_text.setText(user.getEmail());
    }

    /**
     * Build a JSON to get a route
     *
     * @param route necesary data to make the correct JSON
     *
     * @return JSON with information about the current origin
     */
    private JSONObject buildJson(String route) {

        JSONObject json = new JSONObject();
        JSONObject routeJson = new JSONObject();

        try {

            routeJson.put("id", route);
            json.put("route", routeJson);

        } catch (JSONException e) { throwToast(R.string.err);}

        return json;
    }

    /**
     * Throw the event that allow to get a route
     *
     * @param route JSON with information to get origin
     */
    private void throwEventGetRoute(JSONObject route) {

        EventDispatcher.getInstance(getApplicationContext())
        .dispatchEvent(Event.GETROUTEBYID, route)
        .addOnCompleteListener(task -> {

            if (!task.isSuccessful() || task.getResult() == null || task.getResult().containsKey("error")) {

                finish();
                throwToast(R.string.err);
            }
            else {

                setDataText(task.getResult());
                removeProgressBar();
            }
        });
    }

    protected void throwToast(int msg) { Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show(); }

    abstract protected void listeners();

    abstract protected void setDataText(HashMap<?,?> resultEvent);
}
