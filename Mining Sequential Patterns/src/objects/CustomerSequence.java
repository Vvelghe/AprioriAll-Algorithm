package objects;

public class CustomerSequence {

	private String[] sequences;
	private int size;

	public CustomerSequence(String data){
		data=data.substring(2, data.length()-2);
		sequences=data.split("\\}\\{");
		
		for(int i=0; i<sequences.length; i++){
			sequences[i]=sequences[i].replace(",","");
		}
		
		size=sequences.length;
	}
	
	public String[] getSequences() {
		return sequences;
	}
	
	public int getSize() {
		return size;
	}
	
	@Override
	public String toString() {
		String output="";
		for(String s : sequences){
			output+="{"+s+"}";
		}
		return output;
	}
}
