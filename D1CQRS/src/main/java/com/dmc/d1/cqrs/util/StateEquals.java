package com.dmc.d1.cqrs.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created By davidclelland on 27/06/2016.
 */
public interface StateEquals<T extends StateEquals<T>> {

    boolean stateEquals(T o);

    static boolean listStateEquals(List<? extends StateEquals> o1,List<? extends StateEquals> o2 ) {
        if (o1 == o2)
            return true;

        if (o1.size() != o2.size())
            return false;

        Iterator<? extends StateEquals> i1 = o1.iterator();
        Iterator<? extends StateEquals> i2 = o2.iterator();

        while (i1.hasNext() && i2.hasNext()) {
            StateEquals s1 = i1.next();
            StateEquals s2 = i2.next();
            if (!(s1 == null ? s2 == null : s1.stateEquals(s2)))
                return false;
        }

        return true;
    }


    static <KEY> boolean  mapStateEquals(Map<KEY, ? extends StateEquals> o1, Map<KEY,? extends StateEquals> o2 ) {
        if (o1 == o2)
            return true;

        if (o1.size() != o2.size())
            return false;

        try {
            Iterator<? extends Map.Entry<KEY, ? extends StateEquals>> i = o1.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<KEY, ? extends StateEquals> e = i.next();
                KEY key = e.getKey();
                StateEquals value = e.getValue();
                if (value == null) {
                    if (!(o2.get(key)==null && o2.containsKey(key)))
                        return false;
                } else {
                    if (!value.equals(o2.get(key)))
                        return false;
                }
            }
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }

        return true;
    }

}
