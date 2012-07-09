/*
 * Copyright 2012 Thomas Amsler
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Application;

public class MainApplication extends Application {

	private Map<Integer, List<WeakReference<ActionBusListener>>> actionBusListeners;
	
	public void registerAction(ActionBusListener actionBusListener, Integer... actions) {
		
		if(null == actionBusListener || null == actions) {
			
			return;
		}
		
		for(Integer action: actions) {
			
			List<WeakReference<ActionBusListener>> listeners = actionBusListeners.get(action);
			
			if(null == listeners) {
				
				listeners = new ArrayList<WeakReference<ActionBusListener>>();
			}

			/*
			 * Before we add the new listener, we remove all previous listeners of that object type 
			 */
			removeReference(listeners, actionBusListener);
			
			listeners.add(new WeakReference<ActionBusListener>(actionBusListener));
			actionBusListeners.put(action, listeners);
		}
	}
	
	public void doAction(Integer action) {
		
		doAction(action, null);
	}
	
	public void doAction(Integer action, Object data) {
		
		if(null == action) {
			
			return;
		}
		
		List<WeakReference<ActionBusListener>> listeners = actionBusListeners.get(action);
		
		if(null == listeners) {
			
			return;
		}
		
		for(WeakReference<ActionBusListener> weakReference : listeners) {
			
			ActionBusListener listener = weakReference.get();
			
			if(null != listener) {
				
				listener.doAction(action, data);
			}
		}
	}
	
	/*
	 * This will be called from the main activity in its onCreate() method
	 */
	public void initActionBusListener() {
		
		actionBusListeners = new HashMap<Integer, List<WeakReference<ActionBusListener>>>();
	}
	
	/*
	 * Helper method that removes items from a list that match the reference object's class type
	 */
	private static <T> void removeReference(List<WeakReference<T>> list, T reference) {
		
		for (Iterator<WeakReference<T>> iterator = list.iterator(); iterator.hasNext();) {
			
			WeakReference<T> weakReference = iterator.next();
			
			if (weakReference.get().getClass().equals(reference.getClass())) {

				iterator.remove();
			}
		}
	}
}
