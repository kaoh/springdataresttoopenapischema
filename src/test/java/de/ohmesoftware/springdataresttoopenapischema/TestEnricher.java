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
    public void enrich() throws Exception {
        Enricher enricher = new Enricher(buildPath(UserRepository.class.getPackage().getName().substring(0, UserRepository.class.getPackage().getName().lastIndexOf("."))), null, Collections.singleton("**.bak"));
        enricher.enrich();
        String newContent = IOUtils.toString(new FileReader(new File(buildPath(UserRepository.class.getName()) + ".java")));
        assertTrue(newContent.contains("package de.ohmesoftware.springdataresttoopenapischema.repository;"));
        assertTrue(newContent.contains("Operation("));
        assertTrue(newContent.contains("Escape \\\"test\\\""));
        assertTrue(newContent.contains("summary=\"Find by DB ID.\""));
        assertTrue(newContent.contains("description=\"Escape \\\"test\\\"\""));
        assertFalse(newContent.contains("@io.swagger.v3.oas.annotations.Operation()"));
    }
}
