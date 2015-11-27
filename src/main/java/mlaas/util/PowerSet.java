package mlaas.util;

import java.util.*;

/**
 * Methods to derive power sets. Adapted from Rosettacode.org: http://rosettacode.org/wiki/Power_set#Java
 */
public class PowerSet {

	/**
	 * Finds the power set of a given collection, sans the empty set.
	 *
	 * @param set The set to find the
	 * @return The power set of the given list, ordered from most number of elements to least.
	 * @throws RuntimeException if there are too many elements in the list to realistically compute the power set.
	 */
	public static <T> List<List<T>> findPowerSet(Collection<T> set) throws RuntimeException {

		// Understanding that the runtime of finding the power set is 2^n (Very bad!)
		if (set.size() > 16)
			throw new RuntimeException("Too many elements in set to realistically find power set.");

		List<List<T>> ps = new ArrayList<List<T>>();
		ps.add(new ArrayList<T>()); // Add the empty set

		// for every item in the original list
		for (T item : set) {

			List<List<T>> newPs = new ArrayList<List<T>>();

			for (List<T> subset : ps) {
				// copy all of the current powerset's subsets
				newPs.add(subset);

				// plus the subsets appended with the current item
				List<T> newSubset = new ArrayList<T>(subset);
				newSubset.add(item);
				newPs.add(newSubset);
			}

			// powerset is now powerset of list.subList(0, list.indexOf(item)+1)
			ps = newPs;
		}

		// Get rid of base case (empty set)
		ps.remove(0);

		// Sort ps from largest sets to smallest
		Collections.sort(ps, new Comparator<List<T>>() {
			@Override
			public int compare(List<T> o1, List<T> o2) {
				if (o1.size() > o2.size()) 		return -1;
				else if (o1.size() < o2.size()) return  1;
				else 							return  0;
			}
		});

		return ps;
	}
}
