package de.ohmesoftware.springdataresttoopenapischema;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

/**
 * Resource method handler for PUT.
 *
 * @author <a href="mailto:karsten@simless.com">Karsten Ohme
 * (karsten@simless.com)</a>
 */
public class PutResourceMethodHandler extends UpdateResourceMethodHandler {

    private static final String UPDATE_METHOD = "update";

    private boolean disablePut;
    /**
     * Constructor.
     *
     * @param sourceFile      The source file.
     * @param sourcePath      The source path of the Java sources.
     * @param basePath        The base path no not include package directories.
     * @param compilationUnit The compilation unit to enrich with annotations.
     * @param disablePut <code>true</code> if PU is disabled.
     */
    protected PutResourceMethodHandler(String sourceFile, String sourcePath, String basePath,
                                       CompilationUnit compilationUnit, boolean disablePut) {
        super(sourceFile, sourcePath, basePath, compilationUnit);
        this.disablePut = disablePut;
    }

    @Override
    protected void addUpdateAnnotation(BodyDeclaration<?> bodyDeclaration) {
        addPUTAnnotation(bodyDeclaration);
    }

    @Override
    protected String getUpdateMethodName() {
        return UPDATE_METHOD;
    }

    @Override
    protected boolean include200Response() {
        return true;
    }

    @Override
    protected String getDescription(String className) {
        return String.format("Updates a(n) %s.", className);
    }

    @Override
    protected void addUpdateOperation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        if (!disablePut) {
            super.addUpdateOperation(compilationUnit, classOrInterfaceDeclaration);
        }
    }
}
