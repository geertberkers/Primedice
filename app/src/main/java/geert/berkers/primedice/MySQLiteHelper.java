package geert.berkers.primedice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;

/**
 * Primedice Application Created by Geert on 23-9-2015
 */
class MySQLiteHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "MyBetsDB";

    // Bet table name
    private static final String TABLE_BET = "Bet";

    // Bet Table Columns names
    private static final String ID = "id";
    private static final String USER = "user";
    private static final String PROFIT = "profit";
    private static final String WIN = "win";
    private static final String AMOUNT = "amount";
    private static final String CONDITION = "condition";
    private static final String TARGET = "target";
    private static final String ROLL = "roll";
    private static final String NONCE = "nonce";
    private static final String CLIENT = "client";
    private static final String MULTIPLIER = "multiplier";
    private static final String TIMESTAMP = "timestamp";
    private static final String SERVER = "server";

    private static final String[] COLUMNS = {ID, USER, PROFIT, WIN, AMOUNT, CONDITION, TARGET, ROLL, NONCE, CLIENT, MULTIPLIER,TIMESTAMP,SERVER};

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //SQL statement to create BET TABLE
        String CREATE_BET_TABLE = "CREATE TABLE Bet( " +
                    "id VARCHAR(15) PRIMARY KEY, " +
                    "user VARCHAR(20), " +
                    "profit VARCHAR(15), " +
                    "win VARCHAR(1), " +
                    "amount VARCHAR(15), " +
                    "condition VARCHAR(1), " +
                    "target VARCHAR(5), " +
                    "roll VARCHAR(5), " +
                    "nonce VARCHAR(10), " +
                    "client VARCHAR(50), " +
                    "multiplier VARCHAR(5), " +
                    "timestamp VARCHAR(25), " +
                    "server VARCHAR(25)" +
                ")";

        // Execute the statement
        db.execSQL(CREATE_BET_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop current table
        db.execSQL("DROP TABLE IF EXISTS Bet");

        // create new table
        this.onCreate(db);
    }

    // Add a bet
    public void addBet(Bet bet) {
        Log.d("addBet", bet.toString());

        // 1. Get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. Create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(ID, bet.getIDString());
        values.put(USER, bet.getPlayer());
        values.put(PROFIT, bet.getProfit());
        if (bet.getWinOrLose()) {values.put(WIN, "Y");} else {values.put(WIN, "N");}
        values.put(AMOUNT, bet.getWagered());
        values.put(CONDITION, bet.getBetGame().substring(0,1));
        values.put(TARGET, bet.getBetGame().substring(1));
        values.put(ROLL, bet.getRoll());
        values.put(NONCE, bet.getNonce());
        values.put(CLIENT, bet.getClientseed());
        values.put(MULTIPLIER, bet.getPayout());
        values.put(TIMESTAMP, bet.getTimeOfBet());
        values.put(SERVER, bet.getServerseed());

        // 3. Insert bet in table
        try {
            db.insert(TABLE_BET, null, values);
        } catch (Exception ex) {
            Log.e("Error 1", ex.toString());
        }
        // 4. Close
        db.close();
    }

    // Get all bets from one user
    public ArrayList<Bet> getAllBetsFromUser(String username) {
        ArrayList<Bet> betList = new ArrayList<>();

        // 1. Get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. Build the query
        Cursor cursor = db.query(TABLE_BET, COLUMNS, " user = ?", new String[]{username}, null, null, null, null);

        // 3. Check each row and create Bet
        Bet bet;
        if (cursor.moveToFirst()) {
            do {
                long id =  Long.valueOf(cursor.getString(0).replace(",", ""));
                String user = cursor.getString(1);
                int profit = (int) (Double.valueOf(cursor.getString(2)) * 100000000);
                String win =cursor.getString(3);
                int amount =(int) (Double.valueOf(cursor.getString(4)) * 100000000);
                String condition = cursor.getString(5);
                Double target = Double.valueOf(cursor.getString(6));
                Double roll = Double.valueOf(cursor.getString(7));
                int nonce = Integer.valueOf(cursor.getString(8));
                String client = cursor.getString(9);
                //todo: remove replace when bets deleted
                Double multiplier = Double.valueOf(cursor.getString(10).replace(",","."));
                String timestamp = cursor.getString(11);
                String server = cursor.getString(12);

                bet = new Bet(id,user,profit,win,amount,condition,target,roll,nonce,client,multiplier,timestamp,server);

                betList.add(0, bet);
            } while (cursor.moveToNext());
        }

        Log.d("getAllBets()", betList.toString());

        // 4. Close database
        cursor.close();
        db.close();

        // 5. Return bets
        return betList;
    }

    // Delete a bet
    public void deleteBet(Bet bet) {
        // 1. Get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. Delete
        db.delete(TABLE_BET, ID + " = ?", new String[]{bet.getIDString()});

        // 3. Close
        db.close();

        Log.d("deleteBet", bet.toString());
    }
}