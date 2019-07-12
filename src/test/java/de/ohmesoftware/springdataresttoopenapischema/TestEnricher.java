package de.ohmesoftware.springdataresttoopenapischema;

import de.ohmesoftware.springdataresttoopenapischema.model.subdir.Searchable;
import de.ohmesoftware.springdataresttoopenapischema.model.subdir.Sortable;
import de.ohmesoftware.springdataresttoopenapischema.repository.*;
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
        FileUtils.copyFile(new File(buildPath(FooRepository.class.getName()) + ".bak"), new File(buildPath(FooRepository.class.getName()) + ".java"));
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
        Enricher enricher = new Enricher(buildPath(UserRepository.class.getPackage().getName().substring(0,
                UserRepository.class.getPackage().getName().lastIndexOf("."))), null,
                Collections.singleton("**.bak"), false, null, null);
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
        Enricher enricher = new Enricher(buildPath(UserRepository.class.getPackage().getName().substring(0, UserRepository.class.getPackage().getName().lastIndexOf("."))),
                null, Collections.singleton("**.bak"), false, null, null);
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
    public void testFindBetweenWithList() throws Exception {
        Enricher enricher = new Enricher(buildPath(UserRepository.class.getPackage().getName().substring(0,
                UserRepository.class.getPackage().getName().lastIndexOf("."))), null, Collections.singleton("**.bak"), false, null, null);
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
        Enricher enricher = new Enricher(buildPath(UserRepository.class.getPackage().getName().substring(0,
                UserRepository.class.getPackage().getName().lastIndexOf("."))), null, Collections.singleton("**.bak"), false, null, null);
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
        Enricher enricher = new Enricher(buildPath(UserRepository.class.getPackage().getName().substring(0,
                UserRepository.class.getPackage().getName().lastIndexOf("."))), null, Collections.singleton("**.bak"), false,
                Searchable.class.getName(), Sortable.class.getName());
        enricher.enrich();
        String newContent = IOUtils.toString(new FileReader(new File(buildPath(UserRepository.class.getName()) + ".java")));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Operation(operationId = \"UserRepository_findAll\", summary = \"Finds all Users and returns the result paginated.\", parameters = { @io.swagger.v3.oas.annotations.Parameter(name = \"username\", description = \"username search criteria.\", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY), @io.swagger.v3.oas.annotations.Parameter(name = \"emailAddress\", description = \"emailAddress search criteria.\", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY), @io.swagger.v3.oas.annotations.Parameter(name = \"page\", description = \"The page number to return.\", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY), @io.swagger.v3.oas.annotations.Parameter(name = \"size\", description = \"The page size.\", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY) }, responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = \"200\", description = \"Paged view with list of: A user being able to log-in.\", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/json;charset=UTF-8\", schema = @io.swagger.v3.oas.annotations.media.Schema(ref = \"#/components/schemas/PageUser\")), @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/hal+json;charset=UTF-8\", schema = @io.swagger.v3.oas.annotations.media.Schema(ref = \"#/components/schemas/PageUser\")) }) })"));
    }

    @Test
    public void testFooNoExport() throws Exception {
        Enricher enricher = new Enricher(buildPath(FooRepository.class.getPackage().getName().substring(0,
                FooRepository.class.getPackage().getName().lastIndexOf("."))), null, Collections.singleton("**.bak"), false, null, null);
        enricher.enrich();
        String newContent = IOUtils.toString(new FileReader(new File(buildPath(FooRepository.class.getName()) + ".java")));
        assertFalse(newContent.contains("@javax.ws.rs.GET"));
        assertFalse(newContent.contains("@javax.ws.rs.POST"));
        assertFalse(newContent.contains("@javax.ws.rs.DELETE"));
        assertFalse(newContent.contains("@io.swagger.v3.oas.annotations.Parameter"));
    }

    @Test
    public void testFindersEnrichExistingQuerydslFinder() throws Exception {
        Enricher enricher = new Enricher(buildPath(OrganisationRepository.class.getPackage().getName().substring(0,
                OrganisationRepository.class.getPackage().getName().lastIndexOf("."))), null, Collections.singleton("**.bak"), false, null, null);
        enricher.enrich();
        String newContent = IOUtils.toString(new FileReader(new File(buildPath(OrganisationRepository.class.getName()) + ".java")));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Operation(operationId = \"OrganisationRepository_findAll\", summary = \"Finds all Organisations and returns the result as array.\", responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = \"200\", description = \"Array of an organisation.\", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/json;charset=UTF-8\", array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Organisation.class))), @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/hal+json;charset=UTF-8\", array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Organisation.class))) }) })"));
        assertTrue(newContent.contains("Iterable<Organisation> findAll(@io.swagger.v3.oas.annotations.Parameter(hidden = true, name = \"sort\") Sort sort)"));
        assertTrue(newContent.contains("@javax.ws.rs.GET"));
        assertTrue(newContent.contains("ArraySchema"));
    }

    @Test
    public void testCustomFinderListReturned() throws Exception {
        Enricher enricher = new Enricher(buildPath(OrganisationRepository.class.getPackage().getName().substring(0,
                OrganisationRepository.class.getPackage().getName().lastIndexOf("."))), null, Collections.singleton("**.bak"), false, null, null);
        enricher.enrich();
        String newContent = IOUtils.toString(new FileReader(new File(buildPath(OrganisationRepository.class.getName()) + ".java")));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Operation(operationId = \"OrganisationRepository_findByCreationDateBetween\", summary = \"Custom finder by CreationDateBetween for startDate,endDate.\", responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = \"200\", description = \"Array of an organisation.\", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/json;charset=UTF-8\", array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Organisation.class))), @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/hal+json;charset=UTF-8\", array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Organisation.class))) }) })"));
        assertTrue(newContent.contains("List<Organisation> findByCreationDateBetween(@javax.ws.rs.QueryParam(value = \"startDate\") @io.swagger.v3.oas.annotations.Parameter(required = true) Date startDate, @javax.ws.rs.QueryParam(value = \"endDate\") @io.swagger.v3.oas.annotations.Parameter(required = true) Date endDate);"));
    }

    @Test
    public void testCustomFinderPageReturned() throws Exception {
        Enricher enricher = new Enricher(buildPath(OrganisationRepository.class.getPackage().getName().substring(0,
                OrganisationRepository.class.getPackage().getName().lastIndexOf("."))), null, Collections.singleton("**.bak"), false, null, null);
        enricher.enrich();
        String newContent = IOUtils.toString(new FileReader(new File(buildPath(OrganisationRepository.class.getName()) + ".java")));
        assertTrue(newContent.contains(" @io.swagger.v3.oas.annotations.Operation(operationId = \"OrganisationRepository_findByNameContaining\", summary = \"Custom finder by NameContaining for name.\", parameters = { @io.swagger.v3.oas.annotations.Parameter(name = \"page\", description = \"The page number to return.\", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY), @io.swagger.v3.oas.annotations.Parameter(name = \"size\", description = \"The page size.\", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY), @io.swagger.v3.oas.annotations.Parameter(name = \"name\", description = \"No description\", in = io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY) })"));
        assertTrue(newContent.contains("Page<Organisation> findByNameContaining(@io.swagger.v3.oas.annotations.Parameter(hidden = true, name = \"name\") String name, @io.swagger.v3.oas.annotations.Parameter(hidden = true, name = \"pageable\") Pageable pageable);"));
    }

    @Test
    public void testDeleteByIdEnricher() throws Exception {
        Enricher enricher = new Enricher(buildPath(OrganisationRepository.class.getPackage().getName().substring(0,
                OrganisationRepository.class.getPackage().getName().lastIndexOf("."))), null, Collections.singleton("**.bak"), false, null, null);
        enricher.enrich();
        String newContent = IOUtils.toString(new FileReader(new File(buildPath(OrganisationRepository.class.getName()) + ".java")));
        assertTrue(newContent.contains("@javax.ws.rs.DELETE"));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Operation(operationId = \"OrganisationRepository_deleteById\", summary = \"Deletes a(n) Organisation by its id.\", responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = \"204\", description = \"No Content.\") })"));
        assertTrue(newContent.contains("void deleteById(@javax.ws.rs.PathParam(value = \"id\") @io.swagger.v3.oas.annotations.Parameter(required = true, description = \"The database id.\") java.lang.String id)"));
    }

    @Test
    public void testCreateEnricher() throws Exception {
        Enricher enricher = new Enricher(buildPath(OrganisationRepository.class.getPackage().getName().substring(0,
                OrganisationRepository.class.getPackage().getName().lastIndexOf("."))), null, Collections.singleton("**.bak"), false, null, null);
        enricher.enrich();
        String newContent = IOUtils.toString(new FileReader(new File(buildPath(OrganisationRepository.class.getName()) + ".java")));
        assertTrue(newContent.contains("@javax.ws.rs.POST"));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Operation(operationId = \"OrganisationRepository_save\", summary = \"Creates a(n) Organisation.\", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = \"An organisation.\", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/json;charset=UTF-8\", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)) }), responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = \"201\", description = \"An organisation.\", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/json;charset=UTF-8\", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)), @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/hal+json;charset=UTF-8\", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)) }) })"));
        assertTrue(newContent.contains("de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation save(de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation entity)"));
    }

    @Test
    public void testPutEnricher() throws Exception {
        Enricher enricher = new Enricher(buildPath(OrganisationRepository.class.getPackage().getName().substring(0, OrganisationRepository.class.getPackage().getName().lastIndexOf("."))), null, Collections.singleton("**.bak"), false,
                null, null);
        enricher.enrich();
        assertFalse(new File(buildPath(OrganisationRepository.class.getPackage().getName()) + "/CustomOrganisationRepository.java").exists());
        String newContent = IOUtils.toString(new FileReader(new File(buildPath(MyCustomOrganisationRepository.class.getName()) + ".java")));
        assertTrue(newContent.contains("@javax.ws.rs.PUT"));
        assertTrue(newContent.contains("@javax.ws.rs.Path(\"/{id}\")"));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Operation(operationId = \"MyCustomOrganisationRepository_update\", summary = \"Updates a(n) Organisation.\", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = \"An organisation.\", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/json;charset=UTF-8\", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)) }), responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = \"204\", description = \"No Content.\"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = \"200\", description = \"An organisation.\", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/json;charset=UTF-8\", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)), @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/hal+json;charset=UTF-8\", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)) }) })"));
        assertTrue(newContent.replace("\n", "").replace("\r", "").contains("default de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation update(de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation entity) {" +
                "        return null;" +
                "    }"));
    }

    @Test
    public void testPatchEnricher() throws Exception {
        Enricher enricher = new Enricher(buildPath(OrganisationRepository.class.getPackage().getName().substring(0, OrganisationRepository.class.getPackage().getName().lastIndexOf("."))),
                null, Collections.singleton("**.bak"), false, null, null);
        enricher.enrich();
        assertFalse(new File(buildPath(OrganisationRepository.class.getPackage().getName()) + "/CustomOrganisationRepository.java").exists());
        String newContent = IOUtils.toString(new FileReader(new File(buildPath(MyCustomOrganisationRepository.class.getName()) + ".java")));
        assertTrue(newContent.contains("@javax.ws.rs.PATCH"));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Operation(operationId = \"MyCustomOrganisationRepository_patch\", summary = \"Patches a(n) Organisation.\", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = \"An organisation.\", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/json;charset=UTF-8\", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation.class)) }), responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = \"204\", description = \"No Content.\") })"));
        assertTrue(newContent.replace("\n", "").replace("\r", "").contains("default de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation update(de.ohmesoftware.springdataresttoopenapischema.model.subdir.Organisation entity) {" +
                "        return null;" +
                "    }"));
    }

    @Test
    public void testPutUserEnricher() throws Exception {
        Enricher enricher = new Enricher(buildPath(
                OrganisationRepository.class.getPackage().getName().substring(0, OrganisationRepository.class.getPackage().getName().lastIndexOf("."))),
                null, Collections.singleton("**.bak"), false, null, null);
        enricher.enrich();
        String newContent = IOUtils.toString(new FileReader(new File(buildPath(UserRepository.class.getPackage().getName()) + "/CustomUserRepository.java")));
        assertTrue(newContent.contains("@javax.ws.rs.PUT"));
        assertTrue(newContent.contains("@io.swagger.v3.oas.annotations.Operation(operationId = \"CustomUserRepository_update\", summary = \"Updates a(n) User.\", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = \"A user being able to log-in.\", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/json;charset=UTF-8\", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)) }), responses = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = \"204\", description = \"No Content.\"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = \"200\", description = \"A user being able to log-in.\", content = { @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/json;charset=UTF-8\", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)), @io.swagger.v3.oas.annotations.media.Content(mediaType = \"application/hal+json;charset=UTF-8\", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = de.ohmesoftware.springdataresttoopenapischema.model.subdir.User.class)) }) })"));
        assertTrue(newContent.replace("\n", "").replace("\r", "").contains("default de.ohmesoftware.springdataresttoopenapischema.model.subdir.User update(de.ohmesoftware.springdataresttoopenapischema.model.subdir.User entity) {" +
                "        return null;" +
                "    }"));
    }
}
