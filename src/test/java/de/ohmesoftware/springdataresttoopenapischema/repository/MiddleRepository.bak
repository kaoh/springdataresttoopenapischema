package de.ohmesoftware.springdataresttoopenapischema.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

/**
 * Middle interface.
 *
 * @author <a href="mailto:k_o_@users.sourceforge.net">Karsten Ohme
 * (k_o_@users.sourceforge.net)</a>
 */
public interface MiddleRepository<T> extends PagingAndSortingRepository<T, String>, QuerydslPredicateExecutor<T> {

    Optional<T> findById(String id);

}
