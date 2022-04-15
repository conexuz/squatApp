package com.project.squatapp;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.project.squatapp.Helper.PreferenceUtils;
import com.google.android.gms.common.annotation.KeepName;
import com.google.mlkit.common.MlKitException;
import com.project.squatapp.Helper.CameraXViewModel;
import com.project.squatapp.Helper.GraphicOverlay;
//import com.google.mlkit.vision.demo.R;
import com.project.squatapp.Helper.VisionImageProcessor;
import com.project.squatapp.Helper.PoseDetectorProcessor;
//import com.example.mocapgame.Helper.PreferenceUtils;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.util.ArrayList;
import java.util.List;

/** Live preview demo app for ML Kit APIs using CameraX. */
@KeepName
@RequiresApi(VERSION_CODES.LOLLIPOP)
public final class MainActivity extends AppCompatActivity {
    private static final String TAG = "CameraXLivePreview";
    private static final int PERMISSION_REQUESTS = 1;

    private PoseDetectorProcessor poseDetectorProcessor;

    private View decorView;

    private static final String POSE_DETECTION = "Pose Detection";

    private PreviewView previewView;
    private GraphicOverlay graphicOverlay;


    @Nullable private ProcessCameraProvider cameraProvider;
    @Nullable private Preview previewUseCase;
    @Nullable private ImageAnalysis analysisUseCase;
    @Nullable private VisionImageProcessor imageProcessor;
    private boolean needUpdateGraphicOverlayImageSourceInfo;

    private int lensFacing = CameraSelector.LENS_FACING_FRONT;
    private CameraSelector cameraSelector;

    int repNum;
    int restTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        Bundle extras = getIntent().getExtras();
        repNum = extras.getInt("repNum");
        restTime = extras.getInt("restTime");

        decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == 0){
                    decorView.setSystemUiVisibility(hideSystemBar());
                }
            }
        });

        if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
            Toast.makeText(
                    getApplicationContext(),
                    "CameraX is only supported on SDK version >=21. Current SDK version is "
                            + VERSION.SDK_INT,
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }

        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();

        setContentView(R.layout.activity_main);
        previewView = findViewById(R.id.preview_view);
        if (previewView == null) {
            Log.d(TAG, "previewView is null");
        }
        graphicOverlay = findViewById(R.id.graphic_overlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        List<String> options = new ArrayList<>();

        options.add(POSE_DETECTION);


        new ViewModelProvider(this, AndroidViewModelFactory.getInstance(getApplication()))
                .get(CameraXViewModel.class)
                .getProcessCameraProvider()
                .observe(
                        this,
                        provider -> {
                            cameraProvider = provider;
                            if (allPermissionsGranted()) {
                                bindAllCameraUseCases();
                            }
                        });

        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus){
            decorView.setSystemUiVisibility(hideSystemBar());
        }
    }

    private int hideSystemBar() {
        return View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    }

    @Override
    public void onResume() {
        super.onResume();
        bindAllCameraUseCases();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
    }

    private void bindAllCameraUseCases() {
        if (cameraProvider != null) {
            // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
            cameraProvider.unbindAll();
            bindPreviewUseCase();
            bindAnalysisUseCase();
        }
    }

    private void bindPreviewUseCase() {

        if (cameraProvider == null) {
            return;
        }
        if (previewUseCase != null) {
            cameraProvider.unbind(previewUseCase);
        }

        Preview.Builder builder = new Preview.Builder();

        previewUseCase = builder.build();
        previewUseCase.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, previewUseCase);
    }

    public static PoseDetectorOptionsBase options(Context context) {
        return new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build();
    }

    private void bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return;
        }
        if (analysisUseCase != null) {
            cameraProvider.unbind(analysisUseCase);
        }
        if (imageProcessor != null) {
            imageProcessor.stop();
        }

        try {
//            Drawable face = getResources().getDrawable(R.drawable.face, null);
//            Drawable leftHand = getResources().getDrawable(R.drawable.left_hand, null);
//            Drawable rightHand = getResources().getDrawable(R.drawable.right_hand, null);
//            Drawable leftFoot = getResources().getDrawable(R.drawable.left_foot, null);
//            Drawable rightFoot = getResources().getDrawable(R.drawable.right_foot, null);
//            Drawable leftElbowMarker = getResources().getDrawable(R.drawable.left_elbow_marker, null);
//            Drawable rightElbowMarker = getResources().getDrawable(R.drawable.right_elbow_marker, null);
//            Drawable leftHandMarker = getResources().getDrawable(R.drawable.left_hand_marker, null);
//            Drawable rightHandwMarker = getResources().getDrawable(R.drawable.right_hand_marker, null);
//            Drawable leftKneeMarker = getResources().getDrawable(R.drawable.left_knee_marker, null);
//            Drawable rightKneeMarker = getResources().getDrawable(R.drawable.right_knee_marker, null);
//            Drawable leftFootMarker = getResources().getDrawable(R.drawable.left_foot_marker, null);
//            Drawable rightFootMarker = getResources().getDrawable(R.drawable.right_foot_marker, null);

//            MediaPlayer bpm60 = MediaPlayer.create(this, R.raw.bpm60);
//            MediaPlayer followBlueHand = MediaPlayer.create(this, R.raw.follow_blue_hand);
//            MediaPlayer followRedKnee = MediaPlayer.create(this, R.raw.follow_red_knee);
//            MediaPlayer combine = MediaPlayer.create(this, R.raw.combine);
//            MediaPlayer oppohit.mp3hit.mp3site = MediaPlayer.create(this, R.raw.opposite);
//            MediaPlayer followRedfoot = MediaPlayer.create(this, R.raw.follow_red_foot);
//            MediaPlayer followBlueElbow = MediaPlayer.create(this, R.raw.follow_blue_elbow);
//            MediaPlayer calibVoice = MediaPlayer.create(this, R.raw.calib);
//            MediaPlayer howToPlayVoice = MediaPlayer.create(this, R.raw.how_to_play);


//            SoundPool sound = new SoundPool(6, AudioManager.STREAM_MUSIC,0);
//            int hitExp = sound.load(this,R.raw.hit,1);


            PoseDetectorOptionsBase poseDetectorOptions =
                    PreferenceUtils.getPoseDetectorOptionsForLivePreview(this);
//            PoseDetectorOptionsBase options = options(this);

            imageProcessor =
                    new PoseDetectorProcessor(
                            this,
                            poseDetectorOptions,
                            /* isStreamMode = */ true,
                            repNum,
                            restTime
//                            face,
//                            leftHand,
//                            rightHand,
//                            leftFoot,
//                            rightFoot
//                            calibVoice,
//                            howToPlayVoice,
//                            leftElbowMarker,
//                            rightElbowMarker,
//                            leftHandMarker,
//                            rightHandwMarker,
//                            leftKneeMarker,
//                            rightKneeMarker,
//                            leftFootMarker,
//                            rightFootMarker,
//                            bpm60,
//                            followBlueHand,
//                            followRedKnee,
//                            combine,
//                            opposite,
//                            followRedfoot,
//                            followBlueElbow,
//                            sound,
//                            hitExp
                    ) {
                    };
        } catch (Exception e) {
            Toast.makeText(
                    getApplicationContext(),
                    "Can not create image processor: " + e.getLocalizedMessage(),
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }


        ImageAnalysis.Builder builder = new ImageAnalysis.Builder();

        analysisUseCase = builder.build();

        needUpdateGraphicOverlayImageSourceInfo = true;
        analysisUseCase.setAnalyzer(
                // imageProcessor.processImageProxy will use another thread to run the detection underneath,
                // thus we can just runs the analyzer itself on main thread.
                ContextCompat.getMainExecutor(this),
                imageProxy -> {
                    if (needUpdateGraphicOverlayImageSourceInfo) {
                        boolean isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT;
                        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                        if (rotationDegrees == 0 || rotationDegrees == 180) {
                            graphicOverlay.setImageSourceInfo(
                                    imageProxy.getWidth(), imageProxy.getHeight(), isImageFlipped);
                        } else {
                            graphicOverlay.setImageSourceInfo(
                                    imageProxy.getHeight(), imageProxy.getWidth(), isImageFlipped);
                        }
                        needUpdateGraphicOverlayImageSourceInfo = false;
                    }
                    try {
                        imageProcessor.processImageProxy(imageProxy, graphicOverlay);
                    } catch (MlKitException e) {
                        Log.e(TAG, "Failed to process image. Error: " + e.getLocalizedMessage());
                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });

        cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, analysisUseCase);
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {
            bindAllCameraUseCases();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }
}
