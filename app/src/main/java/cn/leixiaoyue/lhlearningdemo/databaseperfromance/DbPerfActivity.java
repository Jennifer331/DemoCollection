package cn.leixiaoyue.lhlearningdemo.databaseperfromance;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cn.leixiaoyue.lhlearningdemo.R;

/**
 * Created by 80119424 on 2016/11/22.
 */

public class DbPerfActivity extends Activity{
    private static final String TAG = "DbPrefActivity";

    private AssetDatabase mDatabase;
    private List<ImageEntry> mImages = new ArrayList<>();
    private List<ImageEntry> mData = new ArrayList<>();
    private EditText mEditText;
    private TextView mTextView;
    private Handler mHandler;

    private  static final String[] PROJECTIONS = new String[]{
            MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.ORIENTATION,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.database_performance);
        mHandler = new Handler();
        mEditText = (EditText) findViewById(R.id.input);
        mTextView = (TextView) findViewById(R.id.output);
        mDatabase = new AssetDatabase(this.getApplicationContext());
        importData();
    }

    private void importData() {
        Cursor cursor = mDatabase.query("images", PROJECTIONS);
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            ImageEntry entry = new ImageEntry();
            entry.title = cursor.getString(0);
            entry.displayName = cursor.getString(1);
            entry.dateTaken = cursor.getLong(2);
            entry.dateModified = cursor.getLong(3);
            entry.dateAdded = cursor.getLong(4);
            entry.mimeType = cursor.getString(5);
            entry.orientation = cursor.getInt(6);
            entry.data = cursor.getString(7);
            entry.size = cursor.getLong(8);
            entry.width = cursor.getInt(9);
            entry.height = cursor.getInt(10);
            mImages.add(entry);
        }
        mEditText.setHint("max size:" + mImages.size());
    }

    public void generateData(View view) {
        mData = getInsertData();
        Toast.makeText(this, mData.size() + " data loaded!", Toast.LENGTH_SHORT).show();
    }

    public void sequentiallyInsert(View view) {
        long startTime = System.currentTimeMillis();

        for (ImageEntry entry : mData) {
            ContentValues contentValues = new ContentValues();
            fillContent(contentValues, entry);
            getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        }

        long endTime = System.currentTimeMillis();
        mTextView.setText("sequentiallyInsert consumes: " + (endTime - startTime) + "ms");
        deleteRecord(mData);
    }

    public void bulkInsert(View view) {
        long startTime = System.currentTimeMillis();

        ContentValues[] contentValuesGroup = new ContentValues[mData.size()];
        int i = 0;
        for (ImageEntry entry : mData) {
            ContentValues contentValues =
                    contentValuesGroup[i] == null ? new ContentValues() : contentValuesGroup[i];
            fillContent(contentValues, entry);
            contentValuesGroup[i] = contentValues;
            i++;
        }
        getContentResolver().bulkInsert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValuesGroup);

        long endTime = System.currentTimeMillis();
        mTextView.setText("bulkInsert consumes: " + (endTime - startTime) + "ms");
        deleteRecord(mData);
    }

    public void applyBatch(View view) {
        long startTime = System.currentTimeMillis();

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        ContentProviderResult[] results = null;
        for (ImageEntry entry : mData) {
            ContentValues values = new ContentValues();
            fillContent(values, entry);
            operations.add(ContentProviderOperation
                    .newInsert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    .withValues(values)
                    .build());
        }
        try {
            results = getContentResolver().applyBatch(MediaStore.AUTHORITY, operations);
        } catch (Exception e) {
            Log.e(TAG, "applyBatch failed!!!", e);
        }

        long endTime = System.currentTimeMillis();
        mTextView.setText("applyBatch consumes: " + (endTime - startTime) + "ms");
        if (null !=  results) {
            for (ContentProviderResult result : results) {
                Log.v(TAG, ContentUris.parseId(result.uri) + "");
            }
        }
        deleteRecord(mData);
    }

    private List<ImageEntry> getInsertData() {
        int number = Integer.parseInt(mEditText.getText().toString());
        int size = mImages.size();
        List<ImageEntry> result;
        if (number >= size)
            result = mImages;
        else {
            int maxStart = size - number;
            Random random = new Random();
            int start = random.nextInt(maxStart);
            result = mImages.subList(start, start + number);
        }
        return result;
    }

    private void fillContent(ContentValues values, ImageEntry entry) {
        values.put(MediaStore.Images.Media.TITLE, entry.title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, entry.displayName);
        values.put(MediaStore.Images.Media.DATE_TAKEN, entry.dateTaken);
        values.put(MediaStore.Images.Media.DATE_MODIFIED, entry.dateModified);
        values.put(MediaStore.Images.Media.DATE_ADDED, entry.dateAdded);
        values.put(MediaStore.Images.Media.MIME_TYPE, entry.mimeType);
        values.put(MediaStore.Images.Media.ORIENTATION, entry.orientation);
        values.put(MediaStore.Images.Media.DATA, entry.data);
        values.put(MediaStore.Images.Media.SIZE, entry.size);
        values.put(MediaStore.Images.Media.WIDTH, entry.width);
        values.put(MediaStore.Images.Media.HEIGHT, entry.height);
    }

    private void deleteRecord(List<ImageEntry> entries) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append(MediaStore.Images.Media.DATA).append(" IN (");
        for (ImageEntry entry : entries) {
            whereClause.append("\"").append(entry.data).append("\"").append(",");
        }
        whereClause.deleteCharAt(whereClause.length() - 1);
        whereClause.append(")");
        Log.v(TAG, whereClause.toString());
        getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, whereClause.toString(), null);
        Toast.makeText(this, "delete done!", Toast.LENGTH_SHORT).show();
    }

    public void scallAll(View view) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent("oppo.intent.action.MEDIA_SCAN_ALL");
                sendBroadcast(intent);
            }
        };
        mHandler.post(runnable);
    }

    public class ImageEntry {
        public String title;
        public String displayName;
        public long dateTaken;
        public long dateModified;
        public long dateAdded;
        public String mimeType;
        public int orientation;
        public String data;
        public long size;
        public int width;
        public int height;

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder("ImageEntry ");
            stringBuilder.append("title: ").append(title)
                    .append("\ndisplayName: ").append(displayName)
                    .append("\ndateTaken: ").append(dateTaken)
                    .append("\ndateModified: ").append(dateModified)
                    .append("\ndateAdded: ").append(dateAdded)
                    .append("\nmimeType: ").append(mimeType)
                    .append("\norientation: ").append(orientation)
                    .append("\ndata: ").append(data)
                    .append("\nsize: ").append(size)
                    .append("\nwidth: ").append(width)
                    .append("\nheight: ").append(height);
            return stringBuilder.toString();
        }
    }
}
