/* Copyright 2012 Thomas Jungblut, 2014 Pierre Senellart
   Derived from https://github.com/thomasjungblut
   
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. */

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.*;

public abstract class PairWritable<T1 extends BinaryComparable & Writable, T2 extends Writable & Comparable<T2>>
		implements WritableComparable<PairWritable<T1, T2>> {

	private T1 first;
	private T2 second;

	public PairWritable(Class<T1> firstref, Class<T2> secondref) {
		try {
			first = firstref.newInstance();
			second = secondref.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			// Unrecoverable
			e.printStackTrace();
			System.exit(1);
		}
	}

	public PairWritable(T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		first.readFields(in);
		second.readFields(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		first.write(out);
		second.write(out);
	}

	public T1 getFirst() {
		return first;
	}

	public T2 getSecond() {
		return second;
	}

	@Override
	public String toString() {
		return first + "\t" + second;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("unchecked")
		PairWritable<T1, T2> other = (PairWritable<T1, T2>) obj;
		if (first == null) {
			if (other.first != null) {
				return false;
			}
		} else if (!first.equals(other.first)) {
			return false;
		}
		if (second == null) {
			if (other.second != null) {
				return false;
			}
		} else if (!second.equals(other.second)) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(PairWritable<T1, T2> o) {
		int firstCompare = first.compareTo(o.first);
		if (firstCompare == 0)
			return second.compareTo(o.second);
		else
			return firstCompare;
	}
}