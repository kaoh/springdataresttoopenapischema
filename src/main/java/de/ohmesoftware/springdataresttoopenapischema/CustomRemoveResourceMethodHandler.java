package de.ohmesoftware.springdataresttoopenapischema;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resource method handler for custom removers.
 *
 * @author <a href="mailto:karsten@simless.com">Karsten Ohme
 * (karsten@simless.com)</a>
 */
public class CustomRemoveResourceMethodHandler extends ResourceMethodHandler {

    private static final String CUSTOM_DELETE_METHOD_PREFIX = "deleteBy";
    private static final String CUSTOM_REMOVE_METHOD_PREFIX = "removeBy";
    private static final String SEARCH_PATH = "search" + SLASH;

    /**
     * Constructor.
     *
     * @param sourceFile      The source file.
     * @param sourcePath      The source path of the Java sources.
     * @param basePath        The base path no not include package directories.
     * @param compilationUnit The compilation unit to enrich with annotations.
     */
    protected CustomRemoveResourceMethodHandler(String sourceFile, String sourcePath, String basePath, CompilationUnit compilationUnit) {
        super(sourceFile, sourcePath, basePath, compilationUnit);
    }

    @Override
    public void addResourceAnnotations() {
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(
                c -> addCustomRemoveOperations(compilationUnit, c)
        );
    }

    @Override
    public void removeResourceAnnotations() {
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(
                c -> removeCustomRemoveOperation(compilationUnit, c)
        );
    }

    private List<MethodDeclaration> getCustomFinderMethods(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        List<MethodDeclaration> methodDeclarations = new ArrayList<>();
        methodDeclarations.addAll(findCustomMethods(compilationUnit, classOrInterfaceDeclaration, CUSTOM_REMOVE_METHOD_PREFIX,
                Collections.emptySet()));
        methodDeclarations.addAll(findCustomMethods(compilationUnit, classOrInterfaceDeclaration, CUSTOM_DELETE_METHOD_PREFIX,
                Collections.emptySet()));
        return methodDeclarations;
    }

    private void removeCustomRemoveOperation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        for (MethodDeclaration methodDeclaration : getCustomFinderMethods(compilationUnit, classOrInterfaceDeclaration)) {
            removeMethodParameterAnnotation(methodDeclaration, JAXRS_PATH_PARAM_CLASS);
            removeMethodParameterAnnotation(methodDeclaration, PARAMETER_CLASS);
            removeJaxRsAnnotations(compilationUnit, methodDeclaration);
            removeAnnotation(compilationUnit, methodDeclaration, OPERATION_ANNOTATION_CLASS);
        }
    }

    private void addCustomRemoveOperations(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        for (MethodDeclaration methodDeclaration : getCustomFinderMethods(compilationUnit, classOrInterfaceDeclaration)) {
            addCustomRemoveOperation(compilationUnit, classOrInterfaceDeclaration, methodDeclaration);
        }
    }

    private void addCustomRemoveOperation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                          MethodDeclaration methodDeclaration) {
        NormalAnnotationExpr methodResource = findClosestMethodResourceAnnotation(compilationUnit,
                classOrInterfaceDeclaration, methodDeclaration.getNameAsString());
        boolean exported = true;
        String methodPath = getMethodPath(methodDeclaration);
        if (methodResource != null) {
            exported = checkResourceExported(methodResource);
            String _path = getResourcePath(methodResource);
            if (_path != null) {
                methodPath = _path;
            }
        }
        if (exported) {
            methodDeclaration.getParameters().forEach(p ->
                    addQueryParamAnnotation(methodDeclaration, p.getNameAsString(), true, null));
            addPathAnnotation(methodDeclaration, SEARCH_PATH + methodPath);
            addDELETEAnnotation(methodDeclaration);
            addOperationAnnotation(methodDeclaration,
                    null,
                    Collections.singletonList(createApiResponse204()), null
            );
        }
    }

}
