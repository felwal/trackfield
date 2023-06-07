package me.felwal.trackfield.ui.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.mapbox.geojson.utils.PolylineUtils;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.ArrayList;
import java.util.HashMap;

import me.felwal.android.util.ResourcesKt;
import me.felwal.trackfield.BuildConfig;
import me.felwal.trackfield.R;
import me.felwal.trackfield.ui.map.model.Trail;
import me.felwal.trackfield.ui.widget.sheet.PeekSheet;
import me.felwal.trackfield.utils.LocationUtilsKt;
import me.felwal.trackfield.utils.ScreenUtils;

public abstract class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
    MapboxMap.OnPolylineClickListener, Style.OnStyleLoaded, PeekSheet.SheetListener {

    // extras names
    protected static final String EXTRA_ID = "id";

    protected static final int MAP_PADDING = 100;
    protected static final int MAP_MAX_ZOOM = 18;

    protected ArrayList<Polyline> selectedPolylines = new ArrayList<>();
    protected final ArrayList<Polyline> tempPolylines = new ArrayList<>();

    protected int id;
    protected MapboxMap mapboxMap;
    protected MapView mapView;

    protected boolean tempShown = false;
    protected boolean satellite = false;

    // extends AppCompatActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScreenUtils.updateTheme(this);
        Mapbox.getInstance(this, BuildConfig.MAPBOX_DOWNLOADS_TOKEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setToolbar();
        ScreenUtils.makeStatusBarTransparent(getWindow(), false, findViewById(R.id.tb_map));

        // extras
        Intent intent = getIntent();
        if (intent == null) finish();
        id = intent.getIntExtra(EXTRA_ID, -1);

        // initialize maps with the latest renderer (currently neccessary for 18.0.0
        //MapsInitializer.initialize(getApplicationContext(), Renderer.LATEST, null);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fr_map);
        //mapFragment.getMapAsync(this);

        mapView = findViewById(R.id.mv_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setMaxZoomPreference(MAP_MAX_ZOOM);
        mapboxMap.getUiSettings().setCompassEnabled(false);
        mapboxMap.setStyle(getStyle());
    }

    @Override
    public void onStyleLoaded(@NonNull Style style) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        else if (itemId == R.id.action_toggle_map_type) {
            // toggle normal/satellite + update polylines color

            satellite = !satellite;
            mapboxMap.setStyle(getStyle());

            @ColorInt int selectedColor = getColorSelected();
            @ColorInt int deselectedColor = getColorDeselected();

            for (Polyline p : selectedPolylines) p.setColor(selectedColor);
            for (Polyline p : tempPolylines) p.setColor(deselectedColor);

            return true;
        }
        else if (itemId == R.id.action_toggle_map_history) {
            togglePolylines();
            return true;
        }
        else if (itemId == R.id.action_recentre_map) {
            recentre();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected abstract void recentre();

    // set

    private void setToolbar() {
        final Toolbar tb = findViewById(R.id.tb_map);
        setSupportActionBar(tb);
        ActionBar ab = getSupportActionBar();
        ab.setTitle("");
        ab.setDisplayHomeAsUpEnabled(true);
    }

    // get

    protected ArrayList<Polyline> getPolylineComplement(Polyline to) {
        ArrayList<Polyline> complement = new ArrayList<>(tempPolylines);
        complement.remove(to);
        return complement;
    }

    protected abstract HashMap<Integer, String> getRestOfPolylines(int exceptId);

    // tools

    private void togglePolylines() {
        // show
        if (!tempShown) {
            mapboxMap.setOnPolylineClickListener(this);

            for (HashMap.Entry<Integer, String> entry : getRestOfPolylines(id).entrySet()) {
                // options
                PolylineOptions options = new PolylineOptions();
                options.color(getColorDeselected());
                options.width(ScreenUtils.px(1));
                options.addAll(LocationUtilsKt.toLatLngs(PolylineUtils.decode(entry.getValue(), 5)));

                // poly
                Polyline polyline = mapboxMap.addPolyline(options);
                polyline.setId(entry.getKey());
                tempPolylines.add(polyline);
            }
        }
        // hide
        else {
            mapboxMap.setOnPolylineClickListener(null);

            for (Polyline p : tempPolylines) p.remove();
            tempPolylines.clear();
        }

        tempShown = !tempShown;
    }

    protected static void moveCamera(MapboxMap mapboxMap, LatLngBounds bounds, int padding, boolean animate) {
        final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        try {
            if (animate) mapboxMap.animateCamera(cu);
            else mapboxMap.moveCamera(cu);
        }
        catch (Exception e) {
        }
    }

    private String getStyle() {
        return satellite ? Style.SATELLITE : (ScreenUtils.isThemeLight(this) ? Style.LIGHT : Style.DARK);
    }

    // implements GoogleMap, PeekSheet

    @Override
    public void onPolylineClick(Polyline polyline) {
        if (polyline.getId() == -1) return;

        // show sheet
        int id = (int) polyline.getId();
        PeekSheet.newInstance(id).show(getSupportFragmentManager());

        // focus camera
        LatLngBounds bounds = LocationUtilsKt.getBounds(polyline.getPoints());
        moveCamera(mapboxMap, bounds, MAP_PADDING, true);

        // appearance
        polyline.setColor(getColorSelected());
        for (Polyline line : getPolylineComplement(polyline)) line.setColor(getColorHidden(this));
        for (Polyline line : selectedPolylines) line.setColor(getColorHidden(this));
    }

    @Override
    public void onPeekSheetClick(int exerciseId) {
        // get polyline
        Polyline polyline = null;
        for (Polyline line : tempPolylines) {
            if ((int) line.getId() == exerciseId) {
                polyline = line;
                break;
            }
        }
        if (polyline == null) return;

        // appearence
        polyline.setColor(getColorDeselected());
        for (Polyline line : getPolylineComplement(polyline)) line.setColor(getColorDeselected());
        for (Polyline line : selectedPolylines) line.setColor(getColorSelected());
    }

    // get static

    @ColorInt
    protected int getColorSelected() {
        return getColorSelected(satellite, this);
    }

    @ColorInt
    protected static int getColorSelected(boolean satellite, Context c) {
        return satellite ? c.getColor(R.color.polyline_satellite) : ResourcesKt.getColorByAttr(c, R.attr.colorSecondary);
    }

    @ColorInt
    protected int getColorDeselected() {
        return getColor(satellite ? R.color.selector_color_polyline_deselected_satellite
            : R.color.selector_color_polyline_deselected);
    }

    @ColorInt
    protected static int getColorHidden(Context c) {
        return c.getColor(android.R.color.transparent);
    }

}
