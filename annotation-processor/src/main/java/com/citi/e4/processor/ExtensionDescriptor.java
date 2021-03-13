/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Cloudogu GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.citi.e4.processor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Describes an extension.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ExtensionDescriptor {

	private String className;
	private String methodName;
	private String annotationValue;
	private String annotationName;

	ExtensionDescriptor() {
	}

	ExtensionDescriptor(String className, String methodName, String annotationName, String annotationValue) {
		this.className = className;
		this.methodName = methodName;
		this.annotationValue = annotationValue;
		this.annotationName = annotationName;
	}

	/**
	 * Returns the name of the extension class.
	 *
	 * @return extension class name
	 */
	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getAnnotationValue() {
		return annotationValue;
	}

	@Override
	public String toString() {
		return className + ": " + methodName + ": " + annotationName + ": " + annotationValue;
	}

	public String getAnnotationName() {
		return annotationName;
	}

}
