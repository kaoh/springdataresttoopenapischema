package de.ohmesoftware.springdataresttoopenapischema.repository;

import de.ohmesoftware.springdataresttoopenapischema.model.subdir.Foo;

public interface CustomFooRepository {

    @javax.ws.rs.PUT()
    @io.swagger.v3.oas.annotations.Operation(operationId = "CustomFooRepository_update", summary = "Updates a(n) Foo.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Doc.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Foo.class)) }), responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "No Content."), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Doc.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Foo.class)), @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/hal+json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Foo.class)) }) })
    default de.ohmesoftware.springdataresttoopenapischema.model.subdir.Foo update(de.ohmesoftware.springdataresttoopenapischema.model.subdir.Foo entity) {
        return null;
    }
}
