package com.armyof2.docscanner;

import android.net.Uri;


public interface ScannerInterface {

    void onBitmapSelect(Uri uri);

    void onScanFinish(Uri uri);
}