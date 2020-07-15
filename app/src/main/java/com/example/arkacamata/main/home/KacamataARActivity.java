package com.example.arkacamata.main.home;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.net.Uri;
import android.os.Bundle;

import com.example.arkacamata.config.Config;
import com.example.arkacamata.config.CustomArFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.example.arkacamata.R;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.AugmentedFace;
import com.google.ar.core.Frame;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.ux.AugmentedFaceNode;

import java.io.File;
import java.util.Collection;

public class KacamataARActivity extends AppCompatActivity {
    private double MIN_OPENGL_VERSION = 3.0;
    private ModelRenderable modelRenderable;
    private Texture texture;
    private boolean isAdded = false;
    private String file_3d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kacamata_a_r);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Coba Kacamata");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (!checkIsSupportedDeviceOrFinish()) {
            return;
        }

        /* Ambil ID dari kacamata */
        Bundle b = getIntent().getExtras();
        file_3d = b.getString("file_3d");

        /* Cek file di media storage apakah ada atau tidak */
        final File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Config.IMAGE_DIRECTORY_NAME);
        File file = new File(mediaStorageDir.getPath() + File.separator + file_3d);

        /* Setelah ada file maka akan di pindahkan ke CustomARFragment*/
        CustomArFragment customArFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);
        ModelRenderable.builder()
                /* Baca File dari Unity yang telah di download*/
                .setSource(this, Uri.fromFile(file))
                .build()
                .thenAccept(renderable -> {
                    modelRenderable = renderable;
                    modelRenderable.setShadowCaster(false);
                    modelRenderable.setShadowReceiver(false);
                });

        Texture.builder().setSource(this, R.drawable.makeup).build().thenAccept(texture -> this.texture = texture);

        /* Render file 3d ke AR Core */
        customArFragment.getArSceneView().setCameraStreamRenderPriority(Renderable.RENDER_PRIORITY_FIRST);
        customArFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            if (modelRenderable == null || texture == null)
                return;

            Frame frame= customArFragment.getArSceneView().getArFrame();
            Collection<AugmentedFace> augmentedFaces = frame.getUpdatedTrackables(AugmentedFace.class);
            for (AugmentedFace augmentedFace : augmentedFaces){
                if (isAdded)
                    return;

                AugmentedFaceNode augmentedFaceNode = new AugmentedFaceNode(augmentedFace);
                augmentedFaceNode.setParent(customArFragment.getArSceneView().getScene());
                augmentedFaceNode.setFaceRegionsRenderable(modelRenderable);
//                augmentedFaceNode.setFaceMeshTexture(texture);

                isAdded = true;
            }

        });
    }

    /* Cek Device apakah support AR atau tidak */
    private boolean checkIsSupportedDeviceOrFinish() {
        if (ArCoreApk.getInstance().checkAvailability(this) == ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE) {
            Toast.makeText(this, "Augmented Faces requires ARCore", Toast.LENGTH_LONG).show();
            finish();
            return false;
        }

        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        String gl_version = configurationInfo.getGlEsVersion();
        if (Double.parseDouble(gl_version) < MIN_OPENGL_VERSION){
            Toast.makeText(this, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG).show();
            finish();
            return false;
        }
        return true;
    }
}
