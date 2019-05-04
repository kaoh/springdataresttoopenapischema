package de.ohmesoftware.springdataresttoopenapischema.repository;

import com.querydsl.core.types.Predicate;
import de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation;
import de.ohmesoftware.springdataresttoopenapischema.model.subdir.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Optional;

/**
 * Repository for Organisations.
 *
 * @author <a href="mailto:k_o_@users.sourceforge.net">Karsten Ohme
 * (k_o_@users.sourceforge.net)</a>
 */
@RepositoryRestResource
public interface OrganisationRepository extends PagingAndSortingRepository<Organisation, String> {

    Iterable<Organisation> findAll(Sort sort);

    @RestResource(exported = false)
    Page<Organisation> findAll(Pageable pageable);

}
