package com.example.arkacamata.main.home;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.arkacamata.config.Config;
import com.example.arkacamata.config.Download;
import com.example.arkacamata.config.DownloadService;
import com.example.arkacamata.config.UserAPIServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.arkacamata.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KacamataDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private String id_tb_kacamata, nama_kacamata, harga_kacamata, nama_kategori, deskripsi_kacamata, file_3d = null, foto_kacamata;
    private ImageView imv;
    private TextView tv_nama_kacamata, tv_harga_kacamata, tv_kategori_kacamata, tv_deskripsi_kacamata;
    private Button btn_lihat, btn_beli, btn_download;
    private LinearLayout ln_file, ln_download;
    public static final String MESSAGE_PROGRESS = "message_progress";
    private static final int PERMISSION_REQUEST_CODE = 1;
    private ProgressBar mProgressBar;
    private TextView mProgressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kacamata_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle b = getIntent().getExtras();
        id_tb_kacamata = b.getString("id_tb_kacamata");

        getSupportActionBar().setTitle("Detail Kacamata");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        tv_nama_kacamata = findViewById(R.id.tv_nama_kacamata);
        tv_harga_kacamata = findViewById(R.id.tv_harga_kacamata);
        tv_kategori_kacamata = findViewById(R.id.tv_kategori_kacamata);
        tv_deskripsi_kacamata = findViewById(R.id.tv_deskripsi_kacamata);
        mProgressBar = findViewById(R.id.progress);
        mProgressText = findViewById(R.id.progress_text);

        ln_file = findViewById(R.id.ln_file);
        ln_download = findViewById(R.id.ln_download);
        imv = findViewById(R.id.imv);
        btn_beli = findViewById(R.id.btn_beli);
        btn_lihat = findViewById(R.id.btn_lihat);
        btn_download = findViewById(R.id.btn_download);
        btn_beli.setOnClickListener(this);
        btn_lihat.setOnClickListener(this);
        btn_download.setOnClickListener(this);

        registerReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();
    }

    private void getData() {
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Tunggu sebentar...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("id_tb_kacamata",id_tb_kacamata);
        MultipartBody requestBody = builder.build();

        /* Pemanggilan API dari Backend */
        UserAPIServices api = Config.getRetrofit(Config.URL).create(UserAPIServices.class);
        Call<ResponseBody> post = api.kacamata_detail(requestBody);
        post.enqueue(new Callback<ResponseBody>(){
            /* Jika respone dari server / backend sukses maka akan di set di tiap tiap tag xml untuk
            * di tampilkan datanya */
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                pDialog.dismiss();
                try {
                    String json = response.body().string();
                    JSONObject jsonObj = new JSONObject(json);
                    Config.jsonArray = jsonObj.getJSONArray("result");
                    for(int i=0;i<Config.jsonArray.length();i++) {
                        JSONObject c = Config.jsonArray.getJSONObject(i);
                        nama_kacamata    = c.getString("nama_kacamata");
                        harga_kacamata    = c.getString("harga_kacamata");
                        nama_kategori   = c.getString("nama_kategori");
                        deskripsi_kacamata   = c.getString("deskripsi_kacamata");
                        file_3d   = c.getString("file_3d");
                        foto_kacamata   = c.getString("foto_kacamata");

                        tv_nama_kacamata.setText(nama_kacamata);
                        tv_harga_kacamata.setText(harga_kacamata);
                        tv_kategori_kacamata.setText(nama_kategori);
                        tv_deskripsi_kacamata.setText(deskripsi_kacamata);

                        Glide.with(KacamataDetailActivity.this)
                                .load(Config.URL_FOTO_KACAMATA+foto_kacamata)
                                .thumbnail(0.5f)
                                .into(imv);

                        setDownloadInvoice();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(KacamataDetailActivity.this, "Tidak bisa mengirim data!", Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(KacamataDetailActivity.this, "Tidak bisa mengirim data!!", Toast.LENGTH_LONG).show();
                }
            }

            /* Jika respone dari server / backend tidak sukses maka akan tampil pesan error */
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                pDialog.dismiss();
                Toast.makeText(KacamataDetailActivity.this, "Internet Gagal", Toast.LENGTH_LONG).show();
            }
        });
    }

    /* tombol dibawah akan di eksekusi misal lihat ya bakal di arahin ke KacamataAR selanjutnya
    * */
    @Override
    public void onClick(View v) {
        if (v == btn_beli){

        } else if (v == btn_lihat){
            Intent intent = new Intent(this, KacamataARActivity.class);
            intent.putExtra("file_3d",file_3d);
            startActivity(intent);
        } else if (v == btn_download){
            if(checkPermission()){
                startDownload();
            } else {
                requestPermission();
            }
        }
    }

    private void setDownloadInvoice(){
        if (!file_3d.isEmpty()){
            final File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Config.IMAGE_DIRECTORY_NAME);
            final File mediaFile = new File(mediaStorageDir.getPath() + File.separator + file_3d);
            if (mediaStorageDir.exists()) {
                if (mediaFile.exists()) {
                    ln_download.setVisibility(View.GONE);
                    ln_file.setVisibility(View.VISIBLE);
                    Log.d("catatan","file ada");
                } else {
                    ln_download.setVisibility(View.VISIBLE);
                    ln_file.setVisibility(View.GONE);
                    Log.d("catatan","file tidak ada");
                }
            } else {
                Log.d("catatan","direktori tidak ada");
                if(!mediaStorageDir.exists()) {
                    mediaStorageDir.mkdirs();
                    Log.d("catatan","direktori dibuat");
                }
            }
        } else {
            ln_download.setVisibility(View.GONE);
            ln_file.setVisibility(View.GONE);
        }
    }

    private void registerReceiver() {
        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MESSAGE_PROGRESS);
        bManager.registerReceiver(broadcastReceiver, intentFilter);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(MESSAGE_PROGRESS)){
                Download download = intent.getParcelableExtra("download");
                mProgressBar.setProgress(download.getProgress());
                if(download.getProgress() == 100){
                    mProgressText.setText("File Download Complete");
                    ln_file.setVisibility(View.VISIBLE);
                    ln_download.setVisibility(View.GONE);
                } else {
                    mProgressText.setText(String.format("Downloaded (%d/%d) MB",download.getCurrentFileSize(),download.getTotalFileSize()));
                }
            }
        }
    };

    /* memberikan akses untuk file manager */
    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
    }

    /* Function download */
    private void startDownload(){
        ln_download.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra("file_3d",file_3d);
        startService(intent);
    }

    /* Jika di berikan akses maka akan mengeksekusi donwload jika tidak makan akan keluar pesan */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startDownload();
                } else {
                    Toast.makeText(KacamataDetailActivity.this,"Permission Denied, Please allow to proceed !", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
