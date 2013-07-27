package test;

import java.io.FileNotFoundException;
import java.io.IOException;

import ll.core.Instances;
import ll.parser.gson.ParseInstances;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Test {
	private static Instances m_insts = null;
	private static Instances m_instsGson = null;
	private static String arffName = "I:/Weka-3-6/data/weather.nominal.arff";
	private static Gson gson = null;
	private static ParseInstances parseInst = null;
	
	public static void main(String[] args) {
		
		try {
			m_insts = new Instances( arffName );
		}
		catch (FileNotFoundException e) {	e.printStackTrace();	}
		catch (IOException e) {	e.printStackTrace();	}
		
		System.out.println(m_insts.numInstances());
		
		gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();

		String strInst = gson.toJson( m_insts.getInstances() );
		String strAttrInfo = gson.toJson( m_insts.getAttributes() );
		System.out.println(strInst);
		System.out.println(strAttrInfo);

		parseInst = new ParseInstances( strInst, strAttrInfo );
		m_instsGson = parseInst.parse();
		
		System.out.println(m_instsGson.numInstances() + ", " + m_instsGson.numAttributes());
	}
}
