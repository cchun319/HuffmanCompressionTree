import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class HuffTest {


	@Test
	public void testmakeHuffTree() {

		Huff cc = new Huff();
		String of = "./src/testfile.txt";
		String oftest = "./src/decompress.txt"; 
		String cf = "./src/compressed.txt";
		try {
			HuffTree HT = cc.makeHuffTree( new BitInputStream(of));
			assertEquals(HT.root().weight(), 18);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testmakeTable() {
		Huff cc = new Huff();
		String of = "./src/testfile.txt";
		String oftest = "./src/decompress.txt"; 
		String cf = "./src/compressed.txt";
		try {
			HuffTree HT = cc.makeHuffTree( new BitInputStream(of));
			Map<Integer, String> Encoding_M = new HashMap<>();;
			Encoding_M = cc.makeTable();
			int check = 'C';
			assertEquals(Encoding_M.get(check).compareTo("1"), 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testgetCode() {
		Huff cc = new Huff();
		String of = "./src/testfile.txt";
		String oftest = "./src/decompress.txt"; 
		String cf = "./src/compressed.txt";
		try {
			HuffTree HT = cc.makeHuffTree( new BitInputStream(of));
			cc.makeTable();
			int check = 'C';
			assertEquals(cc.getCode(check).compareTo("1"), 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testshowCounts() {
		Huff cc = new Huff();
		String of = "./src/testfile.txt";
		String oftest = "./src/decompress.txt"; 
		String cf = "./src/compressed.txt";
		try {
			HuffTree HT = cc.makeHuffTree( new BitInputStream(of));
			cc.makeTable();
			Map<Integer, Integer> count = new HashMap<>();;
			count = cc.showCounts();
			int check = 'A';
			assertEquals((int)count.get(check), 4);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testheaderSize() {
		Huff cc = new Huff();
		String of = "./src/testfile.txt";
		String oftest = "./src/decompress.txt"; 
		String cf = "./src/compressed.txt";
		try {
			HuffTree HT = cc.makeHuffTree( new BitInputStream(of));
			cc.makeTable();
			assertEquals(cc.headerSize(), 86);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testwriteHeader() {
		Huff cc = new Huff();
		String of = "./src/testfile.txt";
		String oftest = "./src/decompress.txt"; 
		String cf = "./src/compressed.txt";
		try {
			HuffTree HT = cc.makeHuffTree( new BitInputStream(of));
			cc.makeTable();
			BitOutputStream cfs = new BitOutputStream(cf);
			assertEquals(cc.writeHeader(cfs), 86);
			cfs.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testreadHeader() {
		Huff cc = new Huff();
		String of = "./src/testfile.txt";
		String oftest = "./src/decompress.txt"; 
		String cf = "./src/compressed.txt";
		try {
			HuffTree HT = cc.makeHuffTree( new BitInputStream(of));
			cc.makeTable();
			BitOutputStream cfs = new BitOutputStream(cf);
			cc.writeHeader(cfs);
			
			cfs.close();
			HuffTree HTR = cc.readHeader(new BitInputStream(cf));
			assertEquals(HT.size(), HTR.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testwrite() {
		Huff cc = new Huff();
		String of = "./src/testfile.txt";
		String oftest = "./src/decompress.txt"; 
		String cf = "./src/compressed.txt";
		assertEquals(cc.write(of, cf, false), 118);
	}

	@Test
	public void testuncompress() {
		Huff cc = new Huff();
		String of = "./src/testfile.txt";
		String oftest = "./src/decompress.txt"; 
		String cf = "./src/compressed.txt";
		cc.write(of, cf, false);
		assertEquals(cc.uncompress(cf, oftest), cc.getOsize());
	}

	@Test
	public void testtraverse() {
		Huff cc = new Huff();
		assertEquals(cc.tra_h(null, null, null) , 0);
		assertEquals(cc.storeHTree(null, null) , 0);
	}

}
