package cn.leixiaoyue.lhlearningdemo.databaseperfromance;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by 80119424 on 2016/11/22.
 */

public class DatabaseHelper extends SQLiteAssetHelper {
    private static final String DB_NAME = "images_record.db";
    private static final int version = 706;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, version);
    }
}
