package com.devrify.deployzerserver.common;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OperationUtil {

    public static <T,F> boolean innerJoinRightList(
            List<T> leftList,
            List<F> rightList,
            Function<F, T> keyExtractor) {
        if (CollectionUtils.isEmpty(leftList) || CollectionUtils.isEmpty(rightList)) {
            return false;
        }
        if (ObjectUtils.isEmpty(keyExtractor)) {
            return false;
        }
        Set<T> set = rightList.stream().map(keyExtractor).collect(Collectors.toSet());
        for (T t : leftList) {
            if (!set.contains(t)) {
                return false;
            }
        }
        return true;
    }

    public static <T,F> F hasDuplicateElement(List<T> list, Function<T,F> keyExtractor) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        if (ObjectUtils.isEmpty(keyExtractor)) {
            return null;
        }
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                F leftValue = keyExtractor.apply(list.get(i));
                F rightValue = keyExtractor.apply(list.get(j));
                if (leftValue.equals(rightValue)) {
                    return leftValue;
                }
            }
        }
        return null;
    }

    public static <T> T hasDuplicateElement(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (list.get(i).equals(list.get(j))) {
                    return list.get(i);
                }
            }
        }
        return null;
    }
}
