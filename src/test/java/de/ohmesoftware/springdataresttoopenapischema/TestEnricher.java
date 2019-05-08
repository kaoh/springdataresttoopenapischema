package de.ohmesoftware.springdataresttoopenapischema;

import de.ohmesoftware.springdataresttoopenapischema.repository.MiddleRepository;
import de.ohmesoftware.springdataresttoopenapischema.repository.MyCustomOrganisationRepository;
import de.ohmesoftware.springdataresttoopenapischema.repository.OrganisationRepository;
import de.ohmesoftware.springdataresttoopenapischema.repository.UserRepository;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
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

    @Before
    public void before() throws Exception {
        FileUtils.copyFile(new File(buildPath(UserRepository.class.getName()) + ".bak"), new File(buildPath(UserRepository.class.getName()) + ".java"));
        FileUtils.copyFile(new File(buildPath(OrganisationRepository.class.getName()) + ".bak"), new File(buildPath(OrganisationRepository.class.getName()) + ".java"));
        FileUtils.copyFile(new File(buildPath(MiddleRepository.class.getName()) + ".bak"), new File(buildPath(MiddleRepository.class.getName()) + ".java"));
        FileUtils.copyFile(new File(buildPath(MyCustomOrganisationRepository.class.getName()) + ".bak"),
                new File(buildPath(MyCustomOrganisationRepository.class.getName()) + ".java"));
        new File(buildPath(MiddleRepository.class.getPackage().getName()) + "CustomUserRepository.java").delete();
        new File(buildPath(MiddleRepository.class.getPackage().getName()) + "CustomOrganisationRepository.java").delete();
    }

    @Test
    public void testFindByIdEnrich() throws Exception {
        Enricher enricher = new Enricher(buildPath(UserRepository.class.getPackage().getName().substring(0, UserRepository.class.getPackage().getName().lastIndexOf("."))), null, Collections.singleton("**.bak"));
        enricher.enrich();
        String newContent = IOUtils.toString(new FileReader(new File(buildPath(UserRepository.class.getName()) + ".java")));
        assertTrue(newContent.contains("package de.ohmesoftware.springdataresttoopenapischema.repository;"));
        assertTrue(newContent.contains("java.util.Optional<de.ohmesoftware.springdataresttoopenapischema.model.subdir.User> findById("));
        assertTrue(newContent.contains("@javax.ws.rs.Path(\"/people\""));
        assertTrue(newContent.contains("@javax.ws.rs.Path(\"/{id}\")"));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Operation(operationId = \"UserRepository_findById\", summary = \"Gets a(n) User by its id.\""));
        assertTrue(newContent.contains("@javax.ws.rs.GET"));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Parameter(required = true, description = \"The database id.\")"));
        assertTrue(newContent.contains("javax.ws.rs.PathParam(value = \"id\""));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Operation(operationId = \"UserRepository_findById\", summary = \"Gets a(n) User by its id.\", responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = \"200\", description = \"A user being able to log-in.\", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/json;charset=UTF-8\", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)), @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/hal+json;charset=UTF-8\", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)) }) })"));
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
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Operation(operationId = \"UserRepository_findByFirstName\", summary = \"Find by username.\", description = \"Escape \\\"Test\\\"\""));
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
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.tags.Tag(name = \"User Methods\""));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Operation(operationId = \"UserRepository_removeByUsername\", summary = \"Deletes the user.\""));
        assertTrue(newContent.contains("@javax.ws.rs.DELETE"));
        assertTrue(newContent.contains("@javax.ws.rs.Path(\"/search/deleteUser\")"));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Parameter(required = true, description = \"The username.\")"));
        assertTrue(newContent.contains("javax.ws.rs.QueryParam(value = \"username\""));
        assertTrue(newContent.contains("responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = \"204\", description = \"No Content.\") }"));
    }

    @Test
    public void testFindersEnrich() throws Exception {
        Enricher enricher = new Enricher(buildPath(UserRepository.class.getPackage().getName().substring(0, UserRepository.class.getPackage().getName().lastIndexOf("."))), null, Collections.singleton("**.bak"));
        enricher.enrich();
        String newContent = IOUtils.toString(new FileReader(new File(buildPath(UserRepository.class.getName()) + ".java")));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Operation(operationId = \"UserRepository_findAll\", summary = \"Finds all Users and returns the result paginated.\""));
        assertTrue(newContent.contains("@javax.ws.rs.Produces(value = { \"application/json;charset=UTF-8\", \"application/hal+json;charset=UTF-8\" })"));
        assertTrue(newContent.contains("org.springframework.data.domain.Page<de.ohmesoftware.springdataresttoopenapischema.model.subdir.User> findAll(@io.swagger.v3.oas.annotations.Parameter(hidden = true, name = \"predicate\") com.querydsl.core.types.Predicate predicate, @io.swagger.v3.oas.annotations.Parameter(hidden = true, name = \"pageable\") org.springframework.data.domain.Pageable pageable)"));
        assertTrue(newContent.contains("@javax.ws.rs.GET"));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Parameter(name = \"emailAddress\", description = \"emailAddress search criteria. Syntax: emailAddress=&lt;value&gt;\", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY)"));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Parameter(name = \"sort\", description = \"The sorting criteria(s). Syntax: ((username|emailAddress|role|firstName|lastName|blocked|failedLoginAttempts|organisation.*|id|creationDate)=&lt;value&gt;,(asc|desc))*\", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY)"));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Parameter(name = \"page\", description = \"The page number to return.\", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY)"));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Parameter(name = \"size\", description = \"The page size.\", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY)"));
    }

    @Test
    public void testFindersEnrichExistingQuerydslFinder() throws Exception {
        Enricher enricher = new Enricher(buildPath(OrganisationRepository.class.getPackage().getName().substring(0, OrganisationRepository.class.getPackage().getName().lastIndexOf("."))), null, Collections.singleton("**.bak"));
        enricher.enrich();
        String newContent = IOUtils.toString(new FileReader(new File(buildPath(OrganisationRepository.class.getName()) + ".java")));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Operation(operationId = \"OrganisationRepository_findAll\", summary = \"Finds all Organisations and returns the result as array.\", parameters = { @io.swagger.v3.oas.annotations.Parameter(name = \"sort\", description = \"The sorting criteria(s). Syntax: ((name|foo|id|creationDate)=&lt;value&gt;,(asc|desc))*\", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY) }, responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = \"200\", description = \"An organisation.\", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/json;charset=UTF-8\", array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class))), @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/hal+json;charset=UTF-8\", array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class))) }) })"));
        assertTrue(newContent.contains("Iterable<Organisation> findAll(@io.swagger.v3.oas.annotations.Parameter(hidden = true, name = \"sort\") Sort sort)"));
        assertTrue(newContent.contains("@javax.ws.rs.GET"));
        assertTrue(newContent.contains("ArraySchema"));
        assertFalse(newContent.contains("name = \"page\""));
        assertFalse(newContent.contains("name = \"size\""));
        assertTrue(newContent.contains("parameters = { @io.swagger.v3.oas.annotations.Parameter(name = \"sort\", description = \"The sorting criteria(s). Syntax: ((name|foo|id|creationDate)=&lt;value&gt;,(asc|desc))*\", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY) }"));
    }

    @Test
    public void testDeleteByIdEnricher() throws Exception {
        Enricher enricher = new Enricher(buildPath(OrganisationRepository.class.getPackage().getName().substring(0, OrganisationRepository.class.getPackage().getName().lastIndexOf("."))), null, Collections.singleton("**.bak"));
        enricher.enrich();
        String newContent = IOUtils.toString(new FileReader(new File(buildPath(OrganisationRepository.class.getName()) + ".java")));
        assertTrue(newContent.contains("@javax.ws.rs.DELETE"));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Operation(operationId = \"OrganisationRepository_deleteById\", summary = \"Deletes a(n) Organisation by its id.\", responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = \"204\", description = \"No Content.\") })"));
        assertTrue(newContent.contains("void deleteById(@javax.ws.rs.PathParam(value = \"id\") @io.swagger.v3.oas.annotations.Parameter(required = true, description = \"The database id.\") java.lang.String id)"));
    }

    @Test
    public void testCreateEnricher() throws Exception {
        Enricher enricher = new Enricher(buildPath(OrganisationRepository.class.getPackage().getName().substring(0, OrganisationRepository.class.getPackage().getName().lastIndexOf("."))), null, Collections.singleton("**.bak"));
        enricher.enrich();
        String newContent = IOUtils.toString(new FileReader(new File(buildPath(OrganisationRepository.class.getName()) + ".java")));
        assertTrue(newContent.contains("@javax.ws.rs.POST"));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Operation(operationId = \"OrganisationRepository_save\", summary = \"Creates a(n) Organisation.\", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = \"An organisation.\", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/json;charset=UTF-8\", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)) }), responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = \"201\", description = \"An organisation.\", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/json;charset=UTF-8\", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)), @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/hal+json;charset=UTF-8\", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)) }) })"));
        assertTrue(newContent.contains("de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation save(de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation entity)"));
    }

    @Test
    public void testUpdateEnricher() throws Exception {
        Enricher enricher = new Enricher(buildPath(OrganisationRepository.class.getPackage().getName().substring(0, OrganisationRepository.class.getPackage().getName().lastIndexOf("."))), null, Collections.singleton("**.bak"));
        enricher.enrich();
        assertFalse(new File(buildPath(OrganisationRepository.class.getPackage().getName()) + "/CustomOrganisationRepository.java").exists());
        String newContent = IOUtils.toString(new FileReader(new File(buildPath(MyCustomOrganisationRepository.class.getName()) + ".java")));
        assertTrue(newContent.contains("@javax.ws.rs.PUT"));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Operation(operationId = \"MyCustomOrganisationRepository_update\", summary = \"Updates a(n) Organisation.\", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = \"An organisation.\", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/json;charset=UTF-8\", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)) }), responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = \"204\", description = \"No Content.\"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = \"200\", description = \"An organisation.\", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/json;charset=UTF-8\", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)), @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/hal+json;charset=UTF-8\", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)) }) })"));
        assertTrue(newContent.replace("\n", "").replace("\r", "").contains("default de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation update(de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation entity) {" +
                "        return null;" +
                "    }"));
    }

    @Test
    public void testUpdateUserEnricher() throws Exception {
        Enricher enricher = new Enricher(buildPath(OrganisationRepository.class.getPackage().getName().substring(0, OrganisationRepository.class.getPackage().getName().lastIndexOf("."))), null, Collections.singleton("**.bak"));
        enricher.enrich();
        String newContent = IOUtils.toString(new FileReader(new File(buildPath(UserRepository.class.getPackage().getName()) + "/CustomUserRepository.java")));
        assertTrue(newContent.contains("@javax.ws.rs.PUT"));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Operation(operationId = \"CustomUserRepository_update\", summary = \"Updates a(n) User.\", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = \"A user being able to log-in.\", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/json;charset=UTF-8\", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)) }), responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = \"204\", description = \"No Content.\"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = \"200\", description = \"A user being able to log-in.\", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/json;charset=UTF-8\", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)), @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/hal+json;charset=UTF-8\", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)) }) })"));
        assertTrue(newContent.replace("\n", "").replace("\r", "").contains("default de.ohmesoftware.springdataresttoopenapischema.model.subdir.User update(de.ohmesoftware.springdataresttoopenapischema.model.subdir.User entity) {" +
                "        return null;" +
                "    }"));
    }
}
