package de.ohmesoftware.springdataresttoopenapischema;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.Collections;

/**
 * Resource method handler for findById.
 *
 * @author <a href="mailto:karsten@simless.com">Karsten Ohme
 * (karsten@simless.com)</a>
 */
public class FindByIdResourceMethodHandler extends MethodByIdResourceMethodHandler {

    private static final String FIND_BY_ID_METHOD = "findById";

    /**
     * Constructor.
     *
     * @param sourceFile      The source file.
     * @param sourcePath      The source path of the Java sources.
     * @param basePath        The base path no not include package directories.
     * @param compilationUnit The compilation unit to enrich with annotations.
     */
    protected FindByIdResourceMethodHandler(String sourceFile, String sourcePath, String basePath, CompilationUnit compilationUnit) {
        super(sourceFile, sourcePath, basePath, compilationUnit, FIND_BY_ID_METHOD, false);
    }

    @Override
    protected void additionalMethodByIdOperation(MethodDeclaration methodDeclaration, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        addGETAnnotation(methodDeclaration);
        addOperationAnnotation(methodDeclaration,
                null,
                Collections.singletonList(
                        createApiResponseAnnotation200WithContent(
                                classOrInterfaceDeclaration)),
                String.format("Gets a(n) %s by its id.",
                        getSimpleNameFromClass(
                                getDomainClass(classOrInterfaceDeclaration).asString())));
    }

}
