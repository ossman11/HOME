import closset.DataBase;
import com.Page;
import entrance.HOME;
import network.HTTP;
import closset.DataBase.AnalyticsTable;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by bobde on 28.05.2016.
 */
public class Analytics extends Page {
    // Public Values
    public AnalyticsTable Data;

    // Private Values
    private static final String URL = "/Analytics";
    private static final String TITLE = "Analytics";

    // Public Functions
    public void init(HOME h){
        Data = h.database.CreateAnalyticsTable("activity");
        Data.load();
    }

    public void close(){
        Data.save();
    }

    protected String doGet(HTTP.HTTPHandler Req) {
        Data.addRow(title, new String[]{"Date","Page"}, new Object[]{Req.Home.getServerTime(),title});
        String ret = "<h1>"+ title +"</h1>"+ Data.size() + "<br/>";

        ret += Data.getColumnAsString("Date");

        return ret;
    }

    protected String doPost(HTTP.HTTPHandler Req) {
        return doGet(Req);
    }

    // Private Functions

    // Public Classes

    // Private Classes

    // Page Constructors
    public Analytics(){
        url = URL;
        title = TITLE;
    }
}
