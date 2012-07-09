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

package org.thomasamsler.android.flashcards.conversion;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.thomasamsler.android.flashcards.AppConstants;
import org.thomasamsler.android.flashcards.MainApplication;
import org.thomasamsler.android.flashcards.R;
import org.thomasamsler.android.flashcards.db.DataSource;
import org.thomasamsler.android.flashcards.model.Card;
import org.thomasamsler.android.flashcards.model.CardSet;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class FileToDbConversion implements AppConstants {

	private Context mContext;
	private DataSource mDataSource;
	private ProgressDialog mDialog;

	public FileToDbConversion() { }

	public void convert(Context context, DataSource dataSource) {

		this.mContext = context;
		this.mDataSource = dataSource;
		
		mDialog = ProgressDialog.show(context, "", mContext.getString(R.string.conversion), true);

		new ConversionTask().execute();
	}

	class ConversionTask extends AsyncTask<Void, Void, List<CardSet>> {


		@Override
		protected List<CardSet> doInBackground(Void... params) {

			List<CardSet> cardSets = new ArrayList<CardSet>();
			
			// Get all the file names
			String[] fileNames = mContext.fileList();

			for(String fileName : fileNames) {

				CardSet cardSet = mDataSource.createCardSet(fileName);
				int displayOrder = 1;

				FileInputStream fis;
				BufferedReader reader = null;

				try {

					fis =  mContext.openFileInput(fileName);
					reader = new BufferedReader(new InputStreamReader(fis));
					String card;

					while((card = reader.readLine()) != null) {

						if(null != card && !"".equals(card) && 3 <= card.length()) {

							String[] words = card.split(":");

							if(words.length == 2) {

								Card newCard = new Card();
								newCard.setQuestion(words[0]);
								newCard.setAnswer(words[1]);
								newCard.setCardSetId(cardSet.getId());
								newCard.setDisplayOrder(displayOrder);
								displayOrder += 1;
								mDataSource.createCard(newCard);
							}
						}
					}
				}
				catch(FileNotFoundException e) {

					Log.w(AppConstants.LOG_TAG, "FileNotFoundException: while reading words from file", e);
				}
				catch(IOException e) {

					Log.w(AppConstants.LOG_TAG, "IOException: while reading words from file", e);
				}
				finally {

					try {

						if(null != reader) {

							reader.close();
						}
					}
					catch (IOException e) {

						Log.e(AppConstants.LOG_TAG, "IOException", e);
					}
				}

				cardSet.setCardCount(displayOrder - 1);
				mDataSource.updateCardSet(cardSet);
				cardSets.add(cardSet);
				mContext.deleteFile(fileName);
			}
			
			return cardSets;
		}
		
		@Override
		protected void onPostExecute(List<CardSet> cardSets) {
			
			MainApplication mainApplication = (MainApplication)((Activity)mContext).getApplication();
			
			for(CardSet cardSet : cardSets) {
				
				mainApplication.doAction(ACTION_ADD_CARD_SET, cardSet);
			}
			
			mDialog.cancel();
		}
	}
}