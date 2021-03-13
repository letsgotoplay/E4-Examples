package com.citi.e4.example;

import com.citi.e4.processor.Extension;
import com.citi.e4.processor.ExtensionMethod;

/**
 * Says ahoi to someone.
 */
@Extension
public class AhoiService {

	/**
	 * Says ahoi to someone.
	 * 
	 * @param name name of someone
	 * @return greeting message
	 */
	@ExtensionMethod
	public String sayAhoi(String name) {
		return "ahoi " + name;
	}
}
