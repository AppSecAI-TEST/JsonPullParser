package net.vvakame.util.jsonpullparser.factory;

public class JsonElement {
	boolean first = false;
	String key;
	String setter;
	Kind kind;
	String modelName;

	public static enum Kind {
		STRING, BOOLEAN, DOUBLE, LONG, MODEL, BYTE, CHAR, FLOAT, INT, SHORT, LIST, JSON_HASH, JSON_ARRAY
	}

	/**
	 * @return the first
	 */
	public boolean isFirst() {
		return first;
	}

	/**
	 * @param first
	 *            the first to set
	 */
	public void setFirst(boolean first) {
		this.first = first;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the setter
	 */
	public String getSetter() {
		return setter;
	}

	/**
	 * @param setter
	 *            the setter to set
	 */
	public void setSetter(String setter) {
		this.setter = setter;
	}

	/**
	 * @return the kind
	 */
	public Kind getKind() {
		return kind;
	}

	/**
	 * @param kind
	 *            the kind to set
	 */
	public void setKind(Kind kind) {
		this.kind = kind;
	}

	/**
	 * @return the modelName
	 */
	public String getModelName() {
		return modelName;
	}

	/**
	 * @param modelName
	 *            the modelName to set
	 */
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
}