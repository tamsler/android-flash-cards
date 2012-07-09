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

import org.thomasamsler.android.flashcards.model.Card;
import org.thomasamsler.android.flashcards.model.CardSet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DataSource {

	private SQLiteDatabase mDb;
	private DatabaseHelper mDbH;
	
	public DataSource(Context context) {
		
		mDbH = new DatabaseHelper(context);
	}
	
	public void open() throws SQLException {
		
		mDb = mDbH.getWritableDatabase();
		
	}
	
	public void close() {
		
		mDbH.close();
	}

	public CardSet createCardSet(String title) {
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.CST_TITLE, title);
		long id = mDb.insert(DatabaseHelper.TABLE_CARD_SETS, null, contentValues);
		
		CardSet cardSet = new CardSet();
		cardSet.setId(id);
		cardSet.setTitle(title);
		
		return cardSet;
	}
	
	public void createCardSet(CardSet cardSet) {
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.CST_CARD_SET_ID, cardSet.getExternalId());
		contentValues.put(DatabaseHelper.CST_TITLE, cardSet.getTitle());
		contentValues.put(DatabaseHelper.CST_CARD_COUNT, cardSet.getCardCount());
		long id = mDb.insert(DatabaseHelper.TABLE_CARD_SETS, null, contentValues);
		
		cardSet.setId(id);
	}
	
	public void deleteCardSet(CardSet cardSet) {
		
		deleteCards(cardSet);
		
		mDb.delete(DatabaseHelper.TABLE_CARD_SETS, "_id=?", new String[] { Long.toString(cardSet.getId()) });
	}
	
	public List<CardSet> getCardSets() {
		
		List<CardSet> cardSets = new ArrayList<CardSet>();
		
		Cursor cursor = mDb.query(true, DatabaseHelper.TABLE_CARD_SETS, DatabaseHelper.CST_ALL_COLUMNS, null, null, null, null, null, null);
		cursor.moveToFirst();
		
		while (!cursor.isAfterLast()) {
			
			cardSets.add(getCardSet(cursor));
			cursor.moveToNext();
		}
		
		cursor.close();
		
		return cardSets;
	}
	
	public void updateCardSet(CardSet cardSet) {
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.CST_TITLE, cardSet.getTitle());
		contentValues.put(DatabaseHelper.CST_CARD_COUNT, cardSet.getCardCount());
		mDb.update(DatabaseHelper.TABLE_CARD_SETS, contentValues, "_id=?", new String[] { Long.toString(cardSet.getId()) });
	}
	
	
	public void createCard(Card card) {
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.CT_CARD_ID, card.getExternalId());
		contentValues.put(DatabaseHelper.CT_QUESTION, card.getQuestion());
		contentValues.put(DatabaseHelper.CT_ANSWER, card.getAnswer());
		contentValues.put(DatabaseHelper.CT_DISPLAY_ORDER, card.getDisplayOrder());
		contentValues.put(DatabaseHelper.CT_CARD_SET_PK, card.getCardSetId());
		long id = mDb.insert(DatabaseHelper.TABLE_CARDS, null, contentValues);
		
		card.setId(id);
	}
	
	public void deleteCards(CardSet cardSet) {
		
		mDb.delete(DatabaseHelper.TABLE_CARDS, "card_set_pk=?", new String[] { Long.toString(cardSet.getId()) });
	}
	
	public void updateCard(Card card) {
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(DatabaseHelper.CT_QUESTION, card.getQuestion());
		contentValues.put(DatabaseHelper.CT_ANSWER, card.getAnswer());
		mDb.update(DatabaseHelper.TABLE_CARDS, contentValues, "_id=?", new String[] { Long.toString(card.getId()) });
	}
	
	public void deleteCard(Card card) {
		
		mDb.delete(DatabaseHelper.TABLE_CARDS, "_id=?", new String[] { Long.toString(card.getId()) });
		mDb.execSQL("update cardsets set card_count = card_count - 1 where _id=" + Long.toString(card.getCardSetId()));
	}
	
	public List<Card> getCards(long cardSetId) {
		
		List<Card> cards = new ArrayList<Card>();
		
		Cursor cursor = mDb.query(true,
								  DatabaseHelper.TABLE_CARDS,
								  DatabaseHelper.CT_ALL_COLUMNS,
								  "card_set_pk=?",
								  new String [] { Long.toString(cardSetId) },
								  null,
								  null,
								  "cards.display_order ASC",
								  null);
		
		cursor.moveToFirst();
		
		while (!cursor.isAfterLast()) {
			
			cards.add(getCard(cursor));
			cursor.moveToNext();
		}
		
		cursor.close();
		
		return cards;
	}
	
	private CardSet getCardSet(Cursor cursor) {
		
		CardSet cardSet = new CardSet();
		cardSet.setId(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.CST_ID)));
		cardSet.setTitle(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CST_TITLE)));
		cardSet.setCardCount(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CST_CARD_COUNT)));
		
		return cardSet;
	}
	
	private Card getCard(Cursor cursor) {
		
		Card card = new Card();
		card.setId(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.CT_ID)));
		card.setQuestion(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CT_QUESTION)));
		card.setAnswer(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CT_ANSWER)));
		card.setDisplayOrder(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CT_CARD_ID)));
		card.setCardSetId(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.CT_CARD_SET_PK)));
		
		return card;
	}
}
