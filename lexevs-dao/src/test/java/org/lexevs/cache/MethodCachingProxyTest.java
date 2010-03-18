/*
 * Copyright: (c) 2004-2009 Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 * 
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 * 		http://www.eclipse.org/legal/epl-v10.html
 * 
 */
package org.lexevs.cache;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.lexevs.cache.annotation.Cacheable;
import org.lexevs.dao.test.LexEvsDbUnitTestBase;
import org.lexevs.dao.test.TestCacheBean;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * The Class MethodCachingProxyTest.
 * 
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class MethodCachingProxyTest extends LexEvsDbUnitTestBase {

	/** The test cache proxy. */
	@Resource 
	private MethodCachingProxy testCacheProxy;
	
	/** The test cache bean. */
	@Resource 
	private TestCacheBean testCacheBean;
	
	/**
	 * Test cache setup.
	 */
	@Test
	public void testCacheSetup(){
		assertNotNull(this.testCacheProxy);
		assertNotNull(this.testCacheBean);
	}
	
	/**
	 * Clear cache.
	 */
	@Before
	public void clearCache(){
		testCacheProxy.getCaches().clear();
	}
	
	/**
	 * Test put in cache.
	 */
	@Test
	public void testPutInCache(){
	
		assertEquals("onetwo", testCacheBean.getValue("one", "two"));
		assertEquals(1, testCacheProxy.getCaches().get("testCache").size());
		assertEquals("onetwo", testCacheProxy.getCaches().get("testCache").values().toArray()[0]);
	}
	
	/**
	 * Test put twice in cache.
	 */
	@Test
	public void testPutTwiceInCache(){
		
		assertEquals("onetwo", testCacheBean.getValue("one", "two"));
		assertEquals("onetwo", testCacheBean.getValue("one", "two"));
		assertEquals(1, testCacheProxy.getCaches().get("testCache").size());
		assertEquals("onetwo", testCacheProxy.getCaches().get("testCache").values().toArray()[0]);
	}
	
	/**
	 * Test two different in cache.
	 */
	@Test
	public void testTwoDifferentInCache(){
		
		assertEquals("onetwo", testCacheBean.getValue("one", "two"));
		assertEquals("threefour", testCacheBean.getValue("three", "four"));
		assertEquals(2, testCacheProxy.getCaches().get("testCache").size());
		assertEquals("onetwo", testCacheProxy.getCaches().get("testCache").values().toArray()[0]);
		assertEquals("threefour", testCacheProxy.getCaches().get("testCache").values().toArray()[1]);
	}
	
	/**
	 * Test un cachable method.
	 */
	@Test
	public void testUnCachableMethod(){
		
		assertEquals("onetwo", testCacheBean.getValueNotCachable("one", "two"));
		
		assertNull(testCacheProxy.getCaches().get("testCache"));
	}
	
	/**
	 * Test clear cache.
	 */
	@Test
	public void testClearCache(){
		
		assertEquals("onetwo", testCacheBean.getValue("one", "two"));
		assertEquals("threefour", testCacheBean.getValue("three", "four"));
		assertEquals(2, testCacheProxy.getCaches().get("testCache").size());
		assertEquals("onetwo", testCacheProxy.getCaches().get("testCache").values().toArray()[0]);
		assertEquals("threefour", testCacheProxy.getCaches().get("testCache").values().toArray()[1]);
		
		testCacheBean.testClear();
		assertEquals(0, testCacheProxy.getCaches().get("testCache").size());
		
	}
	
	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 */
	public static void main(String[] args){
		TestCacheBean bean = new TestCacheBean();
		Class clazz = bean.getClass();
		Cacheable annotation = AnnotationUtils.findAnnotation(bean.getClass(), Cacheable.class);
		System.out.println(annotation);
	}
	

}
