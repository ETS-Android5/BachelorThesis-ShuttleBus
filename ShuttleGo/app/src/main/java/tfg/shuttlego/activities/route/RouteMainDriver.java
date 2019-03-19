package tfg.shuttlego.activities.route;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.view.MenuItem;
import android.view.View;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import tfg.shuttlego.R;
import tfg.shuttlego.activities.person.driver.DriverMain;
import tfg.shuttlego.model.event.Event;
import tfg.shuttlego.model.event.EventDispatcher;

@SuppressLint("Registered")
public class RouteMainDriver extends RouteMain implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    /*private JSONObject buildJSONBeginRoute(String routeMainIdRoute) {

        [...]
    }*/

    /* void throwEventBeginRoute(JSONObject route) {

        EventDispatcher.getInstance(getApplicationContext())
        .dispatchEvent(...)
        .addOnCompleteListener(task -> {

            if (!task.isSuccessful() || task.getResult() == null) {
                removeProgressBar();
                throwToast(R.string.errConexion);
            }
            else if (task.getResult().containsKey("error")) {
                removeProgressBar();
                throwToast(R.string.errServer);
            }
            else {

                // Accion despues del resultado.
            }
        });
    }*/

    private JSONObject buildJSONDeleteRoute(String routeMainIdRoute) {

        JSONObject dataUser = new JSONObject();
        JSONObject dataRoute = new JSONObject();
        JSONObject deleteRoute = new JSONObject();

        try {

        dataUser.put("email", this.user.getEmail());
        dataUser.put("password", this.user.getPassword());

        dataRoute.put("id", routeMainIdRoute);

        deleteRoute.put("user", dataUser);
        deleteRoute.put("route", dataRoute);
        }
        catch (JSONException e) { throwToast(R.string.err); }

        return deleteRoute;
    }

    private void throwEventDeleteRoute(JSONObject route) {

        EventDispatcher.getInstance(getApplicationContext())
        .dispatchEvent(Event.DELETEROUTEBYID, route)
        .addOnCompleteListener(task -> {

            if (!task.isSuccessful() || task.getResult() == null) {
                removeProgressBar();
                throwToast(R.string.errConexion);
            }
            else if (task.getResult().containsKey("error")) {
                removeProgressBar();
                throwToast(R.string.errServer);
            }
            else {

                throwToast(R.string.deleteRouteSuccesful);
                startActivity(new Intent(RouteMainDriver.this, DriverMain.class));
                finish();
            }
        });
    }

    @Override
    protected void listeners() {
        routeMainRemoveButton.setOnClickListener(this);
        routeMainCloseButton.setOnClickListener(this);
        routeMainNavigation.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void setDataText(HashMap<?,?> resultEvent) {

        String origin = routeMainOrigin.getText() + " " + resultEvent.get("origin");
        String limit = routeMainLimit.getText() + " " + String.valueOf(resultEvent.get("destination"));
        String passengers = routeMainPassenger.getText() + " " + String.valueOf(resultEvent.get("max"));
        String phone = routeMainPhone.getText() + " " + String.valueOf(resultEvent.get("driverNumber"));
        String driverNameComplete = routeMainDriver.getText() + " " +
                                    resultEvent.get("driverSurname") + " " +
                                    resultEvent.get("driverName");

        routeMainOrigin.setText(origin);
        routeMainLimit.setText(limit);
        routeMainPassenger.setText(passengers);
        routeMainDriver.setText(driverNameComplete);
        routeMainPhone.setText(phone);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.route_main_begin_btn:
                //setProgressBar();
                //throwEventBeginRoute(buildJSONBeginRoute(routeMainIdRoute));
                break;

            case R.id.route_main_delete_btn:
                setProgressBar();
                throwEventDeleteRoute(buildJSONDeleteRoute(routeMainIdRoute));
                break;

            case R.id.route_main_close_btn:
                startActivity(new Intent(RouteMainDriver.this, DriverMain.class));
                finish();
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            case R.id.driver_drawer_list:
                startActivity(new Intent(RouteMainDriver.this, RouteListDriver.class));
                finish();
                break;

            case R.id.driver_drawer_home:
                startActivity(new Intent(RouteMainDriver.this, DriverMain.class));
                finish();
                break;
        }

        routeMainDrawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (routeMainDrawer.isDrawerOpen(GravityCompat.START)) routeMainDrawer.closeDrawer(GravityCompat.START);
        else {
            startActivity(new Intent(RouteMainDriver.this, DriverMain.class));
            finish();
        }
    }
}
