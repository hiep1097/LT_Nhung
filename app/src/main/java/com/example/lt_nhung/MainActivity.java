package com.example.lt_nhung;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.lt_nhung.api.RetrofitInstance;
import com.example.lt_nhung.api.Service;
import com.example.lt_nhung.model.Record;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button mDieuHoa, mNongLanh, mDen1, mDen2;
    boolean dieuHoa, nongLanh, den1, den2;
    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_PERMISSION = 200;
    private static String fileName = null;
    Button mStartRecord, mStopRecord, mStartPlay, mStopPlay;
    private MediaPlayer player = null;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    WavRecorder wavRecorder;

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initRecorder();


    }

    private void initView(){
        mDieuHoa = findViewById(R.id.btn_dieuhoa);
        mNongLanh = findViewById(R.id.btn_nonglanh);
        mDen1 = findViewById(R.id.btn_den1);
        mDen2 = findViewById(R.id.btn_den2);
        dieuHoa = true;
        nongLanh = true;
        den1 = true;
        den2 = true;
        mDieuHoa.setOnClickListener(this);
        mNongLanh.setOnClickListener(this);
        mDen1.setOnClickListener(this);
        mDen2.setOnClickListener(this);
    }

    private void initRecorder(){
        fileName = getFilesDir().getAbsolutePath();
        fileName += "/record.wav";
        if(!hasPermissions(this, permissions)){
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION);
        }
        mStartRecord = findViewById(R.id.btn_start_record);
        mStopRecord = findViewById(R.id.btn_stop_record);
        mStartPlay = findViewById(R.id.btn_start_play);
        mStopPlay = findViewById(R.id.btn_stop_play);
        mStartRecord.setOnClickListener(this);
        mStopRecord.setOnClickListener(this);
        mStartPlay.setOnClickListener(this);
        mStopPlay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_dieuhoa:
                if (!dieuHoa) {
                    mDieuHoa.setBackground(getResources().getDrawable(R.drawable.ic_dieuhoa_bat));
                } else {
                    mDieuHoa.setBackground(getResources().getDrawable(R.drawable.ic_dieuhoa_tat));
                }
                dieuHoa = !dieuHoa;
                break;
            case R.id.btn_nonglanh:
                if (!nongLanh) {
                    mNongLanh.setBackground(getResources().getDrawable(R.drawable.ic_nonglanh_bat));
                } else {
                    mNongLanh.setBackground(getResources().getDrawable(R.drawable.ic_nonglanh_tat));
                }
                nongLanh = !nongLanh;
                break;
            case R.id.btn_den1:
                if (!den1) {
                    mDen1.setBackground(getResources().getDrawable(R.drawable.ic_den_bat));
                } else {
                    mDen1.setBackground(getResources().getDrawable(R.drawable.ic_den_tat));
                }
                den1 = !den1;
                break;
            case R.id.btn_den2:
                if (!den2) {
                    mDen2.setBackground(getResources().getDrawable(R.drawable.ic_den_bat));
                } else {
                    mDen2.setBackground(getResources().getDrawable(R.drawable.ic_den_tat));
                }
                den2 = !den2;
                break;
            case R.id.btn_start_record:
                onRecord(true);
                break;
            case R.id.btn_stop_record:
                onRecord(false);
                break;
            case R.id.btn_start_play:
                onPlay(true);
                break;
            case R.id.btn_stop_play:
                onPlay(false);
                break;
        }
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
            mStartRecord.setVisibility(View.INVISIBLE);
            mStopRecord.setVisibility(View.VISIBLE);
        } else {
            stopRecording();
            mStartRecord.setVisibility(View.VISIBLE);
            mStopRecord.setVisibility(View.INVISIBLE);
        }
    }

    private void onPlay(boolean start) {
        encodeAndWriteToFile();
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }

    private void startRecording() {
        wavRecorder = new WavRecorder(fileName);
        wavRecorder.startRecording();
    }

    private void stopRecording() {
        wavRecorder.stopRecording();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void encodeAndWriteToFile(){
        File file = new File(fileName);
        int size = (int) file.length();
        byte[] bytes = new byte[size];

        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String base64String = new String(Base64.encode(bytes,Base64.NO_WRAP),StandardCharsets.UTF_8);
        Log.d("BASE6444444444",base64String);
        writeData(base64String);
    }

    public void writeData(String data) {
        try {
            FileOutputStream out= openFileOutput("record.txt",0);
            OutputStreamWriter writer= new OutputStreamWriter(out);
            writer.write(data);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void guiFileLenServer(){
        Service service = RetrofitInstance.getRetrofit().create(Service.class);
        Map<String, String> data = new HashMap<>();
//        data.put("lat",UtilPref.getInstance().getFloat("lat",0)+"");
//        data.put("lon",UtilPref.getInstance().getFloat("lon",0)+"");
//        data.put("units",UtilPref.getInstance().getString("unit","metric"));
//        data.put("lang","vi");
//        data.put("APPID",Config.API_KEY);
        Observable<Record> observable = service.getRecord(data);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> updateView(response),
                        error -> Toast.makeText(MainActivity.this,
                                error.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateView(Record record){

    }
}
