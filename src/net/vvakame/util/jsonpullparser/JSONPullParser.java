package net.vvakame.util.jsonpullparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * JSONPullParserを提供します.<br>
 * [予定] 現在処理中の場所までのJSONとして正しい形式かチェックされます.<br>
 * ただし、最後まで処理したときにJSONとして正しい形式になっているかはわかりません.<br>
 * 途中でJSONとして正しくない形式だった場合は例外が発生します.<br>
 * ライブラリ利用者は、その場合も正しくプログラムが動作するようにコーディングしなければならないです.
 * 
 * @author vvakame
 * 
 */
public class JSONPullParser {
	/**
	 * 現在処理中のトークン.
	 * 
	 * @author vvakame
	 */
	static enum Current {
		/**
		 * 初期状態.
		 */
		ORIGIN,
		/**
		 * キー.<br>
		 * keyの文字列 → {"key":"value"}
		 */
		KEY,
		/**
		 * 文字列の値.<br>
		 * valueの値 → {"key":"value"}
		 */
		VALUE_STRING,
		/**
		 * 数字の値.<br>
		 * valueの値 → {"key":0123}
		 */
		VALUE_INTEGER,
		/**
		 * 数字の値.<br>
		 * valueの値 → {"key":0123.11}
		 */
		VALUE_DOUBLE,
		/**
		 * 真偽値の値.<br>
		 * valueの値 → {"key":true}
		 */
		VALUE_BOOLEAN,
		/**
		 * nullの値.<br>
		 * valueの値 → {"key":null}
		 */
		VALUE_NULL,
		/**
		 * ハッシュのスタート.<br>
		 * これ → {
		 */
		START_HASH,
		/**
		 * ハッシュのエンド.<br>
		 * これ → }
		 */
		END_HASH,
		/**
		 * 配列のスタート.<br>
		 * これ → [
		 */
		START_ARRAY,
		/**
		 * 配列のエンド.<br>
		 * これ → ]
		 */
		END_ARRAY,
	}

	BufferedReader br;
	Deque<Current> stack;
	String valueStr;
	int valueInt;
	double valueDouble;
	boolean valueBoolean;

	public void setInput(InputStream is) throws IOException {
		br = new BufferedReader(new InputStreamReader(is));
		stack = new ArrayDeque<JSONPullParser.Current>();
		stack.push(Current.ORIGIN);
	}

	public Current getEventType() throws IOException, JSONFormatException {
		char c = getNextChar();
		switch (stack.pop()) {
		case ORIGIN:
			switch (c) {
			case '{':
				stack.push(Current.START_HASH);
				break;
			case '[':
				stack.push(Current.START_ARRAY);
				break;
			default:
				throw new JSONFormatException();
			}
			break;
		case START_ARRAY:
			stack.push(Current.START_ARRAY);
			switch (c) {
			case '{':
				stack.push(Current.START_HASH);
				break;
			case '[':
				stack.push(Current.START_ARRAY);
				break;
			case '"':
				stack.push(Current.VALUE_STRING);
				valueStr = getNextString();
				break;
			case ']':
				stack.push(Current.END_ARRAY);
				break;
			case 't':
				expectNextChar('r');
				expectNextChar('u');
				expectNextChar('e');

				stack.push(Current.VALUE_BOOLEAN);
				valueBoolean = true;
				break;
			case 'f':
				expectNextChar('a');
				expectNextChar('l');
				expectNextChar('s');
				expectNextChar('e');

				stack.push(Current.VALUE_BOOLEAN);
				valueBoolean = false;
				break;
			case 'n':
				expectNextChar('u');
				expectNextChar('l');
				expectNextChar('l');

				stack.push(Current.VALUE_NULL);
				break;
			default:
				// 数字
				String str = getNextNumeric();
				try {
					int i = Integer.parseInt(str);
					stack.push(Current.VALUE_INTEGER);
					valueInt = i;
					break;
				} catch (NumberFormatException e) {
				}
				try {
					double d = Double.parseDouble(str);
					stack.push(Current.VALUE_DOUBLE);
					valueDouble = d;
					break;
				} catch (NumberFormatException e) {
				}

				throw new JSONFormatException();
			}
			break;

		case START_HASH:
			stack.push(Current.START_HASH);
			switch (c) {
			case '{':
				stack.push(Current.START_HASH);
				break;
			case '[':
				stack.push(Current.START_ARRAY);
				break;
			case '}':
				stack.push(Current.END_HASH);
				break;
			case '"':
				stack.push(Current.KEY);
				valueStr = getNextString();
				c = getNextChar();
				if (c != ':') {
					throw new JSONFormatException();
				}
				break;
			default:
				throw new JSONFormatException();
			}
			break;

		case END_ARRAY:
			if (!Current.START_ARRAY.equals(stack.pop())) {
				throw new JSONFormatException();
			}
			switch (c) {
			case ',':
				getEventType();
				break;
			case ']':
				stack.push(Current.END_ARRAY);
				break;
			case '}':
				stack.push(Current.END_HASH);
				break;
			default:
				throw new JSONFormatException();
			}

		case END_HASH:
			if (!Current.START_HASH.equals(stack.pop())) {
				throw new JSONFormatException();
			}
			switch (c) {
			case ',':
				getEventType();
				break;
			case ']':
				stack.push(Current.END_ARRAY);
				break;
			case '}':
				stack.push(Current.END_HASH);
				break;
			default:
				throw new JSONFormatException();
			}
			break;
		case KEY:
			switch (c) {
			case '"':
				stack.push(Current.VALUE_STRING);
				valueStr = getNextString();
				break;
			case 't':
				expectNextChar('r');
				expectNextChar('u');
				expectNextChar('e');

				stack.push(Current.VALUE_BOOLEAN);
				valueBoolean = true;
				break;
			case 'f':
				expectNextChar('a');
				expectNextChar('l');
				expectNextChar('s');
				expectNextChar('e');

				stack.push(Current.VALUE_BOOLEAN);
				valueBoolean = false;
				break;
			case 'n':
				expectNextChar('u');
				expectNextChar('l');
				expectNextChar('l');

				stack.push(Current.VALUE_NULL);
				break;
			default:
				// 数字
				String str = getNextNumeric();
				try {
					int i = Integer.parseInt(str);
					stack.push(Current.VALUE_INTEGER);
					valueInt = i;
					break;
				} catch (NumberFormatException e) {
				}
				try {
					double d = Double.parseDouble(str);
					stack.push(Current.VALUE_DOUBLE);
					valueDouble = d;
					break;
				} catch (NumberFormatException e) {
				}

				throw new JSONFormatException();
			}
			break;
		case VALUE_STRING:
		case VALUE_INTEGER:
		case VALUE_DOUBLE:
		case VALUE_NULL:
		case VALUE_BOOLEAN:
			switch (c) {
			case ',':
				getEventType();
				break;
			case '}':
				stack.push(Current.END_HASH);
				break;
			case ']':
				stack.push(Current.END_ARRAY);
				break;
			default:
				throw new JSONFormatException();
			}
			break;
		default:
			throw new JSONFormatException();
		}

		return stack.getFirst();
	}

	private void expectNextChar(char expect) throws IOException,
			JSONFormatException {
		char c = getNextChar();
		if (c != expect) {
			throw new JSONFormatException();
		}
	}

	public Object getValue() {
		return null;
	}

	public String getValueString() {
		return valueStr;
	}

	public int getValueInt() {
		return valueInt;
	}

	public double getValueDouble() {
		return valueDouble;
	}

	public boolean getValueBoolean() {
		return valueBoolean;
	}

	private char getNextChar() throws IOException {
		br.mark(1);
		char c = (char) br.read();
		while (c == ' ' || c == '\r' || c == '\n') {
			br.mark(1);
			c = (char) br.read();
		}
		return c;
	}

	StringBuilder stb = new StringBuilder();

	private String getNextNumeric() throws IOException {
		stb.setLength(0);
		br.reset();
		char c;
		loop: while (true) {
			c = (char) br.read();
			switch (c) {
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
			case '-':
			case '.':
			case 'e':
			case 'E':
				break;
			default:
				br.reset();
				break loop;
			}
			br.mark(1);
			stb.append(c);
		}
		return stb.toString();
	}

	private String getNextString() throws IOException {
		stb.setLength(0);
		char c;
		while (true) {
			c = (char) br.read();
			if (c == '"') {
				break;
			}
			stb.append(c);
		}
		return stb.toString();
	}
}
