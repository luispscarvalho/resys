package br.org.resys.util;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Generic utils
 * 
 * @author Luis Paulo
 */
public class Util {

	/**
	 * Creates a unique id
	 * <p>
	 * Necessary to avoid duplication of files names.
	 * 
	 * @return a new unique id
	 */
	public static String generateUid() {
		SecureRandom random = new SecureRandom();
		String uid = new BigInteger(130, random).toString(32);

		return uid;
	}

}
