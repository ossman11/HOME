import com.Page;
import network.HTTP;

import java.util.Random;

/**
 * Created by bobde on 27.05.2016.
 */
public class Home extends Page {
    // Public Values

    // Private Values
    private static final String URL = "/";
    private static final String TITLE = "Home";

    // Public Functions
    protected String doGet(HTTP.HTTPHandler Req) {
        String ret = "<h1>Welcome Home</h1>" +
                "<form action='/' method='post'>" +
                "<input TableName='User' type='text' />" +
                "<input TableName='Pass' type='password' />" +
                "<input TableName='Auth' list='levels' />" +
                "<datalist id='levels'>" +
                "	<option value='Master'>" +
                "	<option value='Admin'>" +
                "	<option value='Helper'>" +
                "	<option value='User'>" +
                "</datalist>" +

                "<input value='Submit' type='submit' />" +
                "</form>";
		/*
		long start = new Date().getTime();
		String dbtable = Req.Home.database.tables.get(0).toHTML();
		long e = new Date().getTime();
		*/
        ret += Req.Home.database.Auth.toHTML();
        // ret += dbtable;

        return	ret;

    }

    protected String doPost(HTTP.HTTPHandler Req) {
        String[] vals = Req.Content.split("&");
        if(vals.length == 3){
            String[] Row = new String[4];
            Row[2] = new Random().nextInt() + "";

            for(int i = 0; i < vals.length; i++) {
                String[] cur = vals[i].split("=");
                if(cur.length == 1){cur = new String[]{cur[0],""};}
                switch(cur[0]) {
                    case "User": Row[0] = cur[1];
                        break;
                    case "Pass": Row[1] = (cur[1] + Row[2]).hashCode() + "";
                        break;
                    case "Auth":
                        if(cur[1].isEmpty()){
                            Row[3] = "User";
                        } else {
                            Row[3] = cur[1];
                        }
                        break;
                    default: continue;
                }
            }
            Req.Home.database.Auth.Add(Row);
        }

        return doGet(Req);
    }

    // Private Functions

    // Public Classes

    // Private Classes

    // Page Constructors
    public Home(){
        url = URL;
        title = TITLE;
    }
}