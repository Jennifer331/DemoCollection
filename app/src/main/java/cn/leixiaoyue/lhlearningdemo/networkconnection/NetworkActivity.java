package cn.leixiaoyue.lhlearningdemo.networkconnection;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.leixiaoyue.lhlearningdemo.R;

/**
 * Created by 80119424 on 2016/2/3.
 */
public class NetworkActivity extends Activity {
    public static final String PIC_URL = "http://pic.baike.soso.com/p/20090711/20090711100323-24213954.jpg";
//    public static final String PIC_URL = "http://leixiaoyue.cn/Koala.jpg";
    private boolean mWifiConnected = false;
    private boolean mDataConnected = false;

    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.network);
        findView();
    }

    private void findView() {
        mImageView = (ImageView)findViewById(R.id.imageview);
    }

    public void connectTo(View view) {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (null != networkInfo && networkInfo.isConnected()) {
            mWifiConnected = ConnectivityManager.TYPE_WIFI == networkInfo.getType();
            mDataConnected = ConnectivityManager.TYPE_MOBILE == networkInfo.getType();
            new DownloadTask().execute(PIC_URL);
        } else {
            mWifiConnected = false;
            mDataConnected = false;
        }
    }

    private class DownloadTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls){
            try {
                return downloadUrl(urls[0]);
            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mImageView.setImageBitmap(bitmap);
        }

        private Bitmap downloadUrl(String myUrl) throws IOException{
            InputStream is = null;
            try {
                URL url = new URL(myUrl);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(100000/*milliseconds*/);
                conn.setConnectTimeout(100000/*milliseconds*/);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                //start the query
                conn.connect();
                int response = conn.getResponseCode();
                is = conn.getInputStream();
                //convert inputstream into a string
                Bitmap content = readIsToBitmap(is);
                return content;
            }catch(Exception e){
                e.printStackTrace();
            }finally {
                if (null != is) {
                    is.close();
                }
            }
            return null;
        }

        //reads an InputString, converts it to String
        private Bitmap readIsToBitmap(InputStream is){
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            return bitmap;
            /** get text
            Reader reader = new InputStreamReader(is, "UTF-8");
             //ByteArrayOutputString
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
             **/
        }
    }
}
