package com.project.squatapp.Helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.odml.image.MlImage;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.common.PointF3D;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.project.squatapp.R;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/** A processor to run pose detector. */
public class PoseDetectorProcessor
        extends VisionProcessorBase<PoseDetectorProcessor.PoseDet> {
    private static final String TAG = "PoseDetectorProcessor";

    private final PoseDetector detector;

    private final boolean isStreamMode;
    private final Context context;
    private final Executor classificationExecutor;

    int status = 0;
    int[] calib = {0,0,0,0};

    int calibInstruct = 0;

    int howToPlay = 1;
    int tutorial = 1;

    int voiceImport = 1;
    int upDown = 1;
    int points = 0;

    int numExercise = -1;

    int calibIdx;
    float kneeDis;

    int repDown = 0;
    int repUp = 1;
    int rep;

    int arrowKnee = 0;
    int errorCountKnee = 0;

    int arrowShoulderR = 0;
    int errorCountStand = 0;
    int arrowShoulderL = 0;

    Random rn = new Random();


    MediaPlayer calibVoice;
    MediaPlayer howToPlayVoice;
    MediaPlayer elbowSquat;
    MediaPlayer elbowToKnee;
    MediaPlayer highKnees;
    MediaPlayer jumpingJack;
    MediaPlayer tutorialMusic;
    MediaPlayer gameMusic;

    SoundPool sound;
    int hitExp;
    int soundExp;

    ArrayList<String> jointArr = new ArrayList<String>();

    int countIdx = 0;

    ArrayList<String> data = new ArrayList<String>();

    Date timeSaved;

    long timePassed;

    int startInstruct = 0;

    int circlePattern = 0;

    int ready4 = 0;

    Date timeNow;

    Date currentTime;

    int timeCounter = 0;

    int targetResolution = 100;

    int repNum;
    int restTime;
    int timer;
    int repNumSave;
    int restTimeSave;


    /** Internal class to hold Pose and classification results. */
    protected static class PoseDet {
        private final Pose pose;

        public PoseDet(Pose pose) throws IOException {
            this.pose = pose;
        }

        public Pose getPose() {
            return pose;
        }

    }

    public PoseDetectorProcessor(
            Context context,
            PoseDetectorOptionsBase options,
            boolean isStreamMode,
            int repNum,
            int restTime
    ) throws IOException {
        super(context);
        detector = PoseDetection.getClient(options);
        this.isStreamMode = isStreamMode;
        this.context = context;
        this.repNum = repNum;
        this.restTime = restTime;
        classificationExecutor = Executors.newSingleThreadExecutor();
    }

    int calcAng(PointF3D joint1, PointF3D joint2, PointF3D joint3) {
        float[] joint1_3Dxyz = {joint1.getX(), joint1.getY(), joint1.getZ()};
        float[] jointMid_3Dxyz = {joint2.getX(), joint2.getY(), joint2.getZ()};
        float[] Vec1 = new float[joint1_3Dxyz.length];
        for (int i = 0; i < joint1_3Dxyz.length; i++) {
            Vec1[i] = joint1_3Dxyz[i] - jointMid_3Dxyz[i];
        }

        float[] joint2_3Dxyz = {joint3.getX(), joint3.getY(), joint3.getZ()};
        float[] Vec2 = new float[jointMid_3Dxyz.length];
        for (int i = 0; i < jointMid_3Dxyz.length; i++) {
            Vec2[i] = joint2_3Dxyz[i] - jointMid_3Dxyz[i];
        }

        float dotVec = 0;
        for (int i = 0; i < Vec1.length; i++)
            dotVec += Vec1[i]*Vec2[i];

        float[] v = new float[Vec1.length];
        v[0] = Vec1[1]*Vec2[2]-Vec1[2]*Vec2[1];
        v[1] = Vec1[2]*Vec2[0]-Vec1[0]*Vec2[2];
        v[2] = Vec1[0]*Vec2[1]-Vec1[1]*Vec2[0];
        float crossVec = (float)Math.sqrt((v[0]*v[0])+(v[1]*v[1])+(v[2]*v[2]));

        return (int)Math.toDegrees(Math.atan2(crossVec, dotVec));
    }

    @Override
    public void processBitmap(Bitmap bitmap, GraphicOverlay graphicOverlay) {

    }

    @Override
    public void processByteBuffer(ByteBuffer data, FrameMetadata frameMetadata, GraphicOverlay graphicOverlay) throws MlKitException {

    }

    @Override
    public void stop() {
        super.stop();
        detector.close();
    }

    @Override
    protected Task<PoseDet> detectInImage(InputImage image) {
        return detector
                .process(image)
                .continueWith(
                        classificationExecutor,
                        task -> {
                            Pose pose = task.getResult();
                            GraphicOverlay overlay = null;
                            return new PoseDet(pose);
                        });
    }

    @Override
    protected Task<PoseDet> detectInImage(MlImage image) {
        return detector
                .process(image)
                .continueWith(
                        classificationExecutor,
                        task -> {
                            Pose pose = task.getResult();
                            GraphicOverlay overlay = null;
                            return new PoseDet(pose);
                        });
    }

    @Override
    protected void onSuccess(
            @NonNull PoseDet poseDet,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.add(
                new PoseGraphic(
                        graphicOverlay,
                        poseDet.pose,
                        status,
                        calib,
                        repNum,
                        arrowKnee,
                        arrowShoulderR,
                        arrowShoulderL,
                        timer
                ));


        List<PoseLandmark> landmarks = poseDet.pose.getAllPoseLandmarks();
        if (landmarks.isEmpty()) {
            return;
        }

//        Toast.makeText(context, String.valueOf(repNum),Toast.LENGTH_LONG).show();


        PoseLandmark leftWrist = poseDet.pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
        PoseLandmark rightWrist = poseDet.pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
        PoseLandmark leftKnee = poseDet.pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark rightKnee = poseDet.pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
        PoseLandmark leftHip = poseDet.pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark rightHip = poseDet.pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        PoseLandmark leftShoulder = poseDet.pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark rightShoulder = poseDet.pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark leftElbow = poseDet.pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
        PoseLandmark rightElbow = poseDet.pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
        PoseLandmark leftAnkle = poseDet.pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
        PoseLandmark rightAnkle = poseDet.pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);
        PoseLandmark leftToe = poseDet.pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX);
        PoseLandmark rightToe = poseDet.pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX);

        float leftWristX = graphicOverlay.getWidth() - ((graphicOverlay.scaleFactor*leftWrist.getPosition().x) - graphicOverlay.postScaleWidthOffset);
        float leftWristY = (graphicOverlay.scaleFactor*leftWrist.getPosition().y) - graphicOverlay.postScaleHeightOffset;

        float rightWristX = graphicOverlay.getWidth() - ((graphicOverlay.scaleFactor*rightWrist.getPosition().x) - graphicOverlay.postScaleWidthOffset);
        float rightWristY = (graphicOverlay.scaleFactor*rightWrist.getPosition().y) - graphicOverlay.postScaleHeightOffset;

        float leftKneeX = graphicOverlay.getWidth() - ((graphicOverlay.scaleFactor*leftKnee.getPosition().x) - graphicOverlay.postScaleWidthOffset);
        float leftKneeY = (graphicOverlay.scaleFactor*leftKnee.getPosition().y) - graphicOverlay.postScaleHeightOffset;

        float rightKneeX = graphicOverlay.getWidth() - ((graphicOverlay.scaleFactor*rightKnee.getPosition().x) - graphicOverlay.postScaleWidthOffset);
        float rightKneeY = (graphicOverlay.scaleFactor*rightKnee.getPosition().y) - graphicOverlay.postScaleHeightOffset;

        float leftHipX = graphicOverlay.getWidth() - ((graphicOverlay.scaleFactor*leftHip.getPosition().x) - graphicOverlay.postScaleWidthOffset);
        float leftHipY = (graphicOverlay.scaleFactor*leftHip.getPosition().y) - graphicOverlay.postScaleHeightOffset;

        float rightHipX = graphicOverlay.getWidth() - ((graphicOverlay.scaleFactor*rightHip.getPosition().x) - graphicOverlay.postScaleWidthOffset);
        float rightHipY = (graphicOverlay.scaleFactor*rightHip.getPosition().y) - graphicOverlay.postScaleHeightOffset;

        float leftShoulderX = graphicOverlay.getWidth() - ((graphicOverlay.scaleFactor*leftShoulder.getPosition().x) - graphicOverlay.postScaleWidthOffset);
        float leftShoulderY = (graphicOverlay.scaleFactor*leftShoulder.getPosition().y) - graphicOverlay.postScaleHeightOffset;

        float rightShoulderX = graphicOverlay.getWidth() - ((graphicOverlay.scaleFactor*rightShoulder.getPosition().x) - graphicOverlay.postScaleWidthOffset);
        float rightShoulderY = (graphicOverlay.scaleFactor*rightShoulder.getPosition().y) - graphicOverlay.postScaleHeightOffset;

        float leftElbowX = graphicOverlay.getWidth() - ((graphicOverlay.scaleFactor*leftElbow.getPosition().x) - graphicOverlay.postScaleWidthOffset);
        float leftElbowY = (graphicOverlay.scaleFactor*leftElbow.getPosition().y) - graphicOverlay.postScaleHeightOffset;

        float rightElbowX = graphicOverlay.getWidth() - ((graphicOverlay.scaleFactor*rightElbow.getPosition().x) - graphicOverlay.postScaleWidthOffset);
        float rightElbowY = (graphicOverlay.scaleFactor*rightElbow.getPosition().y) - graphicOverlay.postScaleHeightOffset;

        float leftAnkleX = graphicOverlay.getWidth() - ((graphicOverlay.scaleFactor*leftAnkle.getPosition().x) - graphicOverlay.postScaleWidthOffset);
        float leftAnkleY = (graphicOverlay.scaleFactor*leftAnkle.getPosition().y) - graphicOverlay.postScaleHeightOffset;

        float rightAnkleX = graphicOverlay.getWidth() - ((graphicOverlay.scaleFactor*rightAnkle.getPosition().x) - graphicOverlay.postScaleWidthOffset);
        float rightAnkleY = (graphicOverlay.scaleFactor*rightAnkle.getPosition().y) - graphicOverlay.postScaleHeightOffset;

        float leftToeX = graphicOverlay.getWidth() - ((graphicOverlay.scaleFactor*leftToe.getPosition().x) - graphicOverlay.postScaleWidthOffset);
        float leftToeY = (graphicOverlay.scaleFactor*leftToe.getPosition().y) - graphicOverlay.postScaleHeightOffset;

        float rightToeX = graphicOverlay.getWidth() - ((graphicOverlay.scaleFactor*rightToe.getPosition().x) - graphicOverlay.postScaleWidthOffset);
        float rightToeY = (graphicOverlay.scaleFactor*rightToe.getPosition().y) - graphicOverlay.postScaleHeightOffset;


        PointF3D leftHip3D = leftHip.getPosition3D();
        PointF3D leftKnee3D = leftKnee.getPosition3D();
        PointF3D leftAnkle3D = leftAnkle.getPosition3D();

        PointF3D rightHip3D = rightHip.getPosition3D();
        PointF3D rightKnee3D = rightKnee.getPosition3D();
        PointF3D rightAnkle3D = rightAnkle.getPosition3D();

        int left_jointAng = calcAng(leftHip3D,leftKnee3D,leftAnkle3D);
        int right_jointAng = calcAng(leftHip3D,leftKnee3D,leftAnkle3D);


        if (voiceImport == 1) {
//            calibVoice = MediaPlayer.create(context, R.raw.calib);
//            howToPlayVoice = MediaPlayer.create(context, R.raw.how_to_play);
//            elbowSquat =  MediaPlayer.create(context, R.raw.elbow_squat);
//            elbowToKnee = MediaPlayer.create(context,R.raw.elbow_to_knee);
//            highKnees = MediaPlayer.create(context,R.raw.high_knees);
//            jumpingJack = MediaPlayer.create(context,R.raw.jumping_jack);
//            tutorialMusic = MediaPlayer.create(context,R.raw.tutorial_music);
//            gameMusic = MediaPlayer.create(context,R.raw.game_music);
//
//            sound = new SoundPool(6, AudioManager.STREAM_MUSIC,0);
//            hitExp = sound.load(context,R.raw.hit,1);
//            soundExp = sound.load(context,R.raw.sound,1);
            voiceImport = 2;
        }

        if (status == 0) {

            if (Math.abs((leftShoulderX-100) - leftAnkleX) <= 100 &&
                    Math.abs(2100 - leftAnkleY) <= 300) {
                calib[0] = 1;
            } else {
                calib[0] = 0;
            }

            if (Math.abs((rightShoulderX+100) - rightAnkleX) <= 100 &&
                    Math.abs(2100 - rightAnkleY) <= 300) {
                calib[1] = 1;
            } else {
                calib[1] = 0;
            }

            int sumUp = calib[0]+calib[1]+calib[2]+calib[3];

            if (sumUp != 2) {
                status = 0;
                calibIdx = 0;
            } else {
                calibIdx += 1;
            }

//            if (calibIdx == 20){
//                sound.play(stayExp,1.0F,1.0F,0,0,1.0F);
//            }

            if (status == 0 && calibIdx >= 70){
                kneeDis = Math.abs(rightKneeX-leftKneeX);
                status = 1;
                repNumSave = repNum;
            }
        }

        if (status == 1){
            countIdx += 1;

//            if (left_jointAng <= 40 && right_jointAng <= 40 && repDown == 0) {
//                sound.play(perfectExp,1.0F,1.0F,0,0,1.0F);
//            }

            if (left_jointAng <= 40 && right_jointAng <= 40 && repDown == 0) {
                repDown = 1;
                repUp = 0;
            }

            if (left_jointAng >= 120 && right_jointAng >= 120 && repUp == 0) {
                repDown = 0;
                repUp = 1;
                repNum -= 1;
            }

//            if (left_jointAng >= 40 && left_jointAng <= 100 && right_jointAng >= 40 && right_jointAng <= 100) {
//                count80_100 += 1;
//            }
//
//            if (count80_100 == 20) {
//                sound.play(lowerExp,1.0F,1.0F,0,0,1.0F);
//                count80_100 = 0;
//            }


            int currentKneeDis = (int) Math.abs(leftKneeX-rightKneeX);

            if (currentKneeDis < kneeDis-20){
                arrowKnee = 1;
                errorCountKnee += 1;
            } else {
                arrowKnee = 0;
            }

            if (rightShoulderX > rightToeX) {
                arrowShoulderR = 1;
                errorCountStand += 1;
            } else{
                arrowShoulderR = 0;
            }

            if (leftShoulderX < leftToeX) {
                arrowShoulderL = 1;
                errorCountStand += 1;
            }else{
                arrowShoulderL = 0;
            }

//            if (errorCountStand == 50) {
//                sound.play(standStraightExp,1.0F,1.0F,0,0,1.0F);
//                errorCountStand = 0;
//            }
//
//            if (errorCountKnee == 50) {
//                sound.play(kneeExp,1.0F,1.0F,0,0,1.0F);
//                errorCountKnee = 0;
//            }

            if (repNum == 0) {
                status = 2;
            }
        }

        if (status == 2) {
            if (timeCounter == 0) {
                timeSaved = Calendar.getInstance().getTime();
                timeCounter = 1;
            }
            timeNow = Calendar.getInstance().getTime();

            timePassed = timeNow.getTime()/1000 - timeSaved.getTime()/1000;

            timer = restTime - (int)timePassed;

            if (timer == 0) {
                status = 1;
                repNum = repNumSave;
            }

//            Toast.makeText(context,String.valueOf(timer),Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Pose detection failed!", e);
    }

    @Override
    protected boolean isMlImageEnabled(Context context) {
        // Use MlImage in Pose Detection by default, change it to OFF to switch to InputImage.
        return true;
    }
}

