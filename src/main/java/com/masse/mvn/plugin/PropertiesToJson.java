package com.masse.mvn.plugin;

import java.util.TreeMap;

public class PropertiesToJson {

	private String key;
	private String jsonKey;
	private String value;
	
	private TreeMap<String, PropertiesToJson> children = new TreeMap<String, PropertiesToJson>();



	public TreeMap<String, PropertiesToJson> getChildren() {
		return children;
	}

	public void setChildren(TreeMap<String, PropertiesToJson> children) {
		this.children = children;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getJsonKey() {
		return jsonKey;
	}

	public void setJsonKey(String jsonKey) {
		this.jsonKey = jsonKey;
	}

	
}
