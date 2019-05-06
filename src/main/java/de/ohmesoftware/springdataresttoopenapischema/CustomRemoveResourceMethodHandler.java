package de.ohmesoftware.springdataresttoopenapischema;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.utils.Pair;

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
    private static final String DELETE_BY_ID_METHOD = "deleteById";
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
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream().filter(
                c -> isConcreteRepositoryClass(compilationUnit, c)
        ).forEach(
                c -> addCustomRemoveOperations(compilationUnit, c)
        );
    }

    @Override
    public void removeResourceAnnotations() {
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream().filter(
                c -> isConcreteRepositoryClass(compilationUnit, c)
        ).forEach(
                c -> removeCustomRemoveOperation(compilationUnit, c)
        );
    }

    private List<MethodDeclaration> getCustomFinderMethods(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        List<MethodDeclaration> methodDeclarations = new ArrayList<>();
        methodDeclarations.addAll(findCustomMethods(compilationUnit, classOrInterfaceDeclaration, CUSTOM_REMOVE_METHOD_PREFIX,
                Collections.singleton(DELETE_BY_ID_METHOD)));
        methodDeclarations.addAll(findCustomMethods(compilationUnit, classOrInterfaceDeclaration, CUSTOM_DELETE_METHOD_PREFIX,
                Collections.singleton(DELETE_BY_ID_METHOD)));
        return methodDeclarations;
    }

    private void removeCustomRemoveOperation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        for (MethodDeclaration methodDeclaration : getCustomFinderMethods(compilationUnit, classOrInterfaceDeclaration)) {
            removeMethodParameterAnnotation(methodDeclaration, JAXRS_QUERY_PARAM_CLASS);
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
        // check if this method is using a precise class
        if (!isMethodOfConcreteRepositoryClass(methodDeclaration)) {
            // add method to class
            classOrInterfaceDeclaration.getMethods().add(methodDeclaration);
        }
        Pair<Boolean, String> exportPathConfig = getResourceConfig(methodDeclaration.getNameAsString(), classOrInterfaceDeclaration,
                methodDeclaration.getParameter(0).getType(), getMethodPath(methodDeclaration));
        if (exportPathConfig.a) {
            methodDeclaration.getParameters().forEach(p ->
                    addQueryParamAnnotation(methodDeclaration, p.getNameAsString(), true, null));
            addPathAnnotation(methodDeclaration, SEARCH_PATH + exportPathConfig.b);
            addDELETEAnnotation(methodDeclaration);
            addOperationAnnotation(methodDeclaration,
                    null,
                    Collections.singletonList(createApiResponse204()), null
            );
        }
    }

}
