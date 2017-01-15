package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GeneralUtils {

	/**
	 * Sorts a HashMap by Value in Descending order
	 * @param map
	 * @return
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDesc(Map<K, V> map) {
	    return map.entrySet()
	              .stream()
	              .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
	              .collect(Collectors.toMap(
	                Map.Entry::getKey, 
	                Map.Entry::getValue, 
	                (e1, e2) -> e1, 
	                LinkedHashMap<K, V>::new
	              ));
	}
	
	/**
	 * Returns the powerset of the originalSet.  If the size parameter is  provided,
	 * it only returns the subsets of that size, otherwise the complete powerset
	 * is returned.
	 * 
	 * @param originalSet
	 * @param size
	 * @return
	 */
	public static HashSet<HashSet<String>> powerset(HashSet<String> originalSet, Integer size) {
		HashSet<HashSet<String>> sets = new HashSet<HashSet<String>>();
        if (originalSet.isEmpty()) {
        	sets.add(new HashSet<String>());
            return sets;
        }
        List<String> list = new ArrayList<String>(originalSet);
        String head = list.get(0);
        HashSet<String> rest = new HashSet<String>(list.subList(1, list.size()));
        for (HashSet<String> set : powerset(rest, null)) {
        	HashSet<String> newSet = new HashSet<String>();
            newSet.add(head);
            newSet.addAll(set);
            if (size != null) {
            	if (newSet.size() == size) {
            		sets.add(newSet);
            	}
            	if (set.size() == size) {
            		sets.add(set);
            	}
            }
            else {
            	sets.add(newSet);
	            sets.add(set);
            }
        }
        return sets;
	}
}