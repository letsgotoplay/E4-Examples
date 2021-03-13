package com.citi.e4.example;

import com.citi.e4.processor.Extension;
import com.citi.e4.processor.ExtensionMethod;

/**
 * Says hello to someone.
 */
@Extension
public class HelloService {

	/**
	 * Says hello to someone.
	 *
	 * @param name someones name
	 *
	 * @return greeting message
	 */
	@ExtensionMethod(1)
	public String sayHello(String name) {
		return "hello " + name;
	}
}
