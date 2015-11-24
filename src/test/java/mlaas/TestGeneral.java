package mlaas;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * General test class.
 */
public class TestGeneral {

	boolean isTrue = true;

	@Before
	public void setup() throws Exception {
		// this.isTrue = false;
	}

	@Test
	public void sampleTest() {
		assertTrue(this.isTrue);
	}
}
