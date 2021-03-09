package com.example.trackfield.activities.mapactivity;

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

    // extends AppCompatActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        D.updateTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setToolbar();
        L.makeStatusBarTransparent(getWindow(), false, findViewById(R.id.toolbar_map));

        // intent
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
        googleMap.getUiSettings().setCompassEnabled(false);

        // set padding (for compass)
        //int mapPaddingTop = /*L.statusBarHeight +*/ L.getAttr(R.attr.actionBarSize, this);
        //googleMap.setPadding(0, mapPaddingTop, 0, 0); // left top right bottom
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
        else if (itemId == R.id.action_maptype) {
            googleMap.setMapType(googleMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL ? GoogleMap.MAP_TYPE_HYBRID : GoogleMap.MAP_TYPE_NORMAL);
            return true;
        }
        else if (itemId == R.id.action_heatmap) {
            togglePolylines();
            return true;
        }
        else if (itemId == R.id.action_filter) {
            return true;
        }
        else if (itemId == R.id.action_recentre) {
            recentre();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected abstract void recentre();

    // set

    private void setToolbar() {
        final Toolbar tb = findViewById(R.id.toolbar_map);
        setSupportActionBar(tb);
        ActionBar ab = getSupportActionBar();
        ab.setTitle("");//getResources().getString(R.string.activity_maps));
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
                    options.color(getColorDeselected(this));
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

    // implements GoogleMap, PeekSheet

    @Override
    public void onPolylineClick(Polyline polyline) {
        if (polyline.getTag() == null) return;

        // sheet
        int id = (int) polyline.getTag();
        PeekSheet.newInstance(id).show(getSupportFragmentManager());

        // focus camera
        //LatLngBounds bounds = Trail.bounds(polyline.getPoints());
        //moveCamera(googleMap, bounds, MAP_PADDING, true);

        // appearence
        polyline.setColor(getColorSelected(this));
        for (Polyline line : getPolylineComplement(polyline)) line.setColor(getColorHidden(this));
    }

    @Override
    public void onPeekSheetDismiss(int id) {
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
        polyline.setColor(getColorDeselected(this));
        for (Polyline line : getPolylineComplement(polyline)) {
            line.setColor(getColorDeselected(this));
        }
    }

    // get static

    @ColorInt
    protected static int getColorSelected(Context c) {
        return c.getResources().getColor(R.color.colorGreenLight);
    }

    @ColorInt
    protected static int getColorDeselected(Context c) {
        return c.getResources().getColor(R.color.colorGreenLightTrans);
    }

    @ColorInt
    protected static int getColorHidden(Context c) {
        return c.getResources().getColor(R.color.colorTrans);
    }

}
