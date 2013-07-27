package ll.core;

import java.io.IOException;
import java.util.ArrayList;

import ll.io.lrfReader;

public class Instances {
	
	public static final String ARFF_RELATION = "@RELATION";
	public static final String ARFF_DATA = "@DATA";
	
	protected Attribute[] m_attrs = null;
	private Instance[] m_insts = null;
	private int m_size = 0;
	private int m_classIndex = -1;
	private String m_relaName = null;
	private transient int m_capcityDefault = 100;
	
	public Instances( String lrfPath ) throws IOException{
		lrfReader reader = new lrfReader( lrfPath ); 
		Instances dataset = reader.getData();
		initialize(dataset);
		copyInstances( dataset );
	}
	public Instances(){		
	}
	public Instances( String relaName, ArrayList<Object> attributes, int size){
		m_relaName = relaName;
		m_attrs = (Attribute[])attributes.toArray(new Attribute[attributes.size()]);
		m_insts = new Instance[m_capcityDefault];
		m_size = 0;
	}
	public Instances( String relaName, Attribute[] attrs ){
		m_relaName = relaName;
		m_attrs = new Attribute[ attrs.length ];
		int i = 0;
		for ( Attribute attr : attrs ){
			m_attrs[i++] = attr.clone();
		}
		m_size = 0;
		m_insts = new Instance[m_capcityDefault];
	}
	public int numInstances(){
		return m_insts.length;
	}
	public int numAttributes(){
		return m_attrs.length;
	}
	public Attribute attribute(int index) {
		return (Attribute) m_attrs[index];
	}
	protected void initialize(Instances dataset) {
		m_classIndex = dataset.m_classIndex;
		m_relaName = dataset.m_relaName;
		m_attrs = dataset.m_attrs;
		m_attrs = new Attribute[ dataset.m_attrs.length ];
		System.arraycopy(dataset.m_attrs, 0, this.m_attrs, 0, dataset.m_attrs.length);
	
		m_insts = new Instance[dataset.numInstances()];
	}
	
	protected void copyInstances( Instances dataset ){
		int i = 0;
		m_size = dataset.m_size;
		for ( Instance inst : dataset.m_insts ){
			this.m_insts[i] = (Instance)inst.clone();
			i++;
		}
	}
	
	@Override
	public Instances clone(){
		Instances insts = new Instances();
		insts.m_relaName 	= this.m_relaName;
		insts.m_size 		= this.m_size;
		insts.m_classIndex 	= this.m_classIndex;
		
		insts.m_attrs = new Attribute[ this.m_attrs.length ];
		System.arraycopy(this.m_attrs, 0, insts.m_attrs, 0, this.m_attrs.length);
		
		insts.m_insts = new Instance[ this.m_insts.length];
		System.arraycopy(this.m_insts, 0, insts.m_insts, 0, this.m_insts.length);
		
		return insts;
	}

	public Instance[] getInstances(){
		Instance[] insts = new Instance[ m_insts.length ];
		int i = 0;
		for ( Instance inst : this.m_insts ){
//			if( inst instanceof SparseInstance )
//				insts[i] = (SparseInstance)inst.clone();
//			else
				insts[i] = (Instance)inst.clone();
			i++;
		}
		return insts;
	}
	public Attribute[] getAttributes(){
		Attribute[] attrs = new Attribute[ m_attrs.length ];
		int i = 0;
		for ( Attribute attr : this.m_attrs ){
			attrs[i++] = attr.clone();
		}
		return attrs;		
	}

	public void add( Instance inst ){
		Instance instTmp = (Instance)inst.clone();
		if( m_insts.length<=m_size){
			Instance[] newInsts = new Instance[ m_insts.length + m_capcityDefault];
			System.arraycopy(m_insts, 0, newInsts, 0, m_insts.length);
			m_insts = newInsts;
		}
		m_insts[m_size++] = instTmp;
		// do not set the dataset of the instance		
	}

	public void compactify(){
		if(m_size<m_insts.length){
			Instance[] newInsts = new Instance[ m_size ];
			System.arraycopy(m_insts, 0, newInsts, 0, m_size);
			m_insts = newInsts;
		}
	}
	public int numClasses(){
		if ( m_attrs[m_attrs.length-1].getType()==Attribute.NOMINAL ) {
			return m_attrs[m_attrs.length-1].numValues();
		}
		return 1;
	}
}
