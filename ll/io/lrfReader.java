package ll.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.text.ParseException;
import java.util.ArrayList;

import ll.core.Attribute;
import ll.core.Instances;
import ll.core.Instance;
import ll.core.SparseInstance;

public class lrfReader {

	private StreamTokenizer m_streamTok = null;
	private Instances m_data = null;
	private long m_line = 0;
    protected double[] m_valueBuffer = null;
    protected int[] m_indicesBuffer = null;
    
	public lrfReader(String lrfPath) throws IOException {
		m_streamTok = new StreamTokenizer(new BufferedReader(new FileReader(lrfPath)));
		initTokenizer();
		
		readHeader(1000);
		m_valueBuffer = new double[m_data.numAttributes()];
		m_indicesBuffer = new int [m_data.numAttributes()];
		
		Instance inst;
		while ( (inst=readInstance()) != null ) {
			m_data.add(inst);
		}
		if( m_data!=null )
			m_data.compactify();
	}
	
	public Instances getData(){
		return m_data;
	}
	
 	private void initTokenizer() {
		m_streamTok.resetSyntax();
		m_streamTok.whitespaceChars(0, ' ');
		m_streamTok.wordChars(' ' + 1, '\u00FF');
		m_streamTok.whitespaceChars(',', ',');
		m_streamTok.commentChar('%');
		m_streamTok.quoteChar('"');
		m_streamTok.quoteChar('\'');
		m_streamTok.ordinaryChar('{');
		m_streamTok.ordinaryChar('}');
		m_streamTok.eolIsSignificant(true);
	}

	private Instance readInstance() throws IOException {

		// Check if any attributes have been declared.
		if (m_data.numAttributes() == 0) {
			errorMessage("no header information available");
		}

		// Check if end of file reached.
		getFirstToken();
		if (m_streamTok.ttype == StreamTokenizer.TT_EOF) {
			return null;
		}

		// Parse instance
		if (m_streamTok.ttype == '{') {
			return getInstanceSparse();
		} else {
			return getInstanceFull();
		}
	}

	protected Instance getInstanceFull() throws IOException {
		double[] instance = new double[m_data.numAttributes()];
		int index;

		// Get values for all attributes.
		for (int i = 0; i < m_data.numAttributes(); i++) {
			// Get next token
			if (i > 0) {
				getNextToken();
			}

			// Check if value is missing.
			if (m_streamTok.ttype == '?' || m_streamTok.sval.equals("?") ) {
				instance[i] = Instance.missingValue();
			} else {

				// Check if token is valid.
				if (m_streamTok.ttype != StreamTokenizer.TT_WORD) {
					errorMessage("not a valid value");
				}
				switch (m_data.attribute(i).getType()) {
				case Attribute.NOMINAL:
					// Check if value appears in header.
					index = m_data.attribute(i).indexOfValue(m_streamTok.sval);
					if (index == -1) {
						errorMessage("nominal value not declared in header");
					}
					instance[i] = (double) index;
					break;
				case Attribute.NUMERIC:
					// Check if value is really a number.
					try {
						instance[i] = Double.valueOf(m_streamTok.sval).doubleValue();
					} catch (NumberFormatException e) {
						errorMessage("number expected");
					}
					break;
				case Attribute.STRING:
					instance[i] = m_data.attribute(i).addStringValue( m_streamTok.sval );
					break;
				case Attribute.DATE:
					try {
						instance[i] = m_data.attribute(i).parseDate(m_streamTok.sval);
					} catch (ParseException e) {
						errorMessage("unparseable date: " + m_streamTok.sval);
					}
					break;
				default:
					errorMessage("unknown attribute type in column " + i);
				}
			}
		}

		double weight = 1.0;
//		if (flag) {
//			// check for an instance weight
//			weight = getInstanceWeight();
//			if (!Double.isNaN(weight)) {
//				getLastToken(true);
//			} else {
//				weight = 1.0;
//			}
//		}

		// Add instance to dataset
		Instance inst = new Instance(weight, instance);
		//inst.setDataset(m_data);

		return inst;
	}
	
    protected Instance getInstanceSparse( ) throws IOException {
		int valIndex, numValues = 0, maxIndex = -1;

		// Get values
		do {
			// Get index
			getIndex();
			if (m_streamTok.ttype == '}') {
				break;
			}

			// Is index valid?
			try {
				m_indicesBuffer[numValues] = Integer.valueOf(m_streamTok.sval)
						.intValue();
			} catch (NumberFormatException e) {
				errorMessage("index number expected");
			}
			if (m_indicesBuffer[numValues] <= maxIndex) {
				errorMessage("indices have to be ordered");
			}
			if ((m_indicesBuffer[numValues] < 0)
					|| (m_indicesBuffer[numValues] >= m_data.numAttributes())) {
				errorMessage("index out of bounds");
			}
			maxIndex = m_indicesBuffer[numValues];

			// Get value;
			getNextToken();

			// Check if value is missing.
			if (m_streamTok.ttype == '?') {
				m_valueBuffer[numValues] = Instance.missingValue();
			} else {

				// Check if token is valid.
				if (m_streamTok.ttype != StreamTokenizer.TT_WORD) {
					errorMessage("not a valid value");
				}
				switch (m_data.attribute(m_indicesBuffer[numValues]).getType()) {
				case Attribute.NOMINAL:
					// Check if value appears in header.
					valIndex = m_data.attribute(m_indicesBuffer[numValues])
							.indexOfValue(m_streamTok.sval);
					if (valIndex == -1) {
						errorMessage("nominal value not declared in header");
					}
					m_valueBuffer[numValues] = (double) valIndex;
					break;
				case Attribute.NUMERIC:
					// Check if value is really a number.
					try {
						m_valueBuffer[numValues] = Double.valueOf(
								m_streamTok.sval).doubleValue();
					} catch (NumberFormatException e) {
						errorMessage("number expected");
					}
					break;
				case Attribute.STRING:
					m_valueBuffer[numValues] = m_data.attribute(
							m_indicesBuffer[numValues]).addStringValue(
							m_streamTok.sval);
					break;
				case Attribute.DATE:
					try {
						m_valueBuffer[numValues] = m_data.attribute(
								m_indicesBuffer[numValues]).parseDate(
								m_streamTok.sval);
					} catch (ParseException e) {
						errorMessage("unparseable date: " + m_streamTok.sval);
					}
					break;
				default:
					errorMessage("unknown attribute type in column "
							+ m_indicesBuffer[numValues]);
				}
			}
			numValues++;
		} while (true);

		double weight = 1.0;
//		if (flag) {
//			// check for an instance weight
//			weight = getInstanceWeight();
//			if (!Double.isNaN(weight)) {
//				getLastToken(true);
//			} else {
//				weight = 1.0;
//			}
//		}

		// Add instance to dataset
		double[] tempValues = new double[numValues];
		int[] tempIndices = new int[numValues];
		System.arraycopy(m_valueBuffer, 0, tempValues, 0, numValues);
		System.arraycopy(m_indicesBuffer, 0, tempIndices, 0, numValues);
		Instance inst = new SparseInstance(weight, tempValues, tempIndices, m_data.numAttributes());
		//inst.setDataset(m_data);

		return inst;
	}
	
	private void getFirstToken() throws IOException {
		while (m_streamTok.nextToken() == StreamTokenizer.TT_EOL) {}
		
		if ((m_streamTok.ttype == '\'') || (m_streamTok.ttype == '"')) {
			m_streamTok.ttype = StreamTokenizer.TT_WORD;
		} 
		else if ((m_streamTok.ttype == StreamTokenizer.TT_WORD)
				&& (m_streamTok.sval.equals("?"))) {
			m_streamTok.ttype = '?';
		}
	}

	protected void getNextToken() throws IOException {
		if (m_streamTok.nextToken() == StreamTokenizer.TT_EOL) {
			errorMessage("premature end of line");
		}
		if (m_streamTok.ttype == StreamTokenizer.TT_EOF) {
			errorMessage("premature end of file");
		} else if ((m_streamTok.ttype == '\'') || (m_streamTok.ttype == '"')) {
			m_streamTok.ttype = StreamTokenizer.TT_WORD;
		} else if ((m_streamTok.ttype == StreamTokenizer.TT_WORD)
				&& (m_streamTok.sval.equals("?"))) {
			m_streamTok.ttype = '?';
		}
	}

	protected void getLastToken(boolean endOfFileOk) throws IOException {
		if ((m_streamTok.nextToken() != StreamTokenizer.TT_EOL)
				&& ((m_streamTok.ttype != StreamTokenizer.TT_EOF) || !endOfFileOk)) {
			errorMessage("end of line expected");
		}
	}

	private void getIndex() throws IOException {
		if (m_streamTok.nextToken() == StreamTokenizer.TT_EOL) {
			errorMessage("premature end of line");
		}
		if (m_streamTok.ttype == StreamTokenizer.TT_EOF) {
			errorMessage("premature end of file");
		}
	}
	
	protected void readHeader(int capacity) throws IOException {
		m_line = 0;
		String relationName = "";

		// Get name of relation.
		getFirstToken();
		if (m_streamTok.ttype == StreamTokenizer.TT_EOF) {
			errorMessage("premature end of file");
		}
		if (Instances.ARFF_RELATION.equalsIgnoreCase(m_streamTok.sval)) {
			getNextToken();
			relationName = m_streamTok.sval;
			getLastToken(false);
		} else {
			errorMessage("keyword " + Instances.ARFF_RELATION + " expected");
		}

		// Create vectors to hold information temporarily.
		ArrayList<Object> attributes = new ArrayList<Object>();

		// Get attribute declarations.
		getFirstToken();
		if (m_streamTok.ttype == StreamTokenizer.TT_EOF) {
			errorMessage("premature end of file");
		}

		while (Attribute.ARFF_ATTRIBUTE.equalsIgnoreCase(m_streamTok.sval)) {
			parseAttribute(attributes);
		}

		// Check if data part follows. We can't easily check for EOL.
		if (!Instances.ARFF_DATA.equalsIgnoreCase(m_streamTok.sval)) {
			errorMessage("keyword " + Instances.ARFF_DATA + " expected");
		}

		// Check if any attributes have been declared.
		if (attributes.size() == 0) {
			errorMessage("no attributes declared");
		}

		m_data = new Instances(relationName, attributes, capacity);
	}

	private void errorMessage(String msg) throws IOException {
		String str = msg + ", read " + m_streamTok.toString();
		if (m_line > 0) {
			int line = Integer.parseInt(str.replaceAll(".* line ", ""));
			str = str.replaceAll(" line .*", " line " + (m_line + line - 1));
		}
		throw new IOException(str);
	}

	private ArrayList<Object> parseAttribute(ArrayList<Object> attributes)	throws IOException {
		String attributeName;
		ArrayList<Object> attributeValues;

		getNextToken();
		attributeName = m_streamTok.sval;
		getNextToken();

		// Check if attribute is nominal.
		if (m_streamTok.ttype == StreamTokenizer.TT_WORD) {

			// Attribute is real, integer, or string.
			if (m_streamTok.sval.equalsIgnoreCase(Attribute.ARFF_ATTRIBUTE_REAL)
					|| m_streamTok.sval.equalsIgnoreCase(Attribute.ARFF_ATTRIBUTE_INTEGER)
					|| m_streamTok.sval.equalsIgnoreCase(Attribute.ARFF_ATTRIBUTE_NUMERIC)) {
				//attributes.addElement(new Attribute(attributeName, attributes.size()));
				attributes.add( new Attribute(attributeName, attributes.size(), Attribute.NUMERIC) );
				readTillEOL();
			} else if (m_streamTok.sval.equalsIgnoreCase(Attribute.ARFF_ATTRIBUTE_STRING)) {
				attributes.add(new Attribute(attributeName, attributes.size(), Attribute.STRING) );
				readTillEOL();
			} else if (m_streamTok.sval.equalsIgnoreCase(Attribute.ARFF_ATTRIBUTE_DATE)) {
				String format = null;
				if (m_streamTok.nextToken() != StreamTokenizer.TT_EOL) {
					if ((m_streamTok.ttype != StreamTokenizer.TT_WORD)
							&& (m_streamTok.ttype != '\'')
							&& (m_streamTok.ttype != '\"')) {
						errorMessage("not a valid date format");
					}
					format = m_streamTok.sval;
					readTillEOL();
				} else {
					m_streamTok.pushBack();
				}
				attributes.add(new Attribute(attributeName, format, attributes.size()));

			}  else {
				errorMessage("no valid attribute type or invalid "
						+ "enumeration");
			}
		} else {

			// Attribute is nominal.
			attributeValues = new ArrayList<Object>();
			m_streamTok.pushBack();

			// Get values for nominal attribute.
			if (m_streamTok.nextToken() != '{') {
				errorMessage("{ expected at beginning of enumeration");
			}
			while (m_streamTok.nextToken() != '}') {
				if (m_streamTok.ttype == StreamTokenizer.TT_EOL) {
					errorMessage("} expected at end of enumeration");
				} else {
					attributeValues.add(m_streamTok.sval);
				}
			}
			attributes.add(new Attribute(attributeName, attributeValues, attributes.size()));
		}
		getLastToken(false);
		getFirstToken();
		if (m_streamTok.ttype == StreamTokenizer.TT_EOF)
			errorMessage("premature end of file");

		return attributes;
	}

    protected void readTillEOL() throws IOException {
        while (m_streamTok.nextToken() != StreamTokenizer.TT_EOL) {};
        
        m_streamTok.pushBack();
	}
	
}
