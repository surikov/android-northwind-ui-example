package northwind.ui.example;

import java.io.File;
import java.text.*;
import java.util.Date;

import reactive.ui.Auxiliary;
import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

public class Tools {
	public static SQLiteDatabase database;
	public static String dbPath = "";
	static SimpleDateFormat toFormatter = new SimpleDateFormat("MM/dd/yyyy");
	static SimpleDateFormat sqliteFormatter = new SimpleDateFormat("yyyy-MM-dd 00:00:00.000");
	public static SQLiteDatabase db(Activity activity) {
		if (database == null) {
			
			database = activity.openOrCreateDatabase(dbPath, Context.MODE_PRIVATE, null);
		}
		if (!database.isOpen()) {
			database = activity.openOrCreateDatabase(dbPath, Context.MODE_PRIVATE, null);
		}
		return database;
	}
	public static void initDB(Activity activity) {
		String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/NorthwindUIExample/";
		new File(dir).mkdir();
		dbPath = dir + "northwind.sqlite.db";
		Auxiliary.exportResource(activity, dbPath, R.raw.northwind);
	}
	public static String sqliteFormat(double time){
		java.util.Date d=new Date();
		d.setTime((long)time);
		return sqliteFormatter.format(d);
	}
	public static String formatDate(java.util.Date d) {
		String r = "";
		try {
			r = toFormatter.format(d);
		}
		catch (Throwable t) {
		}
		return r;
	}
	
}
