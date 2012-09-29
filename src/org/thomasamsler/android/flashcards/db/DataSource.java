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

package org.thomasamsler.android.flashcards.db;

import java.util.ArrayList;
import java.util.List;

import org.thomasamsler.android.flashcards.AppConstants;
import org.thomasamsler.android.flashcards.model.Card;
import org.thomasamsler.android.flashcards.model.CardSet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DataSource implements AppConstants {

	// private SQLiteDatabase mDb;
	private DatabaseHelper mDbH;

	public DataSource(Context context) {

		try {

			mDbH = new DatabaseHelper(context);
		}
		catch (Exception e) {

			Log.e(LOG_TAG, "EXCEPTION: " + e.getMessage());
		}
	}

	public void close() {

		try {

			mDbH.close();
		}
		catch (Exception e) {

			Log.e(LOG_TAG, "EXCEPTION: " + e.getMessage());
		}
	}

	public CardSet createCardSet(String title) {

		CardSet cardSet = new CardSet();

		try {

			SQLiteDatabase db = mDbH.getWritableDatabase();
			ContentValues contentValues = new ContentValues();
			contentValues.put(DatabaseHelper.CST_TITLE, title);
			long id = db.insert(DatabaseHelper.TABLE_CARD_SETS, null, contentValues);

			cardSet.setId(id);
			cardSet.setTitle(title);
		}
		catch (Exception e) {

			Log.e(LOG_TAG, "EXCEPTION: " + e.getMessage());
		}

		return cardSet;
	}

	public void createCardSet(CardSet cardSet) {

		try {

			ContentValues contentValues = new ContentValues();
			contentValues.put(DatabaseHelper.CST_CARD_SET_ID, cardSet.getExternalId());
			contentValues.put(DatabaseHelper.CST_TITLE, cardSet.getTitle());
			contentValues.put(DatabaseHelper.CST_CARD_COUNT, cardSet.getCardCount());
			SQLiteDatabase db = mDbH.getWritableDatabase();
			long id = db.insert(DatabaseHelper.TABLE_CARD_SETS, null, contentValues);

			cardSet.setId(id);
		}
		catch (Exception e) {

			Log.e(LOG_TAG, "EXCEPTION: " + e.getMessage());
		}
	}

	public void deleteCardSet(CardSet cardSet) {

		try {

			deleteCards(cardSet);

			SQLiteDatabase db = mDbH.getWritableDatabase();
			db.delete(DatabaseHelper.TABLE_CARD_SETS, "_id=?", new String[] { Long.toString(cardSet.getId()) });
		}
		catch (Exception e) {

			Log.e(LOG_TAG, "EXCEPTION: " + e.getMessage());
		}
	}

	public List<CardSet> getCardSets() {

		List<CardSet> cardSets = new ArrayList<CardSet>();

		try {

			SQLiteDatabase db = mDbH.getWritableDatabase();
			Cursor cursor = db.query(true, DatabaseHelper.TABLE_CARD_SETS, DatabaseHelper.CST_ALL_COLUMNS, null, null, null, null, null, null);
			cursor.moveToFirst();

			while (!cursor.isAfterLast()) {

				cardSets.add(getCardSet(cursor));
				cursor.moveToNext();
			}

			cursor.close();
		}
		catch (Exception e) {

			Log.e(LOG_TAG, "EXCEPTION: " + e.getMessage());
		}
		return cardSets;
	}

	public void updateCardSet(CardSet cardSet) {

		try {

			ContentValues contentValues = new ContentValues();
			contentValues.put(DatabaseHelper.CST_TITLE, cardSet.getTitle());
			contentValues.put(DatabaseHelper.CST_CARD_COUNT, cardSet.getCardCount());

			SQLiteDatabase db = mDbH.getWritableDatabase();
			db.update(DatabaseHelper.TABLE_CARD_SETS, contentValues, "_id=?", new String[] { Long.toString(cardSet.getId()) });
		}
		catch (Exception e) {

			Log.e(LOG_TAG, "EXCEPTION: " + e.getMessage());
		}
	}

	public void createCard(Card card) {

		try {

			ContentValues contentValues = new ContentValues();
			contentValues.put(DatabaseHelper.CT_CARD_ID, card.getExternalId());
			contentValues.put(DatabaseHelper.CT_QUESTION, card.getQuestion());
			contentValues.put(DatabaseHelper.CT_ANSWER, card.getAnswer());
			contentValues.put(DatabaseHelper.CT_DISPLAY_ORDER, card.getDisplayOrder());
			contentValues.put(DatabaseHelper.CT_CARD_SET_PK, card.getCardSetId());

			SQLiteDatabase db = mDbH.getWritableDatabase();
			long id = db.insert(DatabaseHelper.TABLE_CARDS, null, contentValues);

			card.setId(id);
		}
		catch (Exception e) {

			Log.e(LOG_TAG, "EXCEPTION: " + e.getMessage());
		}
	}

	public void deleteCards(CardSet cardSet) {

		try {

			SQLiteDatabase db = mDbH.getWritableDatabase();
			db.delete(DatabaseHelper.TABLE_CARDS, "card_set_pk=?", new String[] { Long.toString(cardSet.getId()) });
		}
		catch (Exception e) {

			Log.e(LOG_TAG, "EXCEPTION: " + e.getMessage());
		}
	}

	public void updateCard(Card card) {

		try {

			ContentValues contentValues = new ContentValues();
			contentValues.put(DatabaseHelper.CT_QUESTION, card.getQuestion());
			contentValues.put(DatabaseHelper.CT_ANSWER, card.getAnswer());

			SQLiteDatabase db = mDbH.getWritableDatabase();
			db.update(DatabaseHelper.TABLE_CARDS, contentValues, "_id=?", new String[] { Long.toString(card.getId()) });
		}
		catch (Exception e) {

			Log.e(LOG_TAG, "EXCEPTION: " + e.getMessage());
		}
	}

	public void deleteCard(Card card) {

		try {

			SQLiteDatabase db = mDbH.getWritableDatabase();
			db.delete(DatabaseHelper.TABLE_CARDS, "_id=?", new String[] { Long.toString(card.getId()) });
			db.execSQL("update cardsets set card_count = card_count - 1 where _id=" + Long.toString(card.getCardSetId()));
		}
		catch (Exception e) {

			Log.e(LOG_TAG, "EXCEPTION: " + e.getMessage());
		}
	}

	public List<Card> getCards(long cardSetId) {

		List<Card> cards = new ArrayList<Card>();

		try {

			SQLiteDatabase db = mDbH.getWritableDatabase();
			Cursor cursor = db.query(true, DatabaseHelper.TABLE_CARDS, DatabaseHelper.CT_ALL_COLUMNS, "card_set_pk=?", new String[] { Long.toString(cardSetId) }, null, null, "cards.display_order ASC", null);

			cursor.moveToFirst();

			while (!cursor.isAfterLast()) {

				cards.add(getCard(cursor));
				cursor.moveToNext();
			}

			cursor.close();
		}
		catch (Exception e) {

			Log.e(LOG_TAG, "EXCEPTION: " + e.getMessage());
		}

		return cards;
	}

	private CardSet getCardSet(Cursor cursor) {

		CardSet cardSet = new CardSet();

		try {

			cardSet.setId(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.CST_ID)));
			cardSet.setTitle(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CST_TITLE)));
			cardSet.setCardCount(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CST_CARD_COUNT)));
		}
		catch (Exception e) {

			Log.e(LOG_TAG, "EXCEPTION: " + e.getMessage());
		}
		return cardSet;
	}

	private Card getCard(Cursor cursor) {

		Card card = new Card();

		try {
			
			card.setId(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.CT_ID)));
			card.setQuestion(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CT_QUESTION)));
			card.setAnswer(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CT_ANSWER)));
			card.setDisplayOrder(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CT_CARD_ID)));
			card.setCardSetId(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.CT_CARD_SET_PK)));
		}
		catch (Exception e) {

			Log.e(LOG_TAG, "EXCEPTION: " + e.getMessage());
		}
		return card;
	}
}
