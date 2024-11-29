package com.example.web.service;

import com.example.base.entities.NewPath;

import java.util.*;
import java.util.stream.Collectors;

public class test {
    public static void main(String[] args) {
        ArrayList<NewPath> newPaths = new ArrayList<>();
        newPaths.add(new NewPath(Arrays.asList(new Integer[]{1,2,3,4}), "1ed2e131qewqe", 1734295234));
        newPaths.add(new NewPath(Arrays.asList(new Integer[]{1,2,3,4}), "f231wsdefwih2", 1734295236));
        newPaths.add(new NewPath(Arrays.asList(new Integer[]{1,4,3,2}), "1ed2e131qewqe", 1734295299));
        newPaths.add(new NewPath(Arrays.asList(new Integer[]{1,4,3,2}), "1ed2e131qewqe", 1734295299));
        newPaths.add(new NewPath(Arrays.asList(new Integer[]{1,4,3,2}), "1ed2e131qewqk", 1734295299));
        newPaths.add(new NewPath(Arrays.asList(new Integer[]{1,4,3,2}), "1ed2e131qewqk", 1734295299));
        Map<String, List<NewPath>> groupNewPath =
                newPaths.stream().collect(Collectors.groupingBy(NewPath::getFuzzerAddress));

        Comparator<List<NewPath>> comparator = new Comparator<List<NewPath>>() {
            @Override
            public int compare(List<NewPath> l1, List<NewPath> l2) {
                return Integer.compare(l2.size(), l1.size());
            }
        };
        LinkedHashMap<String, List<NewPath>> sortedmap = groupNewPath.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(comparator))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1,e2)->e1, LinkedHashMap::new));
        sortedmap.forEach((key, value) -> {
            System.out.println(key + ":" + value);
        });
    }
}
