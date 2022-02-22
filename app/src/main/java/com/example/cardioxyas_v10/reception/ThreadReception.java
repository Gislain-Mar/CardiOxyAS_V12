package com.example.cardioxyas_v10.reception;

import android.text.BoringLayout;

import com.example.cardioxyas_v10.MainActivity;
import com.example.cardioxyas_v10.envoi.ThreadEnvoi;

import java.io.IOException;
import java.io.InputStream;

public class ThreadReception extends Thread {

    private ReceptionFragment monReceptionFragment = new ReceptionFragment();
    private ThreadEnvoi monThreadEnvoi;
    private MainActivity mMainActivity = new MainActivity();

    private InputStream is;
    private byte buffer[] = new byte[100];
    private int bytes_read;
    private boolean ThreadReceptionStop = true;
    private String Trame = "";

    public void run() {
        System.out.println(this.getName());
        mMainActivity.setmonThreadReception(this);
        while (true) {
            if(mMainActivity.getmonThreadEnvoi() != null)
            {
                monThreadEnvoi = mMainActivity.getmonThreadEnvoi();
            }else{
                monThreadEnvoi = null;
            }

            if (is != null && ThreadReceptionStop == false) {
                try {
                    do {
                        this.bytes_read = is.read(buffer, 0, 1);
                    } while (buffer[0] != 87);
                    this.bytes_read = is.read(buffer, 1, 3);
                    Trame = Trame + (int) buffer[0] + "/" + (int) buffer[1] + "/" + buffer[2];
                    //getActivity().runOnUiThread(AffichageDonnees);
                    monReceptionFragment.setTextOfTextFreqCard(buffer[1] + "");
                    monReceptionFragment.setTextOfTextOxygene(buffer[2] + "");
                    if(monThreadEnvoi != null) {
                        if (monThreadEnvoi.getStateOfTCPconected()) {
                            monThreadEnvoi.setTextofTrame(buffer[1] + "/" + buffer[2]);
                            monThreadEnvoi.setStateOfThreadEnvoiStop(false);
                        }
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    public void setIS(InputStream _is){
        this.is = _is;
    }

    public void setStateOfThreadReceptionStop(Boolean state){
        this.ThreadReceptionStop = state;
    }

    public void setMonThreadEnvoi(Thread thread){
        this.monThreadEnvoi = (ThreadEnvoi) thread;
    }
}
