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

import org.thomasamsler.android.flashcards.AppConstants;
import org.thomasamsler.android.flashcards.MainApplication;
import org.thomasamsler.android.flashcards.R;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class AboutFragment extends Fragment implements AppConstants {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		return inflater.inflate(R.layout.about_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		String version = null;

		// Get version information from AndroidManifest.xml
		try {

			version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
		}
		catch (NameNotFoundException e) {

			Log.w(AppConstants.LOG_TAG, "Exception accessing version information", e);
		}

		TextView versionTextView = (TextView)getActivity().findViewById(R.id.textViewAboutVersion);
		versionTextView.append(version);

		ImageButton imageButtonCancel = (ImageButton)getActivity().findViewById(R.id.imageButtonAboutClose);
		imageButtonCancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				((MainApplication)getActivity().getApplication()).doAction(ACTION_SHOW_CARD_SETS, Boolean.TRUE);
			}
		});
	}
}
