package de.ohmesoftware.springdataresttoopenapischema;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.Collections;

/**
 * Resource method handler for deleteById.
 *
 * @author <a href="mailto:karsten@simless.com">Karsten Ohme
 * (karsten@simless.com)</a>
 */
public class DeleteByIdResourceMethodHandler extends MethodByIdResourceMethodHandler {

    private static final String DELETE_BY_ID_METHOD = "deleteById";

    /**
     * Constructor.
     *
     * @param sourceFile      The source file.
     * @param sourcePath      The source path of the Java sources.
     * @param basePath        The base path no not include package directories.
     * @param compilationUnit The compilation unit to enrich with annotations.
     */
    protected DeleteByIdResourceMethodHandler(String sourceFile, String sourcePath, String basePath, CompilationUnit compilationUnit) {
        super(sourceFile, sourcePath, basePath, compilationUnit, DELETE_BY_ID_METHOD, true);
    }

    @Override
    protected void addMethodByIdOperation(MethodDeclaration methodDeclaration, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        addDELETEAnnotation(methodDeclaration);
        addOperationAnnotation(methodDeclaration,
                null,
                Collections.singletonList(
                        createApiResponse204()),
                String.format("Deletes a(n) %s by its id.",
                        getSimpleNameFromClass(
                                getDomainClass(classOrInterfaceDeclaration).asString())));
    }


}
