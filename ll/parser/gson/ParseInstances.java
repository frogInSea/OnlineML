package ll.parser.gson;

import java.util.Iterator;

import ll.core.Attribute;
import ll.core.Instance;
import ll.core.Instances;
import ll.core.SparseInstance;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonStreamParser;
import com.google.gson.JsonArray;

public class ParseInstances {
	
	private String strInstToParse = null;
	private String strAttToParse = null;
	private ParseAttrInfo attrInfo = null;
	
	public ParseInstances( String strInst, String strAttr) {
		strInstToParse = strInst;
		strAttToParse = strAttr;
		attrInfo = new ParseAttrInfo(strAttToParse);
	}
	
	public Instances parse(){
		//Object[] ins = gson.fromJson(strToParse, Object[].class);
		Attribute[] attrs = attrInfo.parseAttr();	
		Instances insts = new Instances( "heihei", attrs );
		
		JsonStreamParser jParser = new JsonStreamParser(strInstToParse);
		
		while( jParser.hasNext() ){
			JsonElement jElement = jParser.next();
			if ( jElement.isJsonArray() ){
				JsonArray jArray= jElement.getAsJsonArray();
				Iterator<JsonElement> jIt = jArray.iterator();
				while ( jIt.hasNext() ){					
					JsonObject jObject = jIt.next().getAsJsonObject();
					Instance inst = parseToInstance(jObject);
					insts.add(inst);
					inst.setDataset(insts);
				}
			}
		}
		insts.compactify();
		return insts;
	}
	
	public Instance parseToInstance( JsonObject jObject ){
		JsonArray jArrayRoot = jObject.get("m_attValues").getAsJsonArray();
		double[] attributes = new double[jArrayRoot.size()];
		int i = 0;
		Iterator<JsonElement> jItRoot = jArrayRoot.iterator();
		while ( jItRoot.hasNext() ){
			attributes[i++] = jItRoot.next().getAsJsonPrimitive().getAsDouble();
		}
		JsonPrimitive jWeight = jObject.get("m_weight").getAsJsonPrimitive();
		double weight = jWeight.getAsDouble();
		if( jObject.has("m_indices")){
			JsonArray jArrayIndices = jObject.get("m_indices").getAsJsonArray();
			int []indices = new int[ jArrayIndices.size() ];
			i = 0;
			Iterator<JsonElement> jItIndicesRoot = jArrayIndices.iterator();
			while( jItIndicesRoot.hasNext() ){
				indices[i++] = jItIndicesRoot.next().getAsJsonPrimitive().getAsInt();
			}
			int numAttrs = jObject.get("m_numAttributes").getAsJsonPrimitive().getAsInt();
			
			return new SparseInstance( weight, attributes, indices, numAttrs);
		}		
		return new Instance( weight, attributes );
	}
	
}
