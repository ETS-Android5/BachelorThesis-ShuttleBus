package tfg.shuttlego.activities.person.passenger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import tfg.shuttlego.R;
import tfg.shuttlego.activities.map.MapMain;
import tfg.shuttlego.model.event.Event;
import tfg.shuttlego.model.event.EventDispatcher;
import tfg.shuttlego.model.map.Map;
import tfg.shuttlego.model.transfer.address.Address;
import tfg.shuttlego.model.transfer.person.Person;
import tfg.shuttlego.model.transfer.route.Route;

import static tfg.shuttlego.model.event.Event.SEARCHROUTE;

/**
 *
 */
public class PassengerMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener, View.OnClickListener, TextWatcher, AdapterView.OnItemClickListener {

    private NavigationView navigationView;
    private Person user;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    private ProgressBar passengerMainProgress;
    private LinearLayout passengerMainLinear;
    private AutoCompleteTextView passengerMainDestiny;
    private Button passengerMainButton;
    private AutoCompleteTextView passengerMainOrigin;
    private ArrayList<String> originList;
    private ArrayList<HashMap<?, ?>> originMap;
    private int numWords;
    private boolean destinySelected;
    private List<Address> searchResult;
    private HashMap<String,String> originIds;
    private Address destination;
    private String originName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Mapbox.getInstance(this, getString(R.string.access_token));
        user = (Person)Objects.requireNonNull(getIntent().getExtras()).getSerializable("user");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.passenger_main);
        inicializateView();
        setProgressBar();
        mapView.onCreate(savedInstanceState);
        setMenuDrawer();
        setCredencials();
        throwEventGerAllOrigins();
        listeners();
    }
    @Override
    protected  void onStart(){
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onPause(){
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop(){
        mapView.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onResume(){
        mapView.onResume();
        super.onResume();
    }
    /**
     *
     */
    private void inicializateView() {

        mapView = findViewById(R.id.passenger_main_content_map);
        passengerMainProgress = findViewById(R.id.passenger_main_content_progress);
        passengerMainLinear = findViewById(R.id.passenger_main_content_linear1);
        passengerMainDestiny = findViewById(R.id.passenger_main_content_autocomplete2);
        passengerMainOrigin = findViewById(R.id.passenger_main_content_autocomplete);
        passengerMainButton  = findViewById(R.id.passenger_main_content_button);
        numWords = 0;
        destinySelected = false;
    }//inicializateView

    /**
     *
     */
    private void setProgressBar () {

        passengerMainProgress.setVisibility(View.VISIBLE);
        passengerMainLinear.setVisibility(View.GONE);
    }//setProgressBar

    /**
     *
     */
    private void removeProgressBar () {

        passengerMainProgress.setVisibility(View.GONE);
        passengerMainLinear.setVisibility(View.VISIBLE);
    }//removeProgressBar

    /**
     *
     */
    private void setMenuDrawer() {

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        DrawerLayout drawer = findViewById(R.id.passenger_main);
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
                    originIds = new HashMap<String, String>();
                    for (HashMap<?, ?> l : originMap){
                        originList.add((String) l.get("name"));
                        originIds.put((String)l.get("name"),(String)l.get("id"));

                    }

                    setAutoCompleteTextView();
                    removeProgressBar();
                }
            }
        });
    }//throwEventGerAllOrigins

    /**
     *
     */
    private void setAutoCompleteTextView() {

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, originList);
        passengerMainOrigin.setThreshold(1);
        passengerMainOrigin.setAdapter(adapter);
    }//setAutoCompleteTextView

    /**
     *
     * @return
     */
    private JSONObject buildJson(String origin,String destiny) throws JSONException { ;
        JSONObject route = new JSONObject();
        route.put("origin",origin);
        route.put("destination",destiny);

        JSONObject r = new JSONObject();
        r.put("route",route);
        return r;
    }//buildJson

    /**
     *
     */
    private void listeners() {

        mapView.getMapAsync(this);
        passengerMainButton.setOnClickListener(this);
        passengerMainDestiny.addTextChangedListener(this);
        passengerMainDestiny.setOnClickListener(this);
        passengerMainDestiny.setOnItemClickListener(this);
    }//listeners

    /**
     *
     * @param msg
     */
    private void throwToast(int msg) { Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show(); }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {

        this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {

            @Override
            public void onStyleLoaded(@NonNull Style style) {

                enableLocationComponent(style);
                mapboxMap.addOnMapClickListener(PassengerMain.this);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {

        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
        }
        else {

            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }//enableLocationComponent

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
        if (granted) enableLocationComponent(mapboxMap.getStyle());
        else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {

        Intent logIntent = new Intent(PassengerMain.this, MapMain.class);
        startActivity(logIntent);

        return true;
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = findViewById(R.id.passenger_main);
        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
        else super.onBackPressed();
    }//onBackPressed

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_settings_admin) { }
        else if (id == R.id.nav_signout_admin) { }

        DrawerLayout drawer = findViewById(R.id.passenger_main);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }//onNavigationItemSelected

    @Override
    public void onClick(View v) {

        boolean empty = false;

        switch (v.getId()) {

            case R.id.passenger_main_content_button:
                if (passengerMainDestiny.getText().toString().isEmpty()) empty = true;
                if (passengerMainOrigin.getText().toString().isEmpty()) empty = true;

                if (!empty) {

                    setProgressBar();
                    try {
                        this.originName = passengerMainOrigin.getText().toString();
                        throwEventSearchRoute(buildJson(originIds.get(passengerMainOrigin.getText().toString()),destination.getPostalCode()));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else throwToast(R.string.errDataEmpty);
                break;
        }
    }

    private void throwEventSearchRoute(JSONObject jsonObject) {
        EventDispatcher.getInstance(getApplicationContext()).dispatchEvent(SEARCHROUTE,jsonObject).addOnCompleteListener(new OnCompleteListener<HashMap<String, String>>() {
            @Override
            public void onComplete(@NonNull Task<HashMap<String, String>> task) {
                if (!task.isSuccessful() || task.getResult() == null) throwToast(R.string.errConexion);
                else if (task.getResult().containsKey("error")) throwToast(R.string.errServer);
                else {
                    Intent logIntent = new Intent(PassengerMain.this, PassengerSearchResult.class);
                    HashMap<?,?> result= task.getResult();
                    ArrayList<HashMap<?,?>> list = (ArrayList<HashMap<?,?>>) result.get("routes");
                    ArrayList<Route> RouteList = routesParser(list);
                    logIntent.putExtra("userAddress",destination);
                    logIntent.putExtra("routes", RouteList);
                    logIntent.putExtra("originName",originName);

                    startActivity(logIntent);

                }//else
            }
        });
    }

    private ArrayList<Route> routesParser(ArrayList<HashMap<?,?>>routes){

        ArrayList<Route> r = new ArrayList<Route>();

        for(HashMap<?,?> route:routes)
            r.add(new Route((String)route.get("id"),(String)route.get("origin"),(Integer) route.get("destination"),(String)route.get("driver"),(Integer)route.get("max"),(Integer)route.get("passengersNumber")));

        return r;
    }

    private void moveMap(List<Double> coordinates) {
        CameraPosition cp = new CameraPosition.Builder()
                .target(new LatLng(coordinates.get(1), coordinates.get(0)))
                .zoom(17)
                .tilt(20)
                .build();

        this.mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp),1000);
        this.mapboxMap.addMarker(new MarkerOptions()
                .position(new LatLng(coordinates.get(1), coordinates.get(0))));
    }


    // Destiny search bar listener methods
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

        if(destinySelected) destinySelected = false; //avoid an infinite loop.
        else {
            String value = s.toString();
            PassengerMain aux = this;
            int newNumWords = value.split(" ").length;

            if (value.matches(".*\\s")) {
                Map.getInstance(getApplicationContext()).getFullAddress(value).addOnCompleteListener(new OnCompleteListener<List<Address>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Address>> task) {
                        searchResult = task.getResult();
                        ArrayList<String> fullAddresses = new ArrayList<String>();

                        for (Address address : searchResult)
                            fullAddresses.add(address.getAddress());

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(aux, android.R.layout.simple_list_item_1, fullAddresses);
                        passengerMainDestiny.setThreshold(1);
                        passengerMainDestiny.setAdapter(adapter);
                        passengerMainDestiny.showDropDown();
                    }
                });

                numWords = newNumWords;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        destinySelected=true;

        int i = 0;
        String text = passengerMainDestiny.getText().toString();
        while(i<searchResult.size() && !searchResult.get(i).getAddress().equals(text)) i++;

        if(i>=searchResult.size()) throwToast(R.string.errDestinyNotExisit);
        else {
            this.destination=searchResult.get(i);
            moveMap(searchResult.get(i).getCoordinates());
        }

    }
}