package com.example.wzc_pc.hw9;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.ListActivity;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class resultDisplay extends ListActivity{
    private List<HashMap<String, String>> mData;
    private String jsondata;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_display);

        Intent intent = getIntent();
        String jsonString = intent.getExtras().getString("jsonString");
        String act2Header = intent.getExtras().getString("act2Header");
        try {
            buildResultTable(jsonString, act2Header);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_result_display, menu);
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

    /* Display the results */
    public void buildResultTable(String jsonString, String act2Header) throws JSONException {
        JSONObject jsonOb = new JSONObject(jsonString);
        jsondata = jsonString;
        //Show header
        String header = "Results for '"+ act2Header+"'";
        TextView res = (TextView)findViewById(R.id.act2Header);
        res.setText(header);
        //Build information string for items
        int count = jsonOb.getInt("itemCount");
        String[] values = new String[count];
        mData = new ArrayList<HashMap<String, String>>();
        for(int i = 0; i < count; ++i){
            HashMap<String, String> map = new HashMap<String, String>();
            String itemName = "item"+ Integer.toString(i);
            JSONObject itemContent = jsonOb.optJSONObject(itemName);
            JSONObject basicInfo = itemContent.optJSONObject("basicInfo");
            map.put("title", basicInfo.optString("title"));
            String priceInfo = "Price: $"+basicInfo.getString("convertedCurrentPrice");
            String shipping = basicInfo.optString("shippingServiceCost");
            if(shipping.equals("0.0")){
                priceInfo += " (FREE Shipping)";
            }
            else{
                priceInfo += " (+ $"+ shipping +" Shipping)";
            }
            map.put("Price", priceInfo);
            String galleryURL = basicInfo.optString("galleryURL");
            map.put("galleryURL", galleryURL);
            String viewItemURL = basicInfo.optString("viewItemURL");
            map.put("viewItemURL", viewItemURL);
            mData.add(map);
        }
        MySimpleArrayAdapter adapter = new MySimpleArrayAdapter(this, values);
        setListAdapter(adapter);
    }

    private class MySimpleArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final String[] values;

        public MySimpleArrayAdapter(Context context, String[] values) {
            super(context, R.layout.rowlayout, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.rowlayout, parent, false);
            TextView firstLine = (TextView) rowView.findViewById(R.id.firstLine);
            TextView secondLine = (TextView) rowView.findViewById(R.id.secondLine);
            ImageView icon = (ImageView)rowView.findViewById(R.id.icon);
            String galleryURL = mData.get(position).get("galleryURL");
            try {
                new loadImg(icon).execute(new URL(galleryURL));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            firstLine.setText(mData.get(position).get("title"));
            secondLine.setText(mData.get(position).get("Price"));
            icon.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Intent eBayPage = new Intent(Intent.ACTION_VIEW);
                    String url = mData.get(position).get("viewItemURL");
                    eBayPage.setData(Uri.parse(url));
                    startActivity(eBayPage);
                }
            });
            firstLine.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Intent details = new Intent();
                    details.setClass(resultDisplay.this, detailsDisplay.class);
                    details.putExtra("jsonString", jsondata);
                    details.putExtra("number", Integer.toString(position));
                    startActivity(details);
                }
            });
            return rowView;
        }
    }

    /* Load image by URL in the background */
    private class loadImg extends AsyncTask<URL, Void, Bitmap> {
        ImageView imgView;

        public loadImg(ImageView _imgView) {
            imgView = _imgView;
        }

        @Override
        protected Bitmap doInBackground(URL... params) {
            try {
                return BitmapFactory.decodeStream(params[0].openStream());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            imgView.setImageBitmap(result);
        }
    }


}
