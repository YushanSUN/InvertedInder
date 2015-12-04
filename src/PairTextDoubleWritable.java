import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;

public class PairTextDoubleWritable extends PairWritable<Text, DoubleWritable> {
	public PairTextDoubleWritable() {
		super(Text.class, DoubleWritable.class);
	}

	public PairTextDoubleWritable(Text first, DoubleWritable second) {
		super(first, second);
	}
	
	@Override
	public boolean equals(Object that) {
		return super.equals(that);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public String toString() {
		return super.toString();
	}
};
