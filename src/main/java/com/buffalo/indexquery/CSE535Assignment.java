import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/*
 * @author nshekhar
 * @uid 50169106
 */

public class CSE535Assignment {

	private static FileWriter logFileWriter ;
	private static BufferedWriter bufferedLogFileWriter;

	public static void main(String[] args) {
		
		String inputIndexFileName = args[0];
		String queryFileName = args[3];
		String outputLogFileName = args[1];
		Long numberOfTerms = Long.parseLong(args[2]);
		
		HashMap<String, LinkedList<String>> indexByIncreasingDocId = new HashMap<String, LinkedList<String>>();
		HashMap<String, LinkedList<String>> indexByDecreasingTermFrequency = new HashMap<String, LinkedList<String>>();
		TreeMap<String,Long> topIndex = new TreeMap<String, Long>();
		BufferedReader inputIndexReader = null;
		BufferedReader inputTermFile = null;
		
		try{
			inputIndexReader = new BufferedReader(new FileReader(inputIndexFileName));
			inputTermFile = new BufferedReader(new FileReader(queryFileName));
			
			File outputLogFile = new File(outputLogFileName);
			if (!outputLogFile.exists()) {
				outputLogFile.createNewFile();
			}
			logFileWriter = new FileWriter(outputLogFile.getAbsoluteFile());
			bufferedLogFileWriter = new BufferedWriter(logFileWriter);
			String postingRead;
			LinkedList<String> postingByIncreasingDocId;
			LinkedList<String> postingByDecreasingTermFrequency;
			while((postingRead = inputIndexReader.readLine())!=null){
	
				//Create the postings in increasing document id
				String[] splitPosting = postingRead.split("\\\\");
				String term = splitPosting[0];
				String postingSize = splitPosting[1];
				String postings = splitPosting[2];
				Integer lengthOfPosting = postings.substring(2).length();
				String cleanedPostingList = postings.substring(2).substring(0, lengthOfPosting-1);
				
				String[] cleanedPostingListSplit = cleanedPostingList.split(",");
				
				//Documents sorted by document id
				TreeMap<Long,Long> sortedPostingsByDocumentID = new TreeMap<Long, Long>();
				
				//Documents sorted by decreasing term frequency
				Map<Long,Long> sortedPostingsByDecreasingTermFrequency;
				
				for(int i=0 ; i <= cleanedPostingListSplit.length-1 ; i++){
					String[] postingElements = cleanedPostingListSplit[i].split("/");
					Long docId = Long.parseLong(postingElements[0].trim());
					Long frequency = Long.parseLong(postingElements[1].trim());
					sortedPostingsByDocumentID.put(docId, frequency);
				}
				
				//Create the linked list posting by using the tree map sorted by document id
				postingByIncreasingDocId = new LinkedList<String>();
				for(Long key : sortedPostingsByDocumentID.keySet()){
					String onePostingElement = key+"/"+sortedPostingsByDocumentID.get(key);
					postingByIncreasingDocId.add(onePostingElement);
				}
				
				//Create the linked list posting by using the tree map decreasing term frequency
				sortedPostingsByDecreasingTermFrequency = sortByValues(sortedPostingsByDocumentID,-1);			
				Set<Entry<Long, Long>> sortedPostingsByDecreasingTermFrequencySet = sortedPostingsByDecreasingTermFrequency.entrySet();
				 
			    // Get an iterator
			    Iterator<Entry<Long, Long>> sortedPostingsByDecreasingTermFrequencyIterator = sortedPostingsByDecreasingTermFrequencySet.iterator();
			    postingByDecreasingTermFrequency = new LinkedList<String>();
			    while(sortedPostingsByDecreasingTermFrequencyIterator.hasNext()) {
			      Map.Entry<Long,Long> mapEntry = (Map.Entry<Long,Long>)sortedPostingsByDecreasingTermFrequencyIterator.next();
			      String onePostingElement = mapEntry.getKey()+"/"+mapEntry.getValue();
			      postingByDecreasingTermFrequency.add(onePostingElement);
			    }
		
			    //Posting size linked list to add it to the ArrayList
			    LinkedList<String> lengthOfPostingList = new LinkedList<String>();
			    lengthOfPostingList.add(postingSize.toString().substring(1));
			    
				//Create the index by adding the sorted document id to the term
				indexByIncreasingDocId.put(term, postingByIncreasingDocId);
				
				//Create the index by adding the sorted term frequency to the term
				indexByDecreasingTermFrequency.put(term, postingByDecreasingTermFrequency);
				
				//Create an index with terms and size of documents sorted in descending order
				topIndex.put(term, Long.parseLong(postingSize.toString().substring(1)));

			}
		}catch(IOException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		//Fetch the first K elements from the "topIndex"  and return just the terms
		ArrayList<String> topKTerms = getTopKTerms(topIndex, numberOfTerms);
		StringBuffer terms = new StringBuffer();
		for(String term : topKTerms){
			terms.append(term);
			terms.append(",");
		}
		if(terms.length()>0){
			terms.setLength(terms.length()-1);
		}

		//Write top K terms into the log file
		try {
			bufferedLogFileWriter.write("FUNCTION: getTopK 10\n");
			bufferedLogFileWriter.write("Result: "+terms+"\n");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		//Read the query file and call methods accordingly
		String termsInALine;
		String individualTerms[];
		try {
			while((termsInALine = inputTermFile.readLine())!=null){
				ArrayList<String> searchTerms = new ArrayList<String>();
				individualTerms = termsInALine.split(" ");
				for(String term: individualTerms){
					//getPostings() - Given one query term it will return an arrayList of docID, first element  ordered by docID and second by decreasing TF
					getPostings(term,indexByIncreasingDocId,indexByDecreasingTermFrequency);
					searchTerms.add(term);
				}
				//termAtATimeQueryAnd - Given two or more query terms finds teh intersection of the documents
				termAtATimeQueryAnd(searchTerms,indexByDecreasingTermFrequency,topIndex);
				
				//termAtATimeQueryOr
				termAtATimeQueryOr(searchTerms,indexByDecreasingTermFrequency,topIndex);
				
				//Document at a time AND
				docAtATimeQueryAnd(searchTerms,indexByIncreasingDocId);
				
				//Document at a time OR
				docAtATimeQueryOr(searchTerms,indexByIncreasingDocId);
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				inputIndexReader.close();
				bufferedLogFileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		
	}
	
	private static void docAtATimeQueryOr(ArrayList<String> searchTerms,HashMap<String, LinkedList<String>> indexByIncreasingDocId) {
		StringBuffer termsList = new StringBuffer();
		long startTime = System.currentTimeMillis();
		long numberOfComparisions = 0L;
		LinkedList<String> resultSet = new LinkedList<String>();
		ArrayList<LinkedList<String>> arrayOfPostings = new ArrayList<LinkedList<String>>();
		for(String term : searchTerms){
			if(indexByIncreasingDocId.get(term)!=null){
				arrayOfPostings.add(indexByIncreasingDocId.get(term));
			}
			termsList.append(term);
			termsList.append(",");
		}
		if(termsList.length() > 0){
			 termsList.setLength(termsList.length()-1);
		}
		Integer size = arrayOfPostings.size();
		int [] arrayOfPointers  = new int[size];
		int [] arrayOfSizes  = new int[size];
		for(int i = 0 ; i < arrayOfPostings.size() ; i++){
			arrayOfSizes[i] = arrayOfPostings.get(i).size();
			arrayOfPointers[i] = 0;
		}
		
		//Check if end of list reached for each
		boolean endOfAllPostings = false;
		
		while (endOfAllPostings == false) {
			int c = 0;
			for (int z = 0; z < size; z++) {
				if (arrayOfPointers[z] == arrayOfSizes[z]) {
					c++;
				}
			}
			
			if(c == size){
				endOfAllPostings = true;
			}
		
			// Perform checks until end of list reached
			if (endOfAllPostings == false) {
				int j=0;	
				while( j < size){
					if(arrayOfPointers[j] < arrayOfSizes[j]){
					String element = arrayOfPostings.get(j).get(arrayOfPointers[j]).split("/")[0];
					boolean flag = true;
					numberOfComparisions++;	
					for(int i = 0 ; i < resultSet.size() ; i++){	
					if (Long.parseLong(element) == Long.parseLong(resultSet.get(i))){
						flag = false;
						arrayOfPointers[j]++;
						}
					}
					if(flag == true){
						resultSet.add(arrayOfPostings.get(j).get(arrayOfPointers[j]).split("/")[0]);
						arrayOfPointers[j]++;
					}
				}
				j++;
			}		
		 }
		}

		StringBuffer documentIds = new StringBuffer();
		for (int kk = 0; kk < resultSet.size(); kk++) {
			documentIds.append(resultSet.get(kk));
			documentIds.append(",");
		}
		if(documentIds.length() > 0){
			documentIds.setLength(documentIds.length()-1);
		}
		
		long endTime = System.currentTimeMillis();
		long timeTakenInSeconds = (endTime - startTime)/1000; 
		int documentsFound = resultSet.size();
		
		try {
			bufferedLogFileWriter.write("FUNCTION: docAtATimeQueryOr "+termsList+"\n");
			bufferedLogFileWriter.write(documentsFound+" documents are found"+"\n");
			bufferedLogFileWriter.write(numberOfComparisions+" comparisions are made"+"\n");
			bufferedLogFileWriter.write(timeTakenInSeconds+" seconds are used"+"\n");
			bufferedLogFileWriter.write("Result: "+documentIds+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static void docAtATimeQueryAnd(ArrayList<String> searchTerms,HashMap<String, LinkedList<String>> indexByIncreasingDocId) {
		long startTime = System.currentTimeMillis();
		long numberOfComparisions = 0L;
		LinkedList<String> resultSet = new LinkedList<String>();
		ArrayList<LinkedList<String>> arrayOfPostings = new ArrayList<LinkedList<String>>();
		StringBuffer termsList = new StringBuffer();
		for(String term : searchTerms){
			if(indexByIncreasingDocId.get(term)!=null){
				arrayOfPostings.add(indexByIncreasingDocId.get(term));
			}
			termsList.append(term);
			termsList.append(",");
		}
		if(termsList.length() > 0){
			termsList.setLength(termsList.length()-1);
		}

		Integer size = arrayOfPostings.size();
		int [] arrayOfPointers  = new int[size];
		int [] arrayOfSizes  = new int[size];
		for(int i = 0 ; i < arrayOfPostings.size() ; i++){
			arrayOfSizes[i] = arrayOfPostings.get(i).size();
			arrayOfPointers[i] = 0;
		}
		
		//Check if end of list reached for each
		boolean endOfAllPostings = false;
		boolean oneElement = false;
		int c = 0;
		Integer elementIndex = 0;
		Integer rowIndex = 0;
		while (endOfAllPostings == false) {
			for (int z = 0; z < size; z++) {
				if (arrayOfPointers[z] == arrayOfSizes[z]) {
					c++;
				}
			}
			
			if(c == size){
				endOfAllPostings = true;
			}
			
			// Perform checks until end of list reached
			if (endOfAllPostings == false) {
						boolean flag = true;
						int minimaIndex = 0;
						int x = 0;
						int numberOfMatches = 0;
						if(size - 1 == 0){
							oneElement = true;
							int kk=0;
							numberOfComparisions++;
							while(kk < arrayOfPostings.get(0).size()){
								resultSet.add(arrayOfPostings.get(0).get(kk).split("/")[0]);
								kk++;
							}
							
						}
						for (int j = 0; j < size - 1; j++) {
							while( j < size -1){
								if(arrayOfPointers[j] < arrayOfSizes[j] && arrayOfPointers[j+1] < arrayOfSizes[j+1]){
									x = 1;
									numberOfComparisions++;
									if (Long.parseLong(arrayOfPostings.get(j)
											.get(arrayOfPointers[j]).split("/")[0]) == Long
											.parseLong(arrayOfPostings.get(j + 1)
													.get(arrayOfPointers[j+1]).split("/")[0])) {//Happening coz second ist is smaller than the firs
										elementIndex = j;
										rowIndex = arrayOfPointers[j];
										numberOfMatches++;
									} else if (Long.parseLong(arrayOfPostings.get(j)
											.get(arrayOfPointers[j]).split("/")[0]) < Long
											.parseLong(arrayOfPostings.get(j + 1)
													.get(arrayOfPointers[j+1]).split("/")[0])) {
										//numberOfComparisions++;
										minimaIndex = j;
										flag = false;
										x = 1;
									} else {
										minimaIndex = j + 1;
										flag = false;
										x=1;
									}
								}
								j++;
							}
						}
						
						//If all elements match, add it to the result and then increment all the index pointers by 1
						if (flag == true && numberOfMatches == size-1) {
							resultSet.add(arrayOfPostings.get(elementIndex).get(rowIndex).split("/")[0]);
							for (int k = 0; k < size; k++) {
								arrayOfPointers[k]++;
							}
						} else {
							// Find the first minimum and increment its pointer
							int cd = arrayOfPointers[minimaIndex]++;
						}

						if(x == 0){
							break;
						}
			}
		}
		StringBuffer documentIds = new StringBuffer();
		for (int kk = 0; kk < resultSet.size(); kk++) {
			documentIds.append(resultSet.get(kk));
			documentIds.append(",");
		}
		if(documentIds.length() > 0){
			documentIds.setLength(documentIds.length()-1);
		}
		
		long endTime = System.currentTimeMillis();
		long timeTakenInSeconds = (endTime - startTime)/1000; 
		int documentsFound = 0;
		if(oneElement==true){
			documentsFound = resultSet.size()-1;
		}else{
			documentsFound = resultSet.size();
		}

		try {
			bufferedLogFileWriter.write("FUNCTION: docAtATimeQueryAnd "+termsList+"\n");
			bufferedLogFileWriter.write(documentsFound+" documents are found"+"\n");
			bufferedLogFileWriter.write(numberOfComparisions+" comparisions are made"+"\n");
			bufferedLogFileWriter.write(timeTakenInSeconds+" seconds are used"+"\n");
			bufferedLogFileWriter.write("Result: "+documentIds+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	private static void termAtATimeQueryOr(ArrayList<String> searchTerms,HashMap<String, LinkedList<String>> indexByDecreasingTermFrequency, TreeMap<java.lang.String, Long> topIndex) {
		long startTime = System.currentTimeMillis();
		//Order the postings in which the terms appear ASCENDING
		Long size;
		StringBuffer termsList = new StringBuffer();
		TreeMap<String,Long> sortedByPostingSize = new TreeMap<String, Long>();
		for(String term: searchTerms){
			size = topIndex.get(term);
			if(size!=null){
				sortedByPostingSize.put(term, size);
			}
			termsList.append(term);
			termsList.append(",");
		}
		if(termsList.length() > 0){
			termsList.setLength(termsList.length()-1);
		}
	
		Map<String,Long> sortedByPostingSizeMap = sortByValues(sortedByPostingSize, 1);
		//Now first element has the shortest posting list
		//Iterate through the above sorted map on value  and create Linked hashmap in the same order as in the sorted map "sortedByPostingSizeMap"
		//Linked hashmap preserves ordering
		LinkedHashMap<String,LinkedList<String>>  mapOfTermAndPostingInAscendingSize = new LinkedHashMap<String, LinkedList<String>>();
		for(String terms : sortedByPostingSizeMap.keySet()){
			//Getting the postings : indexByDecreasingTermFrequency.get(terms)
			mapOfTermAndPostingInAscendingSize.put(terms, indexByDecreasingTermFrequency.get(terms));
		}
		
		//Iterate through the mapOfTermAndPostingInAscendingSize to find the union
		TreeMap<String,String> orDocId = new TreeMap<String, String>();
		//ArrayList<String> resultSet = new ArrayList<String>();
		Entry<String, LinkedList<String>> shortestPostingElement = mapOfTermAndPostingInAscendingSize.entrySet().iterator().next();
		
		String shortestPostingTerm = shortestPostingElement.getKey();
		LinkedList<String> shortestPosting = shortestPostingElement.getValue();
		//Remove the first element as we are not going to compare it with itself again while calculating the intersection
		mapOfTermAndPostingInAscendingSize.remove(shortestPostingTerm);
		//Initiate the result set with the shortest posting elements
		for(String docId : shortestPosting){
			orDocId.put(docId.split("/")[0],null);//Since it is the form 9714/2 docID/TF
		}
		
		int numberOfComparisionsWithOptimization = 0;
		//If the document present in the other postings also, keep them or else remove
		for(String term: mapOfTermAndPostingInAscendingSize.keySet()){
			ArrayList<String> newDocId = new ArrayList<String>();
			for(String resultDocId : orDocId.keySet()){
				//Iterate through the current posting list to check if has the elements already in result set
				boolean flag = false;
				for(String currentPostingListElement : mapOfTermAndPostingInAscendingSize.get(term)){
					String docId = currentPostingListElement.split("/")[0];
					numberOfComparisionsWithOptimization++;
					if(!resultDocId.equals(docId)){
//						flag=true;
						newDocId.add(docId);
						//orDocId.put(docId, null);
					}
				}
			}
			//add the new ones to the result
			for(int i = 0 ; i < newDocId.size() ;i++){
				orDocId.put(newDocId.get(i), null);
			}
		}
		long endTime = System.currentTimeMillis();
		long timeTakenInSeconds = (endTime - startTime)/1000; 
		int documentsFound = orDocId.size();
		if(orDocId.size() == 0){
			System.out.println("Terms not found");
		}

		StringBuffer documentIds = new StringBuffer();
		TreeMap<Long,String> intersectDocIdSorted = new TreeMap<Long, String>();
		for(String s: orDocId.keySet()){
			intersectDocIdSorted.put(Long.parseLong(s), null);
		}
		for(Long s: intersectDocIdSorted.keySet()){
			documentIds.append(s);
			documentIds.append(",");
		}
		if(documentIds.length() > 0){
			documentIds.setLength(documentIds.length()-1);
		}
		//writeToTheFile()
		try {
			bufferedLogFileWriter.write("FUNCTION: termAtATimeQueryOr "+termsList+"\n");
			bufferedLogFileWriter.write(documentsFound+" documents are found"+"\n");
			bufferedLogFileWriter.write(numberOfComparisionsWithOptimization+" comparisions are made"+"\n");
			bufferedLogFileWriter.write(timeTakenInSeconds+" seconds are used"+"\n");
			bufferedLogFileWriter.write("Result: "+documentIds+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	private static void termAtATimeQueryAnd(ArrayList<String> searchTerms,HashMap<String, LinkedList<String>> indexByDecreasingTermFrequency, TreeMap<String, Long> topIndex) {
		
		long startTime = System.currentTimeMillis();
		StringBuffer termsList = new StringBuffer();
		//Order the postings in which the terms appear ASCENDING
		Long size;
		TreeMap<String,Long> sortedByPostingSize = new TreeMap<String, Long>();
		for(String term: searchTerms){
			termsList.append(term);
			termsList.append(",");
			size = topIndex.get(term);
			if(size!=null){
				sortedByPostingSize.put(term, size);
			}
		}
		if(termsList.length() > 0){
			termsList.setLength(termsList.length()-1);
		}
		Map<String,Long> sortedByPostingSizeMap = sortByValues(sortedByPostingSize, 1);
		//Now first element has the shortest posting list
		//Iterate through the above sorted map on value  and create Linked hashmap in the same order as in the sorted map "sortedByPostingSizeMap"
		//Linked hashmap preserves ordering
		LinkedHashMap<String,LinkedList<String>>  mapOfTermAndPostingInAscendingSize = new LinkedHashMap<String, LinkedList<String>>();
		for(String terms : sortedByPostingSizeMap.keySet()){
			//Getting the postings : indexByDecreasingTermFrequency.get(terms)
			mapOfTermAndPostingInAscendingSize.put(terms, indexByDecreasingTermFrequency.get(terms));
		}
		
		//Iterate through the mapOfTermAndPostingInAscendingSize to find the intersection
		TreeMap<String,String> intersectDocId = new TreeMap<String, String>();
		//ArrayList<String> resultSet = new ArrayList<String>();
		Entry<String, LinkedList<String>> shortestPostingElement = mapOfTermAndPostingInAscendingSize.entrySet().iterator().next();
		
		String shortestPostingTerm = shortestPostingElement.getKey();
		LinkedList<String> shortestPosting = shortestPostingElement.getValue();
		//Remove the first element as we are not going to compare it with itself again while calculating the intersection
		mapOfTermAndPostingInAscendingSize.remove(shortestPostingTerm);
		//Initiate the result set with the shortest posting elements
		for(String docId : shortestPosting){
			intersectDocId.put(docId.split("/")[0],null);//Since it is the form 9714/2 docID/TF
		}
		
		int numberOfComparisionsWithOptimization = 0;
		//If the document present in the other postings also, keep them or else remove
		for(String term: mapOfTermAndPostingInAscendingSize.keySet()){
			ArrayList<String> newDocIdToBeRemoved = new ArrayList<String>();
			for(String resultDocId : intersectDocId.keySet()){
				//Iterate through the current posting list to check if has the elements already in result set
				boolean flag = false;
				for(String currentPostingListElement : mapOfTermAndPostingInAscendingSize.get(term)){
					String docId = currentPostingListElement.split("/")[0];
					numberOfComparisionsWithOptimization++;
					if(resultDocId.equals(docId)){
						flag=true;
					}
				}
				if(flag==false){
					newDocIdToBeRemoved.add(resultDocId);
					//intersectDocId.remove(resultDocId);
				}
			}
			for(int i = 0 ; i < newDocIdToBeRemoved.size() ; i++){
				intersectDocId.remove(newDocIdToBeRemoved.get(i));
			}
		}
		long endTime = System.currentTimeMillis();
		long timeTakenInSeconds = (endTime - startTime)/1000; 
		int documentsFound = intersectDocId.size();
		if(intersectDocId.size() == 0){
			System.out.println("Terms not found");
		}
		
		//intersectDocId.descendingKeySet()
		StringBuffer documentIds = new StringBuffer();
		TreeMap<Long,String> intersectDocIdSorted = new TreeMap<Long, String>();
		for(String s: intersectDocId.keySet()){
			intersectDocIdSorted.put(Long.parseLong(s), null);
		}
		for(Long s: intersectDocIdSorted.keySet()){
			documentIds.append(s);
			documentIds.append(",");
		}
		if(documentIds.length()>0){
			documentIds.setLength(documentIds.length()-1);
		}
		
		//write to the log file
		try {
			bufferedLogFileWriter.write("FUNCTION: termAtATimeQueryAnd "+termsList+"\n");
			bufferedLogFileWriter.write(documentsFound+" documents are found"+"\n");
			bufferedLogFileWriter.write(numberOfComparisionsWithOptimization+" comparisions are made"+"\n");
			bufferedLogFileWriter.write(timeTakenInSeconds+" seconds are used"+"\n");
			bufferedLogFileWriter.write("Result: "+documentIds+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//return intersectDocId;
		
	}

	private static void getPostings(String searchTerm,HashMap<String, LinkedList<String>> indexByIncreasingDocId,HashMap<String, LinkedList<String>> indexByDecreasingTermFrequency) {
		LinkedList<String> postingsByIncreasingDocId = null;
		LinkedList<String> postingsByDecreasingTermFrequency = null;
		
		boolean flag1 = false;
		boolean flag2 = false;
		
		StringBuffer orderedByDocId = new StringBuffer();
		StringBuffer orderedByFrequency = new StringBuffer();
		
		if(indexByIncreasingDocId.containsKey(searchTerm)){
			flag1 = true;
			postingsByIncreasingDocId = indexByIncreasingDocId.get(searchTerm);
		}else{
			System.out.println("Term not found "+searchTerm);
		}
		if(indexByDecreasingTermFrequency.containsKey(searchTerm)){
			flag2 = true;
			postingsByDecreasingTermFrequency = indexByDecreasingTermFrequency.get(searchTerm);
		}
		
		if(flag1 == true){
			for(int i = 0; i < postingsByIncreasingDocId.size() ;i++){
				orderedByDocId.append(postingsByIncreasingDocId.get(i).split("/")[0]);
				orderedByDocId.append(",");
			}
			if(orderedByDocId.length()>0){
				orderedByDocId.setLength(orderedByDocId.length()-1);
			}
		}
		if(flag2 == true){
			for(int i = 0; i < postingsByDecreasingTermFrequency.size() ;i++){
				orderedByFrequency.append(postingsByDecreasingTermFrequency.get(i).split("/")[0]);
				orderedByFrequency.append(",");
			}
			if(orderedByFrequency.length()>0){
				orderedByFrequency.setLength(orderedByFrequency.length()-1);
			}
		}

		//Write to the log file
		try {
			bufferedLogFileWriter.write("FUNCTION: getPostings "+searchTerm+"\n");
			bufferedLogFileWriter.write("Ordered by doc IDs: "+orderedByDocId+"\n");
			bufferedLogFileWriter.write("Ordered by TF: "+orderedByFrequency+"\n");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static ArrayList<String> getTopKTerms(TreeMap<java.lang.String, Long> topIndex, Long numberOfTerms) {
		Map<String,Long> topIndexSortedDescending = sortByValues(topIndex,-1);
		ArrayList<String> topKTerms = new ArrayList<String>();
		int counter = 0;
		for(String term : topIndexSortedDescending.keySet()){
			if(counter < numberOfTerms){
				topKTerms.add(term);
				counter++;
			}
		}
		return topKTerms;
	}

	//Pass n as 1 for ascending sort and -1 for descending sort
	public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map, final Integer n) {
		Comparator<K> valueComparator = new Comparator<K>() {
			public int compare(K k1, K k2) {
				int compare = n* (map.get(k1).compareTo(map.get(k2)));
				if (compare == 0)
					return 1;
				else
					return compare;
			}
		};
		Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
		sortedByValues.putAll(map);
		return sortedByValues;
	}
	
}


