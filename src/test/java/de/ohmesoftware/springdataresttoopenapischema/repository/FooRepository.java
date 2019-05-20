package de.ohmesoftware.springdataresttoopenapischema.repository;

import de.ohmesoftware.springdataresttoopenapischema.model.subdir.Foo;
import de.ohmesoftware.springdataresttoopenapischema.model.subdir.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Repository for Foos.
 *
 * @author <a href="mailto:k_o_@users.sourceforge.net">Karsten Ohme
 * (k_o_@users.sourceforge.net)</a>
 */
@RepositoryRestResource
@javax.ws.rs.Path("/foos")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Foo Methods")
public interface FooRepository extends PagingAndSortingRepository<Foo, String>, QuerydslPredicateExecutor<Foo> {

    @RestResource(exported = false)
    Optional<Foo> findById(String id);

    @RestResource(exported = false)
    Page<Foo> findAll(@io.swagger.v3.oas.annotations.Parameter(hidden = true, name = "predicate") Predicate predicate, @io.swagger.v3.oas.annotations.Parameter(hidden = true, name = "pageable") Pageable pageable);

    @RestResource(exported = false)
    void deleteById(String id);

    @RestResource(exported = false)
    Foo save(Foo entity);
}
