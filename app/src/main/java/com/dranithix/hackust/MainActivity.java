package com.dranithix.hackust;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import im.delight.android.ddp.Meteor;
import im.delight.android.ddp.MeteorSingleton;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {
    @ViewById(R.id.cameraView)
    SurfaceView cameraView;

    @ViewById(R.id.slidingLayout)
    SlidingUpPanelLayout slidingLayout;

    boolean activityRunning;

    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;


    @AfterViews
    public void init() {
        if (!MeteorSingleton.hasInstance())
            MeteorSingleton.createInstance(this, "https://57bef2fa.ngrok.io/websocket");
        MeteorSingleton.getInstance().connect();


        barcodeDetector =

                new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.ALL_FORMATS)
                        .build();
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                String[] ticketParams = null;
                for (int i = 0; i < barcodes.size(); i++) {
                    String barcodeVal = barcodes.valueAt(i).displayValue;
                    String[] params = barcodeVal.split(",");
                    if (params.length == 5) {
                        ticketParams = params;
                        break;
                    }
                }

                if (ticketParams != null) {
                    ConfirmBookingActivity_.intent(MainActivity.this).extra("passenger", ticketParams[0]).extra("airport", ticketParams[1]).extra("airline", ticketParams[2]).extra("flightNum", ticketParams[3]).extra("operationDate", ticketParams[4]).start();
                    barcodeDetector.release();
                    finish();
                }


            }
        });

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(metrics.heightPixels, metrics.widthPixels)
                .setAutoFocusEnabled(true)
                .build();

        slidingLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    try {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException ie) {
                        ie.printStackTrace();
                    }
                } else if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    cameraSource.stop();
                }
            }
        });
    }
}
