package com.project.squatapp.Helper;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import com.google.mlkit.vision.common.PointF3D;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.project.squatapp.Helper.GraphicOverlay.Graphic;
import com.project.squatapp.R;

import java.util.List;

/** Draw the detected pose in preview. */
public class PoseGraphic extends Graphic {

    private static final float DOT_RADIUS = 10.0f;
    private static final float STROKE_WIDTH = 5.0f;

//    public Resources resources;

    private final Pose pose;
    private final Paint leftPaint;
    private final Paint rightPaint;
    private final Paint whitePaint;
    private final Paint greenPaint;
    private final Paint wCirclePaint;
    private final Paint pointsText;
    private final Paint blackBox;
    private final Paint whiteLine;
    private final Paint greenBar;
    private final Paint redBar;

    private final Paint whiteText;
    private final Paint restText;

    private final Paint redPaint;
    private final Paint bluePaint;
    private final Paint yellowPaint;

    private final Paint textPaint;

    int status;
    int[] calib;

    Drawable arrow_RL;
    Drawable arrow_LR;
    Drawable rightHand;
    Drawable leftFoot;
    Drawable rightFoot;

    int upDown;
    int points;

    long timePassed;

    int rep;
    int arrowKnee;
    int arrowShoulderR;
    int arrowShoulderL;

    int timer;


    PoseGraphic(
            GraphicOverlay overlay,
            Pose pose,
            int status,
            int[] calib,
//            Drawable face,
//            Drawable leftHand,
//            Drawable rightHand,
//            Drawable leftFoot,
//            Drawable rightFoot,
//            Drawable leftElbowMarker,
//            Drawable rightElbowMarker,
//            Drawable leftHandMarker,
//            Drawable rightHandwMarker,
//            Drawable leftKneeMarker,
//            Drawable rightKneeMarker,
//            Drawable leftFootMarker,
//            Drawable rightFootMarker,
            int rep,
            int arrowKnee,
            int arrowShoulderR,
            int arrowShoulderL,
            int timer
    )
    {
        super(overlay);
        this.pose = pose;
        this.status = status;
        this.calib = calib;
        this.rep = rep;
        this.arrowKnee = arrowKnee;
        this.arrowShoulderR = arrowShoulderR;
        this.arrowShoulderL = arrowShoulderL;
        this.timer = timer;

        whitePaint = new Paint();
        whitePaint.setStrokeWidth(STROKE_WIDTH);
        whitePaint.setColor(Color.WHITE);
        textPaint = new Paint();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.GREEN);
        textPaint.setTextSize(300);
        greenPaint = new Paint();
        greenPaint.setStrokeWidth(50);
        greenPaint.setColor(Color.GREEN);
        redPaint = new Paint();
        redPaint.setStrokeWidth(50);
        redPaint.setColor(Color.RED);
        bluePaint = new Paint();
        bluePaint.setStrokeWidth(50);
        bluePaint.setColor(Color.BLUE);
        yellowPaint = new Paint();
        yellowPaint.setStrokeWidth(50);
        yellowPaint.setColor(Color.YELLOW);


        wCirclePaint = new Paint();
        wCirclePaint.setStyle(Paint.Style.STROKE);
        wCirclePaint.setStrokeWidth(15);
        wCirclePaint.setColor(Color.WHITE);
        pointsText = new Paint();
        pointsText.setStyle(Paint.Style.FILL);
        pointsText.setColor(Color.RED);
        pointsText.setTextSize(100);
        blackBox = new Paint();
        blackBox.setStrokeWidth(STROKE_WIDTH);
        blackBox.setColor(Color.BLACK);
        blackBox.setAlpha(200);
        whiteLine = new Paint();
        whiteLine.setStrokeWidth(20);
        whiteLine.setColor(Color.WHITE);
        greenBar = new Paint();
        greenBar.setStrokeWidth(STROKE_WIDTH);
        greenBar.setColor(Color.GREEN);
        redBar = new Paint();
        redBar.setStrokeWidth(STROKE_WIDTH);
        redBar.setColor(Color.RED);
        whiteText = new Paint();
        whiteText.setStyle(Paint.Style.FILL);
        whiteText.setColor(Color.WHITE);
        whiteText.setTextSize(100);
        restText = new Paint();
        restText.setStyle(Paint.Style.FILL);
        restText.setColor(Color.WHITE);
        restText.setTextSize(150);

        leftPaint = new Paint();
        leftPaint.setStrokeWidth(STROKE_WIDTH);
        leftPaint.setColor(Color.RED);
        rightPaint = new Paint();
        rightPaint.setStrokeWidth(STROKE_WIDTH);
        rightPaint.setColor(Color.BLUE);
    }

    @Override
    public void draw(Canvas canvas) {
        List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
        if (landmarks.isEmpty()) {
            return;
        }


        // Draw all the points
//        for (PoseLandmark landmark : landmarks) {
//            drawPoint(canvas, landmark, greenPaint);
//        }


        arrow_RL = getApplicationContext().getDrawable(R.drawable.arrow_rl_no_bg);
        arrow_LR = getApplicationContext().getDrawable(R.drawable.arrow_lr_no_bg);
//        rightHand = getApplicationContext().getDrawable(R.drawable.right_hand);
//        leftFoot = getApplicationContext().getDrawable(R.drawable.left_foot);
//        rightFoot = getApplicationContext().getDrawable(R.drawable.right_foot);


        PoseLandmark nose = pose.getPoseLandmark(PoseLandmark.NOSE);
        PoseLandmark lefyEyeInner = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_INNER);
        PoseLandmark lefyEye = pose.getPoseLandmark(PoseLandmark.LEFT_EYE);
        PoseLandmark leftEyeOuter = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_OUTER);
        PoseLandmark rightEyeInner = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_INNER);
        PoseLandmark rightEye = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE);
        PoseLandmark rightEyeOuter = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_OUTER);
        PoseLandmark leftEar = pose.getPoseLandmark(PoseLandmark.LEFT_EAR);
        PoseLandmark rightEar = pose.getPoseLandmark(PoseLandmark.RIGHT_EAR);
        PoseLandmark leftMouth = pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH);
        PoseLandmark rightMouth = pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH);

        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
        PoseLandmark rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
        PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
        PoseLandmark rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
        PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
        PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);

        PoseLandmark leftPinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY);
        PoseLandmark rightPinky = pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY);
        PoseLandmark leftIndex = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX);
        PoseLandmark rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX);
        PoseLandmark leftThumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB);
        PoseLandmark rightThumb = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB);
        PoseLandmark leftHeel = pose.getPoseLandmark(PoseLandmark.LEFT_HEEL);
        PoseLandmark rightHeel = pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL);
        PoseLandmark leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX);
        PoseLandmark rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX);


        // Face
//        drawLine(canvas, nose, lefyEyeInner, whitePaint);
//        drawLine(canvas, lefyEyeInner, lefyEye, whitePaint);
//        drawLine(canvas, lefyEye, leftEyeOuter, whitePaint);
//        drawLine(canvas, leftEyeOuter, leftEar, whitePaint);
//        drawLine(canvas, nose, rightEyeInner, whitePaint);
//        drawLine(canvas, rightEyeInner, rightEye, whitePaint);
//        drawLine(canvas, rightEye, rightEyeOuter, whitePaint);
//        drawLine(canvas, rightEyeOuter, rightEar, whitePaint);
//        drawLine(canvas, leftMouth, rightMouth, whitePaint);

//        drawLine(canvas, leftShoulder, rightShoulder, whitePaint);
//        drawLine(canvas, leftHip, rightHip, whitePaint);

        // Left body
        drawLine(canvas, leftShoulder, leftElbow, leftPaint);
        drawLine(canvas, leftElbow, leftWrist, leftPaint);
        drawLine(canvas, leftShoulder, leftHip, leftPaint);
        drawLine(canvas, leftHip, leftKnee, leftPaint);
        drawLine(canvas, leftKnee, leftAnkle, leftPaint);
        drawLine(canvas, leftWrist, leftThumb, leftPaint);
        drawLine(canvas, leftWrist, leftPinky, leftPaint);
        drawLine(canvas, leftWrist, leftIndex, leftPaint);
        drawLine(canvas, leftIndex, leftPinky, leftPaint);
        drawLine(canvas, leftAnkle, leftHeel, leftPaint);
        drawLine(canvas, leftHeel, leftFootIndex, leftPaint);

        // Right body
        drawLine(canvas, rightShoulder, rightElbow, rightPaint);
        drawLine(canvas, rightElbow, rightWrist, rightPaint);
        drawLine(canvas, rightShoulder, rightHip, rightPaint);
        drawLine(canvas, rightHip, rightKnee, rightPaint);
        drawLine(canvas, rightKnee, rightAnkle, rightPaint);
        drawLine(canvas, rightWrist, rightThumb, rightPaint);
        drawLine(canvas, rightWrist, rightPinky, rightPaint);
        drawLine(canvas, rightWrist, rightIndex, rightPaint);
        drawLine(canvas, rightIndex, rightPinky, rightPaint);
        drawLine(canvas, rightAnkle, rightHeel, rightPaint);
        drawLine(canvas, rightHeel, rightFootIndex, rightPaint);

        PointF3D leftKnee3D = leftKnee.getPosition3D();
        PointF3D leftHip3D = leftHip.getPosition3D();
        PointF3D leftShoulder3D = leftShoulder.getPosition3D();

        PointF3D rightKnee3D = rightKnee.getPosition3D();
        PointF3D rightHip3D = rightHip.getPosition3D();
        PointF3D rightShoulder3D = rightShoulder.getPosition3D();

        Paint countText = new Paint();
        countText.setTextSize(100);
        countText.setColor(Color.GREEN);

        if (status == 0) {
            canvas.drawLine(translateX(leftShoulder.getPosition().x),2200,
                    translateX(leftShoulder.getPosition().x)-200,2200,redPaint);
            canvas.drawLine(translateX(rightShoulder.getPosition().x),2200,
                    translateX(rightShoulder.getPosition().x)+200,2200,redPaint);

            if (calib[0] == 1) {
                canvas.drawLine(translateX(leftShoulder.getPosition().x),2200,
                        translateX(leftShoulder.getPosition().x)-200,2200,greenPaint);
            }

            if (calib[1] == 1) {
                canvas.drawLine(translateX(rightShoulder.getPosition().x),2200,
                        translateX(rightShoulder.getPosition().x)+200,2200,greenPaint);
            }

            if (calib[0] == 1 && calib[1] == 1) {
                canvas.drawText("Stay there",10, 300, countText);
            }

        }

        if (status == 1) {

            if (arrowKnee == 1) {
                arrow_RL.setBounds((int)translateX(leftKnee.getPosition().x)-200,(int)translateY(leftKnee.getPosition().y),
                        (int)translateX(leftKnee.getPosition().x),(int)translateY(leftKnee.getPosition().y)+100);
                arrow_RL.draw(canvas);

                arrow_LR.setBounds((int)translateX(rightKnee.getPosition().x),(int)translateY(rightKnee.getPosition().y),
                        (int)translateX(rightKnee.getPosition().x)+200,(int)translateY(rightKnee.getPosition().y)+100);
                arrow_LR.draw(canvas);
            }

            if (arrowShoulderR == 1) {
                arrow_RL.setBounds((int)translateX(rightShoulder.getPosition().x),(int)translateY(rightShoulder.getPosition().y),
                        (int)translateX(rightShoulder.getPosition().x)+200,(int)translateY(rightShoulder.getPosition().y)+100);
                arrow_RL.draw(canvas);
            }

            if (arrowShoulderL == 1) {
                arrow_LR.setBounds((int)translateX(leftShoulder.getPosition().x)-200,(int)translateY(leftShoulder.getPosition().y),
                        (int)translateX(leftShoulder.getPosition().x),(int)translateY(leftShoulder.getPosition().y)+100);
                arrow_LR.draw(canvas);
            }

//            if (rep >= 10) {
//                pointsX = 5;
//                pointsY = 750;
//            }
//                canvas.drawLine(30,600,150,600,whiteLine);
//                canvas.drawLine(30,800,150,800,whiteLine);
            canvas.drawText(String.valueOf(rep), 200, 300, textPaint);

//            if (kneeAngL >= 40 && kneeAngL <= 120) {
//                canvas.drawRoundRect(20,900+(kneeAngL-10)*10,120,2000,100,100,redBar);
//                canvas.drawRoundRect(20,900,120,2000,100,100,wCirclePaint);
//            } else if (kneeAngL >= 10 && kneeAngL < 40) {
//                canvas.drawRoundRect(20,900+(kneeAngL-10)*10,120,2000,100,100,greenBar);
//                canvas.drawRoundRect(20,900,120,2000,100,100,wCirclePaint);
//            } else if (kneeAngL < 10) {
//                canvas.drawRoundRect(20,900,120,2000,100,100,greenBar);
//                canvas.drawRoundRect(20,900,120,2000,100,100,wCirclePaint);
//            } else if (kneeAngL > 120) {
//                canvas.drawRoundRect(20,2000,120,2000,100,100,greenBar);
//                canvas.drawRoundRect(20,900,120,2000,100,100,wCirclePaint);
//            }

        }

        if (status == 2) {
            canvas.drawText(String.valueOf(timer),800,300,textPaint);
        }

    }

    void drawPoint(Canvas canvas, PoseLandmark landmark, Paint paint) {
        PointF3D point = landmark.getPosition3D();
        canvas.drawCircle(translateX(point.getX()), translateY(point.getY()), DOT_RADIUS, paint);
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

    void drawLine(Canvas canvas, PoseLandmark startLandmark, PoseLandmark endLandmark, Paint paint) {
        PointF3D start = startLandmark.getPosition3D();
        PointF3D end = endLandmark.getPosition3D();

        canvas.drawLine(
                translateX(start.getX()),
                translateY(start.getY()),
                translateX(end.getX()),
                translateY(end.getY()),
                paint);
    }

}
