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

package org.thomasamsler.android.flashcards.dialog;

import org.thomasamsler.android.flashcards.R;

import android.app.Dialog;
import android.content.Context;
import android.text.Html;
import android.widget.TextView;

public class HelpDialog extends Dialog {

	private TextView mHelpTextView;
	
	public HelpDialog(Context context) {
		super(context);

		setContentView(R.layout.help_dialog);
		setTitle(R.string.help_dialog_title);
		setCanceledOnTouchOutside(true);
		
		 mHelpTextView = (TextView) findViewById(R.id.textViewHelp);
	}

	public void setHelp(String help) {
		
		mHelpTextView.setText(Html.fromHtml(help));
	}
}
