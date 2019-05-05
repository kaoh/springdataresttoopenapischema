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
@javax.ws.rs.Path("/people")
public interface UserRepository extends PagingAndSortingRepository<User, String>, QuerydslPredicateExecutor<User> {

    /**
     * Find by username.
     * <p>
     *     Escape "Test"
     * </p>
     * @param firstName The first name.
     * @return The found user.
     */
    @javax.ws.rs.Path("/search/findByFirstName")
    @javax.ws.rs.GET()
    @io.swagger.v3.oas.annotations.Operation(summary = "Find by username.", description = "Escape \"Test\"", responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "A user being able to log-in.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = User.class)), @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/hal+json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = User.class)) }) })
    Optional<User> findByFirstName(@javax.ws.rs.QueryParam(value = "firstName") @io.swagger.v3.oas.annotations.Parameter(required = true, description = "The first name.") String firstName);

    /**
     * Deletes the user.
     * @param username The username.
     */
    @RestResource(path = "deleteUser")
    @javax.ws.rs.Path("/search/removeByUsername")
    @javax.ws.rs.DELETE()
    @io.swagger.v3.oas.annotations.Operation(summary = "Deletes the user.", responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "No Content.") })
    void removeByUsername(@javax.ws.rs.QueryParam(value = "username") @io.swagger.v3.oas.annotations.Parameter(required = true, description = "The username.") String username);

    <S extends User> S create(S entity);

    <S extends User> S update(S entity);

    @javax.ws.rs.GET()
    @io.swagger.v3.oas.annotations.Operation(summary = "Gets a(n) User by its id.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "A user being able to log-in.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)), @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/hal+json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)) }), responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "A user being able to log-in.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)), @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/hal+json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)) }) })
    java.util.Optional<de.ohmesoftware.springdataresttoopenapischema.model.subdir.User> findById(@javax.ws.rs.PathParam(value = "id") @io.swagger.v3.oas.annotations.Parameter(required = true, description = "The database id.") java.lang.String id);

    @javax.ws.rs.GET()
    @io.swagger.v3.oas.annotations.Operation(summary = "Finds all Users.", responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "A user being able to log-in.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)), @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/hal+json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)) }) })
    org.springframework.data.domain.Page<de.ohmesoftware.springdataresttoopenapischema.model.subdir.User> findAll(com.querydsl.core.types.Predicate predicate, org.springframework.data.domain.Pageable pageable);
}
