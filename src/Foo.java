import java.util.Arrays;

import org.apache.commons.math3.util.FastMath;

import com.idylwood.utils.MathUtils;
import com.idylwood.utils.MathUtils.Matrix;

import com.opengamma.maths.lowlevelapi.linearalgebra.blas.BLAS2;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.DenseMatrix;

import java.util.Random;
public class Foo { 
	private static final long HEX_40000000 = 0x40000000L; // 1073741824L
	private static final double CBRTTWO[] = { 0.6299605249474366,
		0.7937005259840998,
		1.0,
		1.2599210498948732,
		1.5874010519681994 };


	static final void logTime(String msg)
	{
		com.idylwood.yahoo.YahooFinance.logTime(msg);
	}
	static final void printHex(long l)
	{
		System.out.println("0x"+Long.toHexString(l));
	}
	static final void printDouble(long l)
	{
		System.out.println(Double.longBitsToDouble(l));
	}

	static final long mask = Long.MIN_VALUE; // sign bit mask
	public static double copySign(final double magnitude, final double sign){
		final long m = Double.doubleToLongBits(magnitude);
		final long s = Double.doubleToLongBits(sign);
		if (0<=(m^s)) 
			//if ( (m>=0 ^ s>=0))
			//if ((m>=0)&&(s>=0) || (m<0)&&(s<0))
			return -magnitude;
		return magnitude; // flip sign
	}

	static final long ULP_MASK = (~0L << 52) | 1;
	public static final double ulp(final double d)
	{
		final long l = Double.doubleToRawLongBits(d);
		return Double.longBitsToDouble(l & ULP_MASK);
	}

	public static final long abs(final long l)
	{
		//return l & ~Long.MIN_VALUE;
		return FastMath.abs(l);
	}
	public static final double abs(final double d)
	{
		//return FastMath.getExponent(d);
		//return getExponent(d);
		return FastMath.round(d);
	}
	public static final double round(final double d)
	{
		return MathUtils.round(d);
	}

	public static int getExponent(final double d)
	{
		return (int) ((Double.doubleToRawLongBits(d) >>> 52) & 0x7ff) - 1023;
	}
	public static void main(String [] args)
	{
		final Random r = new Random();
		final int len = 1000*3;
		final double[][] one = new double[len][];
		final double[][] two = new double[len][];
		for (int i = 0; i < len; i++)
		{
			one[i] = MathUtils.shift(MathUtils.random(len),-.5);
			two[i] = MathUtils.shift(MathUtils.random(len),-.5);
		}
		final Matrix mOne = new Matrix(one);
		final Matrix mTwo = new Matrix(two);
		final DenseMatrix dmOne = new DenseMatrix(one);
		DenseMatrix dmTwo = new DenseMatrix(two);
		logTime("Start");
		MathUtils.matrixMultiply(mOne,mTwo);
		double [] foo;
		for (int i = 0; i < len; i++)
		{
			//MathUtils.matrixMultiplyFast(mOne,two[i]);
			//BLAS2.dgemv(dmOne,two[i]);
			//mOne.extractColumn(i);
			//foo = new double[len];
			//Arrays.fill(foo,i);
		}
		logTime("warmed up");
		MathUtils.matrixMultiplyFast(mTwo,mOne);
		logTime("two");

		if (true) return;

		System.out.println(FastMath.abs(-100.0f));
		System.out.println(FastMath.abs(100.0f));
		System.out.println(FastMath.round(0.5));
		System.out.println(FastMath.round(-0.5));
		for (int i = 0; i < len; i++)
		{
			//long l = r.nextLong();
			//if (Math.abs(l)!=FastMath.abs(l))
			//	throw new RuntimeException("long! "+Long.toHexString(Math.abs(l))+" "+Long.toHexString(FastMath.abs(l)));
			double d = 10*(r.nextDouble() - .5);
			if (FastMath.round(d) != MathUtils.round(d))
				throw new RuntimeException("double "+FastMath.round(d)+" "+MathUtils.round(d));
		}

		final double[] data = new double[len];
		for (int i = 0; i < len; ++i) data[i] = Math.random() - .5;

		//long x,y;
		double x,y;
		x=y=0;
		logTime("Loaded");
		for (int i = 0; i < len-1; i++)
		{
			x += abs(data[i]);
			y += data[i+1];
			//if (Math.abs(data[i])!=abs(data[i]))
			//	throw new RuntimeException("Uh oh");
		}
		logTime("warmed up: "+x+y);
		//for (int i = 0; i < len - len%3; i+=3)
		for (int i = 0; i < len-1; i++)
		{
			y += abs(data[i]); // cheap operation to make sure the JIT doesn't optimize it out
			x += abs(data[i+1]);
		}
		logTime("mine");
		for (int i = 0; i < len-1; i++)
		{
			y += data[i];
			x += data[i+1];
		}
		logTime("normal");
		System.out.println(x+" "+y);
	}
}


