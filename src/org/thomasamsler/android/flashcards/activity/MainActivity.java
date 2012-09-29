/*
 * Copyright 2011, 2012 Thomas Amsler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package org.thomasamsler.android.flashcards.activity;

import org.thomasamsler.android.flashcards.ActionBusListener;
import org.thomasamsler.android.flashcards.AppConstants;
import org.thomasamsler.android.flashcards.MainApplication;
import org.thomasamsler.android.flashcards.R;
import org.thomasamsler.android.flashcards.conversion.FileToDbConversion;
import org.thomasamsler.android.flashcards.db.DataSource;
import org.thomasamsler.android.flashcards.dialog.HelpDialog;
import org.thomasamsler.android.flashcards.fragment.AboutFragment;
import org.thomasamsler.android.flashcards.fragment.ActionbarFragment;
import org.thomasamsler.android.flashcards.fragment.AddCardFragment;
import org.thomasamsler.android.flashcards.fragment.CardSetsFragment;
import org.thomasamsler.android.flashcards.fragment.SetupFragment;
import org.thomasamsler.android.flashcards.model.CardSet;
import org.thomasamsler.android.flashcards.pager.CardsPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements ActionBusListener, AppConstants {

	private static final String FEEDBACK_EMAIL_ADDRESS = "tamsler@gmail.com";

	private ActionbarFragment mActionbarFragment;
	private CardSetsFragment mCardSetsFragment;
	private AddCardFragment mAddCardFragment;
	private SetupFragment mSetupFragment;
	private AboutFragment mAboutFragment;
	private CardSet mCurrentCardSet;
	private int mHelpContext;
	private DataSource mDataSource;
	private int mActiveFragmentReference;

	private LinearLayout mFragmentContainer;
	private ViewPager mViewPager;

	private MainApplication mMainApplication;

	private boolean mExitOnBackPressed;

	private int mFontSize;

	private CardsPager mCurrentCardsPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		mDataSource = new DataSource(this);

		mMainApplication = (MainApplication) getApplication();

		mMainApplication.initActionBusListener();

		mMainApplication.registerAction(this, ACTION_SHOW_CARD_SETS, ACTION_SHOW_CARDS, ACTION_SHOW_HELP, ACTION_SHOW_SETUP, ACTION_SHOW_ABOUT, ACTION_GET_EXTERNAL_CARD_SETS, ACTION_SET_HELP_CONTEXT, ACTION_SHOW_ADD_CARD, ACTION_ADD_CARD_SET, ACTION_FONT_SIZE_CHANGE, ACTION_SEND_FEEDBACK, ACTION_RESHUFFLE_CARDS);

		/*
		 * Determine if we need to run the File to DB conversion
		 */
		SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		boolean runConversion = sharedPreferences.getBoolean(PREFERENCE_RUN_CONVERSION, PREFERENCE_RUN_CONVERSION_DEFAULT);

		if (runConversion) {

			FileToDbConversion conversion = new FileToDbConversion();
			conversion.convert(this, mDataSource);

			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putBoolean(PREFERENCE_RUN_CONVERSION, false);
			editor.commit();
		}

		/*
		 * Getting the preferred font size
		 */
		int fontSizePreference = sharedPreferences.getInt(PREFERENCE_FONT_SIZE, PREFERENCE_NORMAL_FONT_SIZE);
		mFontSize = getFontSizePreference(fontSizePreference);

		mFragmentContainer = (LinearLayout) findViewById(R.id.fragmentContainer);
		mViewPager = (ViewPager) findViewById(R.id.viewpager);

		showCardSetsFragment(false);
	}

	@Override
	public void onPause() {
		
		mMainApplication.doAction(ACTION_SHOW_CARD_SETS, Boolean.TRUE);
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		
		mDataSource.close();
		super.onDestroy();
	}
	

	@Override
	public void onBackPressed() {

		/*
		 * Intercepting the back button press since we need to handle the Cards
		 * view
		 */
		if (mExitOnBackPressed) {

			finish();
		}
		else {

			showCardSetsFragment(true);
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		super.onKeyUp(keyCode, event);

		if (keyCode == KeyEvent.KEYCODE_MENU) {

			mMainApplication.doAction(ACTION_SHOW_OVERFLOW_ACTIONS);
		}

		return true;
	}

	private void setHelpContext(Integer context) {

		if (null != context) {

			this.mHelpContext = context.intValue();
		}
	}

	private void showCardSetsFragment(boolean addToBackStack) {

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		if (null == mActionbarFragment) {

			mActionbarFragment = ActionbarFragment.newInstance(LIST_FRAGMENT);
			fragmentTransaction.replace(R.id.actionbarContainer, mActionbarFragment);
		}
		else {

			mActionbarFragment.configureFor(LIST_FRAGMENT);
		}

		if (null == mCardSetsFragment) {

			mCardSetsFragment = new CardSetsFragment();
		}

		fragmentTransaction.replace(R.id.fragmentContainer, mCardSetsFragment);

		if (addToBackStack) {

			fragmentTransaction.addToBackStack(null);
		}

		fragmentTransaction.commit();

		mFragmentContainer.setVisibility(View.VISIBLE);
		mViewPager.setVisibility(View.GONE);
		mHelpContext = HELP_CONTEXT_CARD_SET_LIST;
		mActiveFragmentReference = LIST_FRAGMENT;
		mExitOnBackPressed = true;
	}

	private void showAddCardFragment(CardSet cardSet) {

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		if (null == mActionbarFragment) {

			mActionbarFragment = ActionbarFragment.newInstance(ADD_FRAGMENT);
			fragmentTransaction.replace(R.id.actionbarContainer, mActionbarFragment);
		}
		else {

			mActionbarFragment.configureFor(ADD_FRAGMENT);
		}

		if (null == mAddCardFragment) {

			mAddCardFragment = new AddCardFragment();
		}

		mAddCardFragment.setCardSet(cardSet);

		fragmentTransaction.replace(R.id.fragmentContainer, mAddCardFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();

		mHelpContext = HELP_CONTEXT_ADD_CARD;
		mActiveFragmentReference = ADD_FRAGMENT;
		mExitOnBackPressed = false;
	}

	private void showSetupFragment() {

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		if (null == mActionbarFragment) {

			mActionbarFragment = ActionbarFragment.newInstance(SETUP_FRAGMENT);
			fragmentTransaction.replace(R.id.actionbarContainer, mActionbarFragment);
		}
		else {

			mActionbarFragment.configureFor(SETUP_FRAGMENT);
		}

		if (null == mSetupFragment) {

			mSetupFragment = new SetupFragment();
		}

		fragmentTransaction.replace(R.id.fragmentContainer, mSetupFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();

		mHelpContext = HELP_CONTEXT_SETUP;
		mActiveFragmentReference = SETUP_FRAGMENT;
		mExitOnBackPressed = false;
	}

	private void showAboutFragment() {

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		if (null == mActionbarFragment) {

			mActionbarFragment = ActionbarFragment.newInstance(ABOUT_FRAGMENT);
			fragmentTransaction.replace(R.id.actionbarContainer, mActionbarFragment);
		}
		else {

			mActionbarFragment.configureFor(ABOUT_FRAGMENT);
		}

		if (null == mAboutFragment) {

			mAboutFragment = new AboutFragment();
		}

		fragmentTransaction.replace(R.id.fragmentContainer, mAboutFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();

		mHelpContext = HELP_CONTEXT_DEFAULT;
		mActiveFragmentReference = ABOUT_FRAGMENT;
		mExitOnBackPressed = false;
	}

	private void showCardsFragment(CardSet cardSet) {

		mCurrentCardSet = cardSet;

		/*
		 * Clear "old" CardsPager so that we see the old cards when the new
		 * pager gets launched.
		 */
		if (null != mCurrentCardsPager) {

			mCurrentCardsPager.clear();
		}

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		if (null == mActionbarFragment) {

			mActionbarFragment = ActionbarFragment.newInstance(CARDS_FRAGMENT);
			fragmentTransaction.replace(R.id.actionbarContainer, mActionbarFragment);
		}
		else {

			mActionbarFragment.configureFor(CARDS_FRAGMENT);
		}

		CardsPager cardsPager = CardsPager.newInstance(cardSet, mFontSize);

		try {

			fragmentTransaction.replace(R.id.viewpager, cardsPager);

			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		}
		catch (Exception e) {

			Log.e(LOG_TAG, "Exception", e);
		}

		mCurrentCardsPager = cardsPager;

		mFragmentContainer.setVisibility(View.GONE);
		mViewPager.setVisibility(View.VISIBLE);
		mHelpContext = HELP_CONTEXT_VIEW_CARD;
		mActiveFragmentReference = CARDS_FRAGMENT;
		mExitOnBackPressed = false;
	}

	private void showHelp() {

		HelpDialog helpDialog = new HelpDialog(this);

		switch (mHelpContext) {

		case HELP_CONTEXT_DEFAULT:
			helpDialog.setHelp(getResources().getString(R.string.help_content_default));
			break;

		case HELP_CONTEXT_SETUP:
			helpDialog.setHelp(getResources().getString(R.string.help_content_setup));
			break;

		case HELP_CONTEXT_CARD_SET_LIST:
			helpDialog.setHelp(getResources().getString(R.string.help_content_card_set_list));
			break;

		case HELP_CONTEXT_ADD_CARD:
			helpDialog.setHelp(getResources().getString(R.string.help_content_add_card));
			break;

		case HELP_CONTEXT_VIEW_CARD:
			helpDialog.setHelp(getResources().getString(R.string.help_content_view_card));
			break;

		default:
			helpDialog.setHelp(getResources().getString(R.string.help_content_default));
			break;
		}

		helpDialog.show();
	}

	private void getExternal() {

		if (mActiveFragmentReference == SETUP_FRAGMENT) {

			showCardSetsFragment(true);
		}

		if (null == mCardSetsFragment) {

			Toast.makeText(getApplicationContext(), R.string.external_data_message_error, Toast.LENGTH_SHORT).show();
		}
		else {

			mCardSetsFragment.getFlashCardExchangeCardSets();
		}
	}

	private int getFontSizePreference(int fontSizePreference) {

		int fontSize = NORMAL_FONT_SIZE;

		switch (fontSizePreference) {

		case PREFERENCE_SMALL_FONT_SIZE:
			fontSize = SMALL_FONT_SIZE;
			break;
		case PREFERENCE_NORMAL_FONT_SIZE:
			fontSize = NORMAL_FONT_SIZE;
			break;
		case PREFERENCE_LARGE_FONT_SIZE:
			fontSize = LARGE_FONT_SIZE;
			break;
		default:
			fontSize = NORMAL_FONT_SIZE;
			break;
		}

		return fontSize;
	}

	private void sendFeedback() {

		String toList[] = { FEEDBACK_EMAIL_ADDRESS };

		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, toList);
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.email_feedback_subject));

		startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.email_feedback_chooser)));
	}

	public DataSource getDataSource() {

		return mDataSource;
	}

	/*
	 * Helper method to check if there is network connectivity
	 */
	public boolean hasConnectivity() {

		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		if (null == connectivityManager) {

			return false;
		}

		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

		if (null != networkInfo && networkInfo.isAvailable() && networkInfo.isConnected()) {

			return true;
		}
		else {

			return false;
		}
	}

	public void doAction(Integer action, Object data) {

		switch (action) {

		case ACTION_SHOW_CARD_SETS:
			boolean addToBackStack = (null != data ? ((Boolean) data).booleanValue() : false);
			showCardSetsFragment(addToBackStack);
			break;
		case ACTION_SHOW_CARDS:
			showCardsFragment((CardSet) data);
			break;
		case ACTION_SHOW_HELP:
			showHelp();
			break;
		case ACTION_SHOW_SETUP:
			showSetupFragment();
			break;
		case ACTION_SHOW_ABOUT:
			showAboutFragment();
			break;
		case ACTION_GET_EXTERNAL_CARD_SETS:
			getExternal();
			break;
		case ACTION_SET_HELP_CONTEXT:
			setHelpContext((Integer) data);
			break;
		case ACTION_SHOW_ADD_CARD:
			showAddCardFragment((CardSet) data);
			break;
		case ACTION_ADD_CARD_SET:
			mCardSetsFragment.addCardSet((CardSet) data);
			break;
		case ACTION_FONT_SIZE_CHANGE:
			int fontSizePreference = (null != data ? ((Integer) data).intValue() : PREFERENCE_NORMAL_FONT_SIZE);
			mFontSize = getFontSizePreference(fontSizePreference);
			break;
		case ACTION_SEND_FEEDBACK:
			sendFeedback();
			break;
		case ACTION_RESHUFFLE_CARDS:
			showCardsFragment(mCurrentCardSet);
			break;
		}
	}
}
