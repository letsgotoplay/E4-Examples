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

package com.citi.e4.gem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import com.google.gson.Gson;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({ "com.citi.e4.gem.GEMValidation" })
public class GEMValidationProcessor extends AbstractProcessor {

	private static final String URL_STRING = "http://localhost:8900/bds/project/{projectid}/lastest-report";
	private static final String GEMErrorMessage = "Please fix dependencies as they are identified";

	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		String projectId = "";
		BuildType buildType = BuildType.GRADLE;
		Map<String, String> reportMap;
		Map<String, String> dependencyMap;
		boolean hasGEMIssues = false;

		for (TypeElement annotation : annotations) {
			for (Element extension : roundEnv.getElementsAnnotatedWith(annotation)) {
				GEMValidation annotationElement = extension.getAnnotation(GEMValidation.class);
				buildType = annotationElement.buildType();
				projectId = annotationElement.projectId();
			}
		}
		if (roundEnv.processingOver()) {
			validateProjectID(projectId);
			reportMap = getGEMReport(roundEnv, projectId);
			dependencyMap = getDependencies(buildType);
			hasGEMIssues = findGEMIssue(reportMap, dependencyMap);
			if (hasGEMIssues) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, GEMErrorMessage);
			}

		}

		return true;
	}

	private boolean findGEMIssue(Map<String, String> reportMap, Map<String, String> dependencyMap) {
		// TODO Auto-generated method stub
		return false;
	}

	private Map<String, String> getDependencies(BuildType buildType) {
		if (buildType.toString().equals("GRADLE")) {
			findDependenciesFile();
			parseFileIntoMap();
		}
		// TODO Auto-generated method stub
		return null;
	}

	private void parseFileIntoMap() {
		// TODO Auto-generated method stub

	}

	private void findDependenciesFile() {
		// TODO Auto-generated method stub

	}

	private Map<String, String> getGEMReport(RoundEnvironment roundEnv, String projectId) {
		String requestURL = URL_STRING.replace("{projectid}", projectId);
		String data = getJSON(requestURL, 2000);
		Report msg = new Gson().fromJson(data, Report.class);
		return msg.getIssues().stream().collect(Collectors.toMap(Issue::getDependencyName, Issue::getVersion));
	}

	private String getJSON(String url, int timeout) {
		HttpURLConnection c = null;
		try {
			URL u = new URL(url);
			c = (HttpURLConnection) u.openConnection();
			c.setRequestMethod("GET");
			c.setRequestProperty("Content-length", "0");
			c.setUseCaches(false);
			c.setAllowUserInteraction(false);
			c.setConnectTimeout(timeout);
			c.setReadTimeout(timeout);
			c.connect();
			int status = c.getResponseCode();

			switch (status) {
			case 200:
			case 201:
				BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
				}
				br.close();
				return sb.toString();
			}

		} catch (MalformedURLException ex) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
		} finally {
			if (c != null) {
				try {
					c.disconnect();
				} catch (Exception ex) {
					Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
		return null;
	}

	private void validateProjectID(String projectId) {
		if (isBlank(projectId)) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
					"project id on annotation could not be empty");
		}
	}

	private boolean isBlank(String input) {
		return input != null && input.trim().isEmpty();
	}

}
