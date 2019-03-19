package tfg.shuttlego.activities.person.admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import tfg.shuttlego.R;
import tfg.shuttlego.activities.origin.OriginList;
import tfg.shuttlego.activities.origin.OriginMain;
import tfg.shuttlego.model.event.Event;
import tfg.shuttlego.model.event.EventDispatcher;
import tfg.shuttlego.model.map.Map;
import tfg.shuttlego.model.session.Session;
import tfg.shuttlego.model.transfer.address.Address;
import tfg.shuttlego.model.transfer.person.Person;

public class AdminMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
                                                            View.OnClickListener,
                                                            OnMapReadyCallback,
                                                            PermissionsListener,
                                                            AdapterView.OnItemClickListener,
                                                            TextWatcher {

    private Person user;

    private DrawerLayout admiMainDrawer;
    private NavigationView navigationView;

    private ProgressBar adminMainProgress;
    private LinearLayout adminMainLinear;

    private EditText adminMainOriginText;
    private AutoCompleteTextView adminMainOriginAutocomplete;
    private Button adminMainButton;

    private List<Address> adminMainSearchResult;
    private Address adminMainOrigin;
    private Boolean adminMainDestinySelected;

    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Mapbox.getInstance(this, getString(R.string.access_token));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_main);

        user = Session.getInstance(getApplicationContext()).getUser();

        incializateView();
        setProgressBar();
        mapView.onCreate(savedInstanceState);
        setMenuDrawer();
        setCredencials();
        removeProgressBar();

        // Listeners
        navigationView.setNavigationItemSelectedListener(this);
        mapView.getMapAsync(this);
        adminMainOriginAutocomplete.addTextChangedListener(this);
        adminMainOriginAutocomplete.setOnItemClickListener(this);
        adminMainButton.setOnClickListener(this);
    }

    /**
     * Inicializate the componentes of this view
     */
    private void incializateView() {

        admiMainDrawer = findViewById(R.id.admin_main_drawer);
        navigationView = findViewById(R.id.admin_main_nav);

        adminMainProgress = findViewById(R.id.admin_main_progress);
        adminMainLinear = findViewById(R.id.admin_main_linear);

        adminMainOriginText = findViewById(R.id.admin_main_origin);
        adminMainOriginAutocomplete = findViewById(R.id.admin_main_autocomplete);
        adminMainButton = findViewById(R.id.admin_main_button);

        mapView = findViewById(R.id.admin_main_map);

        adminMainDestinySelected = false;
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    /**
     * Show the progress bar component visible and put invisble the rest of the view
     */
    private void setProgressBar () {

        adminMainProgress.setVisibility(View.VISIBLE);
        adminMainLinear.setVisibility(View.GONE);
    }

    /**
     * Show the view visible and put invisble progress bar component
     */
    private void removeProgressBar () {

        adminMainProgress.setVisibility(View.GONE);
        adminMainLinear.setVisibility(View.VISIBLE);
    }

    /**
     * Inicializate the components to put the menu in the view
     */
    private void setMenuDrawer() {

        Toolbar toolbar = findViewById(R.id.admin_main_toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, admiMainDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        admiMainDrawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    /**
     * Put the personal data about the current user
     */
    private void setCredencials() {

        View hView =  navigationView.getHeaderView(0);

        TextView nav_name_text = hView.findViewById(R.id.menu_nav_header_name);
        TextView nav_email_text = hView.findViewById(R.id.menu_nav_header_email);

        String complete_name = user.getName() + " " + user.getSurname();
        nav_name_text.setText(complete_name);
        nav_email_text.setText(user.getEmail());
    }

    /**
     * Build a JSON for to allow make a create a new origin
     *
     * @param nameOrigin Name to create a new origin
     *
     * @return JSON with information to create origin
     */
    private JSONObject buildJson(String nameOrigin, Address adress) {

        JSONObject dataUser = new JSONObject();
        JSONObject dataOrigin = new JSONObject();
        JSONObject createOrigin = new JSONObject();

        try {

            dataUser.put("email", user.getEmail());
            dataUser.put("password", user.getPassword());
            dataOrigin.put("name", nameOrigin);
            dataOrigin.put("coordAlt", adress.getCoordinates().get(0));
            dataOrigin.put("coordLong", adress.getCoordinates().get(1));
            createOrigin.put("user", dataUser);
            createOrigin.put("origin", dataOrigin);

        } catch (JSONException e) { throwToast(R.string.err); }

        return createOrigin;
    }

    /**
     * Throw the event that allow create a new origin
     *
     * @param createOrigin JSON with information to create a origin
     */
    private void throwEventAddOrigin(JSONObject createOrigin) {

        EventDispatcher.getInstance(getApplicationContext())
        .dispatchEvent(Event.CREATEORIGIN, createOrigin)
        .addOnCompleteListener(task -> {

            if (!task.isSuccessful() || task.getResult() == null) {
                removeProgressBar();
                throwToast(R.string.errConexion);

            } else if (task.getResult().containsKey("error")){
                removeProgressBar();

                switch (Objects.requireNonNull(task.getResult().get("error"))) {
                    case "badRequestForm": throwToast(R.string.errBadFormat); break;
                    case "originAlreadyExists": throwToast(R.string.errOriginAlreadyExists); break;
                    case "server": throwToast(R.string.errServer); break;
                }
            }
            else {

                Intent logIntent = new Intent(AdminMain.this, OriginMain.class);
                logIntent.putExtra("origin", task.getResult().get("id"));
                throwToast(R.string.createOriginSuccesful);
                startActivity(logIntent);
            }
        });
    }

    private void throwToast(int msg) { Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show(); }

    /*********************************************************************************************************************
    Bar to put the destiny into de map **/

    @Override
    public void afterTextChanged(Editable s) {

        if (adminMainDestinySelected) adminMainDestinySelected = false;
        else {

            if (s.toString().matches(".*\\s")) Map.getInstance(getApplicationContext()).getFullAddress(s.toString()).addOnCompleteListener(task-> {

                adminMainSearchResult = task.getResult();
                ArrayList<String> fullAddresses = new ArrayList<>();

                for (Address address : adminMainSearchResult) fullAddresses.add(address.getAddress());

                ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminMain.this, android.R.layout.simple_list_item_1, fullAddresses);
                adminMainOriginAutocomplete.setThreshold(1);
                adminMainOriginAutocomplete.setAdapter(adapter);
                adminMainOriginAutocomplete.showDropDown();
            });
        }
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        adminMainDestinySelected = true;

        int i = 0;
        String text = adminMainOriginAutocomplete.getText().toString();

        while(i < adminMainSearchResult.size() && !adminMainSearchResult.get(i).getAddress().equals(text)) i++;

        if(i >= adminMainSearchResult.size()) throwToast(R.string.errDestinyNotExisit);
        else {
            adminMainOrigin = adminMainSearchResult.get(i);
            moveMap(adminMainSearchResult.get(i).getCoordinates());
        }
    }

    /*********************************************************************************************************************
     MAPBOX **/

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {

        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.LIGHT);
    }

    @SuppressLint("MissingPermission")
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {

        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
        }
        else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) enableLocationComponent(Objects.requireNonNull(mapboxMap.getStyle()));
        else { Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show(); finish(); }
    }

    @SuppressWarnings("deprecation")
    private void moveMap(List<Double> coordinates) {

        CameraPosition cp = new CameraPosition.Builder()
                                .target(new LatLng(coordinates.get(1), coordinates.get(0)))
                                .zoom(17)
                                .tilt(20)
                                .build();

        this.mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp),2000);
        this.mapboxMap.addMarker(new MarkerOptions().position(new LatLng(coordinates.get(1), coordinates.get(0))));
    }


    /*********************************************************************************************************************
    Activity´s action */


    @Override
    public void onClick(View v) {

        if (adminMainOriginText.getText().toString().isEmpty() ||
            adminMainOrigin == null) throwToast(R.string.errDataEmpty);
        else {

            setProgressBar();
            throwEventAddOrigin(buildJson(String.valueOf(adminMainOriginText.getText()), adminMainOrigin));
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            case R.id.admin_drawer_list: startActivity(new Intent(AdminMain.this, OriginList.class));break;
        }

        admiMainDrawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (admiMainDrawer.isDrawerOpen(GravityCompat.START)) admiMainDrawer.closeDrawer(GravityCompat.START);
    }
}
