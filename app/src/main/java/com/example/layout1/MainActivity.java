package com.example.layout1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    private String geladenerText = "";
    private static int wortindex = 0;

    private WorkerUsingThread textDurchlauf;
    private static int sleepTime = 0;

    private TextView textT2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("Speed-Reader");
        setContentView(R.layout.activity_main);

        TextView textT1 = (TextView) findViewById(R.id.textViewT1);
        textT2 = (TextView) findViewById(R.id.textViewT2);
        TextView textT3 = (TextView) findViewById(R.id.textViewFrequenz);
        final Button buttonS1 = (Button) findViewById(R.id.button_s1);
        final Button buttonS2 = (Button) findViewById(R.id.button_s2);
        final ToggleButton buttonS3 = (ToggleButton) findViewById(R.id.toggleButton_s3);
        SeekBar frequenzRegler = (SeekBar) findViewById(R.id.seekBar);

        textDurchlauf = new WorkerUsingThread();

        int MAX_SLEEPTIME = 1000;
        int STEP_SLEEPTIME = 100;



        buttonS1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                geladenerText = ausschnittAuslesen("texte/uebung_persistenz.txt");
                textT1.setText(geladenerText);
            }
        });
        buttonS2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textT1.setText("");
                textT2.setText("");
                geladenerText = "";
                wortindex = 0;
            }
        });
        buttonS3.setText("Starte Speed-Reader");
        buttonS3.setTextOn("Stoppe Speed-Reader");
        buttonS3.setTextOff("Starte Speed-Reader");
        buttonS3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v == buttonS3) {
                    if (buttonS3.isChecked()){
                        textDurchlauf.start();
                        sleepTime = MAX_SLEEPTIME;
                        textT3.setText(String.format("%.1f",(1/((float)sleepTime/1000))) + " Wörter/Sekunde");
                    }else{
                        textDurchlauf.stop();
                    }
                }
            }
        });
        frequenzRegler.setMin(0);
        frequenzRegler.setMax(MAX_SLEEPTIME - 1*STEP_SLEEPTIME);
        frequenzRegler.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = (int)Math.round(progress/STEP_SLEEPTIME)*STEP_SLEEPTIME;
                seekBar.setProgress(progress);
                sleepTime = MAX_SLEEPTIME - frequenzRegler.getProgress();
                float frequenz = 1/((float)sleepTime/1000);
                textT3.setText(String.format("%.1f",frequenz) + " Wörter/Sekunde");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private String ausschnittAuslesen(String assetName) {
        AssetManager mngr = getAssets();
        try {
            InputStream is = mngr.open(assetName);
            String text = ausschnittEinlesen(is);
            return text;
        } catch (IOException e) {
            //TODO
            return "";
        }
    }

    private static String ausschnittEinlesen(InputStream is) throws IOException {
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        byte[] bytes = new byte[4096];
        int len;
        while ((len = is.read(bytes)) > 0) {
            bytestream.write(bytes, 0, len);
        }
        return new String(bytestream.toByteArray(), "UTF8");
    }

    class WorkerUsingThread implements Runnable {
        private volatile boolean running = false;
        private Thread thread;
        private String threadName = "textDurchlauf_Thread";

        private void print(final String s){
            runOnUiThread(new Runnable() {
                @Override
                public void run(){
                    textT2.setText(s);
                }
            });
        }

        @Override
        public void run(){
            int wortindex = 0;
            String[] einzelwoerter = geladenerText.split(" ");
            while(running){
                if(wortindex < einzelwoerter.length){
                    print(einzelwoerter[wortindex]);
                    wortindex++;
                }else{
                    print(einzelwoerter[0]);
                    wortindex = 1;
                }
                try {
                    Thread.sleep(sleepTime);
                    print("");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            print("");
        }

        void start() {
            running = true;
            thread = new Thread( this );
            thread.setName(threadName);
            thread.start();
        }

        void stop(){
            if(!running){
                //TODO
            }else{
                running = false;
                while(true){
                    try {
                        thread.join();
                        break;
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}