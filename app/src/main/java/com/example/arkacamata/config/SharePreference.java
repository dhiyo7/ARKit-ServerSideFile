package com.example.arkacamata.config;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.arkacamata.SignInActivity;

import java.util.HashMap;

public class SharePreference {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "ar_kacamata";
    private static final String IS_LOGIN = "LOGIN";
    public static final String KEY_ID_PENGGUNA = "id_tb_pengguna";
    public static final String KEY_NAMA = "nama";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_JENIS_KELAMIN = "jenis_kelamin";

    public SharePreference(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void create_session(String id, String nama, String username, String jenis_kelamin){
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_ID_PENGGUNA, id);
        editor.putString(KEY_NAMA, nama);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_JENIS_KELAMIN, jenis_kelamin);
        editor.commit();
    }

    public void update(String nama, String username, String jenis_kelamin){
        editor.putString(KEY_NAMA, nama);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_JENIS_KELAMIN, jenis_kelamin);
        editor.commit();
    }
    public void logoutUser(){
        editor.clear();
        editor.commit();

        Intent i = new Intent(_context, SignInActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(i);
    }

    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();

        user.put(KEY_ID_PENGGUNA, pref.getString(KEY_ID_PENGGUNA, null));
        user.put(KEY_NAMA, pref.getString(KEY_NAMA, null));
        user.put(KEY_USERNAME, pref.getString(KEY_USERNAME, null));
        user.put(KEY_JENIS_KELAMIN, pref.getString(KEY_JENIS_KELAMIN, null));
        return user;
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }
}
