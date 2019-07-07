package de.ohmesoftware.springdataresttoopenapischema.repository;

import de.ohmesoftware.springdataresttoopenapischema.model.subdir.User;

public interface CustomUserRepository {

    @javax.ws.rs.PUT()
    @javax.ws.rs.Path("/{id}")
    @io.swagger.v3.oas.annotations.Operation(operationId = "CustomUserRepository_update", summary = "Updates a(n) User.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "A user being able to log-in.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)) }), responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "No Content."), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "A user being able to log-in.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)), @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/hal+json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)) }) })
    default de.ohmesoftware.springdataresttoopenapischema.model.subdir.User update(de.ohmesoftware.springdataresttoopenapischema.model.subdir.User entity) {
        return null;
    }

    @javax.ws.rs.PATCH()
    @javax.ws.rs.Path("/{id}")
    @io.swagger.v3.oas.annotations.Operation(operationId = "CustomUserRepository_patch", summary = "Patches a(n) User.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "A user being able to log-in.", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json;charset=UTF-8", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)) }), responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "No Content.") })
    default de.ohmesoftware.springdataresttoopenapischema.model.subdir.User patch(de.ohmesoftware.springdataresttoopenapischema.model.subdir.User entity) {
        return null;
    }
}
