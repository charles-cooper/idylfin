/* Copyright (C) 2013 by Idylwood Technologies, LLC. All rights reserved.
 *
 * Developed at Idylwood Technologies, LLC.
 * Permission to use, copy, modify, and distribute this
 * software is freely granted, provided that this notice 
 * is preserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * The License should have been distributed to you with the source tree.
 * If not, it can be found at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Author: Charles Cooper
 * Date: 2013
 * ====================================================
 */

package com.idylwood.utils;

import java.util.List;
import java.util.ArrayList;

import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optim.linear.NonNegativeConstraint;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.linear.SimplexSolver;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer;

// Holder class for new stuff which hasn't had the dependencies gotten rid of yet.
public final class OptimizationUtils {

	private OptimizationUtils() {}

	private static final class MarkowitzFunction implements org.apache.commons.math3.analysis.MultivariateFunction
	{
		final double[][] S;
		final double[]R;
		final double q;
		MarkowitzFunction(final double[][] S, final double[]R, final double q)
		{
			this.S = S; this.R = R; this.q = q;
		}
		// this is gonna be so effing slow! haha
		// see http://en.wikipedia.org/wiki/Modern_portfolio_theory
		@Override public final double value(double[] w)
		// w weights, S cov matrix, R expected returns, q risk tolerance
		{
			final double foo = MathUtils.linearCombination(w,MathUtils.matrixMultiply(S,w));
			final double bar = MathUtils.linearCombination(R,w) * q;
			return foo - bar;
		}
	}
	// TODO make this work
	static final double[] cvxSolve(final double[][] S, final double []R, final double q)
	{
		ObjectiveFunction f = new ObjectiveFunction(new MarkowitzFunction(S,R,q));
		final double[] startPoint = new double[S.length];
		java.util.Arrays.fill(startPoint,1.0/S.length);

		final double[] ret =
			new NonLinearConjugateGradientOptimizer(
					NonLinearConjugateGradientOptimizer.Formula.FLETCHER_REEVES,
					new SimpleValueChecker(.1,.1)
				)
			.optimize(f,
					GoalType.MINIMIZE,
					new InitialGuess(startPoint)
				)
			.getPoint();
		return ret;
	}

	// returns double[] x such that T(c)*x is maximized, A*x <= b, and x>=0
	// TODO debug!
	static final double[] lpSolve(final double[][] A, final double[] b, final double[] c)
	{
		// TODO implement Feynman's simplex solver!
		// all apache stuff
		LinearObjectiveFunction f = new LinearObjectiveFunction(c, 0);
		List<LinearConstraint> constraints = new ArrayList<LinearConstraint>(A.length);
		for (int i = 0; i < A.length; ++i)
			constraints.add(new LinearConstraint(A[i],Relationship.GEQ,b[i]));

		final double ret[] = new SimplexSolver()
			.optimize(f,
					new LinearConstraintSet(constraints),
					GoalType.MINIMIZE,
					new NonNegativeConstraint(false),
					new MaxIter(100)
				 )
			.getPoint();

		return ret;
	}

	public static void main(String [] args)
	{
	}
}


