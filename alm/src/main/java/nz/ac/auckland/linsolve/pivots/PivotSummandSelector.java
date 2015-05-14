package nz.ac.auckland.linsolve.pivots;

import java.util.List;

import nz.ac.auckland.linsolve.Constraint;
import nz.ac.auckland.linsolve.LinearSpec;
import nz.ac.auckland.linsolve.Summand;
import nz.ac.auckland.linsolve.Variable;

public interface PivotSummandSelector {
	
	List<Constraint> init(List<Constraint> constraints, List<Variable> variables, int maxIndex);
	
	List<Constraint> init(LinearSpec linearSpec, int maxIndex);
	
	List<Constraint> init(LinearSpec linearSpec);

	List<Constraint> removeConstraint(List<Constraint> constraints, Constraint c);
	
	Summand selectPivotSummand(Constraint constraint);
}
 