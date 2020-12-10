package com.metoo.app.test;

public class Stack {
	 public static void main(String[] args) { 
		 Person person = new Person("张三");
		 person.sayHello(); 
		 }
	 }

class Person {
	String name;
	public Person(String name) {
		this.name = name;
	} 
	
	public void sayHello() {
		
		System.out.println("hello " + name); 
		
		}
	}

