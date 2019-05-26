package com.example.lt_nhung;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button mDieuHoa, mNongLanh, mDen1, mDen2, mBatAll, mTatAll;
    boolean dieuHoa, nongLanh, den1, den2;
    private static final int REQUEST_PERMISSION = 200;
    private static String fileName = null;
    Button mStartRecord, mStopRecord;
    private MediaPlayer player = null;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    WavRecorder wavRecorder;
    TextView mResult;
    private Handler mHandler;
    boolean block = false;

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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        block = false;
        initView();
        initRecorder();
        mStartRecord.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                block = false;
                Toast.makeText(MainActivity.this,"Reset",Toast.LENGTH_SHORT).show();
                return false;
            }
        });

    }

    private void initView() {
        mHandler = new Handler(Looper.getMainLooper());
        mDieuHoa = findViewById(R.id.btn_dieuhoa);
        mNongLanh = findViewById(R.id.btn_nonglanh);
        mDen1 = findViewById(R.id.btn_den1);
        mDen2 = findViewById(R.id.btn_den2);
        mBatAll = findViewById(R.id.btn_bat_all);
        mTatAll = findViewById(R.id.btn_tat_all);
        mResult = findViewById(R.id.tv_result);
        dieuHoa = true;
        nongLanh = true;
        den1 = true;
        den2 = true;
        mDieuHoa.setOnClickListener(this);
        mNongLanh.setOnClickListener(this);
        mDen1.setOnClickListener(this);
        mDen2.setOnClickListener(this);
        mBatAll.setOnClickListener(this);
        mTatAll.setOnClickListener(this);
    }

    private void initRecorder() {
        fileName = getFilesDir().getAbsolutePath();
        fileName += "/record.wav";
        if (!hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION);
        }
        mStartRecord = findViewById(R.id.btn_start_record);
        mStopRecord = findViewById(R.id.btn_stop_record);
        mStartRecord.setOnClickListener(this);
        mStopRecord.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
//        if (v==mStartRecord){
//            onRecord(true);
        if (v==mStopRecord){
            onRecord(false);
        }
        else if (!block) {
            block = true;
            switch (v.getId()) {
                case R.id.btn_dieuhoa:
                    dieuHoa = !dieuHoa;
                    if (dieuHoa) {
                        guiLenhChoPi("Bật điều hòa");
                    } else {
                        guiLenhChoPi("Tắt điều hòa");
                    }
                    break;
                case R.id.btn_nonglanh:
                    nongLanh = !nongLanh;
                    if (nongLanh) {
                        guiLenhChoPi("Bật nóng lạnh");
                    } else {
                        guiLenhChoPi("Tắt nóng lạnh");
                    }
                    break;
                case R.id.btn_den1:
                    den1 = !den1;
                    if (den1) {
                        guiLenhChoPi("Bật đèn phòng khách");
                    } else {
                        guiLenhChoPi("Tắt đèn phòng khách");
                    }
                    break;
                case R.id.btn_den2:
                    den2 = !den2;
                    if (den2) {
                        guiLenhChoPi("Bật đèn hành lang");
                    } else {
                        guiLenhChoPi("Tắt đèn hành lang");
                    }
                    break;
                case R.id.btn_bat_all:
                    dieuHoa = nongLanh = den1 = den2 = true;
                    guiLenhChoPi("Bật tất cả");
                    break;
                case R.id.btn_tat_all:
                    dieuHoa = nongLanh = den1 = den2 = false;
                    guiLenhChoPi("Tắt tất cả");
                    break;
                case R.id.btn_start_record:
                    onRecord(true);
                    block = false;
                    break;
//                case R.id.btn_stop_record:
//                    onRecord(false);
//                    break;
            }
            checkStatus();
        }
    }

    private void checkStatus() {
        if (dieuHoa) {
            mDieuHoa.setBackground(getResources().getDrawable(R.drawable.ic_dieuhoa_bat));
        } else {
            mDieuHoa.setBackground(getResources().getDrawable(R.drawable.ic_dieuhoa_tat));
        }
        if (nongLanh) {
            mNongLanh.setBackground(getResources().getDrawable(R.drawable.ic_nonglanh_bat));
        } else {
            mNongLanh.setBackground(getResources().getDrawable(R.drawable.ic_nonglanh_tat));
        }
        if (den1) {
            mDen1.setBackground(getResources().getDrawable(R.drawable.ic_den_bat));
        } else {
            mDen1.setBackground(getResources().getDrawable(R.drawable.ic_den_tat));
        }
        if (den2) {
            mDen2.setBackground(getResources().getDrawable(R.drawable.ic_den_bat));
        } else {
            mDen2.setBackground(getResources().getDrawable(R.drawable.ic_den_tat));
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
            writeToFile();
        }
    }

    private void startRecording() {
        wavRecorder = new WavRecorder(fileName);
        wavRecorder.startRecording();
    }

    private void stopRecording() {
        wavRecorder.stopRecording();
    }

    private void writeToFile() {
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
        if (!block) {
            block = true;
            speechToText();
        }

    }

    private void speechToText() {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(null, new File(fileName));
        Request request = new Request.Builder()
                .url("https://api.openfpt.vn/fsr")
                .post(body)
                .addHeader("api_key", "0cc0805efb6f4126a076613a6215a7af")
                .addHeader("Content-Type", "")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    block = false;
                    throw new IOException("Unexpected code " + response);
                } else {
                    // do something wih the result
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateLenh(response);
                        }
                    });
                }
            }
        });
    }

    public void updateLenh(Response response) {
        try {
            String json = response.body().string();
            Log.d("responseeeeeee", json);
            JSONObject root = new JSONObject(json);
            JSONArray hypotheses = root.getJSONArray("hypotheses");
            JSONObject element1 = (JSONObject) hypotheses.get(0);
            String text = element1.getString("utterance");
            Log.d("textttttt", text);
            guiLenhChoPi(text);
        } catch (Exception e) {
        }
    }

    public void guiLenhChoPi(String text) {
        mResult.setText("Lệnh gửi đi: " + text);

        OkHttpClient client = new OkHttpClient.Builder().authenticator(new Authenticator() {
            public Request authenticate(Route route, Response response) throws IOException {
                String credential = Credentials.basic("admin", "admin");
                return response.request().newBuilder().header("Authorization", credential).build();
            }
        }).build();

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, "{\"text\": \"" + text + "\"}");
        Request request = new Request.Builder()
                .url("http://192.168.43.191:5000/say/")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Basic YWRtaW46YWRtaW4=")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    block = false;
                    throw new IOException("Unexpected code " + response);
                } else {
                    // do something wih the result
                    //  Log.d("responseeeeeee",response.body().string());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String xx = response.body().string();
                                JSONObject root = new JSONObject(xx);
                                int status = root.getInt("status");
                                String res = root.getString("text");
                                updateView(status, res);
                            } catch (Exception e) {
                            }
                        }
                    });
                }
            }
        });
    }

    private void updateView(int status, String res) {
        block = false;
        TextView tv = MainActivity.this.findViewById(R.id.tv_result_1);
        tv.setText("Pi trả về: " + res);
        switch (status) {
            case 0:
                den1 = true;
                break;
            case 1:
                den1 = false;
                break;
            case 2:
                dieuHoa = true;
                break;
            case 3:
                dieuHoa = false;
                break;
            case 4:
                nongLanh = true;
                break;
            case 5:
                nongLanh = false;
                break;
            case 6:
                den2 = true;
                break;
            case 7:
                den2 = false;
                break;
            case 10:
                dieuHoa = nongLanh = den1 = den2 = true;
                break;
            case 11:
                dieuHoa = nongLanh = den1 = den2 = false;
                break;
            case 200:
                tv.setText("Pi trả về: " + res);
                break;
        }
        checkStatus();
        Toast.makeText(this, res + " done!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}