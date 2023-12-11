package com.devrify.deployzerserver.common;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OperationUtil {

    public static <T, F> boolean innerJoinRightList(
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
}
