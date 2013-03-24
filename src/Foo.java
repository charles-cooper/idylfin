import org.apache.commons.math3.util.FastMath;
import com.idylytics.utils.MathUtils;
public class Foo { 
	private static final long HEX_40000000 = 0x40000000L; // 1073741824L
	private static final double CBRTTWO[] = { 0.6299605249474366,
		0.7937005259840998,
		1.0,
		1.2599210498948732,
		1.5874010519681994 };


	static final void logTime(String msg)
	{
		com.idylytics.yahoo.YahooFinance.logTime(msg);
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

	public static int getExponent(final double d)
	{
		return (int) ((Double.doubleToRawLongBits(d) >>> 52) & 0x7ff) - 1023;
	}

	public static double nextAfter(double d, double direction) {
		// handling of some important special cases
		if (Double.isNaN(d) || Double.isNaN(direction)) {
			return Double.NaN;
		} else if (d == direction) {
			return direction;
		} else if (Double.isInfinite(d)) {
			return copySign(Double.MAX_VALUE,direction);
		} else if (d == 0) {
			return copySign(Double.MIN_VALUE,direction);
		}
		// special cases MAX_VALUE to infinity and  MIN_VALUE to 0
		// are handled just as normal numbers

		final long bits = Double.doubleToRawLongBits(d);
		final long sign = bits & 0x8000000000000000L;
		if ((direction < d) ^ (sign == 0L))
			return Double.longBitsToDouble(sign | ((bits & 0x7fffffffffffffffL) + 1));
		else
			return Double.longBitsToDouble(sign | ((bits & 0x7fffffffffffffffL) - 1));
	}

	public static void main(String [] args)
	{
		double d = -Math.nextUp(-Double.MIN_NORMAL);
		d = Double.POSITIVE_INFINITY;
		if (getExponent(d)==Math.getExponent(d))
		{
			System.out.println("passed");
		}
		logTime("Start");
		double mag = Math.random() - 0.5;
		final int len = 1000*1000*100;
		final double [] random = MathUtils.shift(MathUtils.random(len),-.5);
		double x = 0;
		int k = 0;
		for (int i = len-1; i--!=0;)
		{
			mag = copySign(mag,random[i]);
			//mag = FastMath.copySign(mag,random[i]);
			//x = nextAfter(random[i],random[i+1]);
			//if (x != FastMath.nextAfter(random[i],random[i+1]))
			//	throw new RuntimeException("Uh oh");
			//k = getExponent(random[i]);
			//if (k != FastMath.getExponent(random[i]))
				//throw new RuntimeException("Uh oh");
		}
		logTime("warmed up");
		for (int i = len-1; i--!=0;)
		{
			mag = copySign(mag,random[i]);
			//x = nextAfter(random[i],random[i+1]);
			//k = getExponent(random[i]);
		}
		logTime("mine");
		for (int i = len-1; i--!=0;)
		{
			//mag = FastMath.copySign(mag,random[i]);
			//x = FastMath.nextAfter(random[i],random[i+1]);
			//k = FastMath.getExponent(random[i]);
		}
		logTime("apache");
		System.out.println(k+x+mag);
	}
}

