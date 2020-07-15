package com.example.arkacamata.config;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface UserAPIServices {
    @POST("signin")
    Call<ResponseBody> signin(@Body RequestBody file);

    @GET("home")
    Call<ResponseBody> home();

    @GET("kategori")
    Call<ResponseBody> kategori();

    @POST("kacamata")
    Call<ResponseBody> kacamata(@Body RequestBody file);

    @POST("kacamata_detail")
    Call<ResponseBody> kacamata_detail(@Body RequestBody file);

    @POST("pembelian")
    Call<ResponseBody> pembelian(@Body RequestBody file);

    @POST("pembelian_tambah")
    Call<ResponseBody> pembelian_tambah(@Body RequestBody file);

    @POST("pembelian_detail")
    Call<ResponseBody> pembelian_detail(@Body RequestBody file);

    @POST("profil_ubah")
    Call<ResponseBody> profil_ubah(@Body RequestBody file);

    @POST("profil_password")
    Call<ResponseBody> profil_password(@Body RequestBody file);

    @GET
    Call<ResponseBody> kacamata_download_3d(@Url String fileUrl);
}
