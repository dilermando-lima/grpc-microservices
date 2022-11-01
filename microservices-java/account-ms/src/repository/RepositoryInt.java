package repository;

import java.util.List;

public interface RepositoryInt<T> {
    public void save(String id, T object);
    public void remove(String id);
    public T getById(String id);
    public List<T> list(Long numPage, Long pageSize);
}
