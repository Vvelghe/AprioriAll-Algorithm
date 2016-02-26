package objects;

public class Candidate implements Comparable<Candidate>{
	
	private String sequence;
	private int support;
	
	public Candidate(String sequence){
		this.sequence=sequence;
		support=0;
	}
	
	public Candidate(char sequence){
		this.sequence=""+sequence;
		support=1;
	}
	
	public String getSequence() {
		return sequence;
	}
	
	public int getSupport() {
		return support;
	}
	
	public void increaseSupport(){
		support+=1;
	}

	public int compareTo(Candidate o) {
		return sequence.compareTo(o.sequence);
	}
	
	@Override
	public String toString() {
		return sequence+"("+support+")";
	}
}
