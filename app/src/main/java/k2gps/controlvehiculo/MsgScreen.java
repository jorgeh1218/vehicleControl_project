package k2gps.controlvehiculo;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

/**
 * Created by jhidalgo on 23/06/15.
 */
public class MsgScreen extends ActionBarActivity {

    public TextView titulo,contenido;
    //public static MsgScreen actividad=null;
    public long idLastMessage=-1;
    public String tel,message;
    public int value;
    public boolean response=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b= getIntent().getExtras();
        tel=b.getString("tel");
        setContentView(R.layout.msgscreen);
        //actividad=this;
        value=15000;
        titulo= (TextView) findViewById(R.id.titulo);
        contenido= (TextView) findViewById(R.id.content);
        contenido.setText(b.getString("info"));
        runThreads();
    }
    public String formMessage(String msg)//Este metodo elimina cierta informacion innecesaria del msj que se recibe del gps
    {
        int top,bottom;
        String begin=null,end,aux;
        top=msg.indexOf("Door");
        if(top!=-1)//Encontro Door
        {
            begin=msg.substring(0,top-1);
            end=msg.substring(top);
            bottom=end.indexOf("\n");
            aux=end.substring(bottom); // Eliminando Door del string
            begin+=aux;
            /*if(aux!=null && bottom!=-1)
                return begin;*/
            System.out.println(begin);
        }
        else
        {
            top=msg.indexOf("DOOR");
            if(top!=-1)//Encontro Door
            {
                begin=msg.substring(0,top);
                end=msg.substring(top);
                bottom=end.indexOf("\n");
                aux=end.substring(bottom); // Eliminando Door del string
                begin+=aux;
            /*if(aux!=null && bottom!=-1)
                return begin;*/
            }
        }
        if(begin!=null)//Ya contiene datos
            msg=begin;
        top=msg.indexOf("Oil");
        if(top!=-1)//Encontro Oil
        {
            System.out.println("Linea 69");
            begin=msg.substring(0,top-1);
            end=msg.substring(top);
            bottom=end.indexOf("\n");
            if(bottom!=-1)
            {
                aux=end.substring(bottom); // Eliminando Oil del string
                begin+=aux;
                System.out.println("///*//");
                System.out.println(begin);
                return begin;
            }
            else
            {
                System.out.println("/////");
                System.out.println(begin);
                bottom=end.indexOf("\0");
                if(bottom!=-1)
                    return begin;//Oil esta al final de la cadena, retornar begin
            }
        }
        top=msg.indexOf("OIL");
        if(top!=-1)//Encontro Door
        {
            begin=msg.substring(0,top-1);
            end=msg.substring(top);
            bottom=end.indexOf("\n");
            if(bottom!=-1)
            {
                aux=end.substring(bottom + 1); // Eliminando Oil del string
                begin+=aux;
                return begin;
            }
            else
            {
                bottom=end.indexOf("\0");
                if(bottom!=-1)
                    return begin;//Oil esta al final de la cadena, retornar begin
            }
        }
        return msg;//Retornar msg original, o el msg modificado por msg=begin
    }
    public void runThreads()
    {
         new MyAsyncTask().execute();
    }
        @Override
        protected void onStop () {
            super.onStop();
            //actividad = null;
        }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        response=true;
       // actividad = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
       // actividad = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
      //  actividad=this;
    }
    class MyAsyncTask extends AsyncTask<String, String, Void> {

        protected void onPreExecute() {
        response=false;
            value=10000;
            idLastMessage=-1;
            contenido.setText("");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                super.onPostExecute(aVoid);
                contenido.setText(message);
            }
            catch(Exception e)
            {

            }
        }

        @Override
        protected Void doInBackground(String... params) {
            int count=-1;
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
                        if (idLastMessage == -1 ) {
                            idLastMessage = id;
                        }
                        else {
                            if (id != idLastMessage)//Si el mensaje es de
                            {
                                if (address.equals(tel) || address.equals("+58"+tel.substring(1)) || address.contains(tel.substring(1))) {
                                    message=formMessage(body);
                                    response=true;
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

                        //                    Log.e("log>>>", "date" + c.getString(0));

                        //                    if (body.contains(getResources().getText(R.string.invite_text).toString()) && address.equals(number)) {
                    /*if (message.equals(body) && address.equals(number)) {
                        // mLogger.logInfo("Deleting SMS with id: " + threadId);
                        int rows = ctx.getContentResolver().delete(Uri.parse("content://sms/" + id), "date=?",new String[] { c.getString(4) });
                        Log.e("log>>>", "Delete success......... rows: "+rows);
                        Log.e("log>>>", "Delete success......... body: "+body);
                    }*/
                    }
                }
            });
            while(!response)
            {
                t.run();
            }
            return null;
        } // protected Void doInBackground(String... params)
    }
}
