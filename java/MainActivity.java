package com.example.wzc_pc.hw9;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner spinner = (Spinner) findViewById(R.id.sortBy);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_list, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class getJSON extends AsyncTask<URL, Integer, Long> {
        private JSONObject jsonOb;
        private String jsonString = "";
        protected Long doInBackground(URL... urls) {
            int count = urls.length;
            for (int i = 0; i < count; i++) {
                try {
                    HttpURLConnection conn = (HttpURLConnection) urls[i].openConnection();
                    conn.setRequestMethod("GET");
                    if(conn.getResponseCode() == 200) {
                        InputStream input = conn.getInputStream();
                        BufferedReader buffer = new BufferedReader(new InputStreamReader(input));
                        String line;
                        while ((line = buffer.readLine()) != null) {
                            jsonString += line;
                        }
                        jsonOb = new JSONObject(jsonString);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            return 0L;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Long result) {
            String ack = jsonOb.optString("ack");
            if(ack.equals("Success")){
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, resultDisplay.class);
                intent.putExtra("jsonString", jsonString);
                EditText keyword = (EditText)findViewById(R.id.keywordText);
                String act2Header = keyword.getText().toString();
                intent.putExtra("act2Header", act2Header);
                startActivity(intent);
            }
            else{
                TextView resultText = (TextView)findViewById(R.id.resultText);
                resultText.setText("No Result Found");
            }
        }
    }

    /* Onclick button "CLEAR", clear the input of the search form */
    public void Clear(View view){
        EditText keyword = (EditText)findViewById(R.id.keywordText);
        keyword.setText("");
        EditText pricef = (EditText)findViewById(R.id.pricefText);
        pricef.setText("");
        EditText pricet = (EditText)findViewById(R.id.pricetText);
        pricet.setText("");
        Spinner sortby = (Spinner)findViewById(R.id.sortBy);
        sortby.setSelection(0);
        TextView result = (TextView)findViewById(R.id.resultText);
        result.setText("");
    }

    /* Onclick button "SEARCH", implement validation and jQuery */
    public void Search(View view) throws UnsupportedEncodingException, MalformedURLException {
        EditText keyword = (EditText)findViewById(R.id.keywordText);
        EditText pricef = (EditText)findViewById(R.id.pricefText);
        EditText pricet = (EditText)findViewById(R.id.pricetText);
        Spinner spinner = (Spinner)findViewById(R.id.sortBy);
        TextView result = (TextView)findViewById(R.id.resultText);
        result.setText("");
        String keywordContent = keyword.getText().toString();
        String pricefContent = pricef.getText().toString();
        String pricetContent = pricet.getText().toString();
        String sortByContent = spinner.getSelectedItem().toString();

        boolean isvalid = Validation(keywordContent, pricefContent, pricetContent);
        if(isvalid){
            //Build URL for the XML
            //Convert sortby content into eBay query value
            String sortBy = "";
            switch (sortByContent){
                case "Best Match":
                    sortBy = "BestMatch";
                    break;
                case "Price: highest first":
                    sortBy = "CurrentPriceHighest";
                    break;
                case "Price + Shipping: highest first":
                    sortBy = "PricePlusShippingHighest";
                    break;
                case "Price + Shipping: lowest first":
                    sortBy = "PricePlusShippingLowest";
                    break;
            }
            String encodedKeyword = URLEncoder.encode(keywordContent,"utf-8");
            String urlString = "http://zhaochiwuweb-env.elasticbeanstalk.com/index.html/readXML.php?keywords="
                                +encodedKeyword+"&MinPrice="+pricefContent+"&MaxPrice="+pricetContent+"&sortOrder="
                                +sortBy+"&paginationInput.entriesPerPage=5&page=1";
            URL url = new URL(urlString);
            new getJSON().execute(url);
        }

    }

    /* Validation for the search form */
    public boolean Validation(String keywordContent, String pricefContent, String pricetContent){
        TextView result = (TextView)findViewById(R.id.resultText);
        if(keywordContent.equals("")){
            result.setText("Please enter a keyword");
            return false;
        }
        Double pricefNumber = 0.0, pricetNumber;
        if(!pricefContent.equals("")){
            pricefNumber = Double.parseDouble(pricefContent);
            if(pricefNumber < 0){
                result.setText("Price can only be a positive number");
                return false;
            }
        }
        if(!pricetContent.equals("")){
            pricetNumber = Double.parseDouble(pricetContent);
            if(pricetNumber < 0){
                result.setText("Price can only be a positive number");
                return false;
            }
            else if(pricetNumber < pricefNumber){
                result.setText("Price From should be less than or equal to Price To");
                return false;
            }
        }
        return true;
    }

}
