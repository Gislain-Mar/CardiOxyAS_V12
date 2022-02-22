package com.example.cardioxyas_v10.settings;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cardioxyas_v10.R;

import java.util.Iterator;
import java.util.Set;


//##############################################################################################//
//##############################################################################################//
//																								//
//declaration de la classe 																		//
//																								//
//Veymont technologie le 10/12/2012																//
//																								//
//date de derniere modification: 																//
//	 - par E.B le 10/12/2012 : suppression code mort et ajout commantaire						//																							//
//																								//
//##############################################################################################//
//##############################################################################################//
public class SettingsActivity extends AppCompatActivity {
    Settings_pref myprefs = null;
    //############ declaration des variables globales ###############//
    final String tag = "ClientRGB BT :Preferences";
    AlertDialog.Builder adb;// = new AlertDialog.Builder(this);
    private Set<BluetoothDevice> pairedDevices;
    int sel = -1;
    
//##################################################################//    
// Methode appelee lors de la creation de l'appli  					//
// 																	//
// 																	//
//##################################################################//   
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.settings);

        //------------- 1 Initialise l'instance de Prefs -----------//
        this.myprefs = new Settings_pref(getApplicationContext());
        //2 Peuple les elements de l'interface
        PopulateScreen();
        this.adb = new AlertDialog.Builder(this);
        final Button btScan =  (Button) findViewById(R.id.btscan);
        final Button savebutton = (Button) findViewById(R.id.settingssave);
        
        //------------------ gestion bouton scan -------------------//
        btScan.setOnClickListener(new Button.OnClickListener() {
            @SuppressLint("LongLogTag")
            public void onClick(View v) {
                try {
                    final EditText btfield = (EditText) findViewById(R.id.bluetooth_ident);
        			if( pairedDevices == null) {
                        BluetoothAdapter btInterface = BluetoothAdapter.getDefaultAdapter();
        				pairedDevices = btInterface.getBondedDevices();
        				sel = -1;
        			}
        			
        			if( pairedDevices.size() > 0 ) {
        				if( sel < 0 || sel >= pairedDevices.size()-1) sel = 0;
        				else sel = sel+1;
        			
        				int x = sel;
            			Iterator<BluetoothDevice> it = pairedDevices.iterator();
            			while (it.hasNext()) {
            				BluetoothDevice bd = it.next();
            				if( x-- == 0 ) btfield.setText( bd.getName());
            			}
        			}
                } catch (Exception e) {
                    Log.i(SettingsActivity.this.tag, "Scan en echec [" + e.getMessage() + "]");
                }
            }

        });
        //--------------------- bouton save ------------------------//
        savebutton.setOnClickListener(new Button.OnClickListener() {
            @SuppressLint("LongLogTag")
            public void onClick(View v) {
                try {
                    //3 Relie EditText a l'interface
                    final EditText btid = (EditText) findViewById(R.id.bluetooth_ident);
                    final EditText ipid = (EditText) findViewById(R.id.ip_ident);
                    final EditText portid = (EditText) findViewById(R.id.port_ident);
                    if (btid.getText().length() == 0) {
                        AlertDialog ad = SettingsActivity.this.adb.create();
                        ad.setMessage("Indiquez l'identifiant Bluetooth du serveur");
                        ad.show();
                        return;
                    }
                    
                    //4 Stocke les parametres
                    SettingsActivity.this.myprefs.setBluetoothIdent(btid.getText().toString());
                    SettingsActivity.this.myprefs.setValue("ip_ident", ipid.getText().toString());
                    SettingsActivity.this.myprefs.setValue("port_ident", portid.getText().toString());
                    SettingsActivity.this.myprefs.save();
                    //5 Stoppe l'activite
                    finish();
                } catch (Exception e) {
                    Log.i(SettingsActivity.this.tag, "Sauvegarde parametres en echec [" + e.getMessage() + "]");
                }
            }

        });
    }
    //------------------- 6 Initialise l'interface -----------------------//
    private void PopulateScreen() {
      try {
        final EditText btfield = (EditText) findViewById(R.id.bluetooth_ident);
        final EditText ipid = (EditText) findViewById(R.id.ip_ident);
        final EditText portid = (EditText) findViewById(R.id.port_ident);
        btfield.setText(this.myprefs.getBluetoothIdent());
        ipid.setText(this.myprefs.getValue("ip_ident", null));
        portid.setText(this.myprefs.getValue("port_ident", null));
          } catch (Exception e) {
        }
    }
}
