package maciek.grafik;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sienek on 21.09.2017.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "DBzmianGrafikowych.db";
    public static final String GRAFIK_TABLE_NAME = "zmianyWGrafiku";
    public static final String GRAFIK_COLUMN_DATE = "dataZmiany";
    public static final String GRAFIK_COLUMN_OPTION = "opcjaZmiany";
    private static final String DEBUG_TAG = "SqLiteTodoManager";
    private static final int DB_VERSION = 1;
    private HashMap hp;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table " + GRAFIK_TABLE_NAME + "(id integer PRIMARY KEY AUTOINCREMENT, " + GRAFIK_COLUMN_DATE + " integer, " + GRAFIK_COLUMN_OPTION + " text);"
        );
        Log.d(DEBUG_TAG, "Database creating... ");
        Log.d(DEBUG_TAG, "Table " + GRAFIK_TABLE_NAME + " ver." + DB_VERSION + " created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + GRAFIK_TABLE_NAME);
        Log.d(DEBUG_TAG, "Database updating...");
        Log.d(DEBUG_TAG, "Table " + GRAFIK_TABLE_NAME + " updated from ver." + oldVersion + " to ver." + newVersion);
        Log.d(DEBUG_TAG, "All data is lost.");
        onCreate(db);
    }

    public boolean wstawZmiane(int dataZmiany, String opcjaZmiany) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(GRAFIK_COLUMN_DATE, dataZmiany);
        contentValues.put(GRAFIK_COLUMN_OPTION, opcjaZmiany);
        db.insert(GRAFIK_TABLE_NAME, null, contentValues);
        Log.d(DEBUG_TAG, "NEW data inserted.----------------------------------------------");
        return true;
    }

    public Cursor getData(int dataZmiany) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + GRAFIK_TABLE_NAME + " where " + GRAFIK_COLUMN_DATE + " = " + dataZmiany, null);
        return res;
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, GRAFIK_TABLE_NAME);
        return numRows;
    }

    public String selectingOptionIfDataExist(String dane) {
        String opcja = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + GRAFIK_TABLE_NAME + " where " + GRAFIK_COLUMN_DATE + " = " + dane + " LIMIT 1", null);
        if (cursor.getCount() <= 0) {
            cursor.moveToFirst();
            cursor.close();
            return opcja;
        }
        cursor.moveToFirst();
        opcja = cursor.getString(cursor.getColumnIndex(GRAFIK_COLUMN_OPTION));
        cursor.close();
        return opcja;
    }

    public boolean updateGrafik(int dataZmiany, String opcjaZmiany) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(GRAFIK_COLUMN_DATE, dataZmiany);
        contentValues.put(GRAFIK_COLUMN_OPTION, opcjaZmiany);
        db.update(GRAFIK_TABLE_NAME, contentValues, GRAFIK_COLUMN_DATE + " = " + dataZmiany, null);
        return true;
    }

    public Integer deleteGrafik(Integer dataZmiany) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("zmianyWGrafiku",
                "dataZmiany = ? ",
                new String[]{Integer.toString(dataZmiany)});
    }

    public ArrayList<String> getAllRowsWhere(int poczatekMiesiaca, int koniecMiesiaca) {
        ArrayList<String> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from zmianyWGrafiku where " + GRAFIK_COLUMN_DATE + ">=" + poczatekMiesiaca + " AND " + GRAFIK_COLUMN_DATE + "<=" + koniecMiesiaca + " ORDER BY " + GRAFIK_COLUMN_DATE, null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            array_list.add(res.getString(res.getColumnIndex(GRAFIK_COLUMN_DATE)));
            array_list.add(res.getString(res.getColumnIndex(GRAFIK_COLUMN_OPTION)));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }

    public ArrayList<String> getAllRows(int dataWartosc) {
        ArrayList<String> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from zmianyWGrafiku where " + GRAFIK_COLUMN_DATE + "=" + dataWartosc, null);
        res.moveToFirst();


        while (!res.isAfterLast()) {
            array_list.add(res.getString(res.getColumnIndex(GRAFIK_COLUMN_DATE)));
            array_list.add(res.getString(res.getColumnIndex(GRAFIK_COLUMN_OPTION)));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }
}
