/**
 * 
 */
package com.trendrr.oss.tests;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import com.trendrr.oss.DynMap;
import com.trendrr.oss.Timeframe;
import com.trendrr.oss.cache.TrendrrCache;
import com.trendrr.oss.concurrent.Sleep;


/**
 * 
 * Test cases for implementations of TrendrrCache
 * 
 * 
 * @author Dustin Norlander
 * @created Apr 27, 2012
 * 
 */
public class TrendrrCacheTests {

	protected static Log log = LogFactory.getLog(TrendrrCacheTests.class);
	
	protected int timeoutSeconds = 5;
	public boolean testDelete(TrendrrCache cache) {
		Date expire = Timeframe.SECONDS.add(new Date(), this.timeoutSeconds);
		cache.set("tests", "testDeleteKey", "testing", expire);
		cache.delete("tests", "testDeleteKey");
		return (cache.get("tests", "testDeleteKey") == null);
	}
	
	public boolean testGetSet(TrendrrCache cache) {
		Date expire = Timeframe.SECONDS.add(new Date(), this.timeoutSeconds);
		try {
			cache.set("tests", "testGetSet", "String Object", expire);
			if (!cache.get(String.class, "tests", "testGetSet").equals("String Object")) {
				return false;
			}
			return true;
		} finally {
			cache.delete("tests", "testGetSet");
		}
	}
	
	public boolean testSetIfAbsent(TrendrrCache cache) {
		Date expire = Timeframe.SECONDS.add(new Date(), this.timeoutSeconds);
		try {
			cache.set("tests", "testSetIfAbsent", "String Object", expire);
			cache.setIfAbsent("tests", "testSetIfAbsent", "String Object 2", expire);
			if (!cache.get(String.class, "tests", "testSetIfAbsent").equals("String Object")) {
				return false;
			}
			cache.setIfAbsent("tests", "testSetIfAbsent2", "String Object 3", expire);
			if (!cache.get(String.class, "tests", "testSetIfAbsent2").equals("String Object 3")) {
				return false;
			}
			
			return true;
		} finally {
			cache.delete("tests", "testIncKey");
		}
	}
	
	public void testInc(TrendrrCache cache) {
		Date expire = Timeframe.SECONDS.add(new Date(), this.timeoutSeconds);
		
		try {
			System.out.println(cache.inc("tests", "testIncKey", 1, expire));
			Assert.assertTrue(cache.get(Long.class, "tests", "testIncKey") == 1l);
			
			for (int i=0; i < 15; i++) {
				cache.inc("tests", "testIncKey", 1, expire);
			}
			Assert.assertTrue (cache.get(Long.class, "tests", "testIncKey") == 16);
			
			//test decrement
			for (int i=0; i < 16; i++) {
				cache.inc("tests", "testIncKey", -1, expire);
			}
			Assert.assertTrue (cache.get(Long.class, "tests", "testIncKey") == 0);
		} finally {
			cache.delete("tests", "testIncKey");
		}
	}
	
	public void testAddToSet(TrendrrCache cache) {
		Date expire = Timeframe.SECONDS.add(new Date(), this.timeoutSeconds);
		
		try {
			List<String> values = new ArrayList<String>();
			values.add("str1");
			values.add("str2");
			values.add("str3");
			values.add("str4");
			
			cache.addToSet("tests", "testSetKey", values, expire);
			
			
			values.add("str5");
			cache.addToSet("tests", "testSetKey", values, expire);
			
			Set<String> results = cache.getSet("tests", "testSetKey");
			Assert.assertTrue(results.containsAll(values));
			Assert.assertEquals(results.size(), 5);
			
		} finally {
			cache.delete("tests", "testSetKey");
		}
	}
	
	public void testGetMulti(TrendrrCache cache) {
		Date expire = Timeframe.SECONDS.add(new Date(), this.timeoutSeconds);
		
		try {
			
			List<String> keys = new ArrayList<String>();
			for (int i=0; i < 15; i++) {
				keys.add("testGetMulti" + i);
				cache.set("tests", "testGetMulti" + i, "String Object " + i, expire);
			}

			DynMap results = cache.getMulti("tests", keys);
			Assert.assertTrue(results.keySet().containsAll(keys));
			Assert.assertEquals(results.get("testGetMulti1"), "String Object 1");
		} finally {
			for (int i=0; i < 15; i++) {
				cache.delete("tests", "testGetMulti" + i);
			}
		}
	}
	
	public void testIncMulti(TrendrrCache cache) {
		Date expire = Timeframe.SECONDS.add(new Date(), this.timeoutSeconds);
		
		try {
			
			HashMap<String, Integer> increments = new HashMap<String, Integer>();
			increments.put("val", 5);
			increments.put("key1", 1);
			increments.put("key2", 2);
			increments.put("key3", 3);
			cache.incMulti("tests", "testIncMulti", increments, expire);
			
			Map<String, Long> results = cache.getIncMulti("tests", "testIncMulti");
			Assert.assertTrue(results.keySet().containsAll(increments.keySet()));
			Assert.assertTrue(results.get("val") == 5);
			Assert.assertTrue(results.get("key1") == 1);	

			increments.put("val", 1);
			increments.put("key4", 4);
			cache.incMulti("tests", "testIncMulti", increments, expire);
			results = cache.getIncMulti("tests", "testIncMulti");
			Assert.assertTrue(results.keySet().containsAll(increments.keySet()));
			Assert.assertTrue(results.get("val") == 6);
			Assert.assertTrue(results.get("key1") == 2);
			Assert.assertTrue(results.get("key4") == 4);

		} finally {
			for (int i=0; i < 15; i++) {
				cache.delete("tests", "testGetMulti" + i);
			}
		}
		
	}
}
