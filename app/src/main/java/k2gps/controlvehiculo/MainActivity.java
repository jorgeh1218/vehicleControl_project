package k2gps.controlvehiculo;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends ActionBarActivity {

   // private static int COMPRESS_HEIGHT=864;
   // private static int COMPRESS_WIDTH=486;

   // private static String SENT = "SMS_SENT";
   // private static String DELIVERED = "SMS_DELIVERED";
   // private static String SMS_RECEIVEDRECEIVED = "SMS_RECEIVED";
    public static long idLastMessage=-1;
    private static int MAX_SMS_MESSAGE_LENGTH = 160;
  //  public final static String EXTRA_MESSAGE="";
    public static String usuario=null, clave=null,lastpos;
    public int value;
    File image=null;
    private int isPhotoOnServer=0;
    private String mCurrentPhotoPath;
    private String mCurrentRoute;
    public boolean sharingPos=false;
    public boolean response=false;
    private boolean allowview=false;
    private String status_saved=null;
    protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
    public static String gps_telno, gps_clave,gps_id,status="";
    //public static String message_received=null;
    public static Bundle datos;//Datos provenientes del login
    public static Context c;
    //public static MainActivity actividad=null;
    public static boolean firstbuttonclicked = false; //Funciona como flag para saber si el boton que se presiono fue el primero, no se muestra el msj en textview
    void leerconf()//Obtiene datos principales de las preferencias definidas
    {
        //SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if(datos.getString("nvehic").equals("1")) //Si hay un solo vehiculo, configurar ese vehiculo para opciones sin entrar a preferencias
        {
            gps_telno=datos.getString("gps_telno0");
            gps_clave=datos.getString("gps_clave0");
            gps_id=datos.getString("gps_id0");
        }
        else
        {
           /* gps_telno = sharedPref.getString("gps_telno", "1234567");
            gps_clave = sharedPref.getString("gps_clave", "1234567");
            if(gps_id.equals("1234567")) // Si no encontro la configuracion, abrir el archivo interno de configuracion
            {*/
            gps_telno="1234567";
            gps_clave="1234567";
            gps_id="1234567";
                try{
                    System.out.println("No configurado, open file");
                    FileInputStream in= openFileInput("savedv.txt");
                    InputStreamReader reader = new InputStreamReader(in);
                    char[] inputbuffer = new char[64];
                    reader.read(inputbuffer);
                    String saved=new String(inputbuffer);
                    saved=saved.substring(0, saved.indexOf('\0'));
                    if(!saved.equals("-1"))
                    {
                        gps_telno=datos.getString("gps_telno"+saved);
                        gps_clave=datos.getString("gps_clave"+saved);
                        gps_id=datos.getString("gps_id"+saved);
                        if(gps_telno==gps_id)
                            gps_telno="";
                    }
                    in.close();
                }
                catch (FileNotFoundException e) {

                } catch (IOException e) {
                    e.printStackTrace();
                }
         //   }
        }
        //usuario = t
       //clave = sharedPref.getString("clave", "1234567");
    }
    void inicializarPref()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int i,n;
        boolean wasDeleted=false;
        c=this;
        String len,veh,telf,delete;
        System.out.println("Create main");
       // actividad=this;
        if(usuario==null && clave==null) // Si clave y usuario no han sido inicializadas
        {
            try {
            Bundle extras = getIntent().getExtras();
            datos=extras;
                SharedPreferences shared=PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            usuario = extras.getString("user");
            clave = extras.getString("pass");
            }
            catch(Exception e)
            {

            }
        }
        delete=datos.getString("delete");
        if(delete.equals("1"))//El archivo debe ser eliminado, nuevo usuario en el sistema
        {
            System.out.println("Delete 1");
            String dir = getFilesDir().getAbsolutePath();
            File f = new File(dir, "savedv.txt");
            System.out.println(f.delete());
            wasDeleted=true;
        }
        setContentView(R.layout.activity_main);
        if(!datos.getString("nvehic").equals("1") && wasDeleted) { //Si hay mas de un vehiculo hay que seleccionarlo y fue eliminado savedv
            Toast toast = Toast.makeText(getBaseContext(),"Configure el vehículo en herramientas", Toast.LENGTH_SHORT);
            toast.show();
        }
        else
        {
            if(!wasDeleted )//Aviso del vehiculo seleccionado
            {
                FileInputStream in= null;
                try {
                    in = openFileInput("savedv.txt");
                    InputStreamReader reader = new InputStreamReader(in);
                    char[] inputbuffer = new char[64];
                    reader.read(inputbuffer);
                    String result=new String(inputbuffer);
                    result=result.substring(0,result.indexOf('\0'));
                    in.close();
                    String vselected=datos.getString("vehic"+result);
                    if(vselected!=null)
                    {
                        getSupportActionBar().setTitle("Vehíc: "+vselected);
                        Toast toast = Toast.makeText(getBaseContext(), "Vehículo seleccionado: "+vselected, Toast.LENGTH_LONG);
                        toast.show();
                    }
                   else
                    {
                        Toast toast = Toast.makeText(getBaseContext(),"Configure el vehículo en herramientas", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK)
        {
           // boolean error=false;
            System.out.println("**/**/*/*/*/");
            compressImageFile();
            System.out.println("------");
            image.delete();
            new MyPost().execute();
           /* String uploadedfilename=gps_telno+".jpg";
            System.out.println(uploadedfilename);
            System.out.println(mCurrentRoute);
            System.out.println(mCurrentPhotoPath);
            new PostConnection(mCurrentPhotoPath,uploadedfilename);
*/
            /*ImageView mImageView = null;
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);*/
            // TODO Auto-generated method stubURL url = new URL("http://localhost:8080/handler");
           /* URL url = null;
            try {
                url = new URL("http://172.16.3.49:80/uploadimage.php");
            } catch (MalformedURLException e) {
                error=true;
            }
            if(!error)
            {
                HttpURLConnection con = null;
                try {
                    con = (HttpURLConnection)url.openConnection();
                    con.setDoInput(true);
                    con.setDoOutput(true);
                    con.setUseCaches(false);
                    con.setRequestProperty("Content-Type", "image/jpeg");
                    con.setRequestProperty("Connection", "Keep-Alive");
                    con.setRequestMethod("POST");
                    InputStream in = new FileInputStream(f);
                    OutputStream out = con.getOutputStream();
                    copy(in, con.getOutputStream());
                    out.flush();
                    out.close();
                    BufferedReader r = new BufferedReader(new  InputStreamReader(con.getInputStream()));

                    System.out.println("Output:");
                    for (String line = r.readLine(); line != null;  line = r.readLine())
                        System.out.println(line);
                } catch (IOException e) {
                    error=true;
                }
            }
            */


            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/jpeg");
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + mCurrentPhotoPath));
            startActivity(Intent.createChooser(shareIntent, "Compartir imagen en.."));
        }
    }
    //Usado en el metodo de arriba
    protected static long copy(InputStream input, OutputStream output)
            throws IOException {
        byte[] buffer = new byte[12288]; // 12K
        long count = 0L;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this) // Exit dialog
                .setTitle("Salir")
                .setMessage("Seguro que desea salir?")
                .setPositiveButton("NO", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }

                })
                .setNegativeButton("SI", new DialogInterface.OnClickListener() // Cambiado por conveniencia del orden en que aparecen
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                }).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*Thread thread=  new Thread(){ // Cierra la app si esta pausada un determinado tiempo
            @Override
            public void run(){
                try {
                    synchronized(this){
                        wait(10000);
                        finish();
                    }
                }
                catch(InterruptedException ex){
                }

                // TODO
            }
        };
        thread.start();*/
    }
    @Override
    protected void onResume() {
        super.onResume();
        try {
            if(!SettingsActivity.vehicle.isEmpty())
            {
                getSupportActionBar().setTitle("Vehíc: "+SettingsActivity.vehicle);
                //System.out.println(SettingsActivity.vehicle+" *****");
            }

        }
        catch (Exception e)
        {
            //System.out.println("Excepcionnnnnn");
        }

        //System.out.println("On Resume main");
       // actividad = this;
    }
    @Override
    protected void onPause() {
        super.onPause();
      //  System.out.println("Pause main");
        //actividad = this;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //System.out.println("Destroy main");
        //actividad = null;
        usuario=null;
        clave=null;
        SharedPreferences.Editor preferencesEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        //Clear all the saved preference values.
        preferencesEditor.clear();
        //Read the default values and set them as the current values.
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, true);
        //Commit all changes.
        preferencesEditor.commit();
       // clearApplicationData();
        System.runFinalization();
        android.os.Process.killProcess(android.os.Process.myPid());//Kill efectivo de la app
    }
    private boolean checkIfNumberIsValid()
    {
        if(gps_telno=="1234567" || gps_telno.isEmpty())
            return false;
        return true;
    }
    @Override
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Button button = (Button) findViewById(R.id.button); // consultar posicion en tracker
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                leerconf();
                if (checkIfNumberIsValid())//Numero no valido
                {
                    value = 5000;// Usado como espera inicial para chequear inbox de msjs
                    response = false;
                    //////////////////////////////////////////////////////
                    Thread thread = new Thread() { // Inicia un thread que permite esperar 4.5 segundos antes de iniciar la llamada, y muestra un aviso
                        @Override
                        public void run() {
                            try {
                                synchronized (this) {
                                    firstbuttonclicked = true;
                                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                                    callIntent.setData(Uri.parse("tel:" + gps_telno));
                                    startActivity(callIntent);
                                }
                            } catch (Exception e) {

                            }

                            // TODO
                        }
                    };
                    //////////////////////////////////////////////////////
                    thread.run();
                    // BroadcastReceiver receiver = new IncomingSms();
                    new AsynckWeb().execute();
                }
                else
                {
                    Toast.makeText(getBaseContext(), "No existe número celular registrado",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        Button button2 = (Button) findViewById(R.id.button2); // ultima posicion en servidor
        button2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                leerconf();
                firstbuttonclicked=false;
                if(gps_id!="1234567")
                {
                    firstbuttonclicked = false;
                    new MyAsyncTask().execute();
                }
                else
                {
                    Toast.makeText(getBaseContext(), "No se puede realizar la operación",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        Button button3 = (Button) findViewById(R.id.button3); // apagar vehiculo
        button3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity.this) // Exit dialog
                        .setTitle("Apagar")
                        .setMessage("Seguro que desea apagar el vehículo?")
                        .setPositiveButton("NO", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }

                        })
                        .setNegativeButton("SI",  new DialogInterface.OnClickListener() // Cambiado por conveniencia del orden en que aparecen
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                leerconf();
                                if(checkIfNumberIsValid()) {
                                    firstbuttonclicked = false;
                                    allowview=false;
                                    // todo: set estatus
                                    sendSMS(gps_telno, "stop" + gps_clave);
                                }
                                else
                                    Toast.makeText(getBaseContext(), "No existe número celular registrado",
                                            Toast.LENGTH_LONG).show();
                            }

                        }).show();

            }
        });

        Button button4 = (Button) findViewById(R.id.button4); // prender vehiculo
        button4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity.this) // Exit dialog
                        .setTitle("Encender")
                        .setMessage("Seguro que desea encender el vehículo?")
                        .setPositiveButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }

                        })
                        .setNegativeButton("SI", new DialogInterface.OnClickListener() // Cambiado por conveniencia del orden en que aparecen
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                leerconf();
                                if (checkIfNumberIsValid()) {
                                    firstbuttonclicked = false;
                                    allowview=false;
                                    // todo: set estatus
                                    sendSMS(gps_telno, "resume" + gps_clave);
                                }
                                else
                                    Toast.makeText(getBaseContext(), "No existe número celular registrado",
                                            Toast.LENGTH_LONG).show();
                            }

                        }).show();

            }
        });

        Button button5 = (Button) findViewById(R.id.button5); // chequeo tracker
        button5.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                leerconf();
                if (checkIfNumberIsValid()) {
                    firstbuttonclicked = false;
                    allowview=true;
                    sendSMS(gps_telno, "check" + gps_clave);
                }
                else
                {
                    Toast.makeText(getBaseContext(), "No existe número celular registrado",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        Button buttonshare = (Button) findViewById(R.id.buttonshare);
        buttonshare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                leerconf();
                if (checkIfNumberIsValid())//Numero no valido
                {
                    sharingPos = true;
                    value = 5000;// Usado como espera inicial para chequear inbox de msjs
                    response = false;
                    //////////////////////////////////////////////////////
                    Thread thread = new Thread() { // Inicia un thread que permite esperar 4.5 segundos antes de iniciar la llamada, y muestra un aviso
                        @Override
                        public void run() {
                            try {
                                synchronized (this) {
                                    firstbuttonclicked = false;
                                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                                    callIntent.setData(Uri.parse("tel:" + gps_telno));
                                    startActivity(callIntent);
                                }
                            } catch (Exception e) {

                            }

                            // TODO
                        }
                    };
                    //////////////////////////////////////////////////////
                    thread.run();
                    // BroadcastReceiver receiver = new IncomingSms();
                    // BroadcastReceiver receiver = new IncomingSms();
                    new AsynckWeb().execute();
                }
                else
                    Toast.makeText(getBaseContext(), "No existe número celular registrado",
                            Toast.LENGTH_LONG).show();
            }
        });
        /*Button button6 = (Button) findViewById(R.id.button6); // preferencias
        button6.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    firstbuttonclicked = false;
                    // System.out.println("+++ " +datos.getString("nvehic"));
                    Intent preferencia = new Intent(MainActivity.this, SettingsActivity.class);

                    preferencia.putExtras(datos); // Colocando  los datos de entrada para settings
                    startActivity(preferencia);
                } catch (Exception e) {
                    Log.e("prefcall", e.toString());
                }
            }
        });*/
        Button recorrido = (Button) findViewById(R.id.recorrido);
        recorrido.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                leerconf();
                if(gps_id!="1234567")
                   new MyRecorrido().execute();
                else
                    Toast.makeText(getBaseContext(), "No se puede realizar la operación",
                            Toast.LENGTH_LONG).show();
            }
        });
        Button foto= (Button) findViewById(R.id.buttonfoto);
        foto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                leerconf();
                if(gps_id!="1234567")
                {
                    new queryForPic().execute();
                }
                else
                    Toast.makeText(getBaseContext(), "No se puede realizar la operación",
                            Toast.LENGTH_LONG).show();
            }
        });
        return true;
    }
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    /*public static Bitmap decodeSampledBitmapFromResource(int resId,int reqWidth, int reqHeight)
    {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(m, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }*/
    private void compressImageFile()
    {
        int alto,ancho;
        try
        {
            File dir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize=4;//calculateInSampleSize(options,COMPRESS_HEIGHT,COMPRESS_WIDTH);
            options.inJustDecodeBounds=false;
            options.inScaled=false;
            Bitmap b=BitmapFactory.decodeFile(mCurrentPhotoPath, options);

            if(b!=null)
            {
                System.out.println(b.getWidth());
                System.out.println(b.getHeight());
                Bitmap out=Bitmap.createScaledBitmap(b, b.getWidth(), b.getHeight(), true);

                File file = new File(dir, "__idp"+".jpg");
                FileOutputStream fOut;
                try {
                    fOut = new FileOutputStream(file);
                    out.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                    fOut.flush();
                    fOut.close();
                    b.recycle();
                    out.recycle();
                    mCurrentPhotoPath=file.getAbsolutePath();
                } catch (Exception e)
                {
                    System.out.println("Excepcion "+e.getMessage());
                }
            }
        }
        catch(Exception e)
        {
            System.out.println("Excepcion "+e.getMessage());
        }

    }
    private File createImageFile() throws IOException {
        // Create an image file name
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        mCurrentRoute = storageDir.toString();
        image = File.createTempFile(
                "__idp",  /* prefijo */
                ".jpg",         /* sufijo */
                storageDir      /* directorio */
        );
        image.deleteOnExit();
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        mCurrentRoute=image.getName();
       // System.out.println(image.getName()+","+image.getCanonicalPath());
        System.out.println("--> "+mCurrentPhotoPath);
        return image;
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Handler
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Crea el archivo donde ira la foto
            System.out.println("Not entering");
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(getBaseContext(), "No hay espacio en el dispositivo",
                        Toast.LENGTH_LONG).show();
            }
            if (photoFile != null) {
                //mCurrentPhotoPath=Uri.fromFile(photoFile).toString();
                //System.out.println("++"+mCurrentPhotoPath);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        }
    }
    private String pullLink(String text) //Pull link de un mensaje entrante
    {
        int i,ilat,ilon,iurl;
        String lat,lon,url;
        iurl=text.indexOf("http");

        if(iurl!=-1)
        {
            url=text.substring(iurl);//Start en iurl
            iurl=url.indexOf("\n");
            url= url.substring(0, iurl);
            return url;//Retorna el url
        }
        ilat=text.indexOf("lat");
        ilon=text.indexOf("long");
        if(ilat!=-1 && ilon!=-1) // Si existen en el mensaje
        {
            ilat += 4; // asume formato lat:
            ilon += 5; // asume formato long:
            lon=text.substring(ilon);
            lat= text.substring(ilat);

            ilat=lat.indexOf("\n");
            lat=lat.substring(0, ilat);


            ilon=lon.indexOf("\n");
            lon=lon.substring(0, ilon);
            return "http://maps.google.com/maps?f=q&q=" + lat + "," + lon + "&z=16";
        }
        ilat=text.indexOf("lat");
        ilon=text.indexOf("lon");
        if(ilat!=-1 && ilon!=-1) // Si existen en el mensaje
        {
            ilat += 4; // asume formato lat:
            ilon += 4; // asume formato lon:
            lon=text.substring(ilon);
            lat= text.substring(ilat);

            ilat=lat.indexOf("\n");
            lat=lat.substring(0, ilat);


            ilon=lon.indexOf("\n");
            lon=lon.substring(0, ilon);
            return "http://maps.google.com/maps?f=q&q=" + lat + "," + lon + "&z=16";
        }
        return "http://maps.google.com/maps?f=q&q=" + "0.279039" + "," + "-23.4279874" + "&z=3";//String generico, localizacion mundial
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings || id==R.id.action_settings2) {  //Menu de preferencias, mostrar pantalla de preferencias
            try {
                Intent preferencia = new Intent(MainActivity.this, SettingsActivity.class);
                preferencia.putExtras(datos);
                firstbuttonclicked=false;
                startActivity(preferencia);
            }
            catch (Exception e)
            {
                Log.e("prefcall", e.toString());
            }
            return true;
        }
        if(id==R.id.action_exit)
        {
            new AlertDialog.Builder(this)
                    .setTitle("Salir")
                    .setMessage("Seguro que desea salir?")
                    .setPositiveButton("NO", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }

                    })
                    .setNegativeButton("SI",  new DialogInterface.OnClickListener() // Cambiado por conveniencia
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }

                    }).show();
        }

        return super.onOptionsItemSelected(item);
    }

    public void sendSMS(String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);
        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {

                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "Consultando",
                                Toast.LENGTH_SHORT).show();
                        // status.setText("Consultando...");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Error generico",
                                Toast.LENGTH_SHORT).show();
                        status = "Error Generico";
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "Sin servicio",
                                Toast.LENGTH_SHORT).show();
                        status = "Mensaje no enviado por falta de servicio";
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "PDU nulo",
                                Toast.LENGTH_SHORT).show();
                        //status.setText("Error: PDU Nulo");
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio apagado",
                                Toast.LENGTH_SHORT).show();
                        status = "Error: Radio Apagado";
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        /*Toast.makeText(getBaseContext(), "Consulta recibida",
                                Toast.LENGTH_SHORT).show();*/
                        //status.setText(status.getText() + " (Consulta recibida)");
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "No recibida",
                                Toast.LENGTH_SHORT).show();
                        status = "Consulta no recibida";
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
        if(allowview)
        {
            Intent newintent= new Intent(MainActivity.this,MsgScreen.class);
            newintent.putExtra("info",status);
            newintent.putExtra("tel", gps_telno);
            startActivity(newintent);
        }

    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //monitor phone call activities
    private class PhoneCallListener extends PhoneStateListener {

        private boolean isPhoneCalling = false;

        String LOG_TAG = "CONTROLVEHICULO";

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            if (TelephonyManager.CALL_STATE_RINGING == state) {
                // phone ringing
                System.out.println("Ringing");
                Log.i(LOG_TAG, "REPICANDO, numero: " + incomingNumber);
            }

            if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
                // active
                Log.i(LOG_TAG, "DESCOLGADO");

                isPhoneCalling = true;
            }

            if (TelephonyManager.CALL_STATE_IDLE == state) {
                // run when class initial and phone call ended,
                // need detect flag from CALL_STATE_OFFHOOK
                Log.i(LOG_TAG, "IDLE");

                if (isPhoneCalling) {

                    Log.i(LOG_TAG, "reiniciando app");
                    // restart app
                    Intent i = getBaseContext().getPackageManager()
                            .getLaunchIntentForPackage(
                                    getBaseContext().getPackageName());
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);

                    isPhoneCalling = false;
                }
            }
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class MyAsyncTask extends AsyncTask<String, String, Void> {
        private ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        InputStream inputStream = null;
        int error=0;
        String result = "", connerror = "";
        boolean datosok = false;

        protected void onPreExecute() {
            progressDialog.setMessage("Consultando posición...");
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
            System.out.println("gps clave "+gps_clave);
            Uri urix = new Uri.Builder()
                    .scheme("https")
                    .authority("www.orioncorp.com.ve")
                    .path("mprs/gps_getpos.php")
                    .appendQueryParameter("gi", gps_id)
                    .appendQueryParameter("gc", gps_clave)
                    .appendQueryParameter("mprs_lu", "crcaicedo@gmail.com")
                    .appendQueryParameter("mprs_lp", "OrionCorp34")
                    .appendQueryParameter("gu", usuario)
                    .appendQueryParameter("gp", clave)
                    .build();
            System.out.println("getpos "+urix.toString());
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
                    error=1;
                   /* Toast.makeText(getBaseContext(), "Error consultando los datos",
                            Toast.LENGTH_LONG).show();
                    Log.e("StringBuilding", e.toString());*/
                    connerror = e.toString();
                }
            }
            return null;
        } // protected Void doInBackground(String... params)

        protected void onPostExecute(Void v) {
            //parse JSON data
            this.progressDialog.dismiss();
            if (datosok) {
                try {
                    JSONObject jObject = new JSONObject(result);

                    String latitud = jObject.getString("latitud");
                    String longitud = jObject.getString("longitud");

                    String msj = jObject.getString("msj");

                    String tiempo = jObject.getString("tiempo");
                    System.out.println(jObject.toString());
                    Timestamp t= new Timestamp(Long.valueOf(tiempo));

                    Date date=new Date(t.getTime()*1000);
                    SimpleDateFormat formateador = new SimpleDateFormat(
                            "dd/MM/yyyy hh:mm a", new Locale("es_ES"));
                    String fecha = formateador.format(date);
                    System.out.println(fecha);

                    if (latitud.isEmpty() || longitud.isEmpty())
                    {
                        Toast.makeText(getBaseContext(), "No se encuentra la posición",
                                Toast.LENGTH_LONG).show(); // Por ahora para control
                    }
                    else {
                        String URL = "http://maps.google.com/maps?f=q&q=" + latitud + "," + longitud + "&z=16";
                        System.out.println(URL);
                        Intent show= new Intent(MainActivity.this,WebClient.class);
                        show.putExtra("url",URL);
                        show.putExtra("title", fecha);
                        startActivity(show);
                    /*WebView webview = (WebView) findViewById(R.id.webView);
                    WebSettings settings = webview.getSettings();
                    settings.setJavaScriptEnabled(true);
                    webview.loadUrl(URL);*/

                    }
                } catch (JSONException e) {
                    Log.e("JSONException", e.toString());
                    Toast.makeText(getBaseContext(), "No hay última posición para este vehículo",
                            Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e("Excepcion", e.toString());
                }
            }
            else
            {
                Toast.makeText(getBaseContext(), "Error del sistema",
                        Toast.LENGTH_LONG).show();
            }
        } // protected void onPostExecute(Void v)
    } //class MyAsyncTask extends AsyncTask<String, String, Void>
    class AsynckWeb extends AsyncTask<String, String, Void> {

        protected void onPreExecute() {
            idLastMessage=-1;
            System.out.println("OnPreExec");
            response=false;
            value=5000;
            lastpos="";
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(!sharingPos)
            {
                try {
                    Intent show = new Intent(MainActivity.this, WebClient.class);
                    lastpos = pullLink(lastpos);
                    show.putExtra("url", lastpos); // Compartiendo datos con la nueva actividad
                    Date current = new Date();
                    SimpleDateFormat formateador = new SimpleDateFormat(
                            "dd/MM/yyyy hh:mm a", new Locale("es_ES"));
                    String fecha = formateador.format(current);
                    show.putExtra("title", current);
                    startActivity(show);
                } catch (Exception e) {

                }
            }
            else // Lanzar intent de msj de texto
            {
                sharingPos=false;
                lastpos=pullLink(lastpos);
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.putExtra("sms_body", lastpos);
                sendIntent.setType("vnd.android-dir/mms-sms");
                startActivity(sendIntent);
            }
        }
        @Override
        protected Void doInBackground(String... params) {
            int count=-1;
            System.out.println("New Thread Asynctask");
            Thread t= new Thread(new Runnable() {
                @Override
                public void run() {
                    // Create Inbox box URI
                    Uri inboxURI = Uri.parse("content://sms/inbox");

                    // List required columns
                    String[] reqCols = new String[]{"_id", "address", "body"};

                    // Get Content Resolver object, which will deal with Content Provider
                    ContentResolver cr = getContentResolver();

                    // Fetch Inbox SMS Message from Built-in Content Provider
                    Cursor c = cr.query(inboxURI, reqCols, null, null, null);

                    if (c != null && c.moveToFirst()) {
                        long id = c.getLong(0);
                        String address = c.getString(1);
                        String body = c.getString(2);
                        System.out.println(address+"***");
                        System.out.println(id+"---***");
                        if (idLastMessage == -1 ) {
                            idLastMessage = id;
                        }
                        else {
                            if (id != idLastMessage)//Si el mensaje es de
                            {
                                System.out.println("New Msg");
                                System.out.println(gps_telno);

                                if (address.equals(gps_telno) ||  address.equals("+58"+gps_telno.substring(1)) ||
                                        address.contains(gps_telno.substring(1)))
                                {  //Codigo de area
                                    lastpos=body;
                                    System.out.println("Change");
                                    response=true;
                                    c.close(); // Modified 28.08
                                }

                            }
                        }
                        try {
                            synchronized(this){
                                try {
                                    Thread.sleep(value);
                                    value=3000;
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        catch(Exception e)
                        {
                            response=true;
                        }
                    }
                }
            });
            while(!response) //Running mientras response sea falso
            {
                t.run();
            }
            return null;
        } // protected Void doInBackground(String... params)
    }
////////////////////////////////////////////////////////////////////////////////////
    class queryForPic extends AsyncTask<String, String, Void> {
        InputStream inputStream = null;
        boolean shareOrTake=false;
        int error=0;
        String result = "", connerror = "",sendpath="";
        boolean datosok = false;
        protected void onPreExecute() {
            isPhotoOnServer=-1;//Inicializar
        }
        @Override
        protected Void doInBackground(String... params) {
            boolean connok = false;
            Uri urix = new Uri.Builder()
                    .scheme("https")
                    .authority("www.orioncorp.com.ve")
                    .path("mprs/gps_photo_cheq.php")
                            // .appendQueryParameter("gt", gps_telno)
                            //.appendQueryParameter("gc", gps_clave)
                    .appendQueryParameter("mprs_lu", "crcaicedo@gmail.com")
                    .appendQueryParameter("mprs_lp", "OrionCorp34")
                    .appendQueryParameter("gu", usuario)
                    .appendQueryParameter("gp", clave)
                    .appendQueryParameter("gi", gps_id)
                    .appendQueryParameter("ni", "vehiculo1")
                    .build();
            //Log.e("getpos", urix.toString());
            try {
                URL url = new URL(urix.toString());
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
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
            if (datosok) {
                try {
                    String exists;
                    JSONObject jObject = new JSONObject(result);
                    JSONArray first;
                    try
                    {
                        exists=jObject.getString("msj");
                        if(exists.equals("1"))//Login exitoso!
                        {
                            isPhotoOnServer=1;
                            System.out.println("exists");
                            urix = new Uri.Builder()
                                    .scheme("https")
                                    .authority("www.orioncorp.com.ve")
                                    .path("mprs/gps_photo.php")
                                            // .appendQueryParameter("gt", gps_telno)
                                            //.appendQueryParameter("gc", gps_clave)
                                    .appendQueryParameter("mprs_lu", "crcaicedo@gmail.com")
                                    .appendQueryParameter("mprs_lp", "OrionCorp34")
                                    .appendQueryParameter("id", gps_id)
                                    .appendQueryParameter("ni", "vehiculo1")
                                    .build();
                            URL url = new URL(urix.toString());
                            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                            urlConnection.setRequestMethod("GET");
                            urlConnection.setDoInput(true);
                            urlConnection.connect();
                            InputStream input = urlConnection.getInputStream();
                            FileOutputStream fOut= new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() +"/Pictures/__idp.jpg");
                            int c;
                            byte[] b = new byte[1024];
                            while ((c = input.read(b)) != -1)
                                fOut.write(b, 0, c);
                            fOut.flush();
                            if (input != null)
                                input.close();
                            if (fOut != null)
                                fOut.close();

                        }
                         else
                        {
                            System.out.println("Foto no disponible");
                            isPhotoOnServer=0;
                        }

                    }
                    catch(JSONException e)
                    {
                        // Intent show= new Intent(Intro.this,MainActivity.class);
                        //startActivity(show);
                    }
                    catch (Exception e) {
                    }
                    } catch (JSONException e) {
                    } catch (Exception e) {
                    }
            }
            return null;
        } // protected Void doInBackground(String... params)
        @Override
        protected void onPostExecute(Void aVoid) {

            int i;
            /*if (datosok) {
                try {
                    String exists;
                    JSONObject jObject = new JSONObject(result);
                    JSONArray first;
                    try
                    {
                        exists=jObject.getString("msj");
                        System.out.println(exists.equals("1"));
                        System.out.println(exists=="1");
                        if(exists.equals("1"))//Login exitoso!
                        {
                            System.out.println("Foto disponible");
                            isPhotoOnServer=0;
                        }
                        else
                        {
                            System.out.println("Foto no disponible");
                           isPhotoOnServer=1;
                        }

                    }
                    catch(JSONException e)
                    {
                        // Intent show= new Intent(Intro.this,MainActivity.class);
                        //startActivity(show);
                        Toast.makeText(getBaseContext(),"Fallo en la conexión",
                                Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getBaseContext(),"Fallo en la conexión",
                            Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(),"Fallo en la conexión",
                            Toast.LENGTH_LONG).show();
                }
            }
            else
            {
                Toast.makeText(getBaseContext(), "Error en la conexión a Internet",
                        Toast.LENGTH_LONG).show();
            }*/
            if(isPhotoOnServer==0)
                dispatchTakePictureIntent();
            if(isPhotoOnServer==1)
            {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                //Uri path = Uri.parse("https://www.orioncorp.com.ve/mprs/gps_photo_cheq.php?mprs_lu=crcaicedo%40gmail.com&mprs_lp=OrionCorp34&gu=lr&gp=123456&gi=353451042833150&ni=vehiculo1");

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/__idp.jpg"));
                intent.setType("image/jpeg");
                startActivity(Intent.createChooser(intent,"Compartir imagen en..."));
            }
            if(isPhotoOnServer==-1)
            {
                Toast.makeText(getBaseContext(),"Error en la conexión a Internet",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
///////////////////////////////////////////////////////////////////////////////////


    class MyPost extends AsyncTask<String, String, Void> {

        ProgressDialog progressDialog=new ProgressDialog(MainActivity.this);
        int prog=0;
        int error=0;
        protected void onPreExecute() {
            System.out.println("OnPreExec");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage("Procesando...");
            progressDialog.setProgress(prog);
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    MyPost.this.cancel(true);
                }
            });
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(error==1)
            {
                progressDialog.cancel();
                Toast.makeText(getBaseContext(),"Error en la conexión a Internet",
                        Toast.LENGTH_LONG).show();
            }
            //Share intent

        }

        public String multipartRequest(String urlTo, Map<String, String> parmas, String filepath, String filefield, String fileMimeType) {
            HttpURLConnection connection = null;
            DataOutputStream outputStream = null;
            InputStream inputStream = null;
            prog+=10;
            String twoHyphens = "--";
            String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
            String lineEnd = "\r\n";
            progressDialog.setProgress(prog);
            String result = "";

            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;

            String[] q = filepath.split("/");
            int idx = q.length - 1;

            try {
                File file = new File(filepath);
                FileInputStream fileInputStream = new FileInputStream(mCurrentPhotoPath);

                URL url = new URL(urlTo);
                connection = (HttpURLConnection) url.openConnection();

                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                prog+=10;
                progressDialog.setProgress(prog);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + filefield + "\"; filename=\"" + "vehiculo1.jpg" + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: " + fileMimeType + lineEnd);
                outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);

                outputStream.writeBytes(lineEnd);

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                System.out.println(bytesAvailable+"??");

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                prog+=30;
                progressDialog.setProgress(prog);
                outputStream.writeBytes(lineEnd);

                // Upload POST Data
                Iterator<String> keys = parmas.keySet().iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = parmas.get(key);

                    outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
                    outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
                    outputStream.writeBytes(lineEnd);
                    outputStream.writeBytes(value);
                    outputStream.writeBytes(lineEnd);
                }

                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


                if (200 != connection.getResponseCode()) {
                    progressDialog.cancel();
                    Toast.makeText(getBaseContext(), "Error al conectarse al sistema",
                            Toast.LENGTH_LONG).show();
                    //throw new CustomException("Failed to upload code:" + connection.getResponseCode() + " " + connection.getResponseMessage());
                }
                else {
                    progressDialog.setProgress(100);
                    progressDialog.cancel();
                    System.out.println("HTTP OK 200");
                }

                inputStream = connection.getInputStream();

                result = this.convertStreamToString(inputStream);

                fileInputStream.close();
                inputStream.close();
                outputStream.flush();
                outputStream.close();

                return result;
            } catch (Exception e) {
                error=1;
                //logger.error(e);
                // throw new CustomException(e);
            }
            return null;
        }

        private String convertStreamToString(InputStream is) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();
        }

        @Override
        protected Void doInBackground(String... params) {
            Map<String, String> params2 = new HashMap<String, String>(2);
            params2.put("campo", "vehiculo1");
            params2.put("gu", usuario);
            params2.put("gp", clave);
            params2.put("gi", gps_id);
            params2.put("mprs_lu", "crcaicedo@gmail.com");
            params2.put("mprs_lp", "OrionCorp34");

            //String result = multipartRequest("http://172.16.3.49/mprs7.8/mprs7.8/gps_photo_up.php", params2, mCurrentPhotoPath, "vehiculo1", "image/jpeg");
            String result = multipartRequest("https://orioncorp.com.ve/mprs/gps_photo_up.php", params2, mCurrentPhotoPath, "vehiculo1", "image/jpeg");
            return null;
        }
    }


    class MyRecorrido extends AsyncTask<String, String, Void> {
        private ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        InputStream inputStream = null;
        String result = "", connerror = "";
        boolean datosok = false;

        protected void onPreExecute() {
            progressDialog.setMessage("Consultando recorrido...");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    MyRecorrido.this.cancel(true);
                }
            });
        }

        @Override
        protected Void doInBackground(String... params) {
            boolean connok = false;
            Date current= new Date();
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY,0);
            cal.set(Calendar.MINUTE,0);
            cal.set(Calendar.SECOND,0);
            cal.set(Calendar.MILLISECOND,0);
            Date midnight= cal.getTime();
            Timestamp t1= new Timestamp(current.getTime()/1000);
            Timestamp t0=new Timestamp(midnight.getTime()/1000);
            System.out.println(t0.getTime());
            System.out.println(t1.getTime());
            Uri urix = new Uri.Builder()
                    .scheme("https")
                    .authority("www.orioncorp.com.ve")
                    .path("mprs/gps_getpath.php")
                    .appendQueryParameter("gi", gps_id)
                    .appendQueryParameter("mprs_lu", "crcaicedo@gmail.com")
                    .appendQueryParameter("mprs_lp", "OrionCorp34")
                    .appendQueryParameter("gu", usuario)
                    .appendQueryParameter("gp", clave)
                    .appendQueryParameter("t0", String.valueOf(t0.getTime()))
                    .appendQueryParameter("t1", String.valueOf(t1.getTime()))
                    .build();
            //Log.e("getpos", urix.toString());
            try {
                URL url = new URL(urix.toString());
                System.out.println(urix.toString());
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                inputStream = new BufferedInputStream(urlConnection.getInputStream());
                connok = true;
            }
            catch (Exception e4) {
                Log.e("Excepcion", e4.toString());
                connerror = e4.toString();
                Toast.makeText(getBaseContext(), "Error: revise su conexión a internet",
                        Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getBaseContext(), "Error: fallo del sistema",
                            Toast.LENGTH_LONG).show();
                    Log.e("StringBuilding", e.toString());
                    connerror = e.toString();
                }
            }
            return null;
        } // protected Void doInBackground(String... params)

        protected void onPostExecute(Void v) {
            //parse JSON data
            int i,n;
            String len;
            JSONArray first,bottom;
            this.progressDialog.dismiss();
            if (datosok) {
                try {
                    JSONObject jObject = new JSONObject(result);
                    len=jObject.getString("coord_c");
                    n=Integer.parseInt(len);
                    System.out.println(result);
                    if(n>0)
                    {
                        JSONArray coord=jObject.getJSONArray("coord");
                        first= coord.getJSONArray(0);
                        bottom=coord.getJSONArray(n-1);
                        String s="<!DOCTYPE html>\n" +
                                "<html>\n" +
                                "  <head>\n" +
                                "    <style type=\"text/css\">\n" +
                                "      html, body, #map-canvas { height: 100%; margin: 0; padding: 0;}\n" +
                                "    </style>\n" +
                                "    <script type=\"text/javascript\"\n" +
                                "      src=\"https://maps.googleapis.com/maps/api/js?key=AIzaSyDaYfas4stcRNPoUMTWMvQhaADVrJbxB5Y&sensor=false\">\n" +
                                "    </script>\n" +
                                "    <script type=\"text/javascript\">\n" +
                                "      function initialize() {\n" +
                                "       var com= new google.maps.LatLng("+first.getString(0)+","+first.getString(1)+");\n"+
                                "       var fin= new google.maps.LatLng("+bottom.getString(0)+","+bottom.getString(1)+");\n"+
                                "        var mapOptions = {\n" +"center: { lat:"+bottom.getString(0)+", lng:"+bottom.getString(1)+"},\n" + //Centrandolo en la ultima posicion
                                "          zoom: 15\n"+
                                "        };\n" +
                                "var map = new google.maps.Map(document.getElementById('map-canvas')," +
                                "mapOptions);"+
                                "var coo=[";
                                for(i=0;i!=n-2;i++)
                                {
                                    s+="new google.maps.LatLng("+coord.getJSONArray(i).getString(0)+","+coord.getJSONArray(i).getString(1)+"),";

                                }
                                s+="new google.maps.LatLng("+coord.getJSONArray(n-1).getString(0)+","+coord.getJSONArray(n-1).getString(1)+")";
                                s+="];\n";
                                s+="var ruta=new google.maps.Polyline({path:coo,strokeColor:'#FF0000',strokeOpacity:1.0,strokeWeight:2});\n"+

                                "ruta.setMap(map);"+"var icoa='http://maps.gstatic.com/mapfiles/markers2/markerA.png';\n"+
                                " var m1 = new google.maps.Marker({position: com, map: map, icon:icoa});\n"+
                                " var icob='http://maps.gstatic.com/mapfiles/markers2/markerB.png';\n"+
                                " var m2 = new google.maps.Marker({position: fin, map: map, icon:icob});\n"+

                                "        \n" +
                                "      }\n" +
                                "      google.maps.event.addDomListener(window, 'load', initialize);\n" +
                                "    </script>\n" +
                                "  </head>\n" +
                                "  <body>\n" +
                                "<div id=\"map-canvas\"></div>\n" +
                                "  </body>\n" +
                                "</html>";
                        Intent mapaIntent=new Intent(MainActivity.this,WebClient.class);
                        mapaIntent.putExtra("html",s);
                        SimpleDateFormat formateador = new SimpleDateFormat(
                                "hh:mm a", new Locale("es_ES"));
                        Date current= new Date();
                        String fecha = formateador.format(current);
                        mapaIntent.putExtra("title","12:00 a.m./ "+fecha);
                        mapaIntent.putExtra("url","empty");
                        System.out.println(s);
                        startActivity(mapaIntent);
                    }
                    else
                    {
                        Toast.makeText(getBaseContext(), "No hay recorrido registrado",
                                Toast.LENGTH_LONG).show();
                    }
                   /* for(i=0;i<n;i++)
                    {
                        first= coord.getJSONArray(i);
                        System.out.println(first);
                    }*/
                    /*String latitud = jObject.getString("latitud");
                    String longitud = jObject.getString("longitud");

                    String msj = jObject.getString("msj");

                    String tiempo = jObject.getString("tiempo");
                    System.out.println(jObject.toString());
                    Timestamp t= new Timestamp(Long.valueOf(tiempo));

                    Date date=new Date(t.getTime()*1000);
                    SimpleDateFormat formateador = new SimpleDateFormat(
                            "dd/MM/yyyy hh:mm a", new Locale("es_ES"));
                    String fecha = formateador.format(date);
                    System.out.println(fecha);*/

                } catch (JSONException e) {
                    Toast.makeText(getBaseContext(), "Error obteniendo datos",
                            Toast.LENGTH_LONG).show();
                    Log.e("JSONException", e.toString());
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), "Error obteniendo datos",
                            Toast.LENGTH_LONG).show();
                    Log.e("Excepcion", e.toString());
                }
            }
            else
            {
                Toast.makeText(getBaseContext(), "Fallo en la conexión a Internet",
                        Toast.LENGTH_LONG).show();
            }
        } // protected void onPostExecute(Void v)
    } //class MyAsyncTask extends AsyncTask<String, String, Void>
}
