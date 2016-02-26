package main;

/**
 * This is an implementation of the AprioriAll algorithm described in :
 * Agrawal R, Srikant R. "Mining Sequential Patterns", IBM Almaden Research Center, 1996
 * 
 * The AprioriAll algorithm finds all the frequents subsequences and their support in
 * a database of customer sequences
 * 
 * @author Victor Velghe
 * @version 26/02/16
 * 
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import objects.Candidate;
import objects.CustomerSequence;

public class AprioriAll {

	public static CustomerSequence [] allCS; 
	public static ArrayList<Integer> [] map;
	public static TreeSet<Candidate> [] seeds;

	public static int sizeMax;
	public static int nbrItems;
	public static int minSupport;

	public static long timeStart;

	public static void main(String[] args){
		aprioriAll("Instances/little.txt",50);
		getExecutionTime();
		printResults("Results/little.txt");
	}

	/**
	 * Print the execution time in System.out
	 * 
	 */
	public static void getExecutionTime(){
		System.out.println("Execution time : "+(System.currentTimeMillis()-timeStart)+" ms");
	}

	/**
	 * Print results on filepath
	 * 
	 */
	public static void printResults(String filepath){

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filepath));
			bw.write("Maximum size of a large sequence : "+sizeMax+"\n");
			bw.write("Number of customer sequence : "+allCS.length+"\n");
			bw.write("Number of different item : "+nbrItems+"\n");
			bw.write("Minimum support : "+minSupport+" ("+(minSupport/(double)allCS.length)*100+"%)\n");

			for(int i=1; i<seeds.length; i++){
				if(!seeds[i].isEmpty()){
					bw.write("["+i+"] ");
					for(Candidate c : seeds[i]){
						bw.write(c+" ");
					}
					bw.write("\n");
				}
			}

			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load into 	seeds[1] all large 1-sequences
	 * 				seeds[2] all large 2-sequences
	 * 				[...]
	 * 
	 * @param filepath : filepath of the customers sequences database
	 * @param minSupport : minimum support in % (0 to 100)
	 * 
	 */
	public static void aprioriAll(String filepath, int minsupport){

		timeStart = System.currentTimeMillis();

		loadCustomerSequences(filepath);

		minSupport=(int)((minsupport/(double)100)*allCS.length);

		load1Sequences();
		load2Sequences();
		computeSupport(2);
		pruneMinSupport(2);

		int size=2;
		while(aprioriGenerate(seeds[size],size)){
			size++;
			computeSupport(size);
			pruneMinSupport(size);
		}
	}

	/**
	 * Load the customers sequences database
	 * 
	 * @param filepath : filepath of the customer sequences database
	 * 
	 * The file need to be structured :
	 * First line : #sequences sizeMax #differentitems
	 * Customer sequence : <{.}{.,.,.}{.,.}{.}>
	 * 
	 * A item must be a single character
	 * 
	 * For instance :
	 * 
	 * 5 4 5
	 * <{1,5}{2}{3}{4}>
	 * <{1}{3}{4}{3,5}>
	 * <{1}{2}{3}{4}>
	 * <{1}{3}{5}>
	 * <{4}{5}>
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static void loadCustomerSequences(String filepath){
		try {
			BufferedReader br = new BufferedReader(new FileReader(filepath));
			String line=br.readLine();
			allCS = new CustomerSequence[Integer.parseInt(line.split(" ")[0])];
			sizeMax = Integer.parseInt(line.split(" ")[1]);
			nbrItems = Integer.parseInt(line.split(" ")[2]);
			map = new ArrayList[sizeMax+1];

			for(int i=1; i<map.length; i++){
				map[i]=new ArrayList<Integer>();
			}

			int index=0;
			line=br.readLine();
			while(line!=null){
				CustomerSequence cs = new CustomerSequence(line);
				allCS[index]=cs;
				map[cs.getSize()].add(index);
				index++;
				line=br.readLine();
			}
			br.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load into seeds[1] all large 1-sequence
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static void load1Sequences(){

		// Initialize array of large sequences
		seeds=new TreeSet[sizeMax+1];
		for(int i=1; i<seeds.length; i++){
			seeds[i]=new TreeSet<Candidate>();
		}

		for(CustomerSequence cs : allCS){
			// Get all differents items of a customer sequence
			TreeSet<Candidate> candidates = new TreeSet<Candidate>();
			for(String s : cs.getSequences()){
				for(int i=0;i<s.length(); i++){
					candidates.add(new Candidate(s.charAt(i)));
				}
			}

			// Put them in seeds[1] (all large 1-sequence)
			for(Candidate c : candidates){
				if(seeds[1].contains(c)){
					seeds[1].ceiling(c).increaseSupport();
				}
				else seeds[1].add(c);
			}
		}
	}

	/**
	 * Remove from seeds[size] all candidate with a support < minSupport
	 * 
	 */
	public static void pruneMinSupport(int size){

		TreeSet<Candidate> toEliminate = new TreeSet<Candidate>();

		for(Candidate c : seeds[size]){
			if(c.getSupport()<minSupport){
				toEliminate.add(c);
			}
		}

		seeds[size].removeAll(toEliminate);
	}

	/**
	 * Load into seeds[2] all candidates for a large 2-sequence
	 * A candidate is a sequence which we have not yet verified its support
	 * 
	 */
	public static void load2Sequences(){
		for(Candidate h : seeds[1]){
			for(Candidate t : seeds[1]){
				if(!h.equals(t)){
					seeds[2].add(new Candidate(h.getSequence()+t.getSequence()));
				}
			}
		}
	}

	/**
	 * Compute support for all candidates in seeds[size]
	 * 
	 */
	public static void computeSupport(int size){
		for(int i=size; i<map.length; i++){
			for(Integer y : map[i]){
				CustomerSequence cs = allCS[y];
				for(Candidate c : seeds[size]){
					if(contained(c.getSequence(),cs)){
						c.increaseSupport();
					}
				}
			}
		}
	}

	/**
	 * @return true if sequence can be contained is cs, false otherwise
	 * 
	 * For instance :
	 * Sequence (12) can be contained in customer sequence <{1}{2}{3,4}>
	 * Sequence (123) can be contained in customer sequence <{1}{2}{3,4}>
	 * Sequence (34) can't be contained in customer sequence <{1}{2}{3,4}>
	 * 
	 */
	public static boolean contained(String sequence, CustomerSequence cs){
		int index=0;
		for(int i=0; i<sequence.length();i++){
			if(index==cs.getSize()) return false;
			char s=sequence.charAt(i);
			while(cs.getSequences()[index].indexOf(s)==-1){
				index++;
				if(index==cs.getSize()) return false;
			}
			index++;
		}
		return true;
	}

	/**
	 * Generate all candidates for large (size+1)-sequences based on large size-sequences (lsequences)
	 * 
	 * First, we join lsequences with lsequences ;
	 * 
	 * 	For instance : lsequences = {(123),(124),(125),(234),(235),(345)}
	 * 
	 * 	(123),(124) and (125) have the same subsequence (12) so we isolate (3),(4) and (5) 
	 * 	and we generate all possible tuples (34),(35),(43),(45),(53),(54)
	 * 	then we concatenate them with the subset. We obtain :
	 * 	(1234),(1235),(1243),(1245),(1253),(1254)
	 * 
	 * 	We can do the same with (234) and (235) with the subset (23). We obtain :
	 * 	(2345),(2354)
	 * 
	 * Secondly, we prune candidates such that some (size-1)-subsequence is not in lsequences
	 * 
	 * 	For instance : candidates = {(1234),(1235),(1243),(1245),(1253),(1254),(2345),(2354)}
	 * 
	 * 	(234) is in lsequences so we keep (1234)
	 * 	(235) is in lsequences so we keep (1235)
	 * 	(245) is not in lsquences so we delete (1245)
	 * 	(253) is not in lsequences so we delete (1253)
	 * 	...
	 * 
	 * 	Finaly, we obtain : {(1234),(1235),(2345)}
	 * 
	 * @return true if large (size+1)-sequences is non empty, false otherwise
	 */
	public static boolean aprioriGenerate(TreeSet<Candidate> lsequences, int size){

		TreeSet<Candidate> newSequences = new TreeSet<Candidate>();

		if(lsequences.size()<=1) return false;

		Iterator<Candidate> iterator = lsequences.iterator();
		boolean flag=true;

		/******** JOIN ********/
		String current = iterator.next().getSequence();
		String next = current;
		while(iterator.hasNext()){
			ArrayList<Character> toAdd = new ArrayList<Character>();
			while(current.substring(0, size-1).equals(next.substring(0, size-1))&& flag){
				toAdd.add(next.charAt(size-1));
				if (iterator.hasNext()) next=iterator.next().getSequence();
				else flag=false;
			}

			if(toAdd.size()>1){
				for(int i=0; i<toAdd.size(); i++){
					Character c = toAdd.get(i);
					for(int j=0; j<toAdd.size(); j++){
						if(i!=j){
							newSequences.add(new Candidate(current.substring(0, size-1)+c+toAdd.get(j)));
						}
					}
				}
			}

			current=next;
		}

		/******** PRUNING ********/
		TreeSet<Candidate> eliminated=new TreeSet<Candidate>();

		for(Candidate s : newSequences){
			if(!lsequences.contains(new Candidate(s.getSequence().substring(1)))){
				eliminated.add(s);
			}
		}

		newSequences.removeAll(eliminated);

		if(newSequences.isEmpty()) return false;

		seeds[size+1]=newSequences;
		return true;
	}
}
