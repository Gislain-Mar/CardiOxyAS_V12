package com.example.cardioxyas_v10.reception;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cardioxyas_v10.MainActivity;
import com.example.cardioxyas_v10.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;

public class ReceptionFragment extends Fragment implements View.OnClickListener{

    //2 - Declare callback
    private SaveUI mCallback;

    // 1 - Declare our interface that will be implemented by any container activity
    public interface SaveUI {
        void saveTextInfo(String key, String string);
        void saveTextBtnConnexion(String key, String string);
    }

    //############ declaration des variables globales ###############//
    private ThreadReception monThreadReception;
    private MainActivity mMainActivity = new MainActivity();

    //----------------- variables de fonctionnement -----------------//
    private BluetoothSocket socket;
    private BroadcastReceiver btMonitor = null;
    private InputStream is = null;
    private OutputStream os = null;
    private boolean bConnecte = false;

    //--------------- variables interface homme/machine --------------//
    private Button mConnectBtn;
    private TextView mTextInfoconnexion;
    private static TextView mTextFreqCard, mTextOxygene;

    //---------------- variables de configuration ------------------//
    private String bluetooth_id;
    private String Trame;
    private boolean ThreadReceptionStop = true;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_reception_bt, container, false);

        //----- Recupereration de  l'adresse bluetooth ----------//
        SharedPreferences mgr = getContext().getSharedPreferences("MIMI1",
                Context.MODE_PRIVATE);
        bluetooth_id = mgr.getString("bluetooth_ident", "defaut");

        //----- affichage du nom du peripherique bluetooth ------//
        Toast.makeText(getContext(), bluetooth_id, Toast.LENGTH_LONG)
                .show();

        //--------- Initialisation scrutation bluetooth ---------//
        setupBTMonitor();

        //---------------- boutons applicatifs ------------------//
        mConnectBtn = root.findViewById(R.id.connect);
        mConnectBtn.setOnClickListener(this);

        //----------------- Zone de texte -----------------------//
        mTextInfoconnexion = root.findViewById(R.id.textInfoConnexion);
        mTextFreqCard = root.findViewById(R.id.ValueTextViewFC);
        mTextOxygene = root.findViewById(R.id.ValueTextViewO);

        mTextInfoconnexion.setText(mMainActivity.getStringBuffer("TEXT_INFO", "Veuillez vous connecter à un capteur" ));
        mConnectBtn.setText(mMainActivity.getStringBuffer("TEXT_BTN_CONNEXION", "Connexion"));

       /* if(!ReceptionDonnees.isAlive()){
            ReceptionDonnees.start();
        }*/

        return root;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.createCallbackToParentActivity();
    }

    // 3 - Create callback to parent activity
    private void createCallbackToParentActivity(){
        try {
            //Parent activity will automatically subscribe to callback
            mCallback = (SaveUI) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(e.toString()+ " must implement OnButtonClickedListener");
        }
    }

    // 3 - Create callback to parent activity
    private void SaveUIData(){
        mCallback.saveTextBtnConnexion("TEXT_BTN_CONNEXION", mConnectBtn.getText().toString());
        mCallback.saveTextInfo("TEXT_INFO", mTextInfoconnexion.getText().toString());
    }

    //##################################################################//
    // Methode appelee lors d'un click sur un des boutons				//
    //			 														//
    // PE: v     : Bouton clicke										//
    //																	//
    //##################################################################//
    public void onClick(View v) {
        if (v == mConnectBtn) {
            //-- emission son + vibration --//
            //Vibro(DUREE_BP);
            //-- connexion deconnexion --//
            if( !bConnecte) {
                findRobot();
            }
            else {
                bConnecte = false;
                disconnectFromRobot(null);
            }
            this.SaveUIData();
        }
    }

    //##################################################################//
    // Methode de config du moniteur Bluetooth							//
    //			 														//
    //##################################################################//
    private void setupBTMonitor() {
        // Creation recepteur de diffusion
        btMonitor = new BroadcastReceiver() {
            @Override
            // Methode standard onReceive()
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(
                        "android.bluetooth.device.action.ACL_CONNECTED")) {
                    // Donc, connexion etablie
                    handleConnected();
                }
                if (intent.getAction().equals(
                        "android.bluetooth.device.action.ACL_DISCONNECTED")) {
                    // Donc, connexion rompue
                    try {
                        handleDisconnected();
                    } catch (IOException e) {
                        //TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    //##################################################################//
    // handler appele lors d'une connection bluetooth					//
    //				 													//
    //##################################################################//
    private void handleConnected() {
        try {
            // 1 Implantation flux entree et sortie
            bConnecte = true;
            // 3 Inversion de l'aspect des boutons
        } catch (Exception e) {
            // 4 Gestion des exceptions
            if( is != null) {
                try {
                    is.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                is = null;
            }
            if( os!=null) {
                try {
                    os.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                os = null;
            }
            bConnecte = false;
            // 5 Fermeture de la connexion si erreur
            disconnectFromRobot(null);
        }
    }

    //##################################################################//
    // handler appele lors d'une deconection bluetooth					//
    //					 												//
    //##################################################################//
    private void handleDisconnected() throws IOException
    {
        bConnecte = false;
        if( is != null) {
            is.close();
            is = null;
        }
        if( os!=null) {
            os.close();
            os = null;
        }
    }

    //##################################################################//
    // Methode appelee lors de la demande de connection bluetooth		//
    // elle permet la detection du module correspondant a Mimi          //
    //																	//
    //##################################################################//
    public void findRobot() {
        String tag = "findRobot";
        try {
            SharedPreferences mgr = getContext().getSharedPreferences("MIMI1",
                    Context.MODE_PRIVATE);
            String dvc = mgr.getString("bluetooth_ident", "MonPheripheriqueBT");

            // 1 Obtention adaptateur
            //-------------- Variables communication BlueTooth (BT)----------//
            BluetoothAdapter btInterface = BluetoothAdapter.getDefaultAdapter();
            Log.v( tag,
                    "Nom interface BlueTooth locale = ["
                            + btInterface.getName() + "]");
            // 2 Obtention liste peripheriques
            Set<BluetoothDevice> pairedDevices = btInterface.getBondedDevices();
            Log.v( tag,
                    "Nombre d\'appareils detectes = [" + pairedDevices.size()
                            + "].");
            Iterator<BluetoothDevice> it = pairedDevices.iterator();
            // 3 Balayage liste
            while (it.hasNext()) {
                BluetoothDevice bd = it.next();
                Log.i(tag, "Appareil identifie : [" + bd.getName() + "]");
                // 4 Recherche du robot par son nom
                if (bd.getName().equalsIgnoreCase(dvc)) {
                    Log.v(tag, "Cible BT detectee !");
                    Log.v(tag, bd.getAddress());
                    Log.v(tag, bd.getBluetoothClass().toString());
                    // 5 Connexion au robot
                    connectToRobot(bd);
                    return;
                }
                mConnectBtn.setText("Déconnexion");
            }
        } catch (Exception e) { // 6 Gestion exceptions de connexion
            Log.e(tag, "Echec dans findRobot() " + e.getMessage());
            mConnectBtn.setText("Connexion");
        }
    }

    //##################################################################//
    // Methode appelee pour se connecter au module bluetooth			//
    //																	//
    // PE: bd     : identificateur bt									//
    //																	//
    //##################################################################//
    private void connectToRobot(BluetoothDevice bd) {
        String tag = "connectToRobot";

        try {
            // 7 Obtention interface socket
            socket = bd.createRfcommSocketToServiceRecord(java.util.UUID
                    .fromString("00001101-0000-1000-8000-00805F9B34FB"));
            // 8 Ouverture connexion
            socket.connect();
            mConnectBtn.setText("Deconnexion");
            mTextInfoconnexion.setText("Connecté à : " + bd.getName());
            try {
                is = socket.getInputStream();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            try {
                os = socket.getOutputStream();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            monThreadReception = new ThreadReception();
            System.out.println(monThreadReception.getId());
            if(!monThreadReception.isAlive())
                monThreadReception.start();
            monThreadReception.setIS(this.is);
            monThreadReception.setStateOfThreadReceptionStop(false);
            bConnecte = true;
        } catch (Exception e) {
            Log.e(tag, "Erreur interaction avec [" + e.getMessage() + "]");
            mConnectBtn.setText("Connexion");
            mTextInfoconnexion.setText("Connexion impossible à : " + bd.getName());
        }
    }

    //##################################################################//
    // Methode appelee pour se deconnecter du module bluetooth			//
    //																	//
    // PE: v     : non utilise											//
    //																	//
    //##################################################################//
    public void disconnectFromRobot( View v) {
        String tag = "disconnectFromRobot";
        try {
            Log.v(tag, "Tentons de rompre la connexion BT");
            try {
                is.close();
            } catch( Exception e) {}
            is =  null;

            try {
                os.close();
            } catch( Exception e) {}
            os = null;

            try {
                socket.close();
            } catch( Exception e) {}
            socket = null;

        } catch (Exception e) {
            Log.e(tag, "Erreur dans disconnectFromRobot [" + e.getMessage()
                    + "]");
        }
        monThreadReception.setStateOfThreadReceptionStop(true);
        monThreadReception.interrupt();
        mConnectBtn.setText("Connexion");
        mTextInfoconnexion.setText("Veuillez vous connecter à un capteur");
    }

    public void setTextOfTextFreqCard(String chaine){
        if(mTextFreqCard != null){
            mTextFreqCard.setText(chaine);
        }
    }
    public void setTextOfTextOxygene(String chaine){
        if(mTextFreqCard != null){
            mTextOxygene.setText(chaine);
        }
    }
}
