package com.citi.e4.example;

import java.io.IOException;

import com.citi.e4.processor.ExtensionDescriptor;
import com.citi.e4.processor.Extensions;

public class App {

	public static void main(String[] args) throws IOException {
		for (ExtensionDescriptor descriptor : Extensions.getExtensions()) {
			System.out.println(descriptor);
		}
	}

}
