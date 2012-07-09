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

package org.thomasamsler.android.flashcards.model;

public class Card {
	
	private long mId;
	private String mExternalId;
	private int mDisplayOrder;
	private String mQuestion;
	private String mAnswer;
	private long mCardSetId;
	private int mRandomCardIndex;
	
	public Card() { }
	
	public long getId() {
		return mId;
	}
	
	public void setId(long id) {
		this.mId = id;
	}
	
	public String getExternalId() {
		return mExternalId;
	}
	
	public void setExternalId(String externalId) {
		this.mExternalId = externalId;
	}
	
	public int getDisplayOrder() {
		return mDisplayOrder;
	}
	
	public void setDisplayOrder(int displayOrder) {
		this.mDisplayOrder = displayOrder;
	}
	
	public String getQuestion() {
		return mQuestion;
	}
	
	public void setQuestion(String question) {
		this.mQuestion = question;
	}
	
	public String getAnswer() {
		return mAnswer;
	}
	
	public void setAnswer(String answer) {
		this.mAnswer = answer;
	}
	
	public long getCardSetId() {
		return mCardSetId;
	}
	
	public void setCardSetId(long cardSetId) {
		this.mCardSetId = cardSetId;
	}

	public int getRandomCardIndex() {
		return mRandomCardIndex;
	}

	public void setRandomCardIndex(int randomCardIndex) {
		this.mRandomCardIndex = randomCardIndex;
	}
	
	
}
