package actors;

import java.lang.Integer;
import java.lang.String;
import java.lang.System;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Iterator;
import java.util.TreeMap;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

import dto.ReduceData;

public class ToptagActor extends UntypedActor {

    public static int MAX_RESULT_SIZE = 5;

    private Map<String, Integer> finalReducedMap = new HashMap<String, Integer>();

    @Override
    public void preStart() {
        System.out.println("ToptagActor is running");
    }

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof String) {
            if (finalReducedMap.size() > 0)
                System.out.println("********* Current TOP tags on this site are : " + sortByValue(finalReducedMap, MAX_RESULT_SIZE));
        }
		else if (message instanceof Map) {
            System.out.println("********** ToptagActor adding new results to toptag.");
            Map<String, Integer> toptagPerFileMap = (Map<String, Integer>) message;
            aggregateInMemoryReduce(toptagPerFileMap);
		} else
			unhandled(message);
	}

    private void aggregateInMemoryReduce(Map<String, Integer> reducedList) {
        Integer count = null;
        for (String key : reducedList.keySet()) {
            if (finalReducedMap.containsKey(key)) {
                count = reducedList.get(key) + finalReducedMap.get(key);
                finalReducedMap.put(key, count);
            } else {
                finalReducedMap.put(key, reducedList.get(key));
            }
        }
    }


    /**
     * returns sorted and truncated map
     * @param map
     * @return
     */
    public static Map sortByValue(Map map, int maxSize) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                // ascending order
//                return ((Comparable) ((Map.Entry) (o1)).getValue())
//                        .compareTo(((Map.Entry) (o2)).getValue());
                // descending order
                return ((Comparable) ((Map.Entry) (o2)).getValue())
                        .compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        int i = 0;
        Map result = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            // limit the size of the return map
            if (i++ == maxSize) break;

            Map.Entry entry = (Map.Entry)it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

}
