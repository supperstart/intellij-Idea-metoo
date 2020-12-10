package com.metoo.app.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.metoo.core.tools.CommUtil;

public class CollectionTest {

	@Test
	public void list(){
		List<String> list = new ArrayList<String>();
		list.add("1");
		list.add("2");
		list.add("3");
		list.add("4");
		list.add("5");
		list.add("6");

		for(String id : list){
			System.out.println(CommUtil.subtract(id, 1));
			if(CommUtil.subtract(id, 1) > 0){
				list.remove(id);
			}
		}
		
		Iterator<String> it=list.iterator();
	    while(it.hasNext()) {
	        String m=it.next();
	        if(!m.equals("")) {
	            it.remove();
	            System.out.println();
	        }
	    }
	}
	
	@Test
	public void array(){
		List list = new ArrayList();
		list.add(12);
		list.add(13);
		list.add(14);
		list.add(15);
		list.add(16);
		List list1 = new ArrayList();
		list1.add(12);
		list1.add(13);
		list1.add(14);
		list1.add(15);
		list1.add(16);
		
		for(int j = 0; j < list.size(); j ++ ){
			list.remove(j);
			System.out.println(list);
			for(int i=0; i < list1.size(); i ++ ){
				list.add(list1.get(i));
				System.out.println(list);
				list1.remove(i);
				System.out.println(list1);
			}
		}
		
		
	}
}
