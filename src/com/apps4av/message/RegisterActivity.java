/*
Copyright (c) 2014, Apps4Av Inc. (apps4av.com) 
All rights reserved.
*/

package com.apps4av.message;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.google.android.gcm.GCMRegistrar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
 
/**
 * 
 * @author zkhan
 *
 */
public class RegisterActivity extends Activity {
    
    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    
    static AsyncTask<Void, Void, Void> mRegisterTask = null;

    // Register button
    private Button mButtonRegister;
    private Button mButtonUnregister;
    private AlertDialog mRegisterDialog;
    private AlertDialog mUnregisterDialog;
    private WebView mPrivacy;
    
    
    /**
     * Intent from notification bar
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }
    
    /**
     * 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        // Check if Google services
        try {
            GCMRegistrar.checkDevice(this);
        }
        catch (Exception e) {
            Helper.showAlert(RegisterActivity.this,
                    getString(R.string.error),
                    getString(R.string.error_google));
            return;            
        }
        
        // Check if Internet present
        if (!Helper.isNetworkAvailable(this)) {
            Helper.showAlert(RegisterActivity.this,
                    getString(R.string.error),
                    getString(R.string.error_internet));
            return;
        }

        // Check if email
        if(PossibleEmail.get(this) == null) {
            Helper.showAlert(RegisterActivity.this,
                    getString(R.string.error),
                    getString(R.string.error_email));
            return;            
        }
 
        Logger.setTextView((TextView) findViewById(R.id.log_text));

        /*
         * privacy policy load
         */
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPrivacy = (WebView)layoutInflater.inflate(R.layout.privacy, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
            .setTitle(getString(R.string.register))
            .setView(mPrivacy)
            .setCancelable(false)
            .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    dialog.dismiss();
                }
            })
            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    
                    // Get GCM registration id
                    if(mRegisterTask != null) {
                        if(mRegisterTask.getStatus() != AsyncTask.Status.FINISHED) {
                            mRegisterTask.cancel(true);
                        }
                    }
                    
                    Logger.Logit(getString(R.string.registering_google) + "...");
                    GCMRegistrar.register(RegisterActivity.this, NetworkHelper.SENDER_ID);
                    dialog.dismiss();
                }
            });
        
        mRegisterDialog = alertDialogBuilder.create();

        alertDialogBuilder = new AlertDialog.Builder(
                RegisterActivity.this);
        alertDialogBuilder
            .setTitle(getString(R.string.unregister))
            .setMessage(getString(R.string.unregister_question))
            .setCancelable(false)
            .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    dialog.dismiss();
                }
            })
            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {

                    
                    // Get GCM registration id
                    if(mRegisterTask != null) {
                        if(mRegisterTask.getStatus() != AsyncTask.Status.FINISHED) {
                            mRegisterTask.cancel(true);
                        }
                    }
                    
                    Logger.Logit(getString(R.string.unregistering_google) + "...");
                    GCMRegistrar.unregister(RegisterActivity.this);
                    dialog.dismiss();
                }
            });
        
        mUnregisterDialog = alertDialogBuilder.create();

        /*
         * Click event on Register button
         *
         */
        mButtonRegister = (Button) findViewById(R.id.btn_register);        
        mButtonRegister.setOnClickListener(new View.OnClickListener() {
             
            @Override
            public void onClick(View arg0) {

                Logger.clear();
                
                if(!GCMRegistrar.getRegistrationId(RegisterActivity.this).equals("")) {
                    Logger.Logit(getString(R.string.already_registered));
                    return;
                }
                
                mPrivacy.loadUrl("file:///android_asset/privacy.html");
                mPrivacy.setWebViewClient(new WebViewClient() {
                    // wait for page to load
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        // show it
                        mRegisterDialog.show();
                    }
                 });
            }
        });
        
        
        mButtonUnregister = (Button) findViewById(R.id.btn_unregister);        
        mButtonUnregister.setOnClickListener(new View.OnClickListener() {
             
            @Override
            public void onClick(View arg0) {
                Logger.clear();
                
                if(GCMRegistrar.getRegistrationId(RegisterActivity.this).equals("")) {
                    Logger.Logit(getString(R.string.already_unregistered));
                    return;
                }
                                
                // show it
                mUnregisterDialog.show();
           }
        });
        
        handleIntent(getIntent());
    }
 
    /**
     * Intent from the notification bar when user clicks an item there
     * @param intent
     */
    private void handleIntent(Intent intent) {
        
        /*
         * Get intent from notification
         */
        String message = intent.getStringExtra("message");
        if(null != message) {
            Logger.clear();
            Logger.Logit(getString(R.string.new_message) + ": " + message);
        }
    }
    
    /**
     * Register this account/device pair within the server.
     *
     */
    public static void register(final Context context, final String regId) {

        mRegisterTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... vals) {
                // Register on our server
                // On server creates a new user
                String serverUrl = NetworkHelper.SERVER_URL + "register.php";
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", "anonoymous");
                params.put("email", PossibleEmail.get(context));
                params.put("regId", regId);
                Random random = new Random();
                long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
                // Once GCM returns a registration id, we need to register on our server
                // As the server might be down, we will retry it a couple
                // times.
                for (int i = 1; i <= MAX_ATTEMPTS; i++) {
                    Logger.Logit("Registering with the Apps4Av servers ...");
                    try {
                        NetworkHelper.post(serverUrl, params);
                        GCMRegistrar.setRegisteredOnServer(context, true);
                        Logger.Logit("You have now registered with the Apps4Av online services!");
                        return null;
                    } 
                    catch (Exception e) {
                        e.printStackTrace();
                        // Here we are simplifying and retrying on any error; in a real
                        // application, it should retry only on unrecoverable errors
                        // (like HTTP error code 503).
                        if (i == MAX_ATTEMPTS) {
                            Logger.Logit("Failed to register!");
                            break;
                        }
                        try {
                            Thread.sleep(backoff);
                        }
                        catch (InterruptedException e1) {
                            // Activity finished before we complete - exit.
                            Thread.currentThread().interrupt();
                            Logger.Logit("Failed to register!");
                            return null;
                        }
                        // increase backoff exponentially
                        backoff *= 2;
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
            }
        };
        
        mRegisterTask.execute(null, null, null);
    }
 
    /**
     * Unregister this account/device pair within the server.
     */
    public static void unregister(final Context context, final String regId) {

        mRegisterTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... vals) {
                
                String serverUrl = NetworkHelper.SERVER_URL + "unregister.php";
                Map<String, String> params = new HashMap<String, String>();
                params.put("regId", regId);
                Random random = new Random();
                long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
                
                
                // Once GCM returns a registration id, we need to register on our server
                // As the server might be down, we will retry it a couple
                // times.
                for (int i = 1; i <= MAX_ATTEMPTS; i++) {
                    Logger.Logit("Unregistering from the Apps4Av servers ...");
                    try {
                        NetworkHelper.post(serverUrl, params);
                        GCMRegistrar.setRegisteredOnServer(context, false);
                        Logger.Logit("You have now unregistered from the Apps4Av online services!");
                        return null;
                    } 
                    catch (Exception e) {
                    }
                    // Here we are simplifying and retrying on any error; in a real
                    // application, it should retry only on unrecoverable errors
                    // (like HTTP error code 503).
                    if (i == MAX_ATTEMPTS) {
                        Logger.Logit("Failed to unregister!");
                        break;
                    }
                    try {
                        Thread.sleep(backoff);
                    }
                    catch (InterruptedException e1) {
                        // Activity finished before we complete - exit.
                        Thread.currentThread().interrupt();
                        Logger.Logit("Failed to unregister!");
                        return null;
                    }
                    backoff *= 2;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
            }

        };
        mRegisterTask.execute(null, null, null);
    }
    
}