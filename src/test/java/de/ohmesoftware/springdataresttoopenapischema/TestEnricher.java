package de.ohmesoftware.springdataresttoopenapischema;

import de.ohmesoftware.springdataresttoopenapischema.repository.UserRepository;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Test;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Test.
 *
 * @author <a href="mailto:k_o_@users.sourceforge.net">Karsten Ohme
 * (k_o_@users.sourceforge.net)</a>
 */
public class TestEnricher {

    private static String buildPath(String classOrPackageName) {
        return "src/test/java/" + classOrPackageName.replace(".", "/");
    }

    @After
    public void after() throws Exception {
        FileUtils.copyFile(new File(buildPath(UserRepository.class.getName()) + ".bak"), new File(buildPath(UserRepository.class.getName()) + ".java"));
    }

    @Test
    public void testFindByIdEnrich() throws Exception {
        Enricher enricher = new Enricher(buildPath(UserRepository.class.getPackage().getName().substring(0, UserRepository.class.getPackage().getName().lastIndexOf("."))), null, Collections.singleton("**.bak"));
        enricher.enrich();
        String newContent = IOUtils.toString(new FileReader(new File(buildPath(UserRepository.class.getName()) + ".java")));
        assertTrue(newContent.contains("package de.ohmesoftware.springdataresttoopenapischema.repository;"));
        assertTrue(newContent.contains("java.util.Optional<de.ohmesoftware.springdataresttoopenapischema.model.subdir.User> findById("));
        assertTrue(newContent.contains("@javax.ws.rs.Path(\"/people\""));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Operation(summary = \"Gets a(n) User by its id.\""));
        assertTrue(newContent.contains("@javax.ws.rs.GET"));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Parameter(required = true, description = \"The database id.\")"));
        assertTrue(newContent.contains("javax.ws.rs.PathParam(value = \"id\""));
        assertTrue(newContent.contains("requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = " +
                "\"A user being able to log-in.\", content = { @io.swagger.v3.oas.annotations.media.Content(" +
                "mediaType = \"application/json;charset=UTF-8\", schema = @io.swagger.v3.oas.annotations.media.Schema(" +
                "implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)), @io.swagger.v3.oas.annotations.media.Content(" +
                "mediaType = \"application/hal+json;charset=UTF-8\", " +
                "schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)) })"));
        assertTrue(newContent.contains("responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = \"200\", " +
                "description = \"A user being able to log-in.\", content = { " +
                "@io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/json;charset=UTF-8\", " +
                "schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)), " +
                "@io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/hal+json;charset=UTF-8\", " +
                "schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)) }) }"));
    }

    @Test
    public void testFindByUsernameEnrich() throws Exception {
        Enricher enricher = new Enricher(buildPath(UserRepository.class.getPackage().getName().substring(0, UserRepository.class.getPackage().getName().lastIndexOf("."))), null, Collections.singleton("**.bak"));
        enricher.enrich();
        String newContent = IOUtils.toString(new FileReader(new File(buildPath(UserRepository.class.getName()) + ".java")));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Operation(summary = \"Find by username.\", description = \"Escape \\\"Test\\\"\""));
        assertTrue(newContent.contains("@javax.ws.rs.GET"));
        assertTrue(newContent.contains("@javax.ws.rs.Path(\"/search/findByFirstName\")"));
        assertFalse(newContent.contains("@io.swagger.v3.oas.annotations.Operation()"));
        assertTrue(newContent.contains("responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = \"200\", " +
                "description = \"A user being able to log-in.\", " +
                "content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/json;charset=UTF-8\", " +
                "schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = User.class)), " +
                "@io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/hal+json;charset=UTF-8\", " +
                "schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = User.class)) }) }"));
    }

    @Test
    public void testRemoveByUsernameEnrich() throws Exception {
        Enricher enricher = new Enricher(buildPath(UserRepository.class.getPackage().getName().substring(0, UserRepository.class.getPackage().getName().lastIndexOf("."))), null, Collections.singleton("**.bak"));
        enricher.enrich();
        String newContent = IOUtils.toString(new FileReader(new File(buildPath(UserRepository.class.getName()) + ".java")));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Operation(summary = \"Deletes the user.\""));
        assertTrue(newContent.contains("@javax.ws.rs.DELETE"));
        assertTrue(newContent.contains("@javax.ws.rs.Path(\"/search/deleteUser\")"));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Parameter(required = true, description = \"The username.\")"));
        assertTrue(newContent.contains("javax.ws.rs.QueryParam(value = \"username\""));
        assertTrue(newContent.contains("responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = \"204\", description = \"No Content.\") }"));
    }
}
