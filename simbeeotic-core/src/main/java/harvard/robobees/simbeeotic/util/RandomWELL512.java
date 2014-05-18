/********************************************************************************
This file is part of Teeser.

Teeser is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Teeser is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Teeser.  If not, see <http://www.gnu.org/licenses/>.

Copyright 2012-2014 Dalimir Orfanus and The Teeser developers as defined under 
https://sourceforge.net/projects/teeser/
*******************************************************************************/

package harvard.robobees.simbeeotic.util;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

/**
 * Implementation of the "Well Equidistributed Long-period Linear" PRNG introduced in the
 * the article: Panneton, F. P. L'Ecuyer, and M. Matsumoto, "Improved Long-Period Generators
 * Based on Linear Recurrences Modulo 2", ACM Transactions on 
 * Mathematical Software, 32, 1 (2006), 1-16.
 * 
 * This implementation WELL512 has period 2^512 states ~10^154, more than number of atoms in 
 * the Universe. Code inspired by Chris Lomont's paper:
 * http://www.lomont.org/Math/Papers/2008/Lomont_PRNG_2008.pdf
 * 
 * @author Dalimir Orfanus
 *
 */
public class RandomWELL512 extends Random {
	private static final long serialVersionUID = -5477612952780544938L;
	
	final static int MASK = 0xF;
	final static int MAXLEN = 16;
	private int seed[];
	private int index = 0;
	
	/**
	 * Constructor that uses only 1 integer to create the complete seed. Seed is an array of
	 * {@link #MASK} integers. Here the first item is provided argument userSeed the rest
	 * of the array is filled with 0. This is not the best way how to init the random generator
	 * but is kept for backwards code compatibility.
	 * @param userSeed
	 */
	public RandomWELL512(int userSeed) {
		seed = new int[MAXLEN];
		seed[0] = userSeed;
		
		for(int i = 1; i < MAXLEN; i++) {
			seed[i] = 0;
		}
	}

	/**
	 * Constructor that uses only 1 long to create the complete seed. Seed is an array of
	 * {@link #MASK} integers. Long is split binary into two integers and seed array
	 * is filled with alternating lower and upper integer part of the long seed. 
	 * This is not the best way how to init the random generator
	 * and full int[16] seed should be used, however is kept for backwards code compatibility.
	 * @param userSeed
	 */
	public RandomWELL512(long userSeed) {
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.putLong(userSeed);
		bb.rewind();

		int low = bb.getInt();
		int high = bb.getInt();
		
		seed = new int[MAXLEN];
		
		for(int i = 0; i < MAXLEN; i+=2) {
			seed[i] = low;
		}

		for(int i = 1; i < MAXLEN; i+=2) {
			seed[i] = high;
		}
		
	}
	
	/**
	 * Constructor that takes the full seed, i.e. array of 16 integers.
	 * @param userSeed Seed as array of 16 integers
	 */
	public RandomWELL512(int[] userSeed) {
		setSeed(userSeed); 
	}
	
	/**
	 * Default constructor that uses seed generated from cryptographycaly secure {@link SecureRandom}
	 * generator.
	 */
	public RandomWELL512() {
		ByteBuffer bb = ByteBuffer.wrap(SecureRandom.getSeed(64));
		bb.rewind();
		seed = new int[MAXLEN];
		bb.asIntBuffer().get(seed);
	}
	
	/**
	 * Here happens the real magic. Function generates pseudo random stream of bits using WELL512
	 * algorithm. All other methods depend on this one when generating random integer, double etc. 
	 */
	@Override
	protected int next(int bits) {
		int a, b, c, d;
		
		a = seed[index];
		c = seed[ (index + 13) & MASK ];
		b = a^c^(a << 16)^(c << MASK);
		c = seed[ (index + 9) & MASK];
		c ^= (c >>> 11);
		a = seed[index] = b^c;
		d = a^( (a << 5) & 0xDA442D24);
		index = (index + MASK) & MASK;
		a = seed[index];		
		seed[index] = a^b^d^(a << 2)^(b << 18)^(c << 28);
		
		long res = seed[index];
		
		res &= ((1L << bits) - 1); 
		
		return (int)res;
	}
	
	/**
	 * Helper method to convert the seed into a string format where
	 * each each integer is represented as decimal number and separated
	 * with comma.
	 * @param userSeed
	 * @return
	 */
	public static String seedToString(int[] userSeed) {
		StringBuilder sb = new StringBuilder();
		for(int s : userSeed) {
			sb.append(s);
			sb.append(",");
		}
		
		// remove the last comma
		sb.deleteCharAt(sb.lastIndexOf(","));
		
		return sb.toString();
	}
	
	/**
	 * From the string of integers separated by comma creates an array of int
	 * of maximal size of 16. If provided String has less than 16 integers,
	 * remaining array is filled with 0. If it contains more than 16 integers,
	 * only first 16 are copied in to the new array.
	 * @param seedString
	 * @return
	 * @throws NumberFormatException
	 */
	public static int[] stringToSeed(String seedString) throws NumberFormatException {				
		String[] numbers = seedString.split(",");		
		int[] userSeed = new int[MAXLEN];
		Arrays.fill(userSeed, 0);
		
		for(int i = 0; i < MAXLEN && i < userSeed.length; i++) {
			userSeed[i] = Integer.valueOf(numbers[i]).intValue();
		}		
		return userSeed;
	}
	
	// --------- GETTERS / SETTERS -----------
	
	/**
	 * Sets seed of this generator to provided value. It immediately overwrites current seed
	 * and effect on generated random numbers is immediate. 
	 * @param userSeed
	 */
	public void setSeed(int[] userSeed) {
		if(userSeed.length != MAXLEN)
			throw new IllegalArgumentException("Provided seed must be an array of 16 integers (int[16])");
		
		seed = Arrays.copyOf(userSeed, MAXLEN);		
	}

	/**
	 * Returns current value of the seed
	 * @return Current value of the seed
	 */
	public int[] getSeed() {
		return this.seed;
	}
	
	/**
	 * Returns the seed as array of bytes of size 64 (16 integers * 4 bytes).
	 * @return
	 */
	public byte[] getSeedByte() {
		int capacity = MAXLEN * 4;
		ByteBuffer bb = ByteBuffer.allocate(capacity);		
		bb.asIntBuffer().put(seed);
		
		return bb.array();
	}
	
	/**
	 * Sets the seed from the given array. Expected size of array is 64 
	 * (16 integers * 4 bytes).
	 * @param userSeed
	 */
	public void setSeedByte(byte[] userSeed) {
		ByteBuffer bb = ByteBuffer.wrap(userSeed);
		bb.rewind();
		int[] newSeed = bb.asIntBuffer().array();		
		setSeed(newSeed);
	}
}
