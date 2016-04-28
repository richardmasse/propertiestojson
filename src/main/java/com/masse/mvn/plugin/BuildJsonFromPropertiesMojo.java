package com.masse.mvn.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.SystemUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name="propertiestojson", defaultPhase=LifecyclePhase.PROCESS_RESOURCES, threadSafe=true)
public class BuildJsonFromPropertiesMojo
    extends AbstractMojo
{

	@Parameter(property="propertiesSourcePath", defaultValue="${basedir}/src/properties")
    private String propertiesSourcePath;

	@Parameter(property="jsonTargetPath", defaultValue="${project.build.directory}/properties")
    private String jsonTargetPath;

	@Parameter(property="fileWilcard", defaultValue="*.properties")
    private String fileWilcard;
	
    public void execute()
        throws MojoExecutionException
    {
    	getLog().info("--- Maven plugin : Properties to JSON ---");
    	
    	File srcPath = new File(propertiesSourcePath);
    	if (!srcPath.isDirectory()) {
    		throw new MojoExecutionException( "PropertiesSourcePath " + propertiesSourcePath + " is not a directory !");
    	}

    	File dstPath = new File(jsonTargetPath);
    	if (!dstPath.isDirectory()) {
    		throw new MojoExecutionException( "JsonTargetPath " + jsonTargetPath + " is not a directory !");
    	}

    	FileFilter fileFilter = new WildcardFileFilter(fileWilcard);
    	File[] listOfFiles = srcPath.listFiles(fileFilter);
        
    	for (File f : listOfFiles) {
    		buidJsonTargetFile(f);
    	}
    }
    
    
    
    private void buidJsonTargetFile(File inputFile) throws MojoExecutionException {

		String inputFileString = inputFile.getAbsolutePath().substring(inputFile.getAbsolutePath().lastIndexOf(SystemUtils.IS_OS_LINUX ? "/" : "\\") + 1);
		String outputFileString = jsonTargetPath + new String(SystemUtils.IS_OS_LINUX ? "/" : "\\") + inputFileString.substring(0, inputFileString.lastIndexOf("."));
       
    	getLog().info("Process file " + inputFileString);
    	
    	if ( !inputFile.exists() )
        {
        	throw new MojoExecutionException( "Properties file " + inputFile + " not found !");
        }
        
        TreeMap<String, PropertiesToJson> propertiesJson = new TreeMap<String, PropertiesToJson>();
		Properties props = new Properties();
		
		try {
			FileInputStream fis = new FileInputStream(inputFile);
			props.load(fis);
			fis.close();

			@SuppressWarnings("rawtypes")
			Enumeration e = props.propertyNames();

			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				
				String rootKey = key.split("\\.")[0];
				
				propertiesJson.put(rootKey, createMap(propertiesJson, key, props.getProperty(key), 1));
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		StringBuffer sb = new StringBuffer();
		sb.append(PrintJsonTree(propertiesJson, 0, false));
		
		File outputFile = new File(outputFileString);
		
		try {
			BufferedWriter bwr = new BufferedWriter(new OutputStreamWriter( new FileOutputStream(outputFile), "UTF-8"));
        
			//write contents of StringBuffer to a file
        
			bwr.write(sb.toString());
			
			//flush the stream
	        bwr.flush();
	       
	        //close the stream
	        bwr.close();
		} catch (IOException e) {
			getLog().error(e);
			throw new MojoExecutionException("json file creation error", e);
		}
    }
    
	private PropertiesToJson createMap(TreeMap<String, PropertiesToJson> json,
			String key, String value, int level) {
		String[] ks = key.split("\\.");
		String newKey = "";
		String jsonKey = "";
		PropertiesToJson ptj = null;

		for (int i = 0; i < level; i++) {
			newKey += ks[i] + ".";
			jsonKey = ks[i];
		}
		newKey = newKey.substring(0, newKey.length() - 1);
		
		if (json != null)
			ptj = json.get(newKey);
		
		if (ptj == null) {
			ptj = new PropertiesToJson();
			ptj.setKey(newKey);
			ptj.setJsonKey(jsonKey);
		}
			
		if (level == ks.length) {
			ptj.setValue(value);
		} else {
			String nextKey = getNextKey(newKey, level + 1);
			PropertiesToJson pj = null;
			if (json != null)
				pj = json.get(nextKey);
			
			PropertiesToJson p = null;
			
			if (pj != null)
				p = createMap(pj.getChildren(), key, value, level + 1);
			else
				p = createMap(null, key, value, level + 1);
			
			ptj.getChildren().put(p.getKey(), p);
		}
		
		return ptj;
	}

	private String escapingQuote(String value) {
	
		return value.replace("\"", "\\\"");
	}

	private StringBuffer PrintJsonTree(TreeMap<String, PropertiesToJson> tm, int indent, boolean hasNext) {
		StringBuffer sb = new StringBuffer();
		sb.append("{\n");
		Iterator<String> itr = tm.keySet().iterator();
		
		while (itr.hasNext()) {
			PropertiesToJson ptj = tm.get(itr.next());
		
			if (ptj.getValue() != null) {
				sb.append(getIndent(indent + 1) + "\"" + ptj.getJsonKey() + "\"" + ": " + "\"" + escapingQuote(ptj.getValue()) + "\"");
				if (itr.hasNext())
					sb.append(",\n");
				else
					sb.append("\n");
			} else {
				sb.append(getIndent(indent + 1) + "\"" + ptj.getJsonKey() + "\"" + ": ");
				sb.append(PrintJsonTree(ptj.getChildren(), indent + 1, itr.hasNext()));
			}	
		}
		if (hasNext) {
			sb.append(getIndent(indent) + "},\n");
		} else { 
			sb.append(getIndent(indent) + "}\n");
		} 
		
		return sb;
	}
	
	private String getIndent(int indent) {
		String ret = "";
		
		for (int i=0; i< indent; i++) {
			ret += "\t";
		}
		
		return ret;
	}
	
	private String getNextKey(String key, int level) {
		String[] ks = key.split("\\.");
		String newKey = "";
		
		for (int i = 0; i < (level > ks.length ? ks.length  : level) ; i++) {
			newKey += ks[i] + ".";
		}
		newKey = newKey.substring(0, newKey.length() - 1);
		
		return newKey;
	}

}
