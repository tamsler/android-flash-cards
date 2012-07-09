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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	// DB
	private static final String DATABASE_NAME = "afc.db";
	private static final int DATABASE_VERSION = 1;

	// Card Set Table (CST)
	public static final String TABLE_CARD_SETS = "cardsets";
	public static final String CST_ID = "_id";
	public static final String CST_CARD_SET_ID = "card_set_id";
	public static final String CST_TITLE = "title";
	public static final String CST_TAGS = "tags";
	public static final String CST_CARD_COUNT = "card_count";
	public static final String[] CST_ALL_COLUMNS = { CST_ID, CST_CARD_SET_ID, CST_TITLE, CST_TAGS, CST_CARD_COUNT };
	
	// Cards Table (CT)
	public static final String TABLE_CARDS = "cards";
	public static final String CT_ID = "_id";
	public static final String CT_CARD_ID = "card_id";
	public static final String CT_DISPLAY_ORDER = "display_order";
	public static final String CT_QUESTION = "question";
	public static final String CT_ANSWER = "answer";
	public static final String CT_CARD_SET_PK = "card_set_pk";
	public static final String[] CT_ALL_COLUMNS = { CT_ID, CT_CARD_ID, CT_DISPLAY_ORDER, CT_QUESTION, CT_ANSWER, CT_CARD_SET_PK };
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("CREATE TABLE IF NOT EXISTS ").append(TABLE_CARD_SETS).append("(")
				   .append(CST_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
				   .append(CST_CARD_SET_ID).append(" TEXT,")
				   .append(CST_TITLE).append(" TEXT NOT NULL,")
				   .append(CST_TAGS).append(" TEXT,")
				   .append(CST_CARD_COUNT).append(" INTEGER)");
		
		db.execSQL(sb.toString());
		
		sb = new StringBuilder();
		
		sb.append("CREATE TABLE IF NOT EXISTS ").append(TABLE_CARDS).append("(")
				   .append(CT_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
				   .append(CT_CARD_ID).append(" TEXT,")
				   .append(CT_DISPLAY_ORDER).append(" INTEGER,")
				   .append(CT_QUESTION).append(" TEXT NOT NULL,")
				   .append(CT_ANSWER).append(" TEXT,")
				   .append(CT_CARD_SET_PK).append(" INTEGER REFERENCES ").append(TABLE_CARD_SETS).append("(").append(CST_ID).append("))");

		db.execSQL(sb.toString());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		// Nothing to do yet
	}
}
