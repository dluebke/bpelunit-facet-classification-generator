package net.bpelunit.suitegenerator.util;

import java.util.HashMap;

@SuppressWarnings("serial")
public class CounterMap<T> extends HashMap<T, Integer> {

	public int inc(T key) {
		Integer v = get(key);
		if(v == null) {
			v = 1;
		} else {
			v++;
		}
		put(key, v);
		return v;
	}
}
