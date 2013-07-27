package ll.parser.gson;

import java.util.Hashtable;
import java.util.Iterator;

import ll.core.Attribute;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonStreamParser;
import com.google.gson.JsonArray;

public class ParseAttrInfo {
	
	private String strToParse = null;
	//private Gson gson = null;
	
	public ParseAttrInfo( String str ){
		strToParse = str;
		//gson = new Gson();		
	}
	
	public Attribute[] parseAttr(){
		
		JsonStreamParser jParser = new JsonStreamParser(strToParse);
		Attribute[] attr = null;
		while( jParser.hasNext() ){
			JsonElement jElement = jParser.next();
			if ( jElement.isJsonArray() ){
				JsonArray jArray= jElement.getAsJsonArray();
				attr = new Attribute[jArray.size()];
				int i = 0;
				Iterator<JsonElement> jIt = jArray.iterator();
				while ( jIt.hasNext() ){					
					JsonObject jObject = jIt.next().getAsJsonObject();
					attr[i++] = parseToAttribute(jObject);
					//System.out.println("ok");
				}
			}
		}
		return attr;
	}
	
	public Attribute parseToAttribute( final JsonObject jObject ){
		
		String attrName = parseName(jObject);
		int type = parseType(jObject);
		int index = parseIndex(jObject);
//		int ordering = parseOdering( jObject);
//		boolean isRegular = parseRegular( jObject);
//		boolean isAverageable = parseAveragable( jObject );
//		boolean hasZeropoint = parseHasZeropoint( jObject );
		//boolean lowerBoundIsOpen = parseLowerBoundIsOpen( jObject );
		//double lowerBound = parseLowerBound( jObject );
		//boolean upperBoundIsOpen = parseUpperBoundIsOpen( jObject );
		//double upperBound = parseUpperBound( jObject );
		double weight = parseWeight( jObject );
		Object[] objs = parseValues( jObject );
		Hashtable hash = parseHashtable( jObject, objs );
		int size = parseSize( jObject );
		//ProtectedProperties propt = parseProtectedProperties( jObject);
		
		return new Attribute( objs, size, hash, attrName, weight, type, index);
		
	}
	
	public String parseName( final JsonObject jObject ){
		if ( jObject.has("m_name") ){
			JsonPrimitive jName = jObject.get("m_name").getAsJsonPrimitive();		
			return  jName.getAsString();
		}
		return "nod.name";
	}
	
	public int parseType( final JsonObject jObject ){
		if ( jObject.has("m_type") ){
			JsonPrimitive jType = jObject.get("m_type").getAsJsonPrimitive();	
			return jType.getAsInt();
		}
		return Attribute.NUMERIC;
	}
	
	public int parseSize( final JsonObject jObject ){
		if ( jObject.has("m_size") ){
			JsonPrimitive jType = jObject.get("m_size").getAsJsonPrimitive();	
			return jType.getAsInt();
		}
		return Attribute.NUMERIC;
	}
	
	public int parseIndex( final JsonObject jObject ){
		if ( jObject.has("m_index") ){
			JsonPrimitive jIndex = jObject.get("m_index").getAsJsonPrimitive();	
			return jIndex.getAsInt();
		}
		return -1;
	}
	public double parseWeight( final JsonObject jObject ){
		if ( jObject.has("m_weight") ) {
			JsonPrimitive jWeight = jObject.get("m_weight").getAsJsonPrimitive();
			return jWeight.getAsDouble();
		}
		return 1.0;
	}
	
	public Object[] parseValues( final JsonObject jObject ){
		//JsonObject jHash = jObject.get("m_Values").getAsJsonObject();
		if (jObject.has("m_member") ){
			JsonArray jM_Array = jObject.get("m_member").getAsJsonArray();
			Object[] obj = new Object[ jM_Array.size() ];
			Iterator<JsonElement> iter = jM_Array.iterator();
			int i = 0;
			while ( iter.hasNext() ){
				obj[i++] = iter.next().getAsJsonPrimitive().getAsString();
			}
			
			return  obj;
		}
		return null;
	}
	
	public Hashtable parseHashtable( final JsonObject jObject , final Object[] objs ){
		if ( jObject.has("m_hashtable") ){
			Hashtable hs = new Hashtable();
			for ( Object obj : objs ){
				JsonPrimitive jPrimit = jObject.get("m_hashtable").getAsJsonObject().get( (String)obj ).getAsJsonPrimitive();
				int index = jPrimit.getAsInt();
				hs.put( (String)obj, index );
			}	
			return hs;
		}
		return new Hashtable();
	}
}
