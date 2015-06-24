package com.meituan.firefly.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ponyets on 15/6/10.
 */
public final class Maps {
    private Maps() {
    }

    public static <K, V> Map<K, V> asMap(Map.Entry<K, V>... kvs) {
        if (kvs == null) {
            return Collections.emptyMap();
        }
        Map<K, V> map = new HashMap<K, V>(kvs.length);
        for (Map.Entry<K, V> kv : kvs) {
            map.put(kv.getKey(), kv.getValue());
        }
        return Collections.unmodifiableMap(map);
    }
}
