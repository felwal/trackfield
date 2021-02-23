package com.example.trackfield.activities.map_activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.trackfield.R;
import com.example.trackfield.dialogs.sheets.PeekSheet;
import com.example.trackfield.objects.Trail;
import com.example.trackfield.toolbox.D;
import com.example.trackfield.toolbox.L;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, PeekSheet.DismissListener {

    protected int _id;

    protected GoogleMap googleMap;
    protected ArrayList<Polyline> tempPolylines = new ArrayList<>();
    protected ArrayList<PolylineOptions> tempOptions = new ArrayList<>();
    protected boolean tempShown = false;

    protected static String EXTRA_ID = "_id";
    protected static final int MAP_PADDING = 100;
    protected static final int MAP_MAX_ZOOM = 18;

    ////

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        D.updateTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        L.transStatusBar(getWindow());
        setToolbar();

        Intent intent = getIntent();
        if (intent == null) finish();
        _id = intent.getIntExtra(EXTRA_ID, -1);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMaxZoomPreference(MAP_MAX_ZOOM);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_maptype:
                googleMap.setMapType(googleMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL ? GoogleMap.MAP_TYPE_HYBRID : GoogleMap.MAP_TYPE_NORMAL);
                return true;

            case R.id.action_heatmap:
                togglePolylines();
                return true;

            case R.id.action_filter:
                return true;

            default: return super.onOptionsItemSelected(item);
        }
    }

    // set

    private void setToolbar() {
        final Toolbar tb = findViewById(R.id.toolbar_map);
        setSupportActionBar(tb);
        ActionBar ab = getSupportActionBar();
        ab.setTitle("");//getResources().getString(R.string.activity_maps));
        ab.setDisplayHomeAsUpEnabled(true);
    }

    // abstract

    protected abstract HashMap<Integer, String> getRestOfPolylines(int exceptId);

    // tools

    private void togglePolylines() {

        if (!tempShown) {
            if (tempOptions.size() != 0) {
                tempOptions.clear(); // temp
                for (PolylineOptions options : tempOptions) {
                    tempPolylines.add(googleMap.addPolyline(options));
                }
            }
            if (tempOptions.size() == 0) {
                for (HashMap.Entry<Integer, String> entry : getRestOfPolylines(_id).entrySet()) {

                    // options
                    PolylineOptions options = new PolylineOptions();
                    options.color(polyColorDeselected(this));
                    options.width(L.px(3));
                    options.addAll(PolyUtil.decode(entry.getValue()));
                    tempOptions.add(options);

                    // poly
                    Polyline polyline = googleMap.addPolyline(options);
                    polyline.setTag(entry.getKey());
                    polyline.setClickable(true);
                    tempPolylines.add(polyline);
                }
            }
        }
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

    protected ArrayList<Polyline> getPolylineComplement(Polyline to) {
        ArrayList<Polyline> complement = new ArrayList<>(tempPolylines);
        complement.remove(to);
        return complement;
    }

    // resources

    @ColorInt
    protected static int polyColorSelected(Context c) {
        return c.getResources().getColor(R.color.colorGreenLight);
    }

    @ColorInt
    protected static int polyColorDeselected(Context c) {
        return c.getResources().getColor(R.color.colorGreenLightTrans);
    }

    @ColorInt
    protected static int polyColorHidden(Context c) {
        return c.getResources().getColor(R.color.colorTrans);
    }

    // implements

    @Override public void onPolylineClick(Polyline polyline) {

        // sheet
        int id = (int) polyline.getTag();
        PeekSheet.newInstance(id).show(getSupportFragmentManager());

        // camera
        LatLngBounds bounds = Trail.bounds(polyline.getPoints());
        moveCamera(googleMap, bounds, MAP_PADDING, true);

        // appearence
        polyline.setColor(polyColorSelected(this));
        for (Polyline line : getPolylineComplement(polyline)) line.setColor(polyColorHidden(this));

    }

    @Override public void onPeekSheetDismiss(int id) {

        // get poly
        Polyline polyline = null;
        for (Polyline line : tempPolylines) {
            if ((int) line.getTag() == id) {
                polyline = line;
                break;
            }
        }
        if (polyline == null) return;

        // appearence
        polyline.setColor(polyColorDeselected(this));
        for (Polyline line : getPolylineComplement(polyline)) line.setColor(polyColorDeselected(this));

    }

}
