package de.ohmesoftware.springdataresttoopenapischema.repository;

public interface MyCustomOrganisationRepository {

    void removeAllOrgs();

    @javax.ws.rs.PUT()
    @io.swagger.v3.oas.annotations.Operation(summary = "Updates a(n) Organisation.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "An organisation.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)) }), responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "No Content."), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "An organisation.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)), @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/hal+json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)) }) })
    default de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation update(de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation entity) {
        return null;
    }
}
