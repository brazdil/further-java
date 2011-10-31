package uk.ac.cam.db538.fjava.tick1;

public class HelloWorld {

	public static void main(String[] args) {
		String name = (args.length == 1) ? args[0] : "world"; 
		System.out.println("Hello, " + name);
	}
}
