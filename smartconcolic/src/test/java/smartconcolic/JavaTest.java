package smartconcolic;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class JavaTest {

	
	@Test
	public void testSetClone(){
		Set<Integer> orig = new HashSet<>();
		orig.add(1);
		
		Set<Integer> copy = new HashSet<>();
		for(int i : orig)
			copy.add(i);
		
		copy.add(2);
		
		System.out.println("orig: " + orig);
	}
}
