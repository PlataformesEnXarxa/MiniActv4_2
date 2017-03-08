package cat.chaimae.minactivitat4_2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    TextView tView;
    String text;
    ImageView image;
    Handler h;
    private NetworkReceiver receiver = new NetworkReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(receiver, filter);
        h = new Handler();
        tView = (TextView) findViewById(R.id.text);
        text = getText(R.string.pagina_web_a_consultar).toString();
        image = (ImageView) findViewById(R.id.imageView);
        downloadWebPage();
        downloadImage(getString(R.string.Image));

    }

    public void downloadImage(final String src) {
        findViewById(R.id.image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    final Thread tr = new Thread() {
                        public void run() {
                            try {
                                URL urli = new URL(src);
                                URLConnection conni = urli.openConnection();
                                InputStream input = conni.getInputStream();
                                Bitmap bitmap = BitmapFactory.decodeStream(input);
                                MainActivity.this.h.post(new updateUIThreadImage(bitmap));

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    tr.start();
                } catch (Exception e) {
                }


            }
        });

    }

    public void downloadWebPage() {
        findViewById(R.id.web).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    tView.setText("");
                    final Thread tr = new Thread() {
                        public void run() {
                            try {
                                // Perform action on click
                                URL url = new URL(text);
                                URLConnection conn = url.openConnection();
                                // Get the response
                                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                                String line = "";
                                while ((line = rd.readLine()) != null) {

                                    /*Message lmsg;
                                    lmsg = new Message();
                                    lmsg.obj = line;
                                    lmsg.what = 0;
                                    GetWebPage.this.h.sendMessage(lmsg);*/

                                    MainActivity.this.h.post(new updateUIThread(line));
                                }
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    tr.start();
                } catch (Exception e) {
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(receiver);
    }

    public class NetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            new AsyncClass().execute(networkInfo);
        }
    }


    public class AsyncClass extends AsyncTask<NetworkInfo, Void, String> {

        @Override
        protected String doInBackground(NetworkInfo... networkInfos) {
            NetworkInfo networkInfo = networkInfos[0];

            if (networkInfo != null
                    && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                //Toast.makeText(getApplicationContext(), R.string.wifi_connected, Toast.LENGTH_SHORT).show();
                return getString(R.string.wifi_connected);
            } else if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                //Toast.makeText(getApplicationContext(), R.string.mobile_connected, Toast.LENGTH_SHORT).show();
                return getString(R.string.mobile_connected);
            } else {
                //Toast.makeText(getApplicationContext(), R.string.lost_connection, Toast.LENGTH_SHORT).show();
                return getString(R.string.lost_connection);
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
        }
    }

    class updateUIThread implements Runnable {
        private String msg;

        public updateUIThread(String str) {
            this.msg = str;
        }

        @Override
        public void run() {
            tView.append(msg);
        }

    }

    class updateUIThreadImage implements Runnable {
        private Bitmap bit;

        public updateUIThreadImage(Bitmap bitmap) {
            this.bit = bitmap;
        }

        @Override
        public void run() {
            image.setImageBitmap(bit);
        }
    }

}

