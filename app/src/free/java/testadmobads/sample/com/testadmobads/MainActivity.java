package testadmobads.sample.com.testadmobads;

import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.AndroidRuntimeException;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.security.MessageDigest;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ViewGroup adViewContainer = ( ViewGroup ) findViewById( R.id.adview_container );

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-4073123456879230~86334354");

        // Avoid activity context as AdView internal has a static reference to the activity
        mAdView = (AdView) LayoutInflater.from( getApplicationContext() ).inflate(
                R.layout.adview_layout,
                adViewContainer,
                true /*attachToRoot*/ ).findViewById(R.id.adView);

        AdRequest request;

        if( BuildConfig.DEBUG )
        {
            final String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            final String deviceId = MainActivity.md5(android_id).toUpperCase();

            request = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice(deviceId)
                    .build();
        }
        else
        {
            request = new AdRequest.Builder().build();
        }

        try
        {
            mAdView.loadAd(request);
        }
        catch( AndroidRuntimeException e )
        {
            final Throwable cause = e.getCause();
            if( cause!= null && cause.getMessage().contains( "com.android.webview" ) )
            {
                // WebView is being updated via Play Store. Ignore Adrequest for this session.
            }
            else
            {
                throw e;
            }
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        try
        {
            mAdView.resume();
        }
        catch( AndroidRuntimeException e )
        {
            final Throwable cause = e.getCause();
            if( cause!= null && cause.getMessage().contains( "com.android.webview" ) )
            {
                // WebView is being updated via Play Store. Ignore Adrequest for this session.
            }
            else
            {
                throw e;
            }
        }
    }

    @Override
    protected void onPause()
    {
        mAdView.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        (( ViewGroup ) mAdView.getParent()).removeAllViews();
        mAdView.destroy();
        super.onDestroy();
    }

    private static String md5(final String s)
    {
        try
        {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for ( byte msgDigest : messageDigest )
            {
                String h = Integer.toHexString(0xFF & msgDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        }
        catch ( NoSuchAlgorithmException e )
        {
        }
        return "";
    }
}
