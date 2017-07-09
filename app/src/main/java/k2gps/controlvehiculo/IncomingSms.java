package k2gps.controlvehiculo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class IncomingSms extends BroadcastReceiver {

    // Get the object of SmsManager
    final SmsManager sms = SmsManager.getDefault();
    //TextView statusToShow;
    public final static String EXTRA_MESSAGE="";
    static String message="";
    public void onReceive(Context context, Intent intent) {
        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();
        System.out.println("Received");
        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                    message = currentMessage.getDisplayMessageBody();
                    //Log.i("SmsReceiver", "senderNum: " + senderNum + "; message: " + message);

                    // TODO: procesar respuesta si es el numero del tracker
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    String telno = sharedPref.getString("gps_telno", "1234567");
                    if (phoneNumber.endsWith(telno)) {
                        Log.e("recvgps", message);
                      //  Toast.makeText(context, message,
                        //        Toast.LENGTH_SHORT).show();
                       // MainActivity.message_received=null;
                     //   if(MainActivity.actividad!=null)
                       // {
                        try
                        {
                            if (!MainActivity.firstbuttonclicked) //Muestra el mensaje en textview si no fue pulsado el boton de obtener posicion
                            {
                                //((TextView) MainActivity.actividad.findViewById(R.id.status)).setText(message);
                                //((TextView) MsgScreen.actividad.findViewById(R.id.content)).setText(message);
                                //Toast.makeText(context, message,Toast.LENGTH_LONG).show();
                            }
                            else
                            {
                                message=pullLink(message); //Hace pull de la url contenida en el mensaje
                                if(message!="ERROR")
                                {
                                    System.out.println("Starting web client from Incoming sms");
                                    Intent show = new Intent(context, WebClient.class);
                                    show.putExtra("url", message); // Compartiendo datos con la nueva actividad webclient a traves de Extra message
                                    show.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Permite ejecutar la actividad desde esta clase
                                    context.startActivity(show);
                                }
                                else
                                {
                                    Toast.makeText(MainActivity.c, "Ocurrió un error durante la consulta",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        catch(Exception e)
                        {

                        }


                       // }
                        /*new AlertDialog.Builder(context)
                                .setTitle("Respuesta del rastreador")
                                .setMessage(message)
                                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();*/
                        }
                    } // end for loop
            } // bundle is null

        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" +e);
        }
    }
    private String pullLink(String text)
    {
        int i,ilat,ilon,iurl;
        String lat,lon,url;
        ilat=text.indexOf("lat");
        ilon=text.indexOf("lon");
        iurl=text.indexOf("http");

        if(iurl!=-1)
        {
            url=text.substring(iurl);//Start en iurl
            iurl=url.indexOf("\n");
            url= url.substring(0, iurl);
            return url;//Retorna el url
        }
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
        return "ERROR";
    }
    /*public class MyDialog extends DialogFragment {

        public String message;
        public void setMessage(String m)
        {
            message = m;
        }
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(message)
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // FIRE ZE MISSILES!
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }*/
}
