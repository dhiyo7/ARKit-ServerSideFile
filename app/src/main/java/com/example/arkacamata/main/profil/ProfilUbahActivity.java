package com.example.arkacamata.main.profil;

import android.app.ProgressDialog;
import android.os.Bundle;

import com.example.arkacamata.config.Config;
import com.example.arkacamata.config.SharePreference;
import com.example.arkacamata.config.UserAPIServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.arkacamata.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfilUbahActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    SharePreference sharePreference;
    EditText et_nama, et_username;
    RadioGroup rg_jenis_kelamin;
    Button btn_simpan;
    String id_tb_pengguna, nama, username, jenis_kelamin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil_ubah);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharePreference = new SharePreference(this);
        HashMap<String, String> user = sharePreference.getUserDetails();
        id_tb_pengguna = user.get(sharePreference.KEY_ID_PENGGUNA);
        nama = user.get(sharePreference.KEY_NAMA);
        username = user.get(sharePreference.KEY_USERNAME);
        jenis_kelamin = user.get(sharePreference.KEY_JENIS_KELAMIN);

        getSupportActionBar().setTitle("Profil");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        et_nama = findViewById(R.id.et_nama);
        et_username = findViewById(R.id.et_username);
        rg_jenis_kelamin = findViewById(R.id.rg_jenis_kelamin);
        rg_jenis_kelamin.setOnCheckedChangeListener(this);
        btn_simpan = findViewById(R.id.btn_simpan);
        btn_simpan.setOnClickListener(this);

        et_nama.setText(nama);
        et_username.setText(username);
        if (jenis_kelamin.equals("Laki-Laki")){
            ((RadioButton)rg_jenis_kelamin.getChildAt(0)).setChecked(true);
        } else {
            ((RadioButton)rg_jenis_kelamin.getChildAt(1)).setChecked(true);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btn_simpan){
            simpan();
        }
    }

    private void simpan() {
        et_nama.setError(null);
        et_username.setError(null);
        String nama = et_nama.getText().toString();
        String username = et_username.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(username)){
            et_nama.setError("Silahkann diisi..");
            focusView = et_nama;
            cancel = true;
        } if (TextUtils.isEmpty(nama)){
            et_username.setError("Silahkann diisi..");
            focusView = et_username;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            final ProgressDialog pDialog = new ProgressDialog(this);
            pDialog.setMessage("Tunggu sebentar...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);
            builder.addFormDataPart("id_tb_pengguna",id_tb_pengguna);
            builder.addFormDataPart("nama",nama);
            builder.addFormDataPart("username",username);
            builder.addFormDataPart("jenis_kelamin",jenis_kelamin);
            MultipartBody requestBody = builder.build();

            UserAPIServices api = Config.getRetrofit(Config.URL).create(UserAPIServices.class);
            Call<ResponseBody> post = api.profil_ubah(requestBody);
            post.enqueue(new Callback<ResponseBody>(){
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    pDialog.dismiss();
                    try {
                        String json = response.body().string();
                        JSONObject jsonObj = new JSONObject(json);
                        Config.jsonArray = jsonObj.getJSONArray("result");
                        for(int i=0;i<Config.jsonArray.length();i++) {
                            JSONObject c = Config.jsonArray.getJSONObject(i);
                            String status = c.getString("status");

                            if (status.equals("1")) {
                                Toast.makeText(getApplicationContext(), "Berhasil Mengubah Profil.", Toast.LENGTH_LONG).show();
                                sharePreference.update(nama, username, jenis_kelamin);
                                onBackPressed();
                            } else {
                                Toast.makeText(getApplicationContext(), "Gagal Mengubah Profil.", Toast.LENGTH_LONG).show();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Tidak bisa mengirim data!", Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Tidak bisa mengirim data!!", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    pDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Tidak bisa mengirim data!!!", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId){
            case R.id.rb_laki2:
                jenis_kelamin = "Laki-Laki";
                break;
            case R.id.rb_perempuan:
                jenis_kelamin = "Perempuan";
                break;
        }
    }
}
