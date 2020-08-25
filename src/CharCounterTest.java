import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.junit.Test;

public class CharCounterTest {

	@Test
	public void testCharCounterConstructor() {
		ICharCounter cc = new CharCounter();
		assertNotNull(cc);
	}

	@Test
	public void testCharCounterCountAll() throws IOException {
		ICharCounter cc = new CharCounter();
		InputStream ins;
		ins = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		cc.countAll(ins);
		Map<Integer, Integer> check = cc.getTable();
		assertEquals(7, check.size());	
		cc.countAll(null);
	}
	
	@Test
	public void testCharCounterGetCount() throws IOException {
		ICharCounter cc = new CharCounter();
		assertNotNull(cc);
		InputStream ins;
		ins = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		cc.countAll(ins);
		char check = 't';
		assertEquals(3, cc.getCount(check));
	}
	
	@Test
	public void testCharCounterClear() throws IOException {
		ICharCounter cc = new CharCounter();
		InputStream ins;
		ins = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
		cc.countAll(ins);
		cc.clear();
		char check2 = 't';
		Map<Integer, Integer> check = cc.getTable();
		assertEquals(0, cc.getCount(check2));	
	}

}
