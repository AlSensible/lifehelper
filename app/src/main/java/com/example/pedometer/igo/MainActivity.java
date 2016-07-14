package com.example.pedometer.igo;

import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.example.pedometer.igo.Fragment.ChooseAreaFragment;
import com.example.pedometer.igo.Fragment.PedometerFragment;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar=null;
    private DrawerLayout drawerLayout=null;
    private ActionBarDrawerToggle drawerToggle=null;
    private NavigationView navigationView=null;
    private static final String TAG = "Pedometer";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout= (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle=new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close);
        drawerToggle.syncState();

        drawerLayout.setDrawerListener(drawerToggle);

        navigationView= (NavigationView) findViewById(R.id.navigation_view);
        setupDrawerContent(navigationView);
        Log.d(TAG, "setupDrawerContent");
        switchToPedometer();
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.pedometer:
                                switchToPedometer();
                                break;
                            case R.id.weather_forcast:
                                switchToWeather();
                                break;
                        }
                        menuItem.setChecked(true);
                        drawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    private void switchToPedometer() {

        getSupportFragmentManager().beginTransaction().replace(R.id.container, new PedometerFragment()).commit();
        toolbar.setTitle(R.string.pedometer);

    }

    private void switchToWeather() {
        Log.d(TAG, "switchToWeatherbefore");
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new ChooseAreaFragment()).commit();
        Log.d(TAG, "switchToWeather");
        toolbar.setTitle(R.string.weather_forcast);
        Log.d(TAG, "setTitle");
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
