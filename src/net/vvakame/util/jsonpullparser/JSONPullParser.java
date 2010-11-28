package net.vvakame.util.jsonpullparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;

/**
 * JSONPullParser��񋟂��܂�.<br>
 * [�\��] ���ݏ������̏ꏊ�܂ł�JSON�Ƃ��Đ������`�����`�F�b�N����܂�.<br>
 * �������A�Ō�܂ŏ��������Ƃ���JSON�Ƃ��Đ������`���ɂȂ��Ă��邩�͂킩��܂���.<br>
 * �r����JSON�Ƃ��Đ������Ȃ��`���������ꍇ�͗�O���������܂�.<br>
 * ���C�u�������p�҂́A���̏ꍇ���������v���O���������삷��悤�ɃR�[�f�B���O���Ȃ���΂Ȃ�Ȃ��ł�.
 * 
 * @author vvakame
 * 
 */
public class JSONPullParser {
	/**
	 * ���ݏ������̃g�[�N��.
	 * 
	 * @author vvakame
	 */
	static enum Current {
		/**
		 * �������.
		 */
		ORIGIN,
		/**
		 * �L�[.<br>
		 * key�̕����� �� {"key":"value"}
		 */
		KEY,
		/**
		 * ������̒l.<br>
		 * value�̒l �� {"key":"value"}
		 */
		VALUE_STRING,
		/**
		 * �����̒l.<br>
		 * value�̒l �� {"key":0123}
		 */
		VALUE_NUMERIC,
		/**
		 * �^�U�l�̒l.<br>
		 * value�̒l �� {"key":true}
		 */
		VALUE_BOOLEAN,
		/**
		 * null�̒l.<br>
		 * value�̒l �� {"key":null}
		 */
		VALUE_NULL,
		/**
		 * �n�b�V���̃X�^�[�g.<br>
		 * ���� �� {
		 */
		START_HASH,
		/**
		 * �n�b�V���̃G���h.<br>
		 * ���� �� }
		 */
		END_HASH,
		/**
		 * �z��̃X�^�[�g.<br>
		 * ���� �� [
		 */
		START_ARRAY,
		/**
		 * �z��̃G���h.<br>
		 * ���� �� ]
		 */
		END_ARRAY,
	}

	BufferedReader br;
	Stack<Current> stack;
	String valueStr;
	int valueInt;
	double valueDouble;
	boolean valueBoolean;

	public void setInput(InputStream is) throws IOException {
		br = new BufferedReader(new InputStreamReader(is));
		stack = new Stack<JSONPullParser.Current>();
		stack.push(Current.ORIGIN);
	}

	public Current getEventType() throws IOException {
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
				// TODO �����Ǝ�Exception�ɂ����ق�������
				throw new IllegalStateException();
			}
		case START_ARRAY:
			switch (c) {
			case '{':
				stack.push(Current.START_HASH);
				break;
			case '[':
				stack.push(Current.START_ARRAY);
				break;
			case '"':
				stack.push(Current.KEY);
				// TODO
				break;
			case ']':
				stack.push(Current.END_ARRAY);
				break;
			default:
				// TODO �����Ǝ�Exception�ɂ����ق�������
				throw new IllegalStateException();
			}
			break;

		case START_HASH:
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
					// TODO �����Ǝ�Exception�ɂ����ق�������
					throw new IllegalStateException();
				}
				break;
			default:
				// TODO �����Ǝ�Exception�ɂ����ق�������
				throw new IllegalStateException();
			}
			break;

		case END_ARRAY:
			switch (c) {
			case ',':
				// TODO
				break;
			case ']':
				stack.push(Current.END_ARRAY);
				break;
			case '}':
				stack.push(Current.END_HASH);
				break;
			default:
				// TODO �����Ǝ�Exception�ɂ����ق�������
				throw new IllegalStateException();
			}

		case END_HASH:
			switch (c) {
			case ',':
				// TODO
				break;
			case ']':
				stack.push(Current.END_ARRAY);
				break;
			case '}':
				stack.push(Current.END_HASH);
				break;
			default:
				// TODO �����Ǝ�Exception�ɂ����ق�������
				throw new IllegalStateException();
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
				// ����
				// true
				// false
				// null

				// TODO �����Ǝ�Exception�ɂ����ق�������
				throw new IllegalStateException();
			}
			break;
		case VALUE_STRING:
		case VALUE_NUMERIC:
		case VALUE_NULL:
		case VALUE_BOOLEAN:
			switch (c) {
			case ',':
				// TODO
				break;
			case '}':
				stack.push(Current.END_HASH);
				break;
			case ']':
				stack.push(Current.END_ARRAY);
				break;
			default:
				// TODO �����Ǝ�Exception�ɂ����ق�������
				throw new IllegalStateException();
			}
			break;
		default:
			// TODO �����Ǝ�Exception�ɂ����ق�������
			throw new IllegalStateException();
		}

		return stack.lastElement();
	}

	private void expectNextChar(char expect) throws IOException {
		char c = getNextChar();
		if (c != expect) {
			// TODO �����Ǝ�Exception�ɂ����ق�������
			throw new IllegalStateException();
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
		char c = (char) br.read();
		while (c == ' ' || c == '\r' || c == '\n') {
			c = (char) br.read();
		}
		return c;
	}

	StringBuilder stb = new StringBuilder();

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
