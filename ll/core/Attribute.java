package ll.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;

public class Attribute {

	static final long serialVersionUID = -742180568732916383L;

	public static final String ARFF_ATTRIBUTE = "@ATTRIBUTE";
	public static final String ARFF_ATTRIBUTE_REAL = "real";
	public static final String ARFF_ATTRIBUTE_INTEGER = "integer";
	public static final String ARFF_ATTRIBUTE_NUMERIC = "numeric";
	public static final String ARFF_ATTRIBUTE_STRING = "string";
	public static final String ARFF_ATTRIBUTE_DATE = "Date";
	
	//private static final int STRING_COMPRESS_THRESHOLD = 200;

	public static final int NUMERIC = 0;
	public static final int NOMINAL = 1;
	public static final int STRING = 2;
	public static final int DATE = 3;

	protected Object[] m_member = null;
	private int m_size = 0;
	private Hashtable m_hashtable = null;
	private String m_name = null;
	private double m_weight = 1.0;
	private int m_type = 0;
	private int m_index = 0;
	private SimpleDateFormat m_DateFormat = null; 

	public Attribute(){}
	
	public Attribute(String aname, int index, int type) {
		m_name = aname;
		m_index = index;
		m_type = type;
	}

	public Attribute(String attributeName, String dateFormat, int size) {
		m_name = attributeName;
		m_index = size;
		m_type = DATE;
		if (dateFormat != null) {
			m_DateFormat = new SimpleDateFormat(dateFormat);
		} else {
			m_DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		}
		m_DateFormat.setLenient(false);
	}

	public Attribute(String attributeName, ArrayList<Object> attributeValues, int size) {
		m_member = new Object[attributeValues.size()];
		m_hashtable = new Hashtable(attributeValues.size());
		m_type = NOMINAL;
		m_index = size;
		for (int i = 0; i < attributeValues.size(); i++) {
			Object store = attributeValues.get(i);
//			if (((String) store).length() > STRING_COMPRESS_THRESHOLD) {
//				try {
//					store = new SerializedObject(attributeValues.elementAt(i), true);
//				} catch (Exception ex) {
//					System.err.println("Couldn't compress nominal attribute value -"
//									+ " storing uncompressed.");
//				}
//			}
			if (m_hashtable.containsKey(store)) {
				throw new IllegalArgumentException("A nominal attribute ("
						+ attributeName + ") cannot"
						+ " have duplicate labels (" + store + ").");
			}
			m_member[i] = store;
			m_hashtable.put(store, new Integer(i));
		}
	}
	
	public Attribute(Object[] objs, int size, Hashtable hashtable, String name, double weight, int type, int index ){
		m_member = objs;
		m_size = size;
		m_hashtable = hashtable;
		m_name = name;
		m_weight = weight;
		m_type = type;
		m_index = index;
		
	} 
	
	public final int indexOfValue(String value) {

		if (!(m_type==NOMINAL) && !(m_type==STRING))
			return -1;
		Object store = value;
//		if (value.length() > STRING_COMPRESS_THRESHOLD) {
//			try {
//				store = new SerializedObject(value, true);
//			} catch (Exception ex) {
//				System.err.println("Couldn't compress string attribute value -"
//						+ " searching uncompressed.");
//			}
//		}
		Integer val = (Integer) m_hashtable.get(store);
		if (val == null)
			return -1;
		else
			return val.intValue();
	}
	
	public int addStringValue(String value) {

		if (!(m_type == STRING)) {
			return -1;
		}
		Object store = value;

		// if (value.length() > STRING_COMPRESS_THRESHOLD) {
		// try {
		// store = new SerializedObject(value, true);
		// } catch (Exception ex) {
		// System.err.println("Couldn't compress string attribute value -"
		// + " storing uncompressed.");
		// }
		// }
		Integer index = (Integer) m_hashtable.get(store);
		if (index != null) {
			return index.intValue();
		} else {
			if (m_member.length <= m_size) {
				Object[] newObjects = new Object[m_member.length + 5];
				System.arraycopy(m_member, 0, newObjects, 0, m_size);
				m_member = newObjects;
			}
			m_member[m_size] = store;
			m_size++;
		}
		m_hashtable.put(store, new Integer(m_size));
		return m_size;
	}

	public int getType() {
		return m_type;
	}

	public int getIndex() {
		return m_index;
	}

	public double getWeight() {
		return m_weight;
	}

	public String getName() {
		return m_name;
	}

	public double parseDate(String sval) throws ParseException {
	    switch (m_type) {
	    case DATE:
	      long time = m_DateFormat.parse(sval).getTime();
	      // TODO put in a safety check here if we can't store the value in a double.
	      return (double)time;
	    default:
	      throw new IllegalArgumentException("Can only parse date values for date"
	                                         + " attributes!");
	    }
	}

	@Override
	public Attribute clone(){
		
		Attribute attr = new Attribute();
		attr.m_name = m_name;
		attr.m_type = m_type;
		attr.m_weight = m_weight;
		attr.m_index = m_index;
		attr.m_size = m_size;
		if( m_DateFormat!= null)
			attr.m_DateFormat = (SimpleDateFormat)m_DateFormat.clone();
		if( m_member!=null ){
			attr.m_member = new Object[m_member.length];
			System.arraycopy( m_member, 0, attr.m_member, 0, m_member.length );
		}
		if(m_hashtable!=null)
			attr.m_hashtable = (Hashtable)m_hashtable.clone();
		
		return attr;
	}
	
	public int numValues(){
		return m_member.length;
	}
}
