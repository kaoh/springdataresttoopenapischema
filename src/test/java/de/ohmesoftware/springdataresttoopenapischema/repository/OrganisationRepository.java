package de.ohmesoftware.springdataresttoopenapischema.repository;

import de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@javax.ws.rs.Path("/organisations")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Organisation Methods")
public interface OrganisationRepository extends MiddleRepository<Organisation>, MyCustomOrganisationRepository {

    @RestResource
    @javax.ws.rs.GET()
    @io.swagger.v3.oas.annotations.Operation(summary = "Finds all Organisations and returns the result as array.", parameters = { @io.swagger.v3.oas.annotations.Parameter(name = "sort", description = "The sorting criteria(s). Syntax: ((name|foo|id|creationDate)=&lt;value&gt;,(asc|desc))*", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY) }, responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "An organisation.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class))), @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/hal+json;charset=UTF-8", array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class))) }) })
    Iterable<Organisation> findAll(@io.swagger.v3.oas.annotations.Parameter(hidden = true, name = "sort") Sort sort);

    @RestResource(exported = false)
    Page<Organisation> findAll(Pageable pageable);

    @javax.ws.rs.Path("/{id}")
    @javax.ws.rs.GET()
    @io.swagger.v3.oas.annotations.Operation(summary = "Gets a(n) Organisation by its id.", responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "An organisation.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)), @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/hal+json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)) }) })
    java.util.Optional<de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation> findById(@javax.ws.rs.PathParam(value = "id") @io.swagger.v3.oas.annotations.Parameter(required = true, description = "The database id.") java.lang.String id);

    @javax.ws.rs.Path("/{id}")
    @javax.ws.rs.DELETE()
    @io.swagger.v3.oas.annotations.Operation(summary = "Deletes a(n) Organisation by its id.", responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "No Content.") })
    void deleteById(@javax.ws.rs.PathParam(value = "id") @io.swagger.v3.oas.annotations.Parameter(required = true, description = "The database id.") java.lang.String id);

    @javax.ws.rs.POST()
    @io.swagger.v3.oas.annotations.Operation(summary = "Creates a(n) Organisation.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "An organisation.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)), @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/hal+json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)) }), responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "An organisation.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)), @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/hal+json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)) }) })
    de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation save(de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation entity);
}
