package repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryRepository<T> implements RepositoryInt<T> {

    private Map<String,T> store = new HashMap<>();
    public static final long PAGE_SIZE_DEFAULT = 10;

    @Override
    public void save(String id, T object){
        store.put(id, object);
    }

    @Override
    public void remove(String id){
        store.remove(id);
    }

    @Override
    public T getById(String id){
        return store.get(id);
    }

    @Override
    public List<T> list(Long numPage, Long pageSize){

        if( numPage == null ) numPage = 0L;
        if( pageSize == null ) pageSize = PAGE_SIZE_DEFAULT;

        pageSize = pageSize <= 0 ? PAGE_SIZE_DEFAULT : pageSize;
        long startAt = (numPage - 1 < 0 ? 0 : numPage - 1) * pageSize;
        long endAt = startAt + pageSize;

        return store.values().stream().skip(startAt).limit(endAt).toList();
    }

    public List<T> list(long numPage){
        return list(numPage, null);
    }

    public List<T> list(){
        return list(null, null);
    }







    



    
}
