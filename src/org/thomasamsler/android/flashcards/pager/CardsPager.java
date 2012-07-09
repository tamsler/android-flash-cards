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

package org.thomasamsler.android.flashcards.pager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.thomasamsler.android.flashcards.ActionBusListener;
import org.thomasamsler.android.flashcards.AppConstants;
import org.thomasamsler.android.flashcards.MainApplication;
import org.thomasamsler.android.flashcards.R;
import org.thomasamsler.android.flashcards.activity.MainActivity;
import org.thomasamsler.android.flashcards.db.DataSource;
import org.thomasamsler.android.flashcards.dialog.HelpDialog;
import org.thomasamsler.android.flashcards.fragment.CardFragment;
import org.thomasamsler.android.flashcards.model.Card;
import org.thomasamsler.android.flashcards.model.CardSet;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.ViewGroup;
import android.widget.Toast;

public class CardsPager implements ActionBusListener, AppConstants {

	private static final Integer NEG_ONE = Integer.valueOf(-1);
	
	private ViewPager mViewPager;
	private MyFragmentPagerAdapter mMyFragmentPagerAdapter;
	private Random mRandom;
	private List<Card> mCards;
	private List<Integer> mRandomCardPositionList;
	private List<Integer> mAvailableCardPositionList;
	private int mNumberOfCards;
	private int mHelpContext;
	private int mFontSize;
	
	private CardSet mCardSet;
	private DataSource mDataSource;
	private MainActivity mActivity;
	private Context mApplicationContext;
	private MainApplication mMainApplication;
	
	public CardsPager(FragmentActivity activity, DataSource dataSource, CardSet cardSet, int fontSize) {

		mActivity =  (MainActivity)activity;
		mApplicationContext = activity.getApplicationContext();
		mMainApplication = (MainApplication) activity.getApplication();
		mDataSource = dataSource;
		mCardSet = cardSet;
		mFontSize = fontSize;
		
		mHelpContext = HELP_CONTEXT_VIEW_CARD;
		
		mMainApplication.registerAction(
				this,
				ACTION_EDIT_CARD,
				ACTION_ZOOM_IN_CARD,
				ACTION_ZOOM_OUT_CARD,
				ACTION_SHOW_CARD_INFO,
				ACTION_DELETE_CARD,
				ACTION_UPDATE_CARD);
		
		mRandom = new Random();
		
		mCards = mDataSource.getCards(mCardSet.getId());
		
		if(0 == mCards.size()) {
			
			Toast.makeText(mApplicationContext, R.string.view_cards_emtpy_set_message, Toast.LENGTH_SHORT).show();
		}
		
		mNumberOfCards = mCards.size();
		mRandomCardPositionList = new ArrayList<Integer>();
		mAvailableCardPositionList = new ArrayList<Integer>();
		
		// Initialize arrays
		for(int i = 0; i < mNumberOfCards; i++) {
			
			mRandomCardPositionList.add(NEG_ONE);
			mAvailableCardPositionList.add(Integer.valueOf(i));
		}
		
		mViewPager = (ViewPager) mActivity.findViewById(R.id.viewpager);
		mMyFragmentPagerAdapter = new MyFragmentPagerAdapter(mActivity.getSupportFragmentManager());
		mViewPager.setAdapter(mMyFragmentPagerAdapter);
		
		/*
		 * Use page change listener to magnify and reduce the word's font size
		 */
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			public void onPageSelected(int currentIndex) {

				CardFragment cardFragment = mMyFragmentPagerAdapter.getFragment(currentIndex);
				
				if(null != cardFragment) {
					
					cardFragment.setFontSize(mFontSize);
				}
			}

			public void onPageScrolled(int arg0, float arg1, int arg2) { /* Nothing to do here */ }
			
			public void onPageScrollStateChanged(int state) { /* Nothing to do here */ }

		});
	}
	
	protected void showHelp() {

		HelpDialog helpDialog = new HelpDialog(mActivity);

		switch(mHelpContext) {

		case HELP_CONTEXT_DEFAULT:
			helpDialog.setHelp(mActivity.getResources().getString(R.string.help_content_default));
			break;

		case HELP_CONTEXT_VIEW_CARD:
			helpDialog.setHelp(mActivity.getResources().getString(R.string.help_content_view_card));
			break;

		default:
			helpDialog.setHelp(mActivity.getResources().getString(R.string.help_content_default));
		}

		helpDialog.show();
	}

	private void updateCard(Card updatedCard) {

		/*
		 * First, we update the in memory list of cards
		 */
		Card card = mCards.get(mRandomCardPositionList.get(updatedCard.getRandomCardIndex()));
		card.setQuestion(updatedCard.getQuestion());
		card.setAnswer(updatedCard.getAnswer());
		
		mDataSource.updateCard(card);
	}
	
	private void editCard() {
		
		int currentIndex = mViewPager.getCurrentItem();
		CardFragment cardFragment = mMyFragmentPagerAdapter.getFragment(currentIndex);

		if(null != cardFragment) {

			cardFragment.onEdit();
		}
	}

	/*
	 * Called from action bar
	 */
	private void zoom(int action) {
		
		int currentIndex = mViewPager.getCurrentItem();
		CardFragment cardFragment = mMyFragmentPagerAdapter.getFragment(currentIndex);
	
		if(null == cardFragment) {
			
			return;
		}
		
		if(ACTION_ZOOM_IN_CARD == action) {
			
			mFontSize += FONT_SIZE_ZOOM_CHANGE;
		}
		else if(ACTION_ZOOM_OUT_CARD == action) {
			
			mFontSize -= FONT_SIZE_ZOOM_CHANGE;
		}
		
		cardFragment.setFontSize(mFontSize);
	}
	

	private void showCardInformation() {
		
		String message = String.format(mActivity.getResources().getString(R.string.card_information), mCardSet.getTitle());
		
		Toast.makeText(mApplicationContext, message, Toast.LENGTH_SHORT).show();
	}
	
	/*
	 * Called From action bar
	 */
	public void deleteCard() {
		
		// Get the current card index
		int currentIndex = mViewPager.getCurrentItem();
		
		// Reduce the card counter by one
		mNumberOfCards -= 1;
		
		Card card = null;
		
		if(mRandomCardPositionList.size() > 0) {

			// Mark card as deleted. The saveCards(...) method ignores null or empty string cards
			card = mCards.set(mRandomCardPositionList.get(currentIndex), null);

			// Delete card
			mDataSource.deleteCard(card);

			// Remove the deleted card position
			mRandomCardPositionList.remove(currentIndex);
		}

		/*
		 * Determine all remaining random card positions
		 */
		int randomNum;
		
		if(mAvailableCardPositionList.size() > 0) {
		
			for(int i = 0; i < mRandomCardPositionList.size(); i++) {

				if(NEG_ONE.compareTo(mRandomCardPositionList.get(i)) == 0 && mAvailableCardPositionList.size() > 0) {

					randomNum = mRandom.nextInt(mAvailableCardPositionList.size());
					mRandomCardPositionList.set(i, mAvailableCardPositionList.remove(randomNum));
				}
			}
		}

		mMyFragmentPagerAdapter.notifyDataSetChanged();
		
		// When we delete the last card in a card set, we return to the list
		if(mRandomCardPositionList.size() == 0) {
			
			String message = String.format(mActivity.getResources().getString(R.string.delete_last_card_message), mCardSet.getTitle());
			Toast.makeText(mApplicationContext, message, Toast.LENGTH_SHORT).show();
			mMainApplication.doAction(ACTION_SHOW_CARD_SETS, Boolean.TRUE);
		}
		else {
			
			Toast.makeText(mApplicationContext, R.string.delete_card, Toast.LENGTH_SHORT).show();
		}
		
		if(null != card) {
		
			// Notify CardSet that we have just deleted a card
			mMainApplication.doAction(ACTION_DELETE_CARD_UPDATE_CARD_SET, Long.valueOf(card.getCardSetId()));
		}
	}

	
	/*
	 * Classes
	 */
	private class MyFragmentPagerAdapter extends FragmentStatePagerAdapter {

		private Map<Integer, WeakReference<CardFragment>> mPageReferenceMap = new HashMap<Integer, WeakReference<CardFragment>>();
		
		public MyFragmentPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int index) {

			int randomNum;
			
			if(mRandomCardPositionList.get(index).compareTo(NEG_ONE) == 0) {
				
				randomNum = mRandom.nextInt(mAvailableCardPositionList.size());
				mRandomCardPositionList.set(index, mAvailableCardPositionList.remove(randomNum));
			}
			
			CardFragment cardFragment = CardFragment.newInstance(mCards.get(mRandomCardPositionList.get(index)), index, mNumberOfCards, mFontSize);
			mPageReferenceMap.put(Integer.valueOf(index), new WeakReference<CardFragment>(cardFragment));
			
			return cardFragment;
		}

		@Override
		public int getCount() {

			return mNumberOfCards;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
		
			super.destroyItem(container, position, object);
			mPageReferenceMap.remove(Integer.valueOf(position));
		}
		
		/*
		 * Overriding this method in conjunction with calling notifyDataSetChanged 
		 * removes a page from the pager.
		 */
		@Override
		public int getItemPosition(Object object) {
		 
			return POSITION_NONE;
		}
		
		public CardFragment getFragment(int key) {

			WeakReference<CardFragment> weakReference = mPageReferenceMap.get(key);
			
			if(null != weakReference) {
				
				return (CardFragment) weakReference.get();
			}
			else {
				
				return null;
			}
		}
	}

	public void doAction(Integer action, Object data) {

		switch(action) {
		
		case ACTION_EDIT_CARD:
			editCard();
			break;
		case ACTION_ZOOM_IN_CARD:
			zoom(ACTION_ZOOM_IN_CARD);
			break;
		case ACTION_ZOOM_OUT_CARD:
			zoom(ACTION_ZOOM_OUT_CARD);
			break;
		case ACTION_SHOW_CARD_INFO:
			showCardInformation();
			break;
		case ACTION_DELETE_CARD:
			deleteCard();
			break;
		case ACTION_UPDATE_CARD:
			updateCard((Card) data);
			break;
		}
	}
}
