import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chunchang
 *
 */
public class Huff implements ITreeMaker, IHuffEncoder, IHuffModel, IHuffHeader{

	private Map<Integer, Integer> chcount;
	private Comparable[] char_nodes;
	private Map<Integer, String> Encoding_M = new HashMap<>();;
	private HuffTree fre;
	private HuffTree rebuit = new HuffTree( 0, 0); // dummy root node
	private int internalnode;
	private int osize = 0;
	
	// return the size of original file
	public int getOsize()
	{
		return osize;
	}

	// use CharCounter class to compute occurrances table
	// update the size of the original file
	// build the huffman tree with minimum heap
	@Override
	public HuffTree makeHuffTree(InputStream stream) throws IOException {
		// TODO Auto-generated method stub
		CharCounter cc = new CharCounter();
		osize = cc.countAll(stream) * IHuffConstants.BITS_PER_WORD;
		chcount = cc.getTable();
		chcount.put(256, 1);
		char_nodes = new Comparable[chcount.size()];
		int ind = 0;
		for(Integer i : chcount.keySet())
		{
			HuffTree HLN = new HuffTree(i, chcount.get(i));
			char_nodes[ind++] = HLN;
		}


		MinHeap MH = new MinHeap(char_nodes, chcount.size() , 256);
		HuffTree re, re2, Merge = null;
		while(MH.heapsize() > 1)
		{
			re = (HuffTree)MH.removemin();
			re2 = (HuffTree)MH.removemin();
			Merge = new HuffTree(re.root(), re2.root(), re.weight() + re2.weight());
			MH.insert(Merge);
		}		
		fre = Merge;

		return Merge;
	}
	
	// traverse the huffman tree from root to leaf
	// update the encoding table with "path string" when reaching the leaf node

	@Override
	public Map<Integer, String> makeTable() {
		// TODO Auto-generated method stub
		internalnode = traverse(fre.root(), Encoding_M);

		for (Integer i : Encoding_M.keySet())
		{
			int w = i;
//			System.out.println(w + " | code: " + Encoding_M.get(i));
		}
		return Encoding_M;
	}

	// return the path code in encoding map made from traversing huffman tree
	
	@Override
	public String getCode(int i) {
		// TODO Auto-generated method stub
		return Encoding_M.get(i);
	}

	@Override
	public Map<Integer, Integer> showCounts() {
		// TODO Auto-generated method stub
		int c = 0;
		for(Integer i : chcount.keySet())
		{
			c = i;
//			System.out.println((char)c  + " | " + chcount.get(i) );
		}
		return chcount;
	}

	/**
	 * The number of bits in the header using the implementation, including
	 * the magic number presumably stored.
	 * @return the number of bits in the header
	 */
	@Override
	public int headerSize() {
		// 32 -> magic number 32 bits int, leaf node take 10 bit to store and internal node takes one bit
		Map<Integer, String> fake = new HashMap<>();

		int total = (IHuffConstants.BITS_PER_WORD + 2) * chcount.size() + traverse(fre.root(),fake) * 1 + 1 * IHuffConstants.BITS_PER_INT;
		return total;
	}

	//write magic number to the output stream first
	//then store Huffman tree by traverse the tree, internal node, write '0'; leaf node '1' + 'element'(9bit) 
	@Override
	public int writeHeader(BitOutputStream out) {
		out.write(IHuffConstants.BITS_PER_INT, IHuffConstants.MAGIC_NUMBER);
		int total = storeHTree(fre.root() ,out);

		return total + BITS_PER_INT;
	}

	//read 32 bits first to verify the magic number
	//if matching, keep reading to rebuild the huffman tree
	@Override
	public HuffTree readHeader(BitInputStream in) throws IOException {
		// read 
		int magic_check = 0;
		int read_ind = 0;
		int read = 0;
		while(read_ind < IHuffConstants.BITS_PER_INT)
		{
			read = in.read(1);
			if(read == -1)
			{
				throw new IOException("reading exception: compressed error");
			}
			read = read << (IHuffConstants.BITS_PER_INT - read_ind - 1) ;
			magic_check = magic_check | read;
			read_ind++;
		}
		
		if (magic_check != IHuffConstants.MAGIC_NUMBER)
		{
			throw new IOException("MagicNumberNotMatch read: " + magic_check + " and " + IHuffConstants.MAGIC_NUMBER);
		}

		IHuffBaseNode rebuiltroot = rebuiltTree(in);

		rebuit.setRoot(rebuiltroot);

		return rebuit;
	}

	// calculate the size of compress file and original file
	// write header (magic number and huffman tree)
	// read the original content and use the encoding table to write the code to compress file bit by bit
	@Override
	public int write(String inFile, String outFile, boolean force) {
		// write header
		int cfsize = 0;
		int wrsize = 0; 

		// read OF byte by byte
		BitInputStream ofs = new BitInputStream(inFile);
		try {
			fre = makeHuffTree(ofs);
			Encoding_M = makeTable();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		cfsize += headerSize();
		//use counting table and encoding table to get the size of compressed file
		
		for ( Integer i : chcount.keySet())
		{
			cfsize += chcount.get(i) * Encoding_M.get(i).length();
		}
		
//		System.out.println("o: " + osize + " cf: " + cfsize);
		// force compression is true, do compression
		// compress size less than original size, do compression
		if (force || (osize > cfsize))
		{
			BitOutputStream cfs = new BitOutputStream(outFile);
			wrsize += writeHeader(cfs);
			int c = 0;
			String code = "";
			try {
				ofs.reset();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				while((c = ofs.read()) != -1)
				{
					// check the encoding Map
					if(!Encoding_M.containsKey(c))
					{
						System.out.println("EncodingMappingNotRight");
						return -1;
					}
					// write to CF bit by bit
					code = Encoding_M.get(c);
//					System.out.println(c + " | " + code);
					for(int i = 0; i < code.length(); i++)
					{
						if(code.charAt(i) == '0'){
							cfs.write(1, 0);}
						else{
							cfs.write(1, 1);}
					}
					wrsize += code.length();
				}
				code = Encoding_M.get(256);

				for(int i = 0; i < code.length(); i++)
				{
					if(code.charAt(i) == '0'){
						cfs.write(1, 0);}
					else{
						cfs.write(1, 1);}
				}
				wrsize += code.length();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			cfs.close();
			ofs.close();
			// if do compressed, return writing size
			return wrsize;
		}
		ofs.close();
		// if not, return original size
		return osize;
	}

	// read the header, verify the magic number
	// after rebuilt the tree
	// read bit by bit to get the code, until EoF
	
	@Override
	public int uncompress(String inFile, String outFile) {
		// TODO Auto-generated method stub
		BitInputStream cfs = new BitInputStream(inFile);
		BitOutputStream dfs = new BitOutputStream(outFile);
		int writecount = 0;
		try {
			readHeader(cfs); // rebuilt the tree
			int readback = 0;
			IHuffBaseNode reach;
			IHuffBaseNode fake = new HuffInternalNode(null, null , 0);
			reach = fake;
			int readbit = 0;
			while(readback != 256) //psuedo EOF
			{
				String edge = "";
				//while not reach leaf node, keep read bit
				while(!reach.isLeaf())
				{
					//read bit by bit after readheader
					readbit = cfs.read(1);
					if(readbit == 1){
						edge += "1";
					}
					else{
						edge += "0";
					}
					reach = TraAndGetHuffNode(rebuit.root(), 0, edge);
				}
				readback = ((HuffLeafNode)reach).element();
				reach = fake;
				if(readback != 256) // if not EOF
				{
					dfs.write(IHuffConstants.BITS_PER_WORD, readback);
					writecount++;
				}
			}


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		cfs.close();
		dfs.close();

		return writecount * IHuffConstants.BITS_PER_WORD;
	}
	// traverse the Huffman tree to update the encoding map and get the number of internal nodes
	private int traverse(IHuffBaseNode root, Map<Integer, String> en)
	{
		int internal_count = 0;
		internal_count = tra_h(root, "", en);
		return internal_count;
	}
	
	
	/**
	 * if internal node return 1, concatenate a 0 in the code string
	 * else return 0, and update encoding map
	 * @param N root node of huffman tree 
	 * @param code, the path the node from root node
	 * @param en encoding map
	 * @return number of internal nodes
	 */
	
	public int tra_h(IHuffBaseNode N, String code, Map<Integer, String> en)
	{
		if(N == null)
		{
			return 0;
		}
		if(N.isLeaf())
		{
			en.put(((HuffLeafNode)N).element(), code);
			return 0;
		}
		return 1 + tra_h(((HuffInternalNode)N).left(), code + "0", en) + tra_h(((HuffInternalNode)N).right(), code + "1", en);
	}

	/**
	 * if internal node, write a 0 to outputstream and go to left and right child
	 * else write a 1 and concatenate the element in the 9 bit format in the back
	 * @param N , node in huffman tree 
	 * @param out, output stream
	 * @return int number of internal nodes
	 */
	
	public int storeHTree(IHuffBaseNode N, BitOutputStream out)
	{
		if(N == null)
		{
			return 0;
		}
		if(N.isLeaf())
		{
			out.write(1, 1);
			out.write(IHuffConstants.BITS_PER_WORD + 1, ((HuffLeafNode)N).element());
			return 1 + IHuffConstants.BITS_PER_WORD + 1;
		}
		out.write(1, 0);
		return (1 + storeHTree(((HuffInternalNode)N).left(), out) + storeHTree(((HuffInternalNode)N).right(), out));
	}

	// if read 1, create and return the left node with element as following 9 bits
	// else, create two base nodes and return an internal node 
	private IHuffBaseNode rebuiltTree(BitInputStream in) throws IOException
	{
		if(in.read(1) == 1) //leaf node
		{
			//read nine bits afterwards
			int readback = 0;
			for(int i = 0; i < 9; i++)
			{
				readback = readback | (in.read(1) << 9 - i - 1);
			}
			return new HuffLeafNode(readback, 0);
		}
		IHuffBaseNode leftnode = rebuiltTree(in);
		IHuffBaseNode rightnode = rebuiltTree(in);

		return new HuffInternalNode(leftnode, rightnode, 0);
	}

	// recursively go through the edges to get the node
	// read 1 in the edges go right, else go left
	private IHuffBaseNode TraAndGetHuffNode(IHuffBaseNode root, int ind, String edges)
	{
		if(ind == edges.length())
		{
			return root;
		}
		if(edges.charAt(ind) == '1')
		{
			return TraAndGetHuffNode(((HuffInternalNode)root).right(), ind + 1, edges);
		}
		return TraAndGetHuffNode(((HuffInternalNode)root).left(), ind + 1, edges);
	}
}
