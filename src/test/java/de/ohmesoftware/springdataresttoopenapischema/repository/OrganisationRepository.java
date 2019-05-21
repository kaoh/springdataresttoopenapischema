package de.ohmesoftware.springdataresttoopenapischema.repository;

import de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import java.util.Date;
import java.util.List;

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

    @javax.ws.rs.Path("/search/findByNameContaining")
    @javax.ws.rs.GET()
    @javax.ws.rs.Produces(value = { "application/json;charset=UTF-8", "application/hal+json;charset=UTF-8" })
    @io.swagger.v3.oas.annotations.Operation(operationId = "OrganisationRepository_findByNameContaining", summary = "Custom finder by NameContaining for name.", parameters = { @io.swagger.v3.oas.annotations.Parameter(name = "sort", description = "The sorting criteria(s). Syntax: (sort=(name : foo : id : creationDate),(asc : desc))*", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY), @io.swagger.v3.oas.annotations.Parameter(name = "page", description = "The page number to return.", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY), @io.swagger.v3.oas.annotations.Parameter(name = "size", description = "The page size.", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY), @io.swagger.v3.oas.annotations.Parameter(name = "name", description = "No description", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY) })
    Page<Organisation> findByNameContaining(@io.swagger.v3.oas.annotations.Parameter(hidden = true, name = "name") String name, @io.swagger.v3.oas.annotations.Parameter(hidden = true, name = "pageable") Pageable pageable);

    @javax.ws.rs.Path("/search/findByCreationDateBetween")
    @javax.ws.rs.GET()
    @io.swagger.v3.oas.annotations.Operation(operationId = "OrganisationRepository_findByCreationDateBetween", summary = "Custom finder by CreationDateBetween for startDate,endDate.", responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Array of an organisation.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Organisation.class))), @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/hal+json;charset=UTF-8", array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Organisation.class))) }) })
    List<Organisation> findByCreationDateBetween(@javax.ws.rs.QueryParam(value = "startDate") @io.swagger.v3.oas.annotations.Parameter(required = true) Date startDate, @javax.ws.rs.QueryParam(value = "endDate") @io.swagger.v3.oas.annotations.Parameter(required = true) Date endDate);

    @RestResource
    @javax.ws.rs.GET()
    @io.swagger.v3.oas.annotations.Operation(operationId = "OrganisationRepository_findAll", summary = "Finds all Organisations and returns the result as array.", parameters = { @io.swagger.v3.oas.annotations.Parameter(name = "sort", description = "The sorting criteria(s). Syntax: (sort=(name : foo : id : creationDate),(asc : desc))*", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY) }, responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Array of an organisation.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Organisation.class))), @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/hal+json;charset=UTF-8", array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Organisation.class))) }) })
    Iterable<Organisation> findAll(@io.swagger.v3.oas.annotations.Parameter(hidden = true, name = "sort") Sort sort);

    @RestResource(exported = false)
    Page<Organisation> findAll(Pageable pageable);

    @javax.ws.rs.Path("/{id}")
    @javax.ws.rs.GET()
    @io.swagger.v3.oas.annotations.Operation(operationId = "OrganisationRepository_findById", summary = "Gets a(n) Organisation by its id.", responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "An organisation.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)), @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/hal+json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)) }) })
    java.util.Optional<de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation> findById(@javax.ws.rs.PathParam(value = "id") @io.swagger.v3.oas.annotations.Parameter(required = true, description = "The database id.") java.lang.String id);

    @javax.ws.rs.Path("/{id}")
    @javax.ws.rs.DELETE()
    @io.swagger.v3.oas.annotations.Operation(operationId = "OrganisationRepository_deleteById", summary = "Deletes a(n) Organisation by its id.", responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "No Content.") })
    void deleteById(@javax.ws.rs.PathParam(value = "id") @io.swagger.v3.oas.annotations.Parameter(required = true, description = "The database id.") java.lang.String id);

    @javax.ws.rs.POST()
    @io.swagger.v3.oas.annotations.Operation(operationId = "OrganisationRepository_save", summary = "Creates a(n) Organisation.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "An organisation.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)) }), responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "An organisation.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)), @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/hal+json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)) }) })
    de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation save(de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation entity);
}
