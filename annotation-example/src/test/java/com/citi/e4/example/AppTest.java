package com.citi.e4.example;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.citi.e4.processor.ExtensionDescriptor;
import com.citi.e4.processor.Extensions;

public class AppTest {

	@Test
	public void testExtensions() throws IOException {
		List<ExtensionDescriptor> descriptors = Extensions.getExtensions();
		assertEquals(4, descriptors.size());

	}

}
