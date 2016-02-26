package tests;

/**
 * Class to generate synthetic customer sequences
 * 
 * nbrTransaction : Maximal number of transactions by customer
 * nbrItemTransaction : Maximal number of items by transaction
 * nbrItem : Number of different item 
 * nbrCustomerSequence : Number of customer sequence
 * 
 * Each item is a single character
 * 
 */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class InstanceGenerator {

	public static int nbrTransaction=20;
	public static int nbrItemTransaction=5;
	public static int nbrItem=100;
	public static int nbrCustomerSequence=10000;

	public static void main(String [] args){
		generateInstance("Instances/big.txt");
		System.out.println("Done");
	}

	public static void generateInstance(String filename){
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			bw.write(nbrCustomerSequence+" "+nbrTransaction+" "+nbrItem+"\n");
			for(int i=0; i<nbrCustomerSequence; i++){
				bw.write(generateCustomerSequence()+"\n");
			}
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String generateCustomerSequence(){
		String res="<";
		int sequencesize = (int) (Math.random()*nbrTransaction)+1;
		for(int i=0; i<sequencesize; i++){
			int littlesize = (int) (Math.random()*nbrItemTransaction)+1;
			String littlesequence="{";
			ArrayList<Character> array = getArrayofCharacter();
			for(int y=0; y<littlesize; y++){
				Character s = getRandomCharacter(array);
				array.remove(s);
				littlesequence+=s+",";
			}
			littlesequence=littlesequence.substring(0, littlesequence.length()-1);
			littlesequence+="}";
			res+=littlesequence;
		}

		return res+">";
	}

	public static ArrayList<Character> getArrayofCharacter(){
		ArrayList<Character> al = new ArrayList<Character>(nbrItem);
		for(int i=500; i<500+nbrItem; i++){
			al.add((char)i);
		}
		return al;
	}

	public static Character getRandomCharacter(ArrayList<Character> al){
		int index = (int) (Math.random()*al.size());
		return al.get(index);
	}
}
