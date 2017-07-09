package k2gps.controlvehiculo;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SettingsActivity extends PreferenceActivity {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
    String gps_id,mCurrentPhotoPath,mCurrentRoute;
    File image=null;
    public static EditTextPreference id,telno,key;
    public static Preference foto;
    public static String vehicle;
    public static int saved=-1;
    private static final boolean ALWAYS_SIMPLE_PREFS = false;
    public CharSequence[] values;
    public CharSequence[] entries;
    public CharSequence[] keys;
    public static SharedPreferences sp;
    public ListPreference lp;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);

        /*
        // Add 'notifications' preferences, and a corresponding header.
        PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_notifications);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_notification);

        // Add 'data and sync' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_data_sync);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_data_sync);
        */

        /*bindPreferenceSummaryToValue(findPreference("usuario"));
        bindPreferenceSummaryToValue(findPreference("clave"));
        */
        //bindPreferenceSummaryToValue(findPreference("gps_imei"));
        bindPreferenceSummaryToValue(findPreference("gps_telno"));
        bindPreferenceSummaryToValue(findPreference("gps_clave"));
    }
    public void readSaveIfExists()
    {
        System.out.println("Checking ");
        try{
            System.out.println("Reading file");
            FileInputStream in= openFileInput("savedv.txt");
            InputStreamReader reader = new InputStreamReader(in);
            char[] inputbuffer = new char[64];
            reader.read(inputbuffer);
            String result=new String(inputbuffer);
            result=result.substring(0,result.indexOf('\0'));
            in.close();
            saved=Integer.valueOf(result);
            System.out.println("value of saved"+saved);
            //saved=-1;
        }
        catch (FileNotFoundException e) {
            saved=-1;
        } catch (IOException e) {
            e.printStackTrace();
            saved=-1;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        try {
            String len, veh, telf;
            int n, i;
            setupSimplePreferencesScreen();
            key= (EditTextPreference) findPreference("gps_clave");
            telno= (EditTextPreference)findPreference("gps_telno");
            //id= (EditTextPreference)findPreference("gps_id");
            foto= (Preference)findPreference("photo");
            foto.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    gps_id="";
                    gps_id=MainActivity.datos.getString("gps_id"+saved);
                    if(!gps_id.isEmpty())
                    {
                        dispatchTakePictureIntent();
                    }
                    return true;
                }
            });
            Bundle extras = getIntent().getExtras();
            len = extras.getString("nvehic");
            n = Integer.valueOf(len);
            //System.out.println("Size of array "+len);
            entries = new CharSequence[n];
            values = new CharSequence[n];
            keys = new CharSequence[n];
            for (i = 0; i < n; i++) {

                entries[i] = extras.getString("vehic" + i);
                values[i] = extras.getString("gps_telno" + i);
                keys[i] =extras.getString("gps_clave"+i);
                if(extras.getString("gps_telno" + i).isEmpty())
                {

                    values[i]=extras.getString("gps_id"+i);
                    System.out.println("Empty");
                }
                System.out.println(entries[i]+" "+values[i]+" "+keys[i]);
            }
            lp = (ListPreference) findPreference("ListaVehiculos");
            lp.setEntries(entries);
            lp.setEntryValues(values);
            if(len.equals("1"))//Un solo vehiculo
                saved=0;
            else
            {
                readSaveIfExists();
            }
            sp = getPreferenceScreen().getSharedPreferences();
            //System.out.println("150 Settings");
            lp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String stringValue = newValue.toString();
                    if (preference instanceof ListPreference) {
                        // For list preferences, look up the correct display value in
                        // the preference's 'entries' list

                        ListPreference listPreference = (ListPreference) findPreference("ListaVehiculos");
                        int index = listPreference.findIndexOfValue(stringValue);
                        // Set the summary to reflect the new value.
                        preference.setSummary(
                                index >= 0
                                        ? listPreference.getEntries()[index]
                                        : null);
                        //listPreference.setValueIndex(index);
                        System.out.println(index + " " + values[index]);
                        key.setText(keys[index].toString());
                        vehicle=entries[index].toString();
                        key.setSummary(sp.getString("gps_clave", keys[index].toString()));
                        telno.setText(values[index].toString());
                        telno.setSummary(sp.getString("gps_telno", values[index].toString()));
                        saved = index;
                    }
                    return false;
                }
            });
            if(saved!=-1)//Hay un estado guardado
            {
                lp.setSummary(entries[saved]);
                vehicle=entries[saved].toString();
                // lp.setValueIndex(saved);
                //  lp.setSummary(sp.getString("ListaVehiculos",values[saved].toString()));

                // lp.setSummary(sp.getString("ListaVehiculos",lp.getEntryValues()[saved].toString()));
                key.setText(keys[saved].toString());
                key.setSummary(sp.getString("gps_clave", keys[saved].toString()));
                telno.setText(values[saved].toString());
                telno.setSummary(sp.getString("gps_telno", values[saved].toString()));
            }
        }
        catch(Exception e)
        {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK)
        {
            // boolean error=false;
            compressImageFile();
            new MyPost().execute();
        }
    }
    private void compressImageFile()
    {
        int alto,ancho;
        try
        {
            File dir= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
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
                    image.delete();
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
    class MyPost extends AsyncTask<String, String, Void> {
        ProgressDialog progressDialog=new ProgressDialog(SettingsActivity.this);
        int error=1;
        int prog=0;
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
                Toast.makeText(getBaseContext(), "Error al conectarse al sistema",
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
                    error=0;
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
            params2.put("gu", MainActivity.usuario);
            params2.put("gp", MainActivity.clave);
            params2.put("gi",gps_id);
            params2.put("mprs_lu", "crcaicedo@gmail.com");
            params2.put("mprs_lp", "OrionCorp34");

            //String result = multipartRequest("http://172.16.3.49/mprs7.8/mprs7.8/gps_photo_up.php", params2, mCurrentPhotoPath, "vehiculo1", "image/jpeg");
            String result = multipartRequest("https://orioncorp.com.ve/mprs/gps_photo_up.php", params2, mCurrentPhotoPath, "vehiculo1", "image/jpeg");
            return null;
        }
    }














    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*System.out.println("Destroy");
        telno.setText("123456");
        key.setText("123456");
        telno.setSummary(sp.getString("gps_telno", "123456"));
        key.setSummary(sp.getString("gps_clave", "123456"));
        saved=-1;
        SharedPreferences.Editor preferencesEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        //Clear all the saved preference values.
        preferencesEditor.clear();
        //Read the default values and set them as the current values.
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, true);
        //Commit all changes.
        preferencesEditor.commit();*/
        System.out.println("Weird");
        try
        {
            System.out.println("Writing file");
            if(saved!=-1)
            {
                FileOutputStream fos = openFileOutput("savedv.txt", Context.MODE_PRIVATE);
                fos.write(String.valueOf(saved).getBytes());
                fos.close();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finish();
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    //preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("example_text"));
            bindPreferenceSummaryToValue(findPreference("example_list"));
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }
    }
}
