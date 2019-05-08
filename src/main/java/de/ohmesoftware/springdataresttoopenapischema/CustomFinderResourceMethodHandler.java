package de.ohmesoftware.springdataresttoopenapischema;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.utils.Pair;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resource method handler for custom finders.
 *
 * @author <a href="mailto:karsten@simless.com">Karsten Ohme
 * (karsten@simless.com)</a>
 */
public class CustomFinderResourceMethodHandler extends ResourceMethodHandler {

    private static final String CUSTOM_FIND_METHOD_PREFIX = "findBy";
    private static final String FIND_BY_ID_METHOD = "findById";
    private static final String SEARCH_PATH = "search" + SLASH;
    private static final String COMMA = ",";

    /**
     * Constructor.
     *
     * @param sourceFile      The source file.
     * @param sourcePath      The source path of the Java sources.
     * @param basePath        The base path no not include package directories.
     * @param compilationUnit The compilation unit to enrich with annotations.
     */
    protected CustomFinderResourceMethodHandler(String sourceFile, String sourcePath, String basePath, CompilationUnit compilationUnit) {
        super(sourceFile, sourcePath, basePath, compilationUnit);
    }

    @Override
    public void addResourceAnnotations() {
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream().filter(
                c -> isConcreteRepositoryClass(compilationUnit, c)
        ).forEach(
                c -> addCustomFinderOperations(compilationUnit, c)
        );
    }

    @Override
    public void removeResourceAnnotations() {
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream().filter(
                c -> isConcreteRepositoryClass(compilationUnit, c)
        ).forEach(
                c -> removeCustomFinderOperation(compilationUnit, c)
        );
    }

    private List<MethodDeclaration> getCustomFinderMethods(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        return findCustomMethods(compilationUnit, classOrInterfaceDeclaration, CUSTOM_FIND_METHOD_PREFIX,
                Collections.singleton(FIND_BY_ID_METHOD));
    }

    private void removeCustomFinderOperation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        for (MethodDeclaration methodDeclaration : getCustomFinderMethods(compilationUnit, classOrInterfaceDeclaration)) {
            removeMethodParameterAnnotation(methodDeclaration, JAXRS_QUERY_PARAM_CLASS);
            removeMethodParameterAnnotation(methodDeclaration, PARAMETER_CLASS);
            removeJaxRsMethodAnnotations(methodDeclaration);
            removeAnnotation(methodDeclaration, OPERATION_ANNOTATION_CLASS);
        }
    }

    private void addCustomFinderOperations(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        for (MethodDeclaration methodDeclaration : getCustomFinderMethods(compilationUnit, classOrInterfaceDeclaration)) {
            addCustomFinderOperation(classOrInterfaceDeclaration, methodDeclaration);
        }
    }

    private void addCustomFinderOperation(ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                          MethodDeclaration methodDeclaration) {
        // check if this method is belonging to a concrete class
        if (!isMethodOfConcreteRepositoryClass(methodDeclaration)) {
            // add method to class the search started
            classOrInterfaceDeclaration.getMethods().add(methodDeclaration);
        }
        Pair<Boolean, String> exportPathConfig = getResourceConfig(methodDeclaration.getNameAsString(), classOrInterfaceDeclaration,
                getMethodPath(methodDeclaration), methodDeclaration.getParameter(0).getType());
        if (exportPathConfig.a) {
            String parameterSummary = methodDeclaration.getParameters().stream().map(NodeWithSimpleName::getNameAsString).collect(Collectors.joining(COMMA));
            methodDeclaration.getParameters().forEach(p ->
                    addQueryParamAnnotation(methodDeclaration, p.getNameAsString(), true, null));
            addPathAnnotation(methodDeclaration, SEARCH_PATH + exportPathConfig.b);
            addGETAnnotation(methodDeclaration);
            addOperationAnnotation(methodDeclaration,
                    null,
                    Collections.singletonList(
                            createApiResponseAnnotation20xWithContentForType(200,
                                    unwrapOptionalClassOrInterfaceType((ClassOrInterfaceType) methodDeclaration.getType()))),
                    String.format("Custom finder by %s.", parameterSummary));
        }
    }

}
