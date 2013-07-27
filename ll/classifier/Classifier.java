package ll.classifier;

import ll.core.Attribute;
import ll.core.Instance;
import ll.core.Instances;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

//import ll.classifiers.Classifier;
//import ll.classifiers.Evaluation;

public abstract class Classifier implements Cloneable, Serializable{
	
	public abstract void buildClassifier(Instances data) throws Exception;

	public double classifyInstance(Instance instance) throws Exception {

		double[] dist = distributionForInstance(instance);
		if (dist == null) {
			throw new Exception("Null distribution predicted");
		}
		switch (instance.classAttribute().getType()) {
		case Attribute.NOMINAL:
			double max = 0;
			int maxIndex = 0;

			for (int i = 0; i < dist.length; i++) {
				if (dist[i] > max) {
					maxIndex = i;
					max = dist[i];
				}
			}
			if (max > 0) {
				return maxIndex;
			} else {
				return Instance.missingValue();
			}
		case Attribute.NUMERIC:
			return dist[0];
		default:
			return Instance.missingValue();
		}
	}
	
	public double[] distributionForInstance(Instance instance) throws Exception {

		double[] dist = new double[instance.numClasses()];
		switch (instance.classAttribute().getType()) {
		case Attribute.NOMINAL:
			double classification = classifyInstance(instance);
			if ( Double.isNaN(classification) ) {
				return dist;
			} else {
				dist[(int) classification] = 1.0;
			}
			return dist;
		case Attribute.NUMERIC:
			dist[0] = classifyInstance(instance);
			return dist;
		default:
			return dist;
		}
	}
	
//	protected static void runClassifier(Classifier classifier, String[] options) {
//		try {
//			System.out.println(Evaluation.evaluateModel(classifier, options));
//		} catch (Exception e) {
//			if (((e.getMessage() != null) && (e.getMessage().indexOf(
//					"General options") == -1))
//					|| (e.getMessage() == null))
//				e.printStackTrace();
//			else
//				System.err.println(e.getMessage());
//		}
//	}
	
}
