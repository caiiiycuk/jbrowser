package org.mozilla.browser.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EventBuffer {

	static Log log = LogFactory.getLog(EventBuffer.class);
	
	//knockout event buffer [eventName=>time]
	Map<String,Long> eventBuffer = new HashMap<String, Long>(); 
	
	//public void record(String anEventName, Object[] anArgs) // agrs are most probably not needed
	public void record(String anEventName){
		log.debug("recording event:"+anEventName); //$NON-NLS-1$
		eventBuffer.put(anEventName, new Long(System.currentTimeMillis()) );
	}
	
	public void replayOn(Object aTarget){
		//order by time (hate Java)	
		List<Map.Entry<String, Long>> entryList = new ArrayList<Map.Entry<String, Long>>( eventBuffer.entrySet() );
		Collections.sort( entryList, 
				new Comparator<Map.Entry<String, Long>>(){
				public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {				
					return o1.getValue().compareTo(o2.getValue());
				}}
		);
		for (Entry<String, Long> entry : entryList) {
			String event = entry.getKey();
			try {
				log.debug("replaying event:"+event); //$NON-NLS-1$
				//execute the method
//				Method method = aTarget.getClass().getMethod( event );
				Method method = getPrivateMethod(event, aTarget );
				method.invoke(aTarget);
			}
			catch(Exception e){
				log.debug("Could not replay event "+event+" on "+aTarget); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
		}
		
		eventBuffer.clear();
	}
	
	public static Method getPrivateMethod( String aMethodName, Object anObject) {
	    final Method methods[] = anObject.getClass().getDeclaredMethods();
	    for (int i = 0; i < methods.length; ++i) {
	      if (aMethodName.equals(methods[i].getName())) {
	        	methods[i].setAccessible(true);
	        	return methods[i];
	      }
	    }
	    return null;
	  }	

	@SuppressWarnings(value="all") //$NON-NLS-1$
	public static void main(String[] args) {
		Object o = new Object() {
			public void onHop(){
				System.err.println("Hop"); //$NON-NLS-1$
			}
			public void onHey(){
				System.err.println("Hey"); //$NON-NLS-1$
			}
		};

		EventBuffer eb = new EventBuffer();
		eb.record("onHop"); //$NON-NLS-1$
		eb.record("onHey"); //$NON-NLS-1$
		eb.record("onHop"); //$NON-NLS-1$
		eb.replayOn(o);
		// should return Hey Hop
	}

}
