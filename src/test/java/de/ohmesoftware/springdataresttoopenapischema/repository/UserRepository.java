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
@io.swagger.v3.oas.annotations.tags.Tag(name = "User Methods")
public interface UserRepository extends PagingAndSortingRepository<User, String>, QuerydslPredicateExecutor<User>, CustomUserRepository {

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
    @io.swagger.v3.oas.annotations.Operation(operationId = "UserRepository_findByFirstName", summary = "Find by username.", description = "Escape \"Test\"", responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "A user being able to log-in.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = User.class)), @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/hal+json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = User.class)) }) })
    Optional<User> findByFirstName(@javax.ws.rs.QueryParam(value = "firstName") @io.swagger.v3.oas.annotations.Parameter(required = true, description = "The first name.") String firstName);

    /**
     * Deletes the user.
     * @param username The username.
     */
    @RestResource(path = "deleteUser")
    @javax.ws.rs.Path("/search/deleteUser")
    @javax.ws.rs.DELETE()
    @io.swagger.v3.oas.annotations.Operation(operationId = "UserRepository_removeByUsername", summary = "Deletes the user.", responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "No Content.") })
    void removeByUsername(@javax.ws.rs.QueryParam(value = "username") @io.swagger.v3.oas.annotations.Parameter(required = true, description = "The username.") String username);

    @javax.ws.rs.Path("/{id}")
    @javax.ws.rs.GET()
    @io.swagger.v3.oas.annotations.Operation(operationId = "UserRepository_findById", summary = "Gets a(n) User by its id.", responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "A user being able to log-in.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)), @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/hal+json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)) }) })
    java.util.Optional<de.ohmesoftware.springdataresttoopenapischema.model.subdir.User> findById(@javax.ws.rs.PathParam(value = "id") @io.swagger.v3.oas.annotations.Parameter(required = true, description = "The database id.") java.lang.String id);

    @javax.ws.rs.GET()
    @io.swagger.v3.oas.annotations.Operation(operationId = "UserRepository_findAll", summary = "Finds all Users and returns the result paginated.", parameters = { @io.swagger.v3.oas.annotations.Parameter(name = "username", description = "username search criteria. Used in SQL like fashion. Syntax: username=value", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY), @io.swagger.v3.oas.annotations.Parameter(name = "emailAddress", description = "emailAddress search criteria. Used in SQL like fashion. Syntax: emailAddress=value", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY), @io.swagger.v3.oas.annotations.Parameter(name = "role", description = "role search criteria. Used in SQL like fashion. Syntax: role=value", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY), @io.swagger.v3.oas.annotations.Parameter(name = "firstName", description = "firstName search criteria. Used in SQL like fashion. Syntax: firstName=value", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY), @io.swagger.v3.oas.annotations.Parameter(name = "lastName", description = "lastName search criteria. Used in SQL like fashion. Syntax: lastName=value", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY), @io.swagger.v3.oas.annotations.Parameter(name = "blocked", description = "blocked search criteria. Used in SQL like fashion. Syntax: blocked=value", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY), @io.swagger.v3.oas.annotations.Parameter(name = "failedLoginAttempts", description = "failedLoginAttempts search criteria. Used in SQL like fashion. Syntax: failedLoginAttempts=value", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY), @io.swagger.v3.oas.annotations.Parameter(name = "organisation.*", description = "organisation.* search criteria. Used in SQL like fashion. Syntax: organisation.*=value", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY), @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "id search criteria. Used in SQL like fashion. Syntax: id=value", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY), @io.swagger.v3.oas.annotations.Parameter(name = "creationDate", description = "creationDate search criteria. Used in SQL like fashion. Syntax: creationDate=value", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY), @io.swagger.v3.oas.annotations.Parameter(name = "sort", description = "The sorting criteria(s). Can be passed multiple times as query with descending priority. Syntax: sort=(username or emailAddress or role or firstName or lastName or blocked or failedLoginAttempts or organisation.* or id or creationDate),(asc or desc)", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY), @io.swagger.v3.oas.annotations.Parameter(name = "page", description = "The page number to return.", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY), @io.swagger.v3.oas.annotations.Parameter(name = "size", description = "The page size.", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY) }, responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paged view with list of: A user being able to log-in.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/PageUser")), @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/hal+json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/PageUser")) }) })
    org.springframework.data.domain.Page<de.ohmesoftware.springdataresttoopenapischema.model.subdir.User> findAll(@io.swagger.v3.oas.annotations.Parameter(hidden = true, name = "predicate") com.querydsl.core.types.Predicate predicate, @io.swagger.v3.oas.annotations.Parameter(hidden = true, name = "pageable") org.springframework.data.domain.Pageable pageable);

    @javax.ws.rs.Path("/{id}")
    @javax.ws.rs.DELETE()
    @io.swagger.v3.oas.annotations.Operation(operationId = "UserRepository_deleteById", summary = "Deletes a(n) User by its id.", responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "No Content.") })
    void deleteById(@javax.ws.rs.PathParam(value = "id") @io.swagger.v3.oas.annotations.Parameter(required = true, description = "The database id.") java.lang.String id);

    @javax.ws.rs.POST()
    @io.swagger.v3.oas.annotations.Operation(operationId = "UserRepository_save", summary = "Creates a(n) User.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "A user being able to log-in.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)) }), responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "A user being able to log-in.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)), @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/hal+json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)) }) })
    de.ohmesoftware.springdataresttoopenapischema.model.subdir.User save(de.ohmesoftware.springdataresttoopenapischema.model.subdir.User entity);
}
