package k2gps.controlvehiculo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by jhidalgo on 29/05/15.
 */
public class WebClient extends ActionBarActivity
{

    public String viewurl,htmlString;
    WebView myWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.webclient);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Boton de atras
        Intent intent=getIntent();
        String titulo=intent.getExtras().getString("title");
        if(titulo!=null)
            getSupportActionBar().setTitle(titulo);
        else
            getSupportActionBar().setTitle("Control Vehículo");
        viewurl= intent.getExtras().getString("url"); //Url
        System.out.println("url "+viewurl);
        htmlString=intent.getExtras().getString("html");
        myWebView = (WebView) findViewById(R.id.my_webView);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        if(!viewurl.equals("empty"))//Vale "null" si ha pulsado recorrido
        {
            myWebView.loadUrl(viewurl);
        }
        else
        {
            myWebView.loadData(htmlString, "text/html", null);
        }

        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }
        });
    }
    //El uso de webclient solo ocurre al pulsar el boton de ultima posicion, y obtener posicion actual
    // Ya que obtener posicion actual no refrezca el textview de main activity, en este metodo, se indica al entrar en pausa o destruirse,
    //que el primer boton no ha sido pulsado, esto por cuestiones de validacion y comportamiento de la aplicacion
    @Override
    protected void onPause() {
        super.onPause();
        MainActivity.firstbuttonclicked=false;
        finish();
        System.out.println("on pause web");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //clearApplicationData();
        MainActivity.firstbuttonclicked=false;
        System.out.println("on destroy web");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {  //Menu de preferencias, mostrar pantalla de preferencias
            try {
                Intent intent = new Intent ();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(android.content.Intent.EXTRA_TEXT,myWebView.getUrl());
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent,"Compartir posición en..."));

            }
            catch (Exception e)
            {
                Log.e("prefcall", e.toString());
            }
            return true;
            /*SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage("", null,"Hello ", null, null);*/
        }
        return super.onOptionsItemSelected(item);
    }
    /*public void clearApplicationData()
    {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists())
        {
            String[] children = appDir.list();
            for (String s : children)
            {
                if (!s.equals("lib") && !s.equals("files"))
                    deleteDir(new File(appDir, s));
                Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
            }
        }
    }

    public static boolean deleteDir(File dir)
    {
        if (dir != null && dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success)
                    return false;
            }
        }
        return dir.delete();
    }*/


}

