package mlaas;

import mlaas.util.*;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.*;
import java.util.stream.*;

/**
 * Quick test for the power set methods.
 */
public class TestPowerSet {

	@Test
	public void testPowerSet() {

		List<Integer> set = Arrays.asList(1,2,3,4,5,6,7,8);

		List<List<Integer>> ps = PowerSet.findPowerSet(set);

		// Should be of size 2^N-1 (it shouldn't have an empty set)
		assertTrue(ps.size() == Math.pow(2, set.size())-1);
		// The first set in the ps should be set we derived it from
		assertTrue(ps.get(0).size() == set.size());
	}
}
