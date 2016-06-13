package hax3.city;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class Login extends Activity {

    public static final String LOCKER_NUM = "LOCKERNUM";
    public static final String USER_ID = "USERID";
    String userid;
    String password;
    private EditText editTextUserId;
    private EditText editTextPassword;
    private  int lockerNum;

    /*자동로그인*/

    CheckBox Auto_LogIn;
    SharedPreferences setting;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUserId = (EditText) findViewById(R.id.editTextUserId);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);

        Auto_LogIn = (CheckBox) findViewById(R.id.Auto_LogIn);
        setting = getSharedPreferences("setting", 0);
        editor= setting.edit();

        if(setting.getBoolean("Auto_Login_enabled", false)){
            editTextUserId.setText(setting.getString("ID", ""));
            editTextPassword.setText(setting.getString("PW", ""));
            userid = editTextUserId.getText().toString();
            password = editTextPassword.getText().toString();
            login(userid, password);
        }

        Auto_LogIn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if(isChecked){
                    editor.putString("ID", userid);
                    editor.putString("PW", password);
                    editor.putBoolean("Auto_Login_enabled", true);
                    editor.commit();
                }else{
                    editor.remove("ID");
                    editor.remove("PW");
                    editor.remove("Auto_Login_enabled");
                    editor.clear();
                    editor.commit();
                }
            }
        });
    }

    public void invokeRegister(View view) {
        Intent intent = new Intent(Login.this, register.class);
        startActivityForResult(intent, 0);
    }

    public void invokeLogin(View view) {
        userid = editTextUserId.getText().toString();
        password = editTextPassword.getText().toString();

        login(userid, password);
    }

    private void login(final String userid, final String password) {

        class LoginAsync extends AsyncTask<String, Void, String> {

            private Dialog loadingDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loadingDialog = ProgressDialog.show(Login.this, "Please wait", "Loading...");
            }

            @Override
            protected String doInBackground(String... params) {
                String id = params[0];
                String pass = params[1];

                InputStream is = null;
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("userid", id));
                nameValuePairs.add(new BasicNameValuePair("password", pass));
                String result = null;

                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(
                            "http://ec2-52-39-206-204.us-west-2.compute.amazonaws.com/login_city.php");
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);

                    HttpEntity entity = response.getEntity();

                    is = entity.getContent();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    result = sb.toString();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                String k = result.trim();
                String s = "fail";
                loadingDialog.dismiss();
                try {
                    JSONObject jsonObj = new JSONObject(k);
                    JSONArray data = jsonObj.getJSONArray("result");
                    JSONObject rsult = data.getJSONObject(0);
                    s = rsult.getString("result");
                    lockerNum = rsult.getInt("lockerNum");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (s.equalsIgnoreCase("success")) {
                    editor.putString("ID", userid);
                    editor.putString("PW", password);
                    editor.commit();
                    Intent intent = new Intent(Login.this, MainActivity.class);
                    intent.putExtra(USER_ID, userid);
                    intent.putExtra(LOCKER_NUM, lockerNum);
                    finish();
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Invalid User Name or Password", Toast.LENGTH_LONG).show();
                }
            }
        }
        LoginAsync la = new LoginAsync();
        la.execute(userid, password);
    }

}