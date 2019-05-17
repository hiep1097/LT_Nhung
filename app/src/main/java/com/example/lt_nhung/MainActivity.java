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
import android.widget.Button;
import android.widget.TextView;

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
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button mDieuHoa, mNongLanh, mDen1, mDen2;
    boolean dieuHoa, nongLanh, den1, den2;
    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_PERMISSION = 200;
    private static String fileName = null;
    Button mStartRecord, mStopRecord, mStartPlay, mStopPlay;
    private MediaPlayer player = null;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    WavRecorder wavRecorder;
    TextView mResult;
    private Handler mHandler;
    //String xxx = "[{\"status\":0,\"msg\":\"\",\"segment\":0,\"result\":{\"hypotheses\":[{\"transcript\":\"xin chào\",\"transcript_normed\":\"xin chào\",\"confidence\":0.9006394,\"likelihood\":141.126},{\"transcript\":\"xin chào anh\",\"transcript_normed\":\"xin chào anh\",\"confidence\":0.0,\"likelihood\":138.817},{\"transcript\":\"xin cha\",\"transcript_normed\":\"xin cha\",\"confidence\":0.0,\"likelihood\":137.957},{\"transcript\":\"xin chào em\",\"transcript_normed\":\"xin chào em\",\"confidence\":0.0,\"likelihood\":137.611},{\"transcript\":\"xin chào chị\",\"transcript_normed\":\"xin chào chị\",\"confidence\":0.0,\"likelihood\":137.443}],\"final\":true},\"segment_start\":0.0,\"segment_length\":2.19,\"total_length\":2.19438}]";
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

    private void initView() {
        mHandler = new Handler(Looper.getMainLooper());
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
        mResult = findViewById(R.id.tv_result);
    }

    private void initRecorder() {
        fileName = getFilesDir().getAbsolutePath();
        fileName += "/record.wav";
        if (!hasPermissions(this, permissions)) {
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
        switch (v.getId()) {
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

    private void encodeAndWriteToFile() {
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
        String base64String = new String(Base64.encode(bytes, Base64.NO_WRAP), StandardCharsets.UTF_8);
        Log.d("BASE6444444444", base64String);
        writeData(base64String);
        guiFileLenServer();
    }

    public void writeData(String data) {
        try {
            FileOutputStream out = openFileOutput("record.txt", 0);
            OutputStreamWriter writer = new OutputStreamWriter(out);
            writer.write(data);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    KeyStore readKeyStore() {
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("BKS");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        // get user password and file input stream
        char[] password = "changeit".toCharArray();

        java.io.InputStream fis = null;
        try {
            fis = getResources().openRawResource(R.raw.severkeystore);
            ks.load(fis, password);
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ks;
    }

    private void guiFileLenServer() {

//        CertificatePinner certificatePinner = new CertificatePinner.Builder()
//                .add("https://vtcc.ai/voice/api/asr/v1/rest/decode_file", "sha256/RjA6QTM6RTg6MUY6MUY6MDY6OTU6QUU6M0U6N0Y6Rjc6ODg6MzE6RkI6NEE6OTA6NTY6MkI6QTQ6REI6MkE6Mzk6MkU6Q0U6NTU6RUQ6ODc6MTg6QzI6REQ6N0I6OEQ=")
//                .build();

        KeyStore keyStore = readKeyStore(); //your method to obtain KeyStore
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        TrustManagerFactory trustManagerFactory = null;
        try {
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            trustManagerFactory.init(keyStore);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        KeyManagerFactory keyManagerFactory = null;
        try {
            keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            keyManagerFactory.init(keyStore, "changeit".toCharArray());
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        try {
            sslContext.init(keyManagerFactory.getKeyManagers(),trustManagerFactory.getTrustManagers(), new SecureRandom());
        } catch (KeyManagementException e) {

        }

        OkHttpClient client = new OkHttpClient.Builder().sslSocketFactory(sslContext.getSocketFactory()).build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", new File(fileName).getName(),
                        RequestBody.create(MediaType.parse("audio/vnd.wave"), new File(fileName)))
                .addFormDataPart("some-field", "some-value")
                .build();
        Request request = new Request.Builder()
                .url("https://vtcc.ai/voice/api/asr/v1/rest/decode_file")
                .post(body)
                .addHeader("token", "z-44QoH3eIf-ovEGom6q4A7dPZYfFuuCl9s6i4A9A9bqV1-nSY7x5nJVzRsPh1WR")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    // do something wih the result
                  //  Log.d("responseeeeeee",response.body().string());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateView(response);
                        }
                    });

                }
            }
        });
    }

    public void updateView(Response response){
        try {
            String json = response.body().string();
            //String json = new String(xxx);
            JSONArray root = new JSONArray(json);
            JSONObject xx = root.getJSONObject(0);
            JSONObject res = xx.getJSONObject("result");
            JSONArray hypotheses = res.getJSONArray("hypotheses");
            JSONObject element1 = (JSONObject) hypotheses.get(0);
            String text = element1.getString("transcript");
            Log.d("textttttt",text);
            mResult.setText("Nội dung:"+text);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}