package com.felwal.trackfield.ui.map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.felwal.android.util.ResUtilsKt;
import com.felwal.trackfield.R;
import com.felwal.trackfield.ui.widget.sheet.PeekSheet;
import com.felwal.trackfield.utils.LayoutUtils;
import com.felwal.trackfield.utils.ScreenUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.MapsInitializer.Renderer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
    GoogleMap.OnPolylineClickListener, PeekSheet.SheetListener {

    // extras names
    protected static final String EXTRA_ID = "id";

    protected static final int MAP_PADDING = 100;
    protected static final int MAP_MAX_ZOOM = 18;

    protected ArrayList<Polyline> seletedPolylines = new ArrayList<>();
    protected final ArrayList<Polyline> tempPolylines = new ArrayList<>();

    protected int id;
    protected GoogleMap map;
    protected boolean tempShown = false;

    // extends AppCompatActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScreenUtils.updateTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setToolbar();
        ScreenUtils.makeStatusBarTransparent(getWindow(), false, findViewById(R.id.tb_map));

        // extras
        Intent intent = getIntent();
        if (intent == null) finish();
        id = intent.getIntExtra(EXTRA_ID, -1);

        // initialize maps with the latest renderer (currently neccessary for 18.0.0
        MapsInitializer.initialize(getApplicationContext(), Renderer.LATEST, null);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fr_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        googleMap.setMaxZoomPreference(MAP_MAX_ZOOM);
        googleMap.getUiSettings().setCompassEnabled(false);
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
            //googleMap.setMapType(googleMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL ? GoogleMap.MAP_TYPE_HYBRID :
            //    (googleMap.getMapType() == GoogleMap.MAP_TYPE_HYBRID ? GoogleMap.MAP_TYPE_TERRAIN : GoogleMap.MAP_TYPE_NORMAL));
            map.setMapType(map.getMapType() == GoogleMap.MAP_TYPE_NORMAL ? GoogleMap.MAP_TYPE_HYBRID
                : GoogleMap.MAP_TYPE_NORMAL);
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
            for (HashMap.Entry<Integer, String> entry : getRestOfPolylines(id).entrySet()) {
                // options
                PolylineOptions options = new PolylineOptions();
                options.color(getColorDeselected(this));
                options.width(ScreenUtils.px(3));
                options.addAll(PolyUtil.decode(entry.getValue()));

                // poly
                Polyline polyline = map.addPolyline(options);
                polyline.setTag(entry.getKey());
                polyline.setClickable(true);
                tempPolylines.add(polyline);
            }
        }
        // hide
        else {
            for (Polyline p : tempPolylines) p.remove();
            tempPolylines.clear();
        }

        tempShown = !tempShown;
    }

    protected static void moveCamera(GoogleMap googleMap, LatLngBounds bounds, int padding, boolean animate) {
        final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        try {
            if (animate) googleMap.animateCamera(cu);
            else googleMap.moveCamera(cu);
        }
        catch (Exception e) {
            googleMap.setOnMapLoadedCallback(() -> {
                if (animate) googleMap.animateCamera(cu);
                else googleMap.moveCamera(cu);
            });
        }
    }

    protected static void setMapStyle(GoogleMap googleMap, Context c) {
        MapStyleOptions style = new MapStyleOptions(ResUtilsKt.getStringByAttr(c, R.attr.mapStyle));
        googleMap.setMapStyle(style);
    }

    // implements GoogleMap, PeekSheet

    @Override
    public void onPolylineClick(Polyline polyline) {
        if (polyline.getTag() == null) return;

        // show sheet
        int id = (int) polyline.getTag();
        PeekSheet.newInstance(id).show(getSupportFragmentManager());

        // focus camera
        //LatLngBounds bounds = Trail.bounds(polyline.getPoints());
        //moveCamera(googleMap, bounds, MAP_PADDING, true);

        // appearance
        polyline.setColor(getColorSelected(this));
        for (Polyline line : getPolylineComplement(polyline)) line.setColor(getColorHidden(this));
        for (Polyline line : seletedPolylines) line.setColor(getColorHidden(this));
    }

    @Override
    public void onPeekSheetClick(int exerciseId) {
        // get polyline
        Polyline polyline = null;
        for (Polyline line : tempPolylines) {
            if ((int) line.getTag() == exerciseId) {
                polyline = line;
                break;
            }
        }
        if (polyline == null) return;

        // appearence
        polyline.setColor(getColorDeselected(this));
        for (Polyline line : getPolylineComplement(polyline)) line.setColor(getColorDeselected(this));
        for (Polyline line : seletedPolylines) line.setColor(getColorSelected(this));
    }

    // get static

    @ColorInt
    protected static int getColorSelected(Context c) {
        return ResUtilsKt.getColorByAttr(c, R.attr.colorPrimaryVariant);
    }

    @ColorInt
    protected static int getColorDeselected(Context c) {
        return c.getColor(ScreenUtils.isThemeLight(c)
            ? R.color.colorGreenDarkTrans
            : R.color.colorGreenLightTrans);
    }

    @ColorInt
    protected static int getColorHidden(Context c) {
        return c.getColor(R.color.colorTrans);
    }

}
