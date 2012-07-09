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

package org.thomasamsler.android.flashcards;

public interface AppConstants {
	
	public static final String PREFERENCE_NAME = "FlashCardsPrefsFile";
	public static final String PREFERENCE_FCEX_USER_NAME = "fcexun";
	public static final String PREFERENCE_SHOW_SAMPLE = "showSample";
	public static final String PREFERENCE_RUN_CONVERSION = "convert";
	public static final String PREFERENCE_FONT_SIZE = "fontSize";
	
	public static final boolean PREFERENCE_SHOW_SAMPLE_DEFAULT = true;
	public static final boolean PREFERENCE_RUN_CONVERSION_DEFAULT = true;
	
	public static final int PREFERENCE_SMALL_FONT_SIZE = 0;
	public static final int PREFERENCE_NORMAL_FONT_SIZE = 1;
	public static final int PREFERENCE_LARGE_FONT_SIZE = 2;
	
	public static final String SELECTED_LIST_ITEM_KEY = "words";
	public static final String LOG_TAG = "Flash Cards";
	public static final String FILE_NAMES_KEY = "files";
	public static final String CARD_SET_TITLE_KEY = "csnk";
	public static final String CARD_SET_ID_KEY = "csik";
	
	public static final int NORMAL_FONT_SIZE = 40;
	public static final int SMALL_FONT_SIZE = 20;
	public static final int LARGE_FONT_SIZE = 60;
	public static final int FONT_SIZE_ZOOM_CHANGE = 20;
	
	public static final String _OF_ = " of ";
	public static final String BACK = " Back";
	public static final String FRONT = " Front";
	
	// ListActivity
	public static final int HELP_CONTEXT_DEFAULT = 0;
	public static final int HELP_CONTEXT_SETUP = 1;
	public static final int HELP_CONTEXT_CARD_SET_LIST = 2;
	public static final int HELP_CONTEXT_ADD_CARD = 3;
	
	// CardsPagerActivity
	public static final int HELP_CONTEXT_VIEW_CARD = 4;
	
	// Activity Result
	public static final int ACTIVITY_RESULT = 0;
	public static final String CARD_SET_ID = "csi";
	
	public static final int INVALID_CARD_SET_ID = -1;
	
	// Fragment References
	public static final int LIST_FRAGMENT = 0;
	public static final int ADD_FRAGMENT = 1;
	public static final int SETUP_FRAGMENT = 2;
	public static final int ABOUT_FRAGMENT = 3;
	public static final int CARDS_FRAGMENT = 4;
	
	public static final String FRAGMENT_TYPE = "ft";
	
	// Action Bus Actions
	public static final int ACTION_EDIT_CARD = 0;
	public static final int ACTION_ZOOM_IN_CARD = 1;
	public static final int ACTION_ZOOM_OUT_CARD = 2;
	public static final int ACTION_SHOW_CARD_INFO = 3;
	public static final int ACTION_DELETE_CARD = 4;
	public static final int ACTION_SHOW_HELP = 5;
	public static final int ACTION_SHOW_SETUP = 6;
	public static final int ACTION_SHOW_ABOUT = 7;
	public static final int ACTION_GET_EXTERNAL_CARD_SETS = 8;
	public static final int ACTION_SET_HELP_CONTEXT = 9;
	public static final int ACTION_SHOW_CARD_SETS = 10;
	public static final int ACTION_SHOW_ADD_CARD = 11;
	public static final int ACTION_SHOW_CARDS = 12;
	public static final int ACTION_ADD_CARD_SET = 13;
	public static final int ACTION_UPDATE_CARD = 14;
	public static final int ACTION_SHOW_OVERFLOW_ACTIONS = 15;
	public static final int ACTION_DELETE_CARD_UPDATE_CARD_SET = 16;
	public static final int ACTION_FONT_SIZE_CHANGE = 17;
	public static final int ACTION_SEND_FEEDBACK = 18;
	public static final int ACTION_RESHUFFLE_CARDS = 19;
}
