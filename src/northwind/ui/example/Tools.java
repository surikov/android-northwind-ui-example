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
	static SimpleDateFormat toFormater = new SimpleDateFormat("MM/dd/yyyy");
	//static SimpleDateFormat fromFormater = new SimpleDateFormat("MM/DD/yyyy");
	//static SimpleDateFormat toFormater = new SimpleDateFormat("yyyy-MM-DD");
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
		//db = this.openOrCreateDatabase(file, Context.MODE_PRIVATE, null);
	}
	public static String formatDate(java.util.Date d) {
		String r = "";
		try {
			r = toFormater.format(d);
		}
		catch (Throwable t) {
		}
		return r;
	}
	
}
