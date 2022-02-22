package com.example.cardioxyas_v10.envoi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.BoringLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.cardioxyas_v10.MainActivity;
import com.example.cardioxyas_v10.R;
import com.example.cardioxyas_v10.reception.ReceptionFragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class EnvoiFragment extends Fragment implements View.OnClickListener {

    private static ThreadEnvoi monThreadEnvoie;
    private MainActivity mMainActivity = new MainActivity();

    //2 - Declare callback
    private SaveUI mCallback;

    private String ADR_SERVEUR = "172.29.41.11";
    private int PORT_SERVEUR_TCP = 1234; //numero de port arbitraire
    private boolean isConnect = false;

    private OutputStream os = null;
    private InputStream is = null;
    private Socket _socket = null;

    private Button mConnect;
    private TextView mTextViewInfo;

    private SharedPreferences mgr;

    private byte maTrame[];
    private String Trame = "";
    private String laTrameLue = "";


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_envoi_tcp, container, false);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here
        }

        //----- Recupereration de  l'adresse bluetooth ----------//
        mgr = getContext().getSharedPreferences("MIMI1", Context.MODE_PRIVATE);

        mConnect = root.findViewById(R.id.connectTCP);
        mConnect.setOnClickListener(this);

        mTextViewInfo = root.findViewById(R.id.textInfoConnexion);

        mConnect.setText(mMainActivity.getStringBuffer("TEXT_BTN_CONNEXION_TCP", "Connexion"));
        mTextViewInfo.setText(mMainActivity.getStringBuffer("TEXT_INFO_TCP", "Veuillez vous connecter à un serveur"));
        isConnect = mMainActivity.getBooleanBuffer("IS_CONNECT_STATE", false);

        return root;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.createCallbackToParentActivity();
    }

    // 1 - Declare our interface that will be implemented by any container activity
    public interface SaveUI {
        void saveTextInfoTCP(String key, String string);
        void saveTextBtnConnexionTCP(String key, String string);
        void saveStateOfisConnect(String key, Boolean state);
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
        mCallback.saveTextBtnConnexionTCP("TEXT_BTN_CONNEXION_TCP", mConnect.getText().toString());
        mCallback.saveTextInfoTCP("TEXT_INFO_TCP", mTextViewInfo.getText().toString());
        mCallback.saveStateOfisConnect("IS_CONNECT_STATE", isConnect);
    }

    @Override
    public void onClick(View view) {
        if (view == mConnect) {
            if (isConnect == false)
                ConnexionTCP(false);
            else
                ConnexionTCP(true);
        }
    }

    public boolean ConnexionTCP(boolean Deconnect) {
        if (!isConnect && !Deconnect) {

            try {
                ADR_SERVEUR = mgr.getString("ip_ident", null);
                PORT_SERVEUR_TCP = Integer.parseInt(mgr.getString("port_ident", null));
            } catch (Exception e) {
                mTextViewInfo.setText("Veuillez saisir l'adresse IP et le port dans les paramètres");
                e.printStackTrace();
            }


            InetAddress addr = null;

            try {
                addr = InetAddress.getByName(ADR_SERVEUR);
            } catch (UnknownHostException e1) {
                mTextViewInfo.setText("Erreur de format d'adresse !");
                Temporisation(3000);
                isConnect = false;
                e1.printStackTrace();
            }

            // Creation d un socket non connecte
            SocketAddress sockaddr = new InetSocketAddress(addr, PORT_SERVEUR_TCP);
            _socket = new Socket();

            int timeout = 5000;   // 5000 millis = 5 seconds
            // Connexion du socket au serveur avec un timeout
            // If timeout occurs, SocketTimeoutException is thrown
            try {
                _socket.connect(sockaddr, timeout);
                isConnect = true;
            } catch (UnknownHostException e) {
                mTextViewInfo.setText("Serveur introuvable !");
                Temporisation(3000);
                isConnect = false;
                e.printStackTrace();
            } catch (IOException e) {
                mTextViewInfo.setText("Problème de connexion serveur !");
                Temporisation(3000);
                isConnect = false;
                e.printStackTrace();
            }

            if (isConnect) {
                isConnect = false;
                Trame = null;
                maTrame = new byte[]{'H', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, '\n'};

                //Ouverture du flux en sortie, ecriture vers serveur
                try {
                    os = _socket.getOutputStream();
                    isConnect = true;
                } catch (IOException e) {
                    mTextViewInfo.setText("Ouverture du flux de sortie impossible !");
                    Temporisation(3000);
                    isConnect = false;
                    e.printStackTrace();
                }

                //Ouverture du flux d'entrée, lecture depuis le serveur
                try {
                    is = _socket.getInputStream();
                    isConnect = true;
                } catch (IOException e) {
                    mTextViewInfo.setText("Ouverture du flux d'entrée impossible !");
                    Temporisation(3000);
                    isConnect = false;
                    e.printStackTrace();
                }

                if (isConnect) {

                   monThreadEnvoie = new ThreadEnvoi();
                    if(!monThreadEnvoie.isAlive())
                        monThreadEnvoie.start();
                    monThreadEnvoie.setIS(this.is);
                    monThreadEnvoie.setOS(this.os);
                    monThreadEnvoie.setSocket(this._socket);
                    monThreadEnvoie.setStateOfTCPconnected(true);

                    mConnect.setText("Deconnexion");
                    mTextViewInfo.setText("Connecté au serveur : " + ADR_SERVEUR);
                } else {
                    mConnect.setText("Connexion");
                    isConnect = false;
                }
            } else {
                //EnableDisable_btnQuitter(false);
                mConnect.setText("Connexion");
                isConnect = false;
            }

        } else {

            mTextViewInfo.setText("Deconnexion en cours....");

            monThreadEnvoie.setStateOfInterruptConnexion(true);

            mTextViewInfo.setText("Client déconnecté");
            mConnect.setText("Connexion");

            isConnect = false;
        }
        this.SaveUIData();
        return isConnect;
    }


//---------------------------------------------------------------------------------------------------------------------------------------
//
//        	Methode acces UI  : setter MAJ du textView Client
//
//---------------------------------------------------------------------------------------------------------------------------------------
   /* private void UpdateTxtInfo(final String chaine)
    {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mAffichageCalcul.setText(chaine);
                Trame = "";
            }
        });
    }*/

    public void Temporisation(int delai) {
        try {
            // Temporisation
            Thread.sleep(delai);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
