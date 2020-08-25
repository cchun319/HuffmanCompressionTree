import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class CharCounter implements ICharCounter, IHuffConstants{

	private Map<Integer, Integer> count = new HashMap<>();
	
	@Override
	public int getCount(int ch) {
		// TODO Auto-generated method stub
		return count.get(ch);
	}

	@Override
	public int countAll(InputStream stream) throws IOException {
		// TODO Auto-genera ted method stub
		if(stream == null)
		{
			throw new IOException("no input data");
		}
		int c = 0;
		int totalwords = 0;
		while((c = stream.read()) != -1)
		{
			add(c);
			totalwords++;
		}
		return totalwords;
	}

	@Override
	public void add(int i) {
		// TODO Auto-generated method stub
		if(count.containsKey(i))
		{
			set(i, this.count.get(i) + 1);
		}
		else
		{
			this.count.put(i, 1);
		}
	}

	@Override
	public void set(int i, int value) {
		// TODO Auto-generated method stub
		this.count.put(i, value);
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		for(Integer i : count.keySet())
		{
			set(i, 0);
		}
	}

	@Override
	public Map<Integer, Integer> getTable() {
		// TODO Auto-generated method stub
		return count;
	}
	
//	public void printCount()
//	{
//		int c = 0;
//		for(Integer i : count.keySet())
//		{
//			c = i;
//			System.out.println((char)c  + " | " + count.get(i) );
//		}
//	}
	

}
