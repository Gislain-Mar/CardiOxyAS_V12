package com.example.cardioxyas_v10.settings;

/* prefs.java  */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

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
public class Settings_pref {
    //1 Objet SharedPreferences
    private SharedPreferences _prefs = null;
    //2 Definition gerant
    private Editor _editor = null;
    //3 Valeurs par defaut
    private String _bluetooth_ident = "Inconnu";

    //4 Peuplement de SharedPreferences
    public Settings_pref(Context context) {
        this._prefs = context.getSharedPreferences("MIMI1", Context.MODE_PRIVATE);
        this._editor = this._prefs.edit();
    }
    //5 Methodes generiques de lecture et ecriture
    public String getValue(String key, String defaultvalue) {
        if (this._prefs == null) {
            return "Inconnu";
        }
        return this._prefs.getString(key, defaultvalue);
    }
    public void setValue(String key, String value) {
        if (this._editor == null) {
            return;
        }
        this._editor.putString(key, value);
    }
    //6 Extraction adresse mail
    public String getBluetoothIdent() {
        if (this._prefs == null) {
            return "Inconnu";
        }
        this._bluetooth_ident = this._prefs.getString("bluetooth_ident", "Unknown");
        return this._bluetooth_ident;
    }
    //7 Ecriture adresse mail
    public void setBluetoothIdent(String newemail) {
        if (this._editor == null) {
            return;
        }
        this._editor.putString("bluetooth_ident", newemail);
    }
    //8 Sauvegarde des parametres
    public void save() {
        if (this._editor == null) {
            return;
        }
        this._editor.commit();
    }
}


