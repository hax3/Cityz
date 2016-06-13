package hax3.city;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends Activity {

    String myJSON;

    private static final String TAG_RESULTS="result";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";

    JSONArray peoples = null;

    ArrayList<HashMap<String, String>> personList;

    ListView list;
    String clicked;
    TextView selected;
    Button infoButton;
    Button mapButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = (ListView) findViewById(R.id.listView);
        selected = (TextView) findViewById(R.id.clicked_spot);
        infoButton = (Button) findViewById(R.id.info_button);
        mapButton = (Button) findViewById(R.id.map_button);
        personList = new ArrayList<HashMap<String,String>>();
        getData("http://ec2-52-39-206-204.us-west-2.compute.amazonaws.com/getdata_city.php");
        list.setOnItemClickListener(listener);
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    AdapterView.OnItemClickListener listener= new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // TODO Auto-generated method stub
            //클릭된 아이템의 위치를 이용하여 데이터인 문자열을 Toast로 출력
            selected.setText(personList.get(position).get(TAG_NAME));
            clicked = personList.get(position).get(TAG_NAME);
        }
    };
    protected void showList(){
        try {
            JSONObject jsonObj = new JSONObject(myJSON);
            peoples = jsonObj.getJSONArray(TAG_RESULTS);

            for(int i=0;i<peoples.length();i++){
                JSONObject c = peoples.getJSONObject(i);
                String id = c.getString(TAG_ID);
                String name = c.getString(TAG_NAME);

                HashMap<String,String> persons = new HashMap<String,String>();

                persons.put(TAG_ID,id);
                persons.put(TAG_NAME,name);

                personList.add(persons);
            }

            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, personList, R.layout.list_item,
                    new String[]{TAG_ID,TAG_NAME},
                    new int[]{R.id.id, R.id.name}
            );

            list.setAdapter(adapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void getData(String url){
        class GetDataJSON extends AsyncTask<String, Void, String>{

            @Override
            protected String doInBackground(String... params) {

                String uri = params[0];

                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();

                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;
                    while((json = bufferedReader.readLine())!= null){
                        sb.append(json+"\n");
                    }

                    return sb.toString().trim();

                }catch(Exception e){
                    return null;
                }



            }

            @Override
            protected void onPostExecute(String result){
                myJSON=result;
                showList();
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);
    }

}