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

public class DBHelperSettings extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "DBgrafik.db";
    public static final String GRAFIK_TABLE_NAME = "settings";
    public static final String GRAFIK_COLUMN_ID = "id";
    public static final String GRAFIK_COLUMN_DATA_POCZATEK_CYKLU = "startCyklu";
    public static final String GRAFIK_COLUMN_POCZATKOWA_DATA_USTAWIEN = "startUstawien";
    public static final String GRAFIK_COLUMN_KONCOWA_DATA_USTAWIEN = "stopUstawien";
    public static final String GRAFIK_COLUMN_STAWKA = "stawka";
    public static final String GRAFIK_COLUMN_ZMIANA = "brygada";
    public int poczatekCyklu, startUstawien;
    public float stawka;
    public String brygada;
    private static final String DEBUG_TAG = "SqLiteTodoManager";
    private static final int DB_VERSION = 4;
    private HashMap hp;

    public DBHelperSettings(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table " + GRAFIK_TABLE_NAME + "(" + GRAFIK_COLUMN_ID + " integer PRIMARY KEY AUTOINCREMENT, " +
                        GRAFIK_COLUMN_DATA_POCZATEK_CYKLU + " integer, " +
                        GRAFIK_COLUMN_POCZATKOWA_DATA_USTAWIEN + " integer, " +
                        GRAFIK_COLUMN_KONCOWA_DATA_USTAWIEN + " integer, " +
                        GRAFIK_COLUMN_STAWKA + " real, " +
                        GRAFIK_COLUMN_ZMIANA + " text );"
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

    public boolean wstawNoweDane(int startCyklu, int startUstawien, int stopUstawien, float stawka, String brygada) {
        //Wstawianie nowego rekordu do bazy
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(GRAFIK_COLUMN_DATA_POCZATEK_CYKLU, startCyklu);
        contentValues.put(GRAFIK_COLUMN_POCZATKOWA_DATA_USTAWIEN, startUstawien);
        contentValues.put(GRAFIK_COLUMN_KONCOWA_DATA_USTAWIEN, stopUstawien);
        contentValues.put(GRAFIK_COLUMN_STAWKA, stawka);
        contentValues.put(GRAFIK_COLUMN_ZMIANA, brygada);
        db.insert(GRAFIK_TABLE_NAME, null, contentValues);
        Log.d(DEBUG_TAG, "NEW data inserted.----------------------------------------------"+startUstawien);
        return true;
    }

    /*
        public Cursor getData(int dataZmiany) {
            // pobranie rekordu zgodnego z parametrem
            SQLiteDatabase db = this.getReadableDatabase();
            return  db.rawQuery( "select * from "+GRAFIK_TABLE_NAME+" where "+GRAFIK_COLUMN_DATA_POCZATEK_CYKLU+" = "+dataZmiany, null );
            //return res;
        }*/
/*
    public int numberOfRows(){
        // policzenie wszystkich rekordów w tabeli.
        SQLiteDatabase db = this.getReadableDatabase();
        return  (int) DatabaseUtils.queryNumEntries(db, GRAFIK_TABLE_NAME);
        //return numRows;
    }*/
    public boolean selectDataIfExist() {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + GRAFIK_TABLE_NAME + " where " + GRAFIK_COLUMN_DATA_POCZATEK_CYKLU + " != 0 ORDER BY " + GRAFIK_COLUMN_ID + " DESC LIMIT 1", null);
        if (cursor.getCount() <= 0) {
            cursor.moveToFirst();
            cursor.close();
            return false;
        }
        cursor.moveToFirst();
        // nazwy kolumn tabeli ustawień: startCyklu, startUstawien, stopUstwien, stawka, brygada
        poczatekCyklu = cursor.getInt(cursor.getColumnIndex("startCyklu"));
        startUstawien = cursor.getInt(cursor.getColumnIndex("startUstawien"));
        stawka = cursor.getFloat(cursor.getColumnIndex("stawka"));
        brygada = cursor.getString(cursor.getColumnIndex("brygada"));
        cursor.close();
        return true;
    }
/*
    public boolean updateGrafik (int dataZmiany, String opcjaZmiany) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(GRAFIK_COLUMN_DATE, dataZmiany);
        contentValues.put(GRAFIK_COLUMN_OPTION, opcjaZmiany);
        db.update(GRAFIK_TABLE_NAME, contentValues, GRAFIK_COLUMN_DATE+" = "+dataZmiany, null);
        return true;
    }*/
/*
    public Integer deleteGrafik (Integer dataZmiany) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("zmianyWGrafiku",
                "dataZmiany = ? ",
                new String[] { Integer.toString(dataZmiany) });
    }*/
/*
    public ArrayList<String> getRowsWhere(int poczatekMiesiaca, int koniecMiesiaca) {
        // Pobranie wielu rekordów spełniających kryteria
        ArrayList<String> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from zmianyWGrafiku where "+GRAFIK_COLUMN_DATE+">="+poczatekMiesiaca+" AND "+GRAFIK_COLUMN_DATE+"<="+koniecMiesiaca+" ORDER BY "+GRAFIK_COLUMN_DATE, null );
        res.moveToFirst();

        while(!res.isAfterLast()){
            array_list.add(res.getString(res.getColumnIndex(GRAFIK_COLUMN_DATE)));
            array_list.add(res.getString(res.getColumnIndex(GRAFIK_COLUMN_OPTION)));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }
*/
/*
    public ArrayList<String> getAllRows(int dataWartosc) {
        ArrayList<String> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from zmianyWGrafiku where "+GRAFIK_COLUMN_DATE+"="+dataWartosc, null );
        res.moveToFirst();


        while(!res.isAfterLast()){
            array_list.add(res.getString(res.getColumnIndex(GRAFIK_COLUMN_DATE)));
            array_list.add(res.getString(res.getColumnIndex(GRAFIK_COLUMN_OPTION)));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }
    */
}
