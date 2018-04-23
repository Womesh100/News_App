package com.example.sspider.android_first;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    final String URL_IDS = "https://hacker-news.firebaseio.com/v0/topstories.json";
    final String URL_ART_START = "https://hacker-news.firebaseio.com/v0/item/";
    final String URL_ART_END = ".json";

    ListView listView ;
    TextView textView;
    ArrayAdapter<String> adapter ;
    ArrayList<String> titleList;
    ArrayList<String> urlList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView)findViewById(R.id.listview);
        textView = (TextView)findViewById(R.id.textView);

        titleList = new ArrayList<>();
        urlList = new ArrayList<>();

/**
 * create a ListView Adapter to hold the articles
 */
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,this.titleList);
        listView.setAdapter(adapter);
/**
 * used when we click on any article displayed on the page will be opened in a browser.
 */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlList.get(position)));
                startActivity(browserIntent);
            }
        });
/**
 * Downloaded file will be executed using object of the inner Class by execute() method
 */
        new DownloadTask().execute();
    }

    /**
     * create a inner class that extends AsyncTask for performing an independent activity
     */
    class DownloadTask extends AsyncTask<Void, String, Void>
    {
        /**
         * override the most important method name: doInBackground() for background task processing
         */
        @Override
        protected Void doInBackground(Void... voids)
        {
            String resArray = "";
            HttpURLConnection connection = null; //create a connection object
            InputStream in = null;
            InputStreamReader reader = null;

            try
            {
                URL urlIds = new URL(URL_IDS); //takes url ids
                connection = (HttpURLConnection) urlIds.openConnection(); //open the json http connection
                in = connection.getInputStream();   // get the connection in input Stream
                reader = new InputStreamReader(in); // open reader of input Stream

                int data = reader.read();  //read the data in integer
                while (data != -1) // while data is not null
                {
                    char c = (char)data; // typecast integer into character
                    resArray += c;
                    data = reader.read();  //read the data in integer again and repeat till data is not null
                }

                JSONArray jsonArray = new JSONArray(resArray); //create a Json Array Object for storing data
                int index = 0;

                for (int i = 0; i<jsonArray.length();i++) // a loop till json length ends
                {
                    String id = jsonArray.getString(i);  //get all ids one by one of json Array in id variable of string type
                    URL urlArticle = new URL(URL_ART_START + id + URL_ART_END);

                    /**
                     * created a new url with a particular ids using URL urlArticle variables
                     */

                    /**
                     * open the connection again for particular Articles
                     * open input Stream
                     * open a input stream reader
                     */
                    connection = (HttpURLConnection) urlArticle.openConnection();
                    in = connection.getInputStream();
                    reader = new InputStreamReader(in);
                    String jsonObjectString = "";

                    /**
                     * read the article one by one and put into json object string till data is not null
                     */
                    data = reader.read();
                    while (data != -1)
                    {
                        char c = (char)data;
                        jsonObjectString += c;
                        data = reader.read();
                    }
                    /**
                     * Create a json object again for confirming
                     * and takes the title and url here
                     */
                    JSONObject jsonObject = new JSONObject(jsonObjectString);

                    if (jsonObject.has("title") && jsonObject.has("url"))
                    {
                        titleList.add(jsonObject.getString("title")); //add title into list
                        urlList.add(jsonObject.getString("url"));  //add url into list
                        publishProgress("Downloaded : " + index + " articles");
                        //publishProgress method calls the onProgressUpdate method to show UI to the users.
                        index++;
                    }
                }

            }catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (connection != null) // if no connection then disconnect
                {
                    connection.disconnect();
                }
                if (in != null)  // if no input stream then close input stream
                {
                    try
                    {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (reader != null)     //if input stream reader is null then close reader
                {
                    try
                    {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;   //if all done simply returns null
        }

        @Override
        protected void onProgressUpdate(String... values)
        {
            super.onProgressUpdate(values);

            String update = values[0];
            textView.setText(update);   // shows the number of articles in the text view // V.V.I.
            textView.setVisibility(View.VISIBLE);
        }

        /**
         * onPostExecute method used to invoked on the UI thread after the background computation finishes.
         * The result of the background computation is passed to this step as a parameter
         * @param aVoid
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.notifyDataSetChanged();
            textView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id == R.id.action_settings)  //action_settings for setting menu ... settings
        {
            return true;
        }
        if (id == R.id.action_refresh) // action_refresh for setting menu... refresh
        {
            titleList.clear();
            urlList.clear();
            adapter.notifyDataSetChanged();
            new DownloadTask().execute();
        }

        return super.onOptionsItemSelected(item);
    }
}