package application;

import java.io.File;
import java.io.IOException;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.schema.ISqlJetIndexDef;
import org.tmatesoft.sqljet.core.schema.ISqlJetTableDef;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.ISqlJetTransaction;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class SettingsDAO {

	private static final String DB_NAME = "settings.sqlite";
	private static final String settingsTable = "settings";
	private static String fileSeperator = System.getProperty("file.separator");
	private static File dbFile;

	public SettingsDAO(File directory) {
		dbFile = new File(directory.getAbsolutePath() + fileSeperator + DB_NAME);
		if(!dbFile.exists()){
			try{
				dbFile.createNewFile();
				System.out.println("Created new database file in " + dbFile.getAbsolutePath());
				//create settings table
				SqlJetDb db = SqlJetDb.open(dbFile, true);
				db.getOptions().setAutovacuum(true);
				db.beginTransaction(SqlJetTransactionMode.WRITE);
				try {
					db.getOptions().setUserVersion(1);
					db.createTable("CREATE TABLE " + settingsTable +  " (name NVARCHAR(255) NOT NULL PRIMARY KEY, value NVARCHAR(255) NOT NULL)");
				} finally {
					db.commit();
					db.close();
				}
			}
			catch(IOException e){
				System.out.println("Error occurred when trying to create new database file: " + e.getMessage());
			}
			catch(SqlJetException e){
				System.out.println("Error occurred when trying to access database file: " + e.getMessage());
			}
		}
	}

	public String readAccessToken() throws SqlJetException{
		SqlJetDb db = SqlJetDb.open(dbFile, false);
		try{
			ISqlJetTable table = db.getTable(settingsTable);
			db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
			ISqlJetCursor cursor = table.order(table.getPrimaryKeyIndexName());
			if (!cursor.eof()) {
				do {
					if(cursor.getString(0).equals("accessToken")){
						return cursor.getString(1);
					}	        		
				} while(cursor.next());
			}
			return null;
		}
		finally{
			db.commit();
			db.close();
		}
	}
	
	public void setAccessToken(String token) throws SqlJetException{
		SqlJetDb db = SqlJetDb.open(dbFile, true);
		try{
			db.beginTransaction(SqlJetTransactionMode.WRITE);
			ISqlJetTable table = db.getTable(settingsTable);
			table.insert("accessToken", token);
			System.out.println("saved access token");
		}
		finally{
			db.commit();
			db.close();
		}
	}

}
