package ll.core;

import ll.core.Instance;

public class SparseInstance extends Instance {

	  protected int[] m_indices;
	  protected int m_numAttributes;
	  
	public SparseInstance(double weight, double[] attValues, int[] indices, int maxNumValues) {

		int vals = 0;
		m_attValues = new double[attValues.length];
		m_indices = new int[indices.length];
		for (int i = 0; i < attValues.length; i++) {
			if (attValues[i] != 0) {
				m_attValues[vals] = attValues[i];
				m_indices[vals] = indices[i];
				vals++;
			}
		}
		if (vals != attValues.length) {
			// Need to truncate.
			double[] newVals = new double[vals];
			System.arraycopy(m_attValues, 0, newVals, 0, vals);
			m_attValues = newVals;
			int[] newIndices = new int[vals];
			System.arraycopy(m_indices, 0, newIndices, 0, vals);
			m_indices = newIndices;
		}
		m_weight = weight;
		m_numAttributes = maxNumValues;
		m_dataset = null;
	}
	public SparseInstance(){
	}
	
	@Override 
	public Object clone(){
		SparseInstance spsInst = new SparseInstance();
		spsInst.m_attValues = new double[ this.m_attValues.length ];
		System.arraycopy( this.m_attValues, 0, spsInst.m_attValues, 0, this.m_attValues.length );
		spsInst.m_indices = new int[ this.m_indices.length ];
		System.arraycopy(this.m_indices, 0, spsInst.m_indices, 0, this.m_indices.length);
		spsInst.m_numAttributes = this.m_numAttributes;
//		spsInst.m_Issparse = true;
		spsInst.m_weight = this.m_weight;
		
		return spsInst;
	}
	  
}
