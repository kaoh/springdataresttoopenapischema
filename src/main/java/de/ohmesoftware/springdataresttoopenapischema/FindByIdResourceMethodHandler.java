package de.ohmesoftware.springdataresttoopenapischema;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.util.Collections;

/**
 * Resource method handler for findById.
 *
 * @author <a href="mailto:karsten@simless.com">Karsten Ohme
 * (karsten@simless.com)</a>
 */
public class FindByIdResourceMethodHandler extends ResourceMethodHandler {

    private static final String FIND_BY_ID_METHOD = "findById";
    private static final String FIND_BY_ID_PARAM = "id";

    /**
     * Constructor.
     *
     * @param sourceFile      The source file.
     * @param sourcePath      The source path of the Java sources.
     * @param basePath        The base path no not include package directories.
     * @param compilationUnit The compilation unit to enrich with annotations.
     */
    protected FindByIdResourceMethodHandler(String sourceFile, String sourcePath, String basePath, CompilationUnit compilationUnit) {
        super(sourceFile, sourcePath, basePath, compilationUnit);
    }

    @Override
    public void addResourceAnnotations() {
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(
                c -> addFindByIdOperation(compilationUnit, c)
        );
    }

    @Override
    public void removeResourceAnnotations() {
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(
                c -> removeFindByOperation(compilationUnit, c)
        );
    }

    private void removeFindByOperation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        MethodDeclaration methodDeclaration = findClosestMethod(compilationUnit, classOrInterfaceDeclaration, FIND_BY_ID_METHOD, String.class.getSimpleName());
        if (methodDeclaration != null) {
            removeMethodParameterAnnotation(methodDeclaration, JAXRS_PATH_PARAM_CLASS);
            removeMethodParameterAnnotation(methodDeclaration, PARAMETER_CLASS);
            removeCrudOperationAnnotationAndMethod(methodDeclaration, compilationUnit, classOrInterfaceDeclaration);
        }
    }

    private void addFindByIdOperation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        MethodDeclaration methodDeclaration = findClosestMethod(compilationUnit, classOrInterfaceDeclaration, FIND_BY_ID_METHOD, String.class.getSimpleName());
        // add missing method automatically if extending CRUD interface
        if (methodDeclaration == null) {
            ClassOrInterfaceType domainClassOrInterfaceType = getDomainClass(compilationUnit, classOrInterfaceDeclaration);
            if (checkIfExtendingCrudInterface(compilationUnit, classOrInterfaceDeclaration)) {
                ClassOrInterfaceType idClass = getIDClass(compilationUnit, classOrInterfaceDeclaration);
                methodDeclaration = addInterfaceMethod(classOrInterfaceDeclaration,
                        FIND_BY_ID_METHOD, getOptionalWrapper(domainClassOrInterfaceType), new Parameter(
                                idClass, FIND_BY_ID_PARAM));
            }
        }
        if (methodDeclaration == null) {
            return;
        }
        NormalAnnotationExpr methodResource = findClosestMethodResourceAnnotation(compilationUnit, classOrInterfaceDeclaration, FIND_BY_ID_METHOD,
                String.class.getSimpleName());
        // if resource is null take default empty path and it is exported
        boolean exported = true;
        // this has no special sub path
        String methodPath = null;
        if (methodResource != null) {
            exported = checkResourceExported(methodResource);
            String _path = getResourcePath(methodResource);
            if (_path != null) {
                methodPath = _path;
            }
        }
        if (exported) {
            if (methodPath != null) {
                addPathAnnotation(classOrInterfaceDeclaration, methodPath);
            }
            addPathParamAnnotation(methodDeclaration, FIND_BY_ID_PARAM, true, "The database id.");
            addGETAnnotation(methodDeclaration);
            addOperationAnnotation(methodDeclaration, createRequestBodyAnnotation(compilationUnit,
                    classOrInterfaceDeclaration),
                    Collections.singletonList(
                            createApiResponseAnnotation200WithContent(compilationUnit,
                                    classOrInterfaceDeclaration)),
                    String.format("Gets a(n) %s by its id.",
                            getSimpleNameFromClass(
                                    getDomainClass(compilationUnit, classOrInterfaceDeclaration).asString())));
        }
    }

}
