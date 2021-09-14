package reference.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListAndSetUtil extends Util{
    private ListAndSetUtil(){
        super();
    }

    public static <E> List<E> setToList(Set<E> s){
        return new ArrayList<>(s);
    }

    public static <E> Set<E> listToSet(List<E> l){
        return new HashSet<>(l);
    }
}
