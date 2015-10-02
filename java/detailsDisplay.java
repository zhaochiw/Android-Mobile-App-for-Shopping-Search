package com.example.wzc_pc.hw9;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static com.example.wzc_pc.hw9.R.drawable.fbicon;
import static com.example.wzc_pc.hw9.R.drawable.symbol_check;
import static com.example.wzc_pc.hw9.R.drawable.toprated;


public class detailsDisplay extends ActionBarActivity {
    private JSONObject jsonItem;
    private  CallbackManager callbackManager;
    private  ShareDialog shareDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_display);
        //Set up for facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                    @Override
                    public void onSuccess(Sharer.Result result) {
                        String postID = result.getPostId();
                        String message;
                        if(postID == null || postID.equals("")){
                            message = "Post Cancelled";
                        }
                        else{
                            message = "Posted Story, ID:"+ postID;
                        }
                        Toast.makeText(getApplicationContext(), message,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel() {
                        String message = "Post Cancelled";
                        Toast.makeText(getApplicationContext(), message,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException e) {
                        String message = "Post Failure";
                        Toast.makeText(getApplicationContext(), message,Toast.LENGTH_SHORT).show();
                    }
                });
        //Get data
        Intent intent = getIntent();
        String jsonString = intent.getExtras().getString("jsonString");
        try {
            JSONObject jsonOb = new JSONObject(jsonString);
            String itemName = "item"+ intent.getExtras().getString("number");
            jsonItem = jsonOb.optJSONObject(itemName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            showDetails();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details_display, menu);
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

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }

    public void showDetails() throws MalformedURLException {
        JSONObject basicInfo = jsonItem.optJSONObject("basicInfo");

        ImageView img = (ImageView)findViewById(R.id.detailImage);
        String galleryURL = basicInfo.optString("pictureURLSuperSize");
        if(galleryURL == null || galleryURL.equals("")){
            galleryURL = basicInfo.optString("galleryURL");
        }
        new loadImg(img).execute(new URL(galleryURL));

        TextView title = (TextView)findViewById(R.id.detailTitle);
        String titleString = basicInfo.optString("title");
        title.setText(titleString);

        TextView price = (TextView)findViewById(R.id.detailPrice);
        String priceInfo = "Price: $"+basicInfo.optString("convertedCurrentPrice");
        String shipping = basicInfo.optString("shippingServiceCost");
        if(shipping.equals("0.0")){
            priceInfo += " (FREE Shipping)";
        }
        else{
            priceInfo += " (+ $"+ shipping +" Shipping)";
        }
        price.setText(priceInfo);

        TextView shipFrom = (TextView)findViewById(R.id.detailLocation);
        String shipFromString = basicInfo.optString("location");
        shipFrom.setText(shipFromString);

        String topRatedString = basicInfo.optString("topRatedListing");
        if(topRatedString.equals("true")){
            ImageView topRated = (ImageView)findViewById(R.id.topRated);
            topRated.setImageResource(toprated);
        }

        ImageView fbIcon = (ImageView)findViewById(R.id.fbIcon);
        fbIcon.setImageResource(fbicon);

        TextView category = (TextView)findViewById(R.id.categoryContent);
        String categoryString = basicInfo.optString("categoryName");
        category.setText(categoryString);

        TextView condition = (TextView)findViewById(R.id.conditionContent);
        String conditionString = basicInfo.optString("conditionDisplayName");
        condition.setText(categoryString);

        TextView format = (TextView)findViewById(R.id.formatContent);
        String formatString = basicInfo.optString("listingType");
        format.setText(formatString);
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

    /* Onclick button "Buy Now", go to eBay page of the item */
    public void GOTOeBay(View view){
        Intent eBayPage = new Intent(Intent.ACTION_VIEW);
        JSONObject basicInfo = jsonItem.optJSONObject("basicInfo");
        String url = basicInfo.optString("viewItemURL");
        eBayPage.setData(Uri.parse(url));
        startActivity(eBayPage);
    }

    /* Onclick button "BASIC INFO", show basic information */
    public void showBasic(View view){
        //Reset buttons status
        ImageButton basic_button = (ImageButton)findViewById(R.id.basic_button);
        basic_button.setBackground(getResources().getDrawable(R.drawable.basic_press));
        ImageButton seller_button = (ImageButton)findViewById(R.id.seller_button);
        seller_button.setBackground(getResources().getDrawable(R.drawable.seller));
        ImageButton shipping_button = (ImageButton)findViewById(R.id.shipping_button);
        shipping_button.setBackground(getResources().getDrawable(R.drawable.shipping));
        //Change visibility of the three layout
        RelativeLayout basicLayout = (RelativeLayout)findViewById(R.id.basicLayout);
        basicLayout.setVisibility(View.VISIBLE);
        RelativeLayout sellerLayout = (RelativeLayout)findViewById(R.id.sellerLayout);
        sellerLayout.setVisibility(View.INVISIBLE);
        RelativeLayout shippingLayout = (RelativeLayout)findViewById(R.id.shippingLayout);
        shippingLayout.setVisibility(View.INVISIBLE);
        //Set up the content of basicLayout
        JSONObject basicInfo = jsonItem.optJSONObject("basicInfo");
        TextView category = (TextView)findViewById(R.id.categoryContent);
        String categoryString = basicInfo.optString("categoryName");
        category.setText(categoryString);

        TextView condition = (TextView)findViewById(R.id.conditionContent);
        String conditionString = basicInfo.optString("conditionDisplayName");
        condition.setText(categoryString);

        TextView format = (TextView)findViewById(R.id.formatContent);
        String formatString = basicInfo.optString("listingType");
        format.setText(formatString);
    }

    /* Onclick button "SELLER", show seller information */
    public void showSeller(View view){
        //Reset buttons status
        ImageButton basic_button = (ImageButton)findViewById(R.id.basic_button);
        basic_button.setBackground(getResources().getDrawable(R.drawable.basic));
        ImageButton seller_button = (ImageButton)findViewById(R.id.seller_button);
        seller_button.setBackground(getResources().getDrawable(R.drawable.seller_press));
        ImageButton shipping_button = (ImageButton)findViewById(R.id.shipping_button);
        shipping_button.setBackground(getResources().getDrawable(R.drawable.shipping));
        //Change visibility of the three layout
        RelativeLayout basicLayout = (RelativeLayout)findViewById(R.id.basicLayout);
        basicLayout.setVisibility(View.INVISIBLE);
        RelativeLayout sellerLayout = (RelativeLayout)findViewById(R.id.sellerLayout);
        sellerLayout.setVisibility(View.VISIBLE);
        RelativeLayout shippingLayout = (RelativeLayout)findViewById(R.id.shippingLayout);
        shippingLayout.setVisibility(View.INVISIBLE);
        //Set up the content of sellerLayout
        JSONObject sellerInfo = jsonItem.optJSONObject("sellerInfo");
        TextView userName = (TextView)findViewById(R.id.userContent);
        String userNameString = sellerInfo.optString("sellerUserName");
        userName.setText(userNameString);

        TextView score = (TextView)findViewById(R.id.scoreContent);
        String scoreString = sellerInfo.optString("feedbackScore");
        score.setText(scoreString);

        TextView positive = (TextView)findViewById(R.id.positiveContent);
        String positiveString = sellerInfo.optString("positiveFeedbackPercent");
        positive.setText(positiveString);

        TextView rating = (TextView)findViewById(R.id.rateContent);
        String ratingString = sellerInfo.optString("feedbackRatingStar");
        rating.setText(ratingString);

        String topRatedString = sellerInfo.optString("topRatedSeller");
        if(topRatedString.equals("true")){
            ImageView topRated = (ImageView)findViewById(R.id.topContent);
            topRated.setImageResource(symbol_check);
        }

        TextView store = (TextView)findViewById(R.id.storeContent);
        String storeString = sellerInfo.optString("sellerStoreName");
        if(storeString == null || storeString.equals("")){
            storeString = "N/A";
        }
        store.setText(storeString);
    }

    /* Onclick button "SHIPPING", show shipping information */
    public void showShipping(View view){
        //Reset buttons status
        ImageButton basic_button = (ImageButton)findViewById(R.id.basic_button);
        basic_button.setBackground(getResources().getDrawable(R.drawable.basic));
        ImageButton seller_button = (ImageButton)findViewById(R.id.seller_button);
        seller_button.setBackground(getResources().getDrawable(R.drawable.seller));
        ImageButton shipping_button = (ImageButton)findViewById(R.id.shipping_button);
        shipping_button.setBackground(getResources().getDrawable(R.drawable.shipping_press));
        //Change visibility of the three layout
        RelativeLayout basicLayout = (RelativeLayout)findViewById(R.id.basicLayout);
        basicLayout.setVisibility(View.INVISIBLE);
        RelativeLayout sellerLayout = (RelativeLayout)findViewById(R.id.sellerLayout);
        sellerLayout.setVisibility(View.INVISIBLE);
        RelativeLayout shippingLayout = (RelativeLayout)findViewById(R.id.shippingLayout);
        shippingLayout.setVisibility(View.VISIBLE);
        //Set up the content of shippingLayout
        JSONObject shippingInfo = jsonItem.optJSONObject("shippingInfo");
        TextView shippingType = (TextView)findViewById(R.id.typeContent);
        String shippingTypeString = shippingInfo.optString("shippingType");
        shippingType.setText(shippingTypeString);

        TextView handling = (TextView)findViewById(R.id.handleContent);
        String handlingString = shippingInfo.optString("handlingTime");
        handling.setText(handlingString);

        TextView locations = (TextView)findViewById(R.id.locationContent);
        String locationsString = shippingInfo.optString("shipToLocations");
        locations.setText(locationsString);

        String expeditedString = shippingInfo.optString("expeditedShipping");
        if(expeditedString.equals("true")){
            ImageView expedited = (ImageView)findViewById(R.id.expContent);
            expedited.setImageResource(symbol_check);
        }

        String oneDayString = shippingInfo.optString("oneDayShippingAvailable");
        if(oneDayString.equals("true")){
            ImageView oneDay = (ImageView)findViewById(R.id.oneDayContent);
            oneDay.setImageResource(symbol_check);
        }

        String returnsString = shippingInfo.optString("returnsAccepted");
        if(returnsString.equals("true")){
            ImageView returns = (ImageView)findViewById(R.id.returnsContent);
            returns.setImageResource(symbol_check);
        }
    }

    /* Onclick the icon of Facebook, show the interface of share to facebook */
    public void shareFacebook(View view){
        JSONObject basicInfo = jsonItem.optJSONObject("basicInfo");
        String title = basicInfo.optString("title");
        String priceInfo = "Price: $"+basicInfo.optString("convertedCurrentPrice");
        String shipping = basicInfo.optString("shippingServiceCost");
        if(shipping.equals("0.0")){
            priceInfo += " (FREE Shipping)";
        }
        else{
            priceInfo += " (+ $"+ shipping +" Shipping)";
        }
        String location = "Location: "+basicInfo.optString("location");
        String description = priceInfo + ", " + location;
        String galleryURL = basicInfo.optString("galleryURL");
        String url = basicInfo.optString("viewItemURL");
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle(title)
                    .setContentDescription(description)
                    .setContentUrl(Uri.parse(url))
                    .setImageUrl(Uri.parse(galleryURL))
                    .build();
            shareDialog.show(linkContent);
        }
    }

}
