package com.metoo.foundation.test;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.Test;

public class IteratorTest {
	
	 @Test
	 public void test1()  {
          ArrayList<Integer> arrayList = new ArrayList<>();
         for (int i = 0; i < 20; i++) {
              arrayList.add(Integer.valueOf(i));
          }
  
          // 复现方法一
          Iterator<Integer> iterator = arrayList.iterator();
          while (iterator.hasNext()) {
             Integer integer = iterator.next();
             if (integer.intValue() == 5) {
                 arrayList.remove(integer);
             }
         }
 
         // 复现方法二
         iterator = arrayList.iterator();
         for (Integer value : arrayList) {
             Integer integer = iterator.next();
             if (integer.intValue() == 5) {
                 arrayList.remove(integer);
             }
         }
     }
}
