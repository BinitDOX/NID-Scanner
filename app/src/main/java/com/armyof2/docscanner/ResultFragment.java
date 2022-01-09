package com.armyof2.docscanner;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import static com.armyof2.docscanner.MainActivity.DOCFLAG;
import static com.armyof2.docscanner.Utils.RotateBitmap;


public class ResultFragment extends Fragment {

    private View view;
    private ImageView scannedImageView;
    private Button doneButton;
    private Bitmap original;
    private Button rotLeftButton;
    private Button rotRightButton;
    private Bitmap transformed = null;
    private boolean rotFlag = false;
    private static ProgressDialogFragment progressDialogFragment;
    private boolean retry = false;

    public ResultFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_result, null);
        init();
        return view;
    }

    private void init() {
        scannedImageView = (ImageView) view.findViewById(R.id.imgview_res);
        rotLeftButton = (Button) view.findViewById(R.id.btn_res_rotateleft);
        rotLeftButton.setOnClickListener(new rotLeftButtonClickListener());
        rotRightButton = (Button) view.findViewById(R.id.btn_res_rotateright);
        rotRightButton.setOnClickListener(new rotRightButtonClickListener());
        Bitmap bitmap = getBitmap();
        setScannedImage(bitmap);
        doneButton = (Button) view.findViewById(R.id.btn_res_done);
        doneButton.setOnClickListener(new DoneButtonClickListener());
    }

    private Bitmap getBitmap() {
        Uri uri = getUri();
        try {
            original = Utils.getBitmap(getActivity(), uri);
            getActivity().getContentResolver().delete(uri, null, null);
            return original;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Uri getUri() {
        Uri uri = getArguments().getParcelable(ScanConstants.SCANNED_RESULT);
        return uri;
    }

    public void setScannedImage(Bitmap scannedImage) {
        scannedImageView.setImageBitmap(scannedImage);
    }

    private class DoneButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            showProgressDialog(getResources().getString(R.string.loading));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent data = new Intent();
                        Bitmap bitmap = transformed;
                        if (bitmap == null) {
                            bitmap = original;
                        }
                        Uri uri = Utils.getUri(getActivity(), bitmap);
                        data.putExtra(ScanConstants.SCANNED_RESULT, uri);
                        getActivity().setResult(Activity.RESULT_OK, data);
                        //original.recycle();
                        //System.gc();

                        //TO SERVER
                        showProgressDialog(getResources().getString(R.string.extracting));
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] byteArrayImage = baos.toByteArray();
                        String encodedImage = Base64.encodeToString(byteArrayImage, Base64.NO_WRAP | Base64.URL_SAFE);
                        System.out.println(encodedImage);

                        toServer(encodedImage);
                        if(retry) {
                            //dismissDialog();
                            return;
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissDialog();
                                getActivity().finish();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /*private class rotLeftButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            showProgressDialog(getResources().getString(R.string.applying_trans));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        transformed = RotateBitmap(original, 90);
                    } catch (final OutOfMemoryError e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                transformed = original;
                                scannedImageView.setImageBitmap(original);
                                e.printStackTrace();
                                dismissDialog();
                                onClick(v);
                            }
                        });
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scannedImageView.setImageBitmap(transformed);
                            dismissDialog();
                        }
                    });
                }
            });
        }
    }


    private class rotRightButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            showProgressDialog(getResources().getString(R.string.applying_trans));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        transformed = RotateBitmap(original, -90);
                    } catch (final OutOfMemoryError e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                transformed = original;
                                scannedImageView.setImageBitmap(original);
                                e.printStackTrace();
                                dismissDialog();
                                onClick(v);
                            }
                        });
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scannedImageView.setImageBitmap(transformed);
                            dismissDialog();
                        }
                    });
                }
            });
        }
    }*/

    private class rotLeftButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            showProgressDialog(getResources().getString(R.string.applying_trans));
            if(!rotFlag)
                transformed = original;
            transformed = RotateBitmap(transformed, -90);
            scannedImageView.setImageBitmap(transformed);
            dismissDialog();
            rotFlag = true;
        }
    }

    private class rotRightButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            showProgressDialog(getResources().getString(R.string.applying_trans));
            if(!rotFlag)
            transformed = original;
            transformed = RotateBitmap(transformed, 90);
            scannedImageView.setImageBitmap(transformed);
            dismissDialog();
            rotFlag = true;
        }
    }


    protected synchronized void showProgressDialog(String message) {
        if (progressDialogFragment != null && progressDialogFragment.isVisible()) {
            // Before creating another loading dialog, close all opened loading dialogs (if any)
            progressDialogFragment.dismissAllowingStateLoss();
        }
        progressDialogFragment = null;
        progressDialogFragment = new ProgressDialogFragment(message);
        FragmentManager fm = getFragmentManager();
        progressDialogFragment.show(fm, ProgressDialogFragment.class.toString());
    }

    protected synchronized void dismissDialog() {
        progressDialogFragment.dismissAllowingStateLoss();
    }

    private void toServer(String encodedImage){
        String data = null;
        switch (DOCFLAG) {
            case 1:
                data = "{\'type\':\'aadhar\',\'image\':" + "\'" + encodedImage + "\'}";
                Log.d("TAG", "toServer: Aadhar: " + data);
                break;
            case 2:
                data = "{\'type\':\'pan\',\'image\':" + "\'" + encodedImage + "\'}";
                Log.d("TAG", "toServer: Pan: " + data);
                break;
            case 3:
                data = "{\'type\':\'dl\',\'image\':" + "\'" + encodedImage + "\'}";
                Log.d("TAG", "toServer: Lic: " + data);
                break;
        }
        JSONObject obj = null;
        try {
            obj = new JSONObject(data);
            Log.d("TAG", obj.toString());
        } catch (Throwable t) {
            Log.e("TAG", "Could not parse malformed JSON: \"" + data + "\"");
        }

        // Create URL
        URL apiEndpoint = null;
        try {
            apiEndpoint = new URL("http://34.93.49.60:8888/api");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.d("TAG", "Bad URL");
        }

        // Create connection
        /*try {
            Log.d("TAG", "toServer: 1");
            HttpURLConnection myConnection =
                    (HttpURLConnection) apiEndpoint.openConnection();
            myConnection.setRequestMethod("POST");

            //myConnection.addRequestProperty("Content-Length", Integer.toString(data.length()));
           // myConnection.addRequestProperty("Content-Type", "application/json"); //add the content type of the request, most post data is of this type
            myConnection.setRequestProperty("User-Agent", "DocScanner-v0.1");

            myConnection.setDoOutput(true);
            //myConnection.getOutputStream().write(data.getBytes());
            //myConnection.connect();
            OutputStream os = myConnection.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            os.close();
            Log.d("TAG", "toServer: 2");

            if (myConnection.getResponseCode() == 200) {
                // Success
                // Further processing here
                Log.d("TAG", "toServer: Succ");
            } else {
                // Error handling code goes here
                Log.d("TAG", "toServer: Fail: " + myConnection.getResponseCode());
            }


            myConnection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
            Log.d("TAG", "run: Connection Failed!");
            dismissDialog();
            showErrorDialog();
            retry = true;
        }*/

        try {
            //sendGET(apiEndpoint);
            Log.d("TAG", "toServer: GET Done");
            sendPOST(apiEndpoint, data, obj);
            Log.d("TAG", "toServer: POST Done");

        } catch (IOException e) {
                e.printStackTrace();
                Log.d("TAG", "run: Connection Failed!");
                dismissDialog();
                showErrorDialog();
                retry = true;
            }
    }

    private void showErrorDialog() {
        SingleButtonDialogFragment fragment = new SingleButtonDialogFragment(R.string.ok, getString(R.string.cantConn), "Connection Failed!", true);
        FragmentManager fm = getActivity().getFragmentManager();
        fragment.show(fm, SingleButtonDialogFragment.class.toString());
    }

    private static void sendGET(URL obj) throws IOException {
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "DocScanner-v0.1");
        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            System.out.println(response.toString());
        } else {
            System.out.println("GET request not worked");
        }

    }

    private static void sendPOST(URL obj, String data, JSONObject jsonOb) throws IOException {
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        //con.setRequestProperty("Content-Type", "application/json; utf-8");
        //con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("User-Agent", "DocScanner-v0.1");

        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        //os.write(jsonOb.toString().getBytes("UTF-8"));
        os.write(data.getBytes());
        //os.flush();
        os.close();

        int responseCode = con.getResponseCode();
        System.out.println("POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            System.out.println("Response: " + response.toString());
        } else {
            System.out.println("POST request not worked");
        }
    }

}
