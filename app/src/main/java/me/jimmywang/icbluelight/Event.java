package me.jimmywang.icbluelight;

/**
 * This object is used under student role. It stores an help event (in hash map) and push the event to Firebase real-time database
 * using Firebase library. A student user can only create one event at the time. If user exit the app and attempt to create a new event again
 * the second constructor will be called to resume the connection to database by a id store in SharedPreferences.
 * Note that once the event is created username, additionMessage and detailLocation can not be updated.
 *
 * Created by yanmingwang on 12/10/16.
 */

import android.content.SharedPreferences;
import android.util.Log;
import com.google.firebase.database.DatabaseReference;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Event {

    private DatabaseReference mDatabase;
    private DatabaseReference ref; //database path
    private String eventId;

    /**
     * Required empty public constructor
     */
    public Event(){
        // Required empty public constructor

    }

    /**
     * This constructor is called when user need resume (before user click on I'm safe) to last requested help session.
     * It will get the last session id from shared preferences and reconstruct the database path as red
     *
     * @param mDatabase Database URL reference
     * @param settings SharedPreferences pointer
     */

    public Event(DatabaseReference mDatabase, SharedPreferences settings) {
        this.mDatabase = mDatabase;;
        this.eventId = settings.getString("evenId","");
        ref = mDatabase.child(eventId);

        //debug Info
        Log.e("LOG_EORROR", settings.getString("evenId","") + " OR");
    }

    /**
     * This constructor is called when a student start a event the first time.
     * This will create an help event (in hash map) and push the event to Firebase real-time database using Firebase library.
     *
     *
     * @param mDatabase top database reference
     * @param longitude user longitude
     * @param latitude user latitude
     * @param username username
     * @param additionMessage additionMessage
     * @param detailLocation detailLocation
     * @param settings SharedPreferences pointer
     */
    public Event(DatabaseReference mDatabase, String longitude, String latitude, String username, String additionMessage, String detailLocation, SharedPreferences settings) {
        //create a new id in database and return the path as ref
        ref = mDatabase.push();

        //get the new Id that just created and put it into SharedPreferences
        this.eventId = ref.getKey();
        settings.edit().putString("evenId",eventId).apply();

        //debug Info
        Log.e("LOG_EORROR", settings.getString("evenId","") + " OR");

        String timestemp = DateFormat.getDateTimeInstance().format(new Date());
        this.mDatabase = mDatabase;

        //create new hasMap
        Map newData= new HashMap();
        newData.put("id",eventId);
        newData.put("longitude",longitude);
        newData.put("latitude",latitude);
        newData.put("username",username);
        newData.put("additionMessage",additionMessage);
        newData.put("detailLocation",detailLocation);
        newData.put("timestemp", timestemp);

        //push the map to database as new obj
        ref.updateChildren(newData);

    }

    /**
     * This method will update user location and timestamp and update to the database
     *
     * @param longitudeNow new longitude
     * @param latitudeNow new latitude
     */
    public void update(String longitudeNow, String latitudeNow){
        //create new hasMap
        Map newData= new HashMap();
        newData.put("longitude",longitudeNow);
        newData.put("latitude",latitudeNow);
        newData.put("timestemp",DateFormat.getDateTimeInstance().format(new Date()));
        //update the map to database
        ref.updateChildren(newData);
    }

    /**
     * Remove event in the database
     */
    public void delete(){
        mDatabase.child(eventId).removeValue();

    }
}
