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

package org.thomasamsler.android.flashcards.fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.thomasamsler.android.flashcards.ActionBusListener;
import org.thomasamsler.android.flashcards.AppConstants;
import org.thomasamsler.android.flashcards.MainApplication;
import org.thomasamsler.android.flashcards.R;
import org.thomasamsler.android.flashcards.activity.MainActivity;
import org.thomasamsler.android.flashcards.db.DataSource;
import org.thomasamsler.android.flashcards.external.FlashCardExchangeData;
import org.thomasamsler.android.flashcards.model.Card;
import org.thomasamsler.android.flashcards.model.CardSet;
import org.thomasamsler.android.flashcards.sample.WordSets;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class CardSetsFragment extends ListFragment implements AppConstants, ActionBusListener, FlashCardExchangeData {

	private static final int MENU_ITEM_ADD = 1;
	private static final int MENU_ITEM_DELETE = 2;
	
	private List<CardSet> mCardSets;
	private ArrayAdapter<CardSet> mArrayAdapter;
	private ProgressBar mProgressBar;
	
	private DataSource mDataSource;
	private MainActivity mActivity;
	private MainApplication mMainApplication;
	
	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mActivity = (MainActivity) getActivity();
		mDataSource = mActivity.getDataSource();
		mMainApplication = (MainApplication) mActivity.getApplication();
		
		mMainApplication.registerAction(this, ACTION_DELETE_CARD_UPDATE_CARD_SET);
		
		registerForContextMenu(getListView());
		
		mProgressBar = (ProgressBar) mActivity.findViewById(R.id.progressBar);
		
		if(null == mCardSets) {
			
			mCardSets = mDataSource.getCardSets();
		}
		
		if(0 == mCardSets.size()) {

			SharedPreferences sharedPreferences = mActivity.getSharedPreferences(AppConstants.PREFERENCE_NAME, Context.MODE_PRIVATE);
			boolean showSample = sharedPreferences.getBoolean(AppConstants.PREFERENCE_SHOW_SAMPLE, AppConstants.PREFERENCE_SHOW_SAMPLE_DEFAULT);

			if(showSample) {

				createDefaultCardSets();
				mCardSets = mDataSource.getCardSets();
			}
			else {

				Toast.makeText(mMainApplication, R.string.list_no_card_sets_message, Toast.LENGTH_SHORT).show();
			}
		}

		Collections.sort(mCardSets);

		mArrayAdapter = new ArrayAdapter<CardSet>(mActivity, android.R.layout.simple_list_item_1, mCardSets) {
			
			/*
			 * Overwriting getView method to style the list item font. If it's a remote item
			 * that hasn't been clicked on, we style it bold
			 */
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				
				TextView textView = (TextView)super.getView(position, convertView, parent);
				CardSet cardSet = mCardSets.get(position);
				
				switch(cardSet.getCardCount()) {
				
				case 0:
					textView.setText(Html.fromHtml(cardSet.getTitle() + "<br /><small><i><font color='#989898'>empty</font></i></small>"));
					break;
				case 1:
					textView.setText(Html.fromHtml(cardSet.getTitle() + "<br /><small><i><font color='#989898'>" + cardSet.getCardCount() + " card</font></i></small>"));
					break;
				default:
					textView.setText(Html.fromHtml(cardSet.getTitle() + "<br /><small><i><font color='#989898'>" + cardSet.getCardCount() + " cards</font></i></small>"));
					break;
				}
				
				
				if(mCardSets.get(position).isRemote()) {

					((TextView)textView).setTypeface(Typeface.DEFAULT_BOLD);
				}
				else {
					
					((TextView)textView).setTypeface(Typeface.DEFAULT);
				}
				
				return textView;
			}
		};

		setListAdapter(mArrayAdapter);
	
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		
		CardSet cardSet = mCardSets.get(position);

		if(!cardSet.isRemote() && !cardSet.hasCards()) {
		
			Toast.makeText(mMainApplication, R.string.view_cards_emtpy_set_message, Toast.LENGTH_SHORT).show();
			return;
		}
		
		if(cardSet.isRemote()) {

			mProgressBar.setVisibility(ProgressBar.VISIBLE);
			cardSet.setFragmentId(CardSet.CARDS_PAGER_FRAGMENT);
			
			if(hasConnectivity()) {
			
				new GetExternalCardsTask().execute(cardSet);
			}
			else {
				
				mProgressBar.setVisibility(ProgressBar.GONE);
				Toast.makeText(mMainApplication, R.string.util_connectivity_error, Toast.LENGTH_SHORT).show();
			}
		}
		else {

			mMainApplication.doAction(ACTION_SHOW_CARDS, cardSet);
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {

		menu.add(MENU_ITEM_ADD, MENU_ITEM_ADD, 1, R.string.list_menu_add);
		menu.add(MENU_ITEM_DELETE, MENU_ITEM_DELETE, 2, R.string.list_meanu_delete);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
			
		AdapterView.AdapterContextMenuInfo info =  (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		int listItemPosition = (int) getListAdapter().getItemId(info.position);
		
		switch(item.getGroupId()) {

		case MENU_ITEM_ADD:
			addCard(listItemPosition);
			break;
		case MENU_ITEM_DELETE:
			deleteCardSet(listItemPosition);
			break;
		default:
			Log.w(AppConstants.LOG_TAG, "List context menu selection not recognized.");
		}

		return false;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		mMainApplication.doAction(ACTION_SET_HELP_CONTEXT, HELP_CONTEXT_CARD_SET_LIST);
	}

	public void addCardSet(CardSet cardSet) {
		
		mCardSets.add(cardSet);
		Collections.sort(mCardSets);
		mArrayAdapter.notifyDataSetChanged();
	}
	
	private void decrementCardCount(long cardSetId) {
	
		if(AppConstants.INVALID_CARD_SET_ID != cardSetId) {
			
			for(CardSet cardSet : mCardSets) {
				
				if(cardSet.getId() == cardSetId) {
					
					cardSet.setCardCount(cardSet.getCardCount() - 1);
					break;
				}
			}
		}
	}
	
	public void getFlashCardExchangeCardSets() {
		
		SharedPreferences preferences = mActivity.getSharedPreferences(AppConstants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		String userName = preferences.getString(AppConstants.PREFERENCE_FCEX_USER_NAME, "");
		
		if(null != userName && !"".equals(userName)) {
		
			mProgressBar.setVisibility(ProgressBar.VISIBLE);
			
			if(hasConnectivity()) {
			
				new GetExternalCardSetsTask().execute(userName);
			}
			else {
				
				mProgressBar.setVisibility(ProgressBar.GONE);
				Toast.makeText(mMainApplication, R.string.util_connectivity_error, Toast.LENGTH_SHORT).show();
			}
		}
		else {
			
			Toast.makeText(mMainApplication, R.string.setup_no_user_name_defined, Toast.LENGTH_SHORT).show();
			mMainApplication.doAction(ACTION_SHOW_SETUP);
		}
	}
	
	private void addCard(int listItemPosition) {
		
		CardSet cardSet = mCardSets.get(listItemPosition);
		
		if(cardSet.isRemote()) {
			
			mProgressBar.setVisibility(ProgressBar.VISIBLE);
			
			cardSet.setFragmentId(CardSet.ADD_CARD_FRAGMENT);
			
			if(hasConnectivity()) {
				
				new GetExternalCardsTask().execute(cardSet);
			}
			else {
				
				mProgressBar.setVisibility(ProgressBar.GONE);
				Toast.makeText(mMainApplication, R.string.util_connectivity_error, Toast.LENGTH_SHORT).show();
			}
		}
		else {

			mMainApplication.doAction(ACTION_SHOW_ADD_CARD, cardSet);
		}
	}
	
	private void deleteCardSet(final int listItemPosition) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setMessage(R.string.delete_card_set_dialog_message);
		builder.setCancelable(false);
		builder.setPositiveButton(R.string.delete_card_set_dialog_ok, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				
				CardSet cardSet = mCardSets.get(listItemPosition);
				List<CardSet> cardSets = mDataSource.getCardSets();
				
				if(cardSets.contains(cardSet)) {
					
					mDataSource.deleteCardSet(cardSet);
				}
				
				mCardSets.remove(listItemPosition);
				Collections.sort(mCardSets);
				mArrayAdapter.notifyDataSetChanged();
			}
		});
		
		builder.setNegativeButton(R.string.delete_card_set_dialog_cancel, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) { 
				
				dialog.cancel();
			}
		});
		
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void createDefaultCardSets() {

		List<CardSet> cardSets = mDataSource.getCardSets();

		// Loading first sample CardSet
		CardSet sampleCardSet = new CardSet();
		sampleCardSet.setTitle(WordSets.mWordSetNames.get(0));
		
		if(!cardSets.contains(sampleCardSet)) {
			
			List<String> samples = new ArrayList<String>(Arrays.asList(getResources().getStringArray(WordSets.mWordSets.get(Integer.valueOf(0)))));
			
			sampleCardSet.setCardCount(samples.size());
			mDataSource.createCardSet(sampleCardSet);
			int displayOrder = 1;
			for(String sample : samples) {
				
				String[] parts = sample.split(":");
				Card newCard = new Card();
				newCard.setQuestion(parts[0]);
				newCard.setAnswer(parts[1]);
				newCard.setCardSetId(sampleCardSet.getId());
				newCard.setDisplayOrder(displayOrder);
				displayOrder += 1;
				mDataSource.createCard(newCard);
			}
		}
		
		// Loading second sample CardSet
		sampleCardSet = new CardSet();
		sampleCardSet.setTitle(WordSets.mWordSetNames.get(1));
		
		if(!cardSets.contains(sampleCardSet)) {
			
			List<String> samples = new ArrayList<String>(Arrays.asList(getResources().getStringArray(WordSets.mWordSets.get(Integer.valueOf(1)))));
			
			sampleCardSet.setCardCount(samples.size());
			mDataSource.createCardSet(sampleCardSet);
			int displayOrder = 1;
			for(String sample : samples) {
				
				String[] parts = sample.split(":");
				Card newCard = new Card();
				newCard.setQuestion(parts[0]);
				newCard.setAnswer(parts[1]);
				newCard.setCardSetId(sampleCardSet.getId());
				newCard.setDisplayOrder(displayOrder);
				displayOrder += 1;
				mDataSource.createCard(newCard);
			}
		}
	}

	
	/*
     * Helper method to check if there is network connectivity
     */
	private boolean hasConnectivity() {
		
		return mActivity.hasConnectivity();
	}
	
	private class GetExternalCardSetsTask extends AsyncTask<String, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(String... params) {
			
			String userName = params[0].trim();
			
			StringBuilder uriBuilder = new StringBuilder();
			uriBuilder.append(API_GET_USER).append(userName).append(API_KEY);
			
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpGet = null;
			
			try {
				
				httpGet = new HttpGet(uriBuilder.toString());
			}
			catch(IllegalArgumentException e) {
				
				Log.e(AppConstants.LOG_TAG, "IllegalArgumentException", e);
			}
			
			HttpResponse response;
			JSONObject jsonObject = null;
			
			if(null == httpGet) {
				
				return jsonObject;
			}

			try {
				
				response = httpclient.execute(httpGet);
				HttpEntity entity = response.getEntity();

				if (entity != null) {

					InputStream inputStream = entity.getContent();
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					StringBuilder content = new StringBuilder();

					String line = null;

					try {

						while((line = reader.readLine()) != null) {

							content.append(line);
						}
					}
					catch(IOException e) {

						Log.e(AppConstants.LOG_TAG, "IOException", e);
					}
					finally {

						try {

							reader.close();
						}
						catch(IOException e) {

							Log.e(AppConstants.LOG_TAG, "IOException", e);
						}
					}

					jsonObject = new JSONObject(content.toString());
				}
			}
			catch(ClientProtocolException e) {

				Log.e(AppConstants.LOG_TAG, "ClientProtocolException", e);
			}
			catch(IOException e) {

				Log.e(AppConstants.LOG_TAG, "IOException", e);
			}
			catch (JSONException e) {
				
				Log.e(AppConstants.LOG_TAG, "JSONException", e);
			}
			catch(Exception e) {

				Log.e(AppConstants.LOG_TAG, "General Exception", e);
			}

			return jsonObject;
		}

		@Override
		protected void onPostExecute(JSONObject jsonObject) {
			
			try {
			
				mProgressBar.setVisibility(ProgressBar.GONE);

				if(null != jsonObject) {

					String responseType = null;

					try {

						responseType = jsonObject.getString(FIELD_RESPONSE_TYPE);
					}
					catch(JSONException e) {

						Log.e(AppConstants.LOG_TAG, "JSONException", e);
					}

					if(null == responseType || !RESPONSE_OK.equals(responseType)) {

						Toast.makeText(mMainApplication, R.string.util_flash_card_exchange_api_error, Toast.LENGTH_LONG).show();
						return;
					}

					try {

						JSONArray jsonArray = jsonObject.getJSONObject(FIELD_RESULT).getJSONArray(FILED_SETS);

						/*
						 * Only add "new" cards to the list
						 */
						for(int i = 0; i < jsonArray.length(); i++) {

							JSONObject data = jsonArray.getJSONObject(i);
							
							CardSet newCardSet = new CardSet();
							newCardSet.setTitle(data.getString(FIELD_TITLE));
							newCardSet.setExternalId(data.getString(FIELD_CARD_SET_ID));
							newCardSet.setCardCount(data.getInt(FIELD_FLASHCARD_COUNT));
							
							if(!mCardSets.contains(newCardSet)) {

								mCardSets.add(newCardSet);
							}
						}
					}
					catch (JSONException e) {

						Log.e(AppConstants.LOG_TAG, "JSONException", e);
					}

					/*
					 * Sorting the list and refresh it
					 */
					Collections.sort(mCardSets);
					mArrayAdapter.notifyDataSetChanged();
				}
				else {

					/*
					 * In case the cardSet list is null, an exception occurred in 
					 * doInBackground().
					 */
					Toast.makeText(mMainApplication, R.string.view_cards_fetch_remote_error, Toast.LENGTH_LONG).show();
				}
			}
			catch(Exception e) {
				
				Log.e(AppConstants.LOG_TAG, "General Exception", e);
			}
		}
	}
	
	private class GetExternalCardsTask extends AsyncTask<CardSet, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(CardSet... cardSets) {
			
			StringBuilder uriBuilder = new StringBuilder();
			uriBuilder.append(API_GET_CARD_SET).append(cardSets[0].getExternalId()).append(API_KEY);
			
			HttpClient httpclient = new DefaultHttpClient();

			HttpGet httpGet = null;

			try {

				httpGet = new HttpGet(uriBuilder.toString());
			}
			catch(IllegalArgumentException e) {

				Log.e(AppConstants.LOG_TAG, "IllegalArgumentException", e);
			}

			HttpResponse response;
			
			JSONObject jsonObject = null;

			if(null == httpGet) {
				
				return jsonObject;
			}
			
			try {
				
				response = httpclient.execute(httpGet);
				HttpEntity entity = response.getEntity();

				if (entity != null) {

					InputStream inputStream = entity.getContent();
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					StringBuilder content = new StringBuilder();

					String line = null;

					try {

						while((line = reader.readLine()) != null) {

							content.append(line);
						}
					}
					catch(IOException e) {

						Log.e(AppConstants.LOG_TAG, "IOException", e);
					}
					finally {

						try {

							reader.close();
						}
						catch(IOException e) {

							Log.e(AppConstants.LOG_TAG, "IOException", e);
						}
					}

					jsonObject = new JSONObject(content.toString());
					
					/*
					 * Serialize CardSet to JSON so that we can pass it to the onPostExecute()
					 * method along with all the cards data
					 */
					jsonObject.put(FIELD_FC_ARG, cardSets[0].getJSON());
				}
			}
			catch(ClientProtocolException e) {

				Log.e(AppConstants.LOG_TAG, "ClientProtocolException", e);
			}
			catch(IOException e) {

				Log.e(AppConstants.LOG_TAG, "IOException", e);
			}
			catch(Exception e) {

				Log.e(AppConstants.LOG_TAG, "General Exception", e);
			}

			return jsonObject;
		}
		
		@Override
		protected void onPostExecute(JSONObject jsonObject) {

			try {
				
				mProgressBar.setVisibility(ProgressBar.GONE);

				if(null == jsonObject) {

					Toast.makeText(mActivity.getApplicationContext(), R.string.view_cards_fetch_remote_error, Toast.LENGTH_LONG).show();
					return;
				}

				CardSet cardSet = null;

				try {

					// Check REST call response
					String responseType = jsonObject.getString(FIELD_RESPONSE_TYPE);

					if(null == responseType || !RESPONSE_OK.equals(responseType)) {

						Toast.makeText(mActivity.getApplicationContext(), R.string.util_flash_card_exchange_api_error, Toast.LENGTH_LONG).show();
						return;
					}

					/*
					 * Since we passed in the CardSet to the AsyncTask, we had to serialize it 
					 * to JSON in the doInBackground() method so that we can get it in this onPostExecute() method
					 */
					JSONObject cardSetJson = jsonObject.getJSONObject(FIELD_FC_ARG);
					cardSet = new CardSet();
					cardSet.setExternalId("");
					cardSet.setTitle(cardSetJson.getString(CardSet.TITLE_KEY));
					cardSet.setFragmentId(cardSetJson.getInt(CardSet.FRAGMENT_KEY));
					cardSet.setCardCount(cardSetJson.getInt(CardSet.CARD_COUNT_KEY));

					// Card Set Cards
					JSONArray jsonArray = jsonObject.getJSONObject(FIELD_RESULT).getJSONArray(FIELD_FLASHCARDS);
					
					// Store the CardSet
					mDataSource.createCardSet(cardSet);

					// Store all the Cards
					for(int i = 0; i < jsonArray.length(); i++) {

						JSONObject data = jsonArray.getJSONObject(i);
						
						Card card = new Card();
						card.setId(data.getLong(FIELD_CARD_ID));
						card.setExternalId(data.getString(FIELD_CARD_ID));
						card.setQuestion(data.getString(FIELD_QUESTION));
						card.setAnswer(data.getString(FIELD_ANSWER));
						card.setDisplayOrder(data.getInt(FIELD_DISPLAY_ORDER));
						card.setCardSetId(cardSet.getId());
						
						mDataSource.createCard(card);
					}

					/* 
					 * Now that we have the cards, we indicate that we don't need to get 
					 * them anymore, thus setting the card set's id to an empty string
					 */
					int position = mCardSets.indexOf(cardSet);
					mCardSets.get(position).setExternalId("");
					mCardSets.get(position).setId(cardSet.getId());
					mArrayAdapter.notifyDataSetChanged();

				}
				catch(JSONException e) {

					Log.e(AppConstants.LOG_TAG, "JSONException", e);
				}
				
				
				if(null == cardSet) {
					
					return;
				}
				
				switch(cardSet.getFragmentId()) {

				case CardSet.ADD_CARD_FRAGMENT:
					mMainApplication.doAction(ACTION_SHOW_ADD_CARD, cardSet);
					return;

				case CardSet.CARDS_PAGER_FRAGMENT:
					mMainApplication.doAction(ACTION_SHOW_CARDS, cardSet);
					return;
				}
			}
			catch(Exception e) { 

				Log.e(AppConstants.LOG_TAG, "General Exception", e);
			}
		}
	}

	public void doAction(Integer action, Object data) {

		switch(action) {
		
		case ACTION_DELETE_CARD_UPDATE_CARD_SET:
			decrementCardCount(((Long) data).longValue());
			break;
		}
	}
}
