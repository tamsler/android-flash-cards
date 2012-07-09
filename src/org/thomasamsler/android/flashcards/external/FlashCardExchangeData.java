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

package org.thomasamsler.android.flashcards.external;

public interface FlashCardExchangeData {
	
	public static final String API_GET_USER = "http://api.flashcardexchange.com/v1/get_user?dataset=3&user_login=";
	public static final String API_GET_CARD_SET = "http://api.flashcardexchange.com/v1/get_card_set?card_set_id=";
	public static final String API_KEY = "&api_key=";
	
	public static final String RESPONSE_OK = "ok";
	public static final String RESPONSE_ERROR = "error";
	
	// APP JSON FIELDS
	public static final String FIELD_FC_ARG = "fc_arg";
	
	// FlashCardExchange JSON FIELDS
	public static final String FIELD_RESPONSE_TYPE = "response_type";
	public static final String FIELD_RESULT = "results";
	public static final String FILED_SETS = "sets";
	public static final String FIELD_CARD_SET_ID = "card_set_id";
	public static final String FIELD_TITLE = "title";
	public static final String FIELD_FLASHCARD_COUNT = "flashcard_count";
	public static final String FIELD_FLASHCARDS = "flashcards";
	public static final String FIELD_CARD_ID = "card_id";
	public static final String FIELD_QUESTION = "question";
	public static final String FIELD_ANSWER = "answer";
	public static final String FIELD_DISPLAY_ORDER = "display_order";

}
