package com.coffeebeantech.social_login_example;

import org.brickred.socialauth.Profile;
import org.brickred.socialauth.android.DialogListener;
import org.brickred.socialauth.android.SocialAuthAdapter;
import org.brickred.socialauth.android.SocialAuthAdapter.Provider;
import org.brickred.socialauth.android.SocialAuthError;

import com.loopj.android.http.*;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends Activity {
	// SocialAuth Component
	SocialAuthAdapter adapter;
	private Provider[] providers = new Provider[] {Provider.FACEBOOK, Provider.TWITTER, Provider.LINKEDIN};

	// Android Components
	Menu menu;
	ImageButton facebookButton;
	ImageButton twitterButton;
	ImageButton linkedinButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Create social-auth adapter
		adapter = new SocialAuthAdapter(new ResponseListener());

		// Add providers
		adapter.addProvider(Provider.FACEBOOK, R.drawable.facebook);
		adapter.addProvider(Provider.TWITTER, R.drawable.twitter);
		adapter.addProvider(Provider.LINKEDIN, R.drawable.linkedin);

		// Providers require setting user callback URL
		adapter.addCallBack(Provider.TWITTER, "http://socialauth.in/socialauthdemo/socialAuthSuccessAction.do");

		// Enable Provider
		facebookButton = (ImageButton) findViewById(R.id.loginFacebook);
		facebookButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				adapter.authorize(MainActivity.this, providers[0]);
			}
		});

		twitterButton = (ImageButton) findViewById(R.id.loginTwitter);
		twitterButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				adapter.authorize(MainActivity.this, providers[1]);
			}
		});

		linkedinButton = (ImageButton) findViewById(R.id.loginLinkedin);
		linkedinButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				adapter.authorize(MainActivity.this, providers[2]);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		this.menu = menu;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	private final class ResponseListener implements DialogListener {
		@Override
		public void onComplete(Bundle values) {
			// Hardcoded data
			final String appId = "???";
			final String apiId = "???";
			final String apiSecret = "???";

			// Get name of provider after authentication
			final String providerName = values.getString(SocialAuthAdapter.PROVIDER);
			Log.d("SIDN", "Provider Name = " + providerName);

			// Social-ID Now POST URL
			final String sidnPostUrl = "https://api.socialidnow.com/v1/marketing/login/apps/"+appId+"/sign_ins/"+providerName;

			// Social-ID Now request parameters
			String accessToken = "";
			String accessSecret = "";

			accessToken = MainActivity.this.adapter.getCurrentProvider().getAccessGrant().getKey();
			Log.d("SIDN", accessToken);

			accessSecret = MainActivity.this.adapter.getCurrentProvider().getAccessGrant().getSecret();
			if(accessSecret == null) accessSecret = "";
			Log.d("SIDN", accessSecret);

			RequestParams params = new RequestParams();
			params.put("app_id", appId);
			params.put("access_token", accessToken);
			params.put("access_secret", accessSecret);

			// Make request
			AsyncHttpClient client = new AsyncHttpClient();
			client.setBasicAuth(apiId, apiSecret);
			client.post(sidnPostUrl, params, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String response)
				{
					Log.d("SIDN", response);
				}

				@Override
				public void onFailure(Throwable e, String response)
				{
					Log.d("SIDN", response);
				}
			});

			// Get profile data
			Profile profileMap = MainActivity.this.adapter.getUserProfile();
			String firstName = profileMap.getFirstName();
			if(firstName == null) {
				String[] tokens = profileMap.getFullName().split(" ");
				if(tokens.length > 0) {
					firstName = tokens[0];
				}
			}
			Toast.makeText(MainActivity.this, "Hello " + firstName, Toast.LENGTH_LONG).show();
		}

		@Override
		public void onError(SocialAuthError error) {
			Log.d("SIDN", "Authentication Error: " + error.getMessage());
		}

		@Override
		public void onCancel() {
			Log.d("SIDN", "Authentication Cancelled");
		}

		@Override
		public void onBack() {
			Log.d("SIDN", "Dialog Closed by pressing Back Key");
		}

	}
}