package de.ohmesoftware.springdataresttoopenapischema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Middle interface.
 *
 * @author <a href="mailto:k_o_@users.sourceforge.net">Karsten Ohme
 * (k_o_@users.sourceforge.net)</a>
 */
public interface MiddleRepository2<T> extends PagingAndSortingRepository<T, String>, QuerydslPredicateExecutor<T> {

    @Override
    Iterable<T> findAll(Sort sort);

    @Override
    Page<T> findAll(Pageable pageable);

    @Override
    <S extends T> S save(S entity);

    @Override
    Optional<T> findById(String id);

    @Override
    Iterable<T> findAll();

    @Override
    void deleteById(String id);

    @Override
    void delete(T entity);

    @Override
    void deleteAll(Iterable entities);

}
