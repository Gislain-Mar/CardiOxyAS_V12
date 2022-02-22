package com.example.cardioxyas_v10;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.cardioxyas_v10.envoi.EnvoiFragment;
import com.example.cardioxyas_v10.envoi.ThreadEnvoi;
import com.example.cardioxyas_v10.reception.ReceptionFragment;
import com.example.cardioxyas_v10.reception.ThreadReception;
import com.example.cardioxyas_v10.settings.SettingsActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ReceptionFragment.SaveUI, EnvoiFragment.SaveUI {

    private static Bundle SaveStateUI = new Bundle();
    private static ThreadReception monThreadReception;
    private static ThreadEnvoi monThreadEnvoi;

    private static FusedLocationProviderClient fusedLocationProviderClient;
    private static double longitude;
    private static double latitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_reception, R.id.navigation_envoi, R.id.navigation_graphique)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        this.setLocation();
    }

    //##################################################################//
    //Methode appelee lors de la creation du menu	  					//
    //																	//
    //																	//
    //##################################################################//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    //##################################################################//
    // Methode appelee lors de la selection d'un menu  					//
    // 																	//
    // PE: item : objet menu											//
    //																	//
    //##################################################################//
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuitem1:
                //------- appelle de la fenetre de configuration -------//
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.menuitem3:
                //--------------- arret de l'appli (quitter) -----------//
                finish();
                break;

            default:
                break;
        }
        return true;
    }

    private void setLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this
                , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    if (location != null) {
                        try {
                            Geocoder geocoder = new Geocoder(MainActivity.this
                                    , Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(
                                    location.getLatitude(), location.getLongitude(), 1
                            );
                            System.out.println(addresses.get(0).getLatitude() + "/" + addresses.get(0).getLongitude() + "/" + addresses.get(0).getCountryName() + "/" + addresses.get(0).getAddressLine(0));
                            latitude = addresses.get(0).getLatitude();
                            longitude = addresses.get(0).getLongitude();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }else{
            ActivityCompat.requestPermissions(MainActivity.this
                    , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }
    public String getLocation() {
        //this.setLocation();
        String location = this.latitude + "/" + this.longitude;
        return location;
    }

    @Override
    public void saveTextInfo(String key, String string) {
        SaveStateUI.putString(key, string);
    }

    @Override
    public void saveTextBtnConnexion(String key, String string) {
        SaveStateUI.putString(key, string);
    }

    @Override
    public void saveTextInfoTCP(String key, String string) {
        SaveStateUI.putString(key, string);
    }

    @Override
    public void saveTextBtnConnexionTCP(String key, String string) {
        SaveStateUI.putString(key, string);
    }

    @Override
    public void saveStateOfisConnect(String key, Boolean state) {
        SaveStateUI.putBoolean(key, state);
    }

    public String getStringBuffer(String key, String DefaultValue) {
        return SaveStateUI.getString(key, DefaultValue);
    }

    public Boolean getBooleanBuffer(String key, Boolean DefaultValue) {
        return SaveStateUI.getBoolean(key, DefaultValue);
    }

    public void setmonThreadReception(ThreadReception thread) {
        monThreadReception = thread;
    }

    public static void setmonThreadEnvoi(ThreadEnvoi thread) {
        monThreadEnvoi = thread;
    }

    public ThreadEnvoi getmonThreadEnvoi() {
        return monThreadEnvoi;
    }

    public ThreadReception getMonThreadReception() {
        return monThreadReception;
    }
}