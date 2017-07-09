package k2gps.controlvehiculo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by jhidalgo on 15/06/15.
 */
public class Intro extends Activity
{
   // public TextView key;

    public static String saveduser=null;
    String clave,usuario,temp;
    private EditText pass,user;
    void grantAccess()
    {
        Intent show= new Intent(Intro.this, MainActivity.class);
        startActivity(show);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button continuar;
        /*SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        clave = sharedPref.getString("gps_clave", "*");

        if(clave=="*")
        {
            grantAccess();
        }
        else
        {*/
        setContentView(R.layout.intro);
        pass= (EditText) findViewById(R.id.pass);
        user= (EditText) findViewById(R.id.user);
        continuar= (Button) findViewById(R.id.continuar);
        try{
            FileInputStream in= openFileInput("user.txt");
            InputStreamReader reader = new InputStreamReader(in);
            char[] inputbuffer = new char[64];
            reader.read(inputbuffer);
            saveduser=new String(inputbuffer);
            saveduser=saveduser.substring(0,saveduser.indexOf('\0'));
            user.setText(saveduser);
            System.out.println(saveduser+"*");
            in.close();
        }
        catch (FileNotFoundException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }


        // key= (TextView) findViewById(R.id.key);
        //key.setGravity(Gravity.CENTER_HORIZONTAL);
        continuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  /*  if(pass.getText().toString().equals(clave))
                        grantAccess();
                    else
                        Toast.makeText(getBaseContext(), "Clave Incorrecta",
                                Toast.LENGTH_SHORT).show();*/
                usuario = user.getText().toString();
                clave = pass.getText().toString();
                if (!usuario.isEmpty() && !clave.isEmpty()) // Si los campos no estan vacios
                    new MyAsyncTask().execute();
            }
        });
        //}
        /*SharedPreferences.Editor preferencesEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        //Clear all the saved preference values.
        preferencesEditor.clear();
        //Read the default values and set them as the current values.
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, true);
       // PreferenceManager.set
        //Commit all changes.
        preferencesEditor.commit();
        // finish();
*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("On start intro");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        System.out.println("On restart intro");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    protected void onPause() {
       super.onPause();
        System.out.println("Pause Intro");
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        System.out.println("Destroy Intro");
    }

    class MyAsyncTask extends AsyncTask<String, String, Void> {
        private ProgressDialog progressDialog = new ProgressDialog(Intro.this);
        InputStream inputStream = null;
        String result = "", connerror = "";
        boolean datosok = false;

        protected void onPreExecute() {
            progressDialog.setMessage("Ingresando...");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    MyAsyncTask.this.cancel(true);
                }
            });
        }

        @Override
        protected Void doInBackground(String... params) {
            boolean connok = false;
            Uri urix = new Uri.Builder()
                    .scheme("https")
                    .authority("www.orioncorp.com.ve")
                    .path("mprs/gps_applogin.php")
                   // .appendQueryParameter("gt", gps_telno)
                    //.appendQueryParameter("gc", gps_clave)
                    .appendQueryParameter("mprs_lu", "crcaicedo@gmail.com")
                    .appendQueryParameter("mprs_lp", "OrionCorp34")
                    .appendQueryParameter("gu", usuario)
                    .appendQueryParameter("gp", clave)
                    .build();
            //Log.e("getpos", urix.toString());
            try {
                URL url = new URL(urix.toString());
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                inputStream = new BufferedInputStream(urlConnection.getInputStream());
                connok = true;
            }
            catch (Exception e4) {
                Log.e("Excepcion", e4.toString());
                connerror = e4.toString();
            }
            // Convert response to string using String Builder
            if (connok) {
                try
                {
                    BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
                    StringBuilder sBuilder = new StringBuilder();

                    String line = null;
                    while ((line = bReader.readLine()) != null) {
                        sBuilder.append(line + "\n");
                    }

                    inputStream.close();
                    result = sBuilder.toString();
                    datosok = true;
                }
                catch (Exception e)
                {
                    Log.e("StringBuilding", e.toString());
                    connerror = e.toString();
                }
            }
            return null;
        } // protected Void doInBackground(String... params)

        protected void onPostExecute(Void v) {
            //parse JSON data
            int i;
            this.progressDialog.dismiss();
            if (datosok) {
                try {
                    String login;
                    JSONObject jObject = new JSONObject(result);


                    System.out.println("*** "+ result);
                    try
                    {
                        login=jObject.getString("loginok");
                        System.out.println(login.equals("1"));
                        System.out.println(login);
                        if(login.equals("1"))//Login exitoso!
                        {
                            JSONArray vehiculos = jObject.getJSONArray("vh");
                            JSONArray first;
                            int len= vehiculos.length();
                            FileOutputStream fos = openFileOutput("user.txt", Context.MODE_PRIVATE);
                            fos.write(usuario.getBytes());
                            fos.close();
                            Intent show= new Intent(Intro.this,MainActivity.class);
                            show.putExtra("user",usuario);
                            show.putExtra("pass", clave);
                            len=vehiculos.length();
                            show.putExtra("nvehic", String.valueOf(len));
                            if(!saveduser.equals(usuario))
                            {
                                System.out.println("Different users");
                                show.putExtra("delete","1");
                            }
                            else
                                show.putExtra("delete","0");
                            System.out.println("// "+len);
                            for(i=0;i<len;i++)
                            {
                                first= vehiculos.getJSONArray(i);
                                System.out.println(first);
                                show.putExtra("gps_id" + i, first.getString(0));
                                show.putExtra("vehic" + i, first.getString(1));
                                show.putExtra("gps_telno"+i,first.getString(2));
                                show.putExtra("gps_clave" + i, first.getString(3));

                            }
                            //System.out.println(show.getString("gps_clave4"));
                            if(len>0)
                               startActivity(show);
                            else
                                Toast.makeText(getBaseContext(), "No hay vehículos asociados",
                                        Toast.LENGTH_LONG).show();

                        }
                        else
                        {
                            Toast.makeText(getBaseContext(), "Usuario o clave incorrecta",
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                    catch(JSONException e)
                    {
                       // Intent show= new Intent(Intro.this,MainActivity.class);
                        //startActivity(show);
                        /*WebView webview = (WebView) findViewById(R.id.webView);
                        WebSettings settings = webview.getSettings();
                        settings.setJavaScriptEnabled(true);
                        webview.loadUrl(URL);*/
                        Toast.makeText(getBaseContext(),"Fallo en la conexión",
                                Toast.LENGTH_LONG).show();
                        System.out.println(e.getMessage());

                    }
                } catch (JSONException e) {
                    Toast.makeText(getBaseContext(),"Fallo en la conexión",
                            Toast.LENGTH_LONG).show();
                    System.out.println(e.getMessage());
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(),"Fallo en la conexión",
                            Toast.LENGTH_LONG).show();
                    System.out.println(e.getMessage());
                }
            }
            else
            {
                Toast.makeText(getBaseContext(), "Error en la conexión a Internet",
                        Toast.LENGTH_LONG).show();
            }
        } // protected void onPostExecute(Void v)
    } //class MyAsyncTask extends AsyncTask<String, String, Void>
}