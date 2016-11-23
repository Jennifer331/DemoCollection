package cn.leixiaoyue.lhlearningdemo.databaseperfromance;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by 80119424 on 2016/11/22.
 */

public class AssetDatabase {
    DatabaseHelper dbHelper;

    public AssetDatabase(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public Cursor query(String table, String[] columns) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query(table, columns, null, null, null, null, null);
    }
}
