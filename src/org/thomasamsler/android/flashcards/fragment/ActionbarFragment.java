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

import java.util.ArrayList;
import java.util.List;

import org.thomasamsler.android.flashcards.ActionBusListener;
import org.thomasamsler.android.flashcards.AppConstants;
import org.thomasamsler.android.flashcards.MainApplication;
import org.thomasamsler.android.flashcards.R;
import org.thomasamsler.android.flashcards.activity.MainActivity;
import org.thomasamsler.android.flashcards.db.DataSource;
import org.thomasamsler.android.flashcards.model.CardSet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ActionbarFragment extends Fragment implements AppConstants, ActionBusListener {

	/*
	 * These values need to be in sync with values present in card_set_actions.xml
	 */
	private final int CS_OVERFLOW_ACTION_SETUP = 0;
	private final int CS_OVERFLOW_ACTION_FCE = 1;
	private final int CS_OVERFLOW_ACTION_SEND_FEEDBACK = 2;
	private final int CS_OVERFLOW_ACTION_ABOUT = 3;
	private final int CS_OVERFLOW_ACTION_HELP = 4;

	/*
	 * These values need to be in sync with values present in card_actions.xml
	 */
	private final int C_OVERFLOW_ACTION_ZOOM_IN = 0;
	private final int C_OVERFLOW_ACTION_ZOOM_OUT = 1;
	private final int C_OVERFLOW_ACTION_DELETE_CARD = 2;
	private final int C_OVERFLOW_ACTION_RESHUFFLE = 3;
	private final int C_OVERFLOW_ACTION_CARD_INFO = 4;
	private final int C_OVERFLOW_ACTION_HELP_CARD = 5;
	
	/*
	 * These values need to be in sync with values present in setup_actions.xml
	 */
	private final int S_OVERFLOW_ACTION_SEND_FEEDBACK = 0;
	private final int S_OVERFLOW_ACTION_ABOUT = 1;
	private final int S_OVERFLOW_ACTION_HELP = 2;

	private DataSource mDataSource;
	private ListView mListViewOverflow;
	private List<String> mOverflowActions;
	private ArrayAdapter<String> mArrayAdapter;

	private ImageButton mImageButtonEdit;
	private ImageButton mImageButtonNewCardSet;
	private ImageButton mImageButtonList;
	private ImageButton mImageButtonOverflow;
	private ImageView mImageViewPrevious;

	private int mFragmentType;

	private MainApplication mMainApplication;
	
	public static ActionbarFragment newInstance(int fragmentType) {

		ActionbarFragment listActionbarFragment = new ActionbarFragment();
		listActionbarFragment.setFragmentType(fragmentType);

		return listActionbarFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		return inflater.inflate(R.layout.actionbar_fragment, container, false);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mDataSource = ((MainActivity)getActivity()).getDataSource();
		
		mMainApplication = (MainApplication) getActivity().getApplication();
		
		mMainApplication.registerAction(this, ACTION_SHOW_OVERFLOW_ACTIONS);

		mImageButtonEdit = (ImageButton)getActivity().findViewById(R.id.imageButtonEdit);
		mImageButtonEdit.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				mMainApplication.doAction(ACTION_EDIT_CARD);
			}
		});

		mImageButtonList = (ImageButton)getActivity().findViewById(R.id.imageButtonList);
		mImageButtonList.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				mMainApplication.doAction(ACTION_SHOW_CARD_SETS, Boolean.TRUE);
			}
		});

		mImageViewPrevious = (ImageView)getActivity().findViewById(R.id.imageViewPrevious);

		mImageButtonNewCardSet = (ImageButton)getActivity().findViewById(R.id.imageButtonNewCardSet);
		mImageButtonNewCardSet.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
				builder.setCancelable(false);

				LayoutInflater inflater = getLayoutInflater(savedInstanceState);
				View layout = inflater.inflate(R.layout.dialog, (ViewGroup) getActivity().findViewById(R.id.layout_root));
				final EditText editText = (EditText)layout.findViewById(R.id.editTextDialogAdd);

				builder.setView(layout);
				builder.setPositiveButton(R.string.new_card_set_save_button, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						String newTitle = editText.getText().toString().trim();

						if(null == newTitle || "".equals(newTitle)) {

							Toast.makeText(getActivity().getApplicationContext(), R.string.new_card_set_dialog_message_warning2, Toast.LENGTH_LONG).show();
						}
						else {

							boolean titleExists = false;
							List<CardSet> cardSets = mDataSource.getCardSets();
							
							for(CardSet cardSet : cardSets) {

								if(newTitle.equals(cardSet.getTitle())) {

									titleExists = true;
									break;
								}
							}

							if(titleExists) {

								Toast.makeText(getActivity().getApplicationContext(), R.string.new_card_set_dialog_message_warning1, Toast.LENGTH_LONG).show();
							}
							else {

								CardSet cardSet = mDataSource.createCardSet(newTitle);
								mMainApplication.doAction(ACTION_ADD_CARD_SET, cardSet);
							}
						}
					}
				});

				builder.setNegativeButton(R.string.new_card_set_cancel_button, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						dialog.cancel();
					}
				});

				AlertDialog alert = builder.create();
				alert.show();
			}
		});

		mImageButtonOverflow = (ImageButton)getActivity().findViewById(R.id.imageButtonOverflow);
		mImageButtonOverflow.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				if(mListViewOverflow.getVisibility() == View.VISIBLE) {

					mListViewOverflow.setVisibility(View.GONE);
				}
				else {

					mListViewOverflow.setVisibility(View.VISIBLE);
				}
			}
		});

		mListViewOverflow = (ListView)getActivity().findViewById(R.id.listViewOverflow);
		mOverflowActions = new ArrayList<String>();
		mArrayAdapter = getArrayAdapter();
		mListViewOverflow.setAdapter(mArrayAdapter);

		/*
		 * Now we configure the action bar for the associated fragment
		 */
		configureFor(mFragmentType);
	}
	
	public void configureFor(int fragmentType) {
		
		if(View.VISIBLE == mListViewOverflow.getVisibility()) {
		
			mListViewOverflow.setVisibility(View.GONE);
		}
		
		switch(fragmentType) {

		case SETUP_FRAGMENT:
			configureForSetup();
			break;
		case LIST_FRAGMENT:
			configureForCardSets();
			break;
		case ADD_FRAGMENT:
			configureForAdd();
			break;
		case ABOUT_FRAGMENT:
			configureForAbout();
			break;
		case CARDS_FRAGMENT:
			configureForCards();
			break;
		}
	}
	
	private void configureForAdd() {

		mImageButtonEdit.setVisibility(View.GONE);
		mImageButtonNewCardSet.setVisibility(View.GONE);
		mImageButtonList.setVisibility(View.VISIBLE);
		mImageButtonOverflow.setVisibility(View.VISIBLE);
		mImageViewPrevious.setVisibility(View.VISIBLE);
		mImageButtonList.setEnabled(true);
		mFragmentType = ADD_FRAGMENT;
	}

	private void configureForCardSets() {

		mImageButtonEdit.setVisibility(View.GONE);
		mImageButtonNewCardSet.setVisibility(View.VISIBLE);
		mImageButtonList.setVisibility(View.VISIBLE);
		mImageButtonOverflow.setVisibility(View.VISIBLE);
		mImageViewPrevious.setVisibility(View.GONE);
		mImageButtonList.setEnabled(false);
		mFragmentType = LIST_FRAGMENT;
		mListViewOverflow.setOnItemClickListener(getCardSetsFragmentActionListener());
		setOverflowActions(getResources().getStringArray(R.array.card_set_actions));
	}

	private void configureForSetup() {

		mImageButtonEdit.setVisibility(View.GONE);
		mImageButtonNewCardSet.setVisibility(View.GONE);
		mImageButtonList.setVisibility(View.VISIBLE);
		mImageButtonOverflow.setVisibility(View.VISIBLE);
		mImageViewPrevious.setVisibility(View.VISIBLE);
		mImageButtonList.setEnabled(true);
		mFragmentType = SETUP_FRAGMENT;
		mListViewOverflow.setOnItemClickListener(getSetupFragmentActionListener());
		setOverflowActions(getResources().getStringArray(R.array.setup_actions));
	}

	private void configureForAbout() {

		mImageButtonEdit.setVisibility(View.GONE);
		mImageButtonNewCardSet.setVisibility(View.GONE);
		mImageButtonList.setVisibility(View.VISIBLE);
		mImageButtonOverflow.setVisibility(View.VISIBLE);
		mImageViewPrevious.setVisibility(View.VISIBLE);
		mImageButtonList.setEnabled(true);
		mFragmentType = ABOUT_FRAGMENT;
	}

	private void configureForCards() {

		mImageButtonEdit.setVisibility(View.VISIBLE);
		mImageButtonNewCardSet.setVisibility(View.GONE);
		mImageButtonList.setVisibility(View.VISIBLE);
		mImageButtonOverflow.setVisibility(View.VISIBLE);
		mImageViewPrevious.setVisibility(View.VISIBLE);
		mImageButtonList.setEnabled(true);
		mFragmentType = CARDS_FRAGMENT;
		mListViewOverflow.setOnItemClickListener(getCardFragmentActionListener());
		setOverflowActions(getResources().getStringArray(R.array.card_actions));
	}

	public void setFragmentType(int fragmentType) {

		this.mFragmentType = fragmentType;
	}
	
	public void doAction(Integer action, Object data) {

		switch(action) {
		
		case ACTION_SHOW_OVERFLOW_ACTIONS:
			toggleVisibility(mListViewOverflow);
			break;
		}
	}

	private ArrayAdapter<String> getArrayAdapter() {

		return new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mOverflowActions) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {

				View view = super.getView(position, convertView, parent);

				((TextView)view).setTextColor(Color.WHITE);

				return view;
			}
		};
	}

	private OnItemClickListener getCardFragmentActionListener() {

		return new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				mListViewOverflow.setVisibility(View.GONE);

				switch(position) {

				case C_OVERFLOW_ACTION_ZOOM_IN:
					mMainApplication.doAction(ACTION_ZOOM_IN_CARD);
					break;

				case C_OVERFLOW_ACTION_ZOOM_OUT:
					mMainApplication.doAction(ACTION_ZOOM_OUT_CARD);
					break;

				case C_OVERFLOW_ACTION_CARD_INFO:
					mMainApplication.doAction(ACTION_SHOW_CARD_INFO);
					break;

				case C_OVERFLOW_ACTION_DELETE_CARD:
					mMainApplication.doAction(ACTION_DELETE_CARD);
					break;
			
				case C_OVERFLOW_ACTION_HELP_CARD:
					mMainApplication.doAction(ACTION_SHOW_HELP);
					break;
					
				case C_OVERFLOW_ACTION_RESHUFFLE:
					mMainApplication.doAction(ACTION_RESHUFFLE_CARDS);
					break;
				}
			}
		};
	}

	private OnItemClickListener getCardSetsFragmentActionListener() {

		return new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				mListViewOverflow.setVisibility(View.GONE);

				switch(position) {

				case CS_OVERFLOW_ACTION_SETUP:
					mMainApplication.doAction(ACTION_SHOW_SETUP);
					break;

				case CS_OVERFLOW_ACTION_ABOUT:
					mMainApplication.doAction(ACTION_SHOW_ABOUT);
					break;

				case CS_OVERFLOW_ACTION_FCE:
					mMainApplication.doAction(ACTION_GET_EXTERNAL_CARD_SETS);
					break;

				case CS_OVERFLOW_ACTION_HELP:
					mMainApplication.doAction(ACTION_SHOW_HELP);
					break;
				
				case CS_OVERFLOW_ACTION_SEND_FEEDBACK:
					mMainApplication.doAction(ACTION_SEND_FEEDBACK);
					break;
				}
			}
		};
	}
	
	private OnItemClickListener getSetupFragmentActionListener() {
		
		return new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				mListViewOverflow.setVisibility(View.GONE);

				switch(position) {

				case S_OVERFLOW_ACTION_ABOUT:
					mMainApplication.doAction(ACTION_SHOW_ABOUT);
					break;

				case S_OVERFLOW_ACTION_HELP:
					mMainApplication.doAction(ACTION_SHOW_HELP);
					break;
				
				case S_OVERFLOW_ACTION_SEND_FEEDBACK:
					mMainApplication.doAction(ACTION_SEND_FEEDBACK);
					break;
				}
			}
		};
	}

	private void setOverflowActions(String[] actions) {

		mOverflowActions.clear();

		for(String action : actions) {

			mOverflowActions.add(action);
		}

		mArrayAdapter.notifyDataSetChanged();
	}


	private void toggleVisibility(View view) {
		
		if(View.VISIBLE == view.getVisibility()) {
			
			view.setVisibility(View.GONE);
		}
		else {
			
			view.setVisibility(View.VISIBLE);
		}
	}
}
