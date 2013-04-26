/*
 * ====================================================
 * Copyright (C) 2013 by Idylwood Technologies, LLC. All rights reserved.
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
 * Authors: Charles Cooper, Harry C Kim
 * Date: 2013
 * ====================================================
 */


package com.idylwood.utils;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
/*
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
*/
import org.apache.commons.math3.linear.SingularValueDecomposition;

// Holder class for new stuff which hasn't had the dependencies gotten rid of yet.
public final class OptimizationUtils {

	private OptimizationUtils() {}

	/**
	 * Solves for Markowitz optimal portfolio by means of lagrange multiplier.
	 * Returns null if it does not exist (the matrix is singular)
	 * Otherwise it attempts to find the lowest variance portfolio for
	 * the given <code>portfolio_return</code>
	 * @param covariance Precalculated covariance matrix
	 * @param returns Precalculated vector of returns
	 * @param portfolio_return Return to optimize risk for
	 * @author Harry C Kim
	 * @return
	 */
	static final double[] MarkowitzSolve(final double[][] covariance, final double[] returns, final double portfolio_return)
	{
		if (covariance.length!=covariance[0].length)
			throw new IllegalArgumentException("Covariance needs to be square matrix");
		if (returns.length!=covariance.length)
			throw new IllegalArgumentException("Returns must be same length as covariance");

		/*
		for (int i = 0; i < covariance.length; i++)
			MathUtils.printArray(covariance[i]);
		System.out.println();
		MathUtils.printArray(returns);
		System.out.println();
		*/

		final int timePoints = covariance.length;
		final double[][] lagrangeMatrix = new double[timePoints+2][timePoints+2];
		//b as in Ax = b
		final double[] b = new double[timePoints+2];
		for(int i = 0; i < timePoints; i++)
		{
			for(int j = 0; j < timePoints; j++)
			{
				lagrangeMatrix[i][j] = 2*covariance[i][j];
				b[i] = 0;
				// this is like riskTolerance*returns[i]; but since
				// returns[i]*weights[i] = portfolio_return it will go away in the derivative
			}
		}

		for(int j = 0; j<timePoints; j++)
		{
			lagrangeMatrix[timePoints][j] = returns[j];
			lagrangeMatrix[timePoints+1][j] = 1;
			lagrangeMatrix[j][timePoints] = returns[j];
			lagrangeMatrix[j][timePoints+1] = 1;
		}
		b[timePoints] = portfolio_return; //**** what is the constraint on total expected return?
		b[timePoints + 1] = 1;

		/*
		// Print out lagrangeMatrix augmented with b vector
		for(int i=0; i<timePoints+2; i++)
		{
			for(int j=0; j<timePoints+2;j++)
			{
				System.out.print(lagrangeMatrix[i][j] + " ");
			}
			System.out.println(b[i]);
		}
		*/
		// TODO use Gaussian elimination solver, may be faster
		// TODO maybe refactor to use idylblas
		RealMatrix lagrangeReal = MatrixUtils.createRealMatrix(lagrangeMatrix);
		RealVector bReal = MatrixUtils.createRealVector(b);
		SingularValueDecomposition svd = new SingularValueDecomposition(lagrangeReal);

		if (!svd.getSolver().isNonSingular())
			return null;

		RealVector solution = svd.getSolver().solve(bReal);

		final double weights[] = new double[timePoints];

		// last two elements of solution are just lagrange multipliers
		for (int i = 0; i < weights.length; i++)
			weights[i] = solution.getEntry(i);

		// put these in some test class
		if (!MathUtils.fuzzyEquals(1,MathUtils.sum(weights)))
			throw new RuntimeException();
		if (!MathUtils.fuzzyEquals(portfolio_return,MathUtils.linearCombination(returns, weights)))
			throw new RuntimeException();
		//The following calculates the risk(variance) for the weights found
		// final double risk = MathUtils.linearCombination(MathUtils.matrixMultiply(covariance, weights),weights);
		return weights;
	}

/*
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
	*/

}


