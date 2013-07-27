package ll.core;

public class Instance {

	protected static final double MISSING_VALUE = Double.NaN;
	protected double[] m_attValues;
	protected double m_weight;
	protected Instances m_dataset;
	//protected boolean m_Issparse = false;

	public static double missingValue() {
		return MISSING_VALUE;
	}

	protected Instance() {
//		if ( this instanceof SparseInstance )
//			m_Issparse = true;
	}
	public Instance(double weight, double[] attValues) {
		m_attValues = attValues;
		m_weight = weight;
		m_dataset = null;
//		if ( this instanceof SparseInstance )
//			m_Issparse = true;
	}
	
	@Override
	public Object clone(){
		Instance inst = new Instance();
		inst.m_weight = this.m_weight;
//		inst.m_Issparse = this.m_Issparse;
		inst.m_attValues = new double[ this.m_attValues.length ];
		System.arraycopy( this.m_attValues, 0, inst.m_attValues, 0, this.m_attValues.length );
		// did not set data set yet
		return inst;
	}
	
	public void setDataset( Instances insts){
		m_dataset = insts;
	}
	
	public Attribute classAttribute(){
		return m_dataset.m_attrs[ m_attValues.length-1 ];
	}
	
	public int numClasses(){
		if (m_dataset!=null)
			return m_dataset.numClasses();
		else
			return -1;
	}
}
