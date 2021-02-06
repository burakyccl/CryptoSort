package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FavDB extends SQLiteOpenHelper{
    private static int DB_VERSION = 1;
    private static String DATABASE_NAME = "CryptoSort";
    private static String TABLE_NAME = "favoriteCurrency";
    public static String KEY_ID = "id";
    public static String FAVORITE_STATUS = "fStatus";

    public static String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
            + KEY_ID + " TEXT NOT NULL PRIMARY KEY, " + FAVORITE_STATUS + " TEXT)";

    public FavDB(Context context) { super(context,DATABASE_NAME, null,DB_VERSION);}

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    //create empty table
    public void insertEmpty() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        db.insert(TABLE_NAME,null , cv);
    }

    public void insertIntoTheDatabase(String item_id, String fav_status){
        SQLiteDatabase db;
        db = this.getWritableDatabase();

        String sql = "INSERT or IGNORE INTO " + TABLE_NAME+ " ("+ KEY_ID + ", " + FAVORITE_STATUS + ")"
                + " VALUES "+ "(" + "'" +item_id + "'," + "'" +fav_status + "'"  +")";
        db.execSQL(sql);
        Log.d("FavDB Status", item_id + ", favstatus - " + fav_status + " - . ");
    }

    public Cursor read_fav_status(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "select * from " + TABLE_NAME + " where " + KEY_ID + "='" + id + "'";
        return db.rawQuery(sql,null,null);
    }

    public void remove_fav(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "UPDATE " + TABLE_NAME + " SET  " + FAVORITE_STATUS + " ='0' WHERE " + KEY_ID + "='" + id + "'";
        db.execSQL(sql);
        Log.d("remove", id.toString());
    }
    public void add_fav(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "UPDATE " + TABLE_NAME + " SET  " + FAVORITE_STATUS + " ='1' WHERE " + KEY_ID + "='" + id + "'";
        db.execSQL(sql);
        Log.d("addtofav", id.toString());
    }

    public Cursor select_all_fav_list(){
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + FAVORITE_STATUS + " ='1'";
        return db.rawQuery(sql,null,null);
    }
}


