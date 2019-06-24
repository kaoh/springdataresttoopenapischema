package de.ohmesoftware.springdataresttoopenapischema.repository;

import de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MyCustomOrganisationRepository {

    void removeAllOrgs();

    @javax.ws.rs.Path("/search/findByName")
    @javax.ws.rs.GET()
    @javax.ws.rs.Produces(value = { "application/json;charset=UTF-8", "application/hal+json;charset=UTF-8" })
    @io.swagger.v3.oas.annotations.Operation(operationId = "MyCustomOrganisationRepository_findByName", summary = "Custom finder by Name for name.", parameters = { @io.swagger.v3.oas.annotations.Parameter(name = "sort", description = "The sorting criteria(s). Syntax: (sort=(name : foo : id : creationDate),(asc : desc))*", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY), @io.swagger.v3.oas.annotations.Parameter(name = "page", description = "The page number to return.", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY), @io.swagger.v3.oas.annotations.Parameter(name = "size", description = "The page size.", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY), @io.swagger.v3.oas.annotations.Parameter(name = "name", description = "No description", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY) })
    Page<Organisation> findByName(@io.swagger.v3.oas.annotations.Parameter(hidden = true, name = "name") String name, @io.swagger.v3.oas.annotations.Parameter(hidden = true, name = "pageable") Pageable pageable);

    @javax.ws.rs.PUT()
    @io.swagger.v3.oas.annotations.Operation(operationId = "MyCustomOrganisationRepository_update", summary = "Updates a(n) Organisation.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "An organisation.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)) }), responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "No Content."), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "An organisation.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)), @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/hal+json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)) }) })
    default de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation update(de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation entity) {
        return null;
    }

    @javax.ws.rs.PATCH()
    @io.swagger.v3.oas.annotations.Operation(operationId = "MyCustomOrganisationRepository_patch", summary = "Patches a(n) Organisation.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "An organisation.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)) }), responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "No Content."), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "An organisation.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)), @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/hal+json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)) }) })
    default de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation patch(de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation entity) {
        return null;
    }
}
