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

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.xml.bind.JAXB;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({ "com.citi.e4.processor.Extension", "com.citi.e4.processor.ExtensionMethod" })
public class ExtensionProcessor extends AbstractProcessor {

	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Set<ExtensionDescriptor> descriptors = collectionExtensions(annotations, roundEnv);
		writeExtensions(descriptors);
		// stop after first round
		return true;
	}

	private Set<ExtensionDescriptor> collectionExtensions(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		Set<ExtensionDescriptor> descriptors = new LinkedHashSet<>();

		for (TypeElement annotation : annotations) {
			for (Element extension : roundEnv.getElementsAnnotatedWith(annotation)) {
				annotation.getQualifiedName();
				ExtensionDescriptor descriptor = null;
				if (extension.getKind().equals(ElementKind.CLASS)) {
					descriptor = createClassDescriptor(extension, annotation.getQualifiedName().toString());
				}
				if (extension.getKind().equals(ElementKind.METHOD)) {
					descriptor = createMethodDescriptor(extension, annotation);
				}
				descriptors.add(descriptor);
			}
		}
		return descriptors;
	}

	private ExtensionDescriptor createClassDescriptor(Element extension, String annotationName) {
		String name = resolveName(extension);
		return new ExtensionDescriptor(name, "NOT METHOD TYPE", annotationName, "No Values on Class Annotation");
	}

	private ExtensionDescriptor createMethodDescriptor(Element extension, TypeElement annotation) {
		String classname = ((TypeElement) extension.getEnclosingElement()).getQualifiedName().toString();
		String methodName = extractMethodName(extension);
		String annotationName = annotation.getQualifiedName().toString();
		return new ExtensionDescriptor(classname, methodName, annotationName,
				String.valueOf(extension.getAnnotation(ExtensionMethod.class).value()));
	}

	private String resolveName(Element extension) {
		return extension.asType().toString();
	}

	private String extractMethodName(Element extension) {
		return extension.getSimpleName().toString();
	}

	private void writeExtensions(Set<ExtensionDescriptor> descriptors) {
		try {
			if (!descriptors.isEmpty()) {
				writeExtensionsXml(descriptors);
			}
		} catch (IOException e) {
			error(e);
		}
	}

	private void error(IOException e) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
				"failed to write extension file: " + e.getMessage());
	}

	private void writeExtensionsXml(Set<ExtensionDescriptor> descriptors) throws IOException {
		File file = getExtensionsFile();
		ensureParentExists(file);
		JAXB.marshal(new ExtensionDescriptorWrapper(descriptors), file);
	}

	private File getExtensionsFile() throws IOException {
		Filer filer = processingEnv.getFiler();
		FileObject f = filer.getResource(StandardLocation.CLASS_OUTPUT, "", "extensions.xml");
		return new File(f.toUri());
	}

	private void ensureParentExists(File file) throws IOException {
		File parent = file.getParentFile();
		if (!parent.exists() && !parent.mkdirs()) {
			throw new IOException("failed to create target directory");
		}
	}

}
