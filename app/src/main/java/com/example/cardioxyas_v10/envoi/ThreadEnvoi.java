package com.example.cardioxyas_v10.envoi;

import com.example.cardioxyas_v10.MainActivity;
import com.example.cardioxyas_v10.reception.ReceptionFragment;
import com.example.cardioxyas_v10.reception.ThreadReception;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ThreadEnvoi extends Thread{

    private ThreadReception monThreadReception;
    private MainActivity mMainActivity = new MainActivity();

    private InputStream is;
    private OutputStream os;
    private Socket socket;
    private Boolean ThreadEnvoiStop = true;
    private Boolean InterruptConnexion = false;
    private Boolean TCPconnected = false;
    private String maTrame;

    public void run() {
        System.out.println(monThreadReception);
        MainActivity.setmonThreadEnvoi(this);
        while (true) {
            if(mMainActivity.getMonThreadReception() != null)
            {
                monThreadReception = mMainActivity.getMonThreadReception();
            }else{
                monThreadReception = null;
            }

            if(InterruptConnexion == true && TCPconnected == true)
            {
                maTrame = "Cmd ClientDeConnecte\n";

                try {
                    os.write(maTrame.getBytes(), 0, maTrame.getBytes().length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Temporisation(1000);

                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    is.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                try {
                    socket.close();
                } catch (Exception e) {
                    //Fin de l'application avec une erreur de connexion car le serveur a deja ferme la connexion
                    e.printStackTrace();
                }
                MainActivity.setmonThreadEnvoi(null);
                this.InterruptConnexion = false;
            }
            else if(ThreadEnvoiStop == false && TCPconnected == true)
            {
                this.maTrame = this.maTrame + "/" + mMainActivity.getLocation();
                sendData(maTrame.getBytes());
                this.ThreadEnvoiStop = true;
            }
        }
    }

    public void setIS(InputStream _is){
        this.is = _is;
    }

    public void setOS(OutputStream _os){
        this.os = _os;
    }

    public void setSocket(Socket _socket){
        this.socket = _socket;
    }

    public void setStateOfThreadEnvoiStop(Boolean state){
        ThreadEnvoiStop = state;
    }

    public void setStateOfTCPconnected(Boolean state){
        this.TCPconnected = state;
    }

    public Boolean getStateOfTCPconected(){
        return this.TCPconnected;
    }

    public void setStateOfInterruptConnexion(Boolean state){
        this.InterruptConnexion = state;
    }

    public void setTextofTrame(String chaine){
        maTrame = chaine;
    }

    public void sendData(byte[] cmd){
        if(TCPconnected){
            try {
                os.write(cmd);
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void Temporisation(int delai) {
        try {
            // Temporisation
            Thread.sleep(delai);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
