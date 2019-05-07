package de.ohmesoftware.springdataresttoopenapischema.repository;

import de.ohmesoftware.springdataresttoopenapischema.model.subdir.User;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import java.util.Optional;

/**
 * Repository for Users.
 *
 * @author <a href="mailto:k_o_@users.sourceforge.net">Karsten Ohme
 * (k_o_@users.sourceforge.net)</a>
 */
@RepositoryRestResource(path = "people")
public interface UserRepository extends PagingAndSortingRepository<User, String>, QuerydslPredicateExecutor<User> {

    /**
     * Find by username.
     * <p>
     *     Escape "Test"
     * </p>
     * @param firstName The first name.
     * @return The found user.
     */
    Optional<User> findByFirstName(String firstName);

    /**
     * Deletes the user.
     * @param username The username.
     */
    @RestResource(path = "deleteUser")
    void removeByUsername(String username);

}
