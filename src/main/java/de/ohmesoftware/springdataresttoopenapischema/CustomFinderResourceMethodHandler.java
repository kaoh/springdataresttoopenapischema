package de.ohmesoftware.springdataresttoopenapischema;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.javadoc.Javadoc;
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
                c -> isConcreteRepositoryClass(c)
        ).forEach(
                c -> addCustomFinderOperations(c)
        );
    }

    @Override
    public void removeResourceAnnotations() {
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream().filter(
                c -> isConcreteRepositoryClass(c)
        ).forEach(
                c -> removeCustomFinderOperation(c)
        );
    }

    private List<MethodDeclaration> getCustomFinderMethods(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        return findCustomMethods(classOrInterfaceDeclaration, CUSTOM_FIND_METHOD_PREFIX,
                Collections.singleton(FIND_BY_ID_METHOD));
    }

    private void removeCustomFinderOperation(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        for (MethodDeclaration methodDeclaration : getCustomFinderMethods(classOrInterfaceDeclaration)) {
            removeMethodParameterAnnotation(methodDeclaration, JAXRS_QUERY_PARAM_CLASS);
            removeMethodParameterAnnotation(methodDeclaration, PARAMETER_CLASS);
            removeJaxRsMethodAnnotations(methodDeclaration);
            removeAnnotation(methodDeclaration, OPERATION_ANNOTATION_CLASS);
            if (!methodDeclaration.getParentNode().get().equals(classOrInterfaceDeclaration)) {
                saveClassOrInterfaceToFile((ClassOrInterfaceDeclaration) methodDeclaration.getParentNode().get());
            }
        }
    }

    private void addCustomFinderOperations(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        for (MethodDeclaration methodDeclaration : getCustomFinderMethods(classOrInterfaceDeclaration)) {
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
            String parameterSummary = methodDeclaration.getParameters().stream().
                    filter(p ->
                            !p.getTypeAsString().endsWith(getSimpleNameFromClass(PAGEABLE_CLASS))
                    && !p.getTypeAsString().endsWith(getSimpleNameFromClass(SORT_CLASS))).
                    map(NodeWithSimpleName::getNameAsString).collect(Collectors.joining(COMMA));

            List<String> params = getMethodParameterTypes(methodDeclaration);
            List<NormalAnnotationExpr> parameters = getPageableSortingAndPredicateParameterAnnotations(methodDeclaration,
                    classOrInterfaceDeclaration, params);

            if (parameters.isEmpty()) {
                methodDeclaration.getParameters().forEach(p ->
                        addQueryParamAnnotation(methodDeclaration, p.getNameAsString(), true, null));
            } else {
                // pageable and sort does not have all needed details (e.g. searchable fields), must be described in Operations anootation directly
                // hide all query params
                methodDeclaration.getParameters().forEach(p ->
                        addParameterHideAnnotation(methodDeclaration, p.getNameAsString()));
//                add to pageable/sorting parameters instead
                methodDeclaration.getParameters().stream().filter(p ->
                        !p.getTypeAsString().endsWith(getSimpleNameFromClass(PAGEABLE_CLASS))
                                && !p.getTypeAsString().endsWith(getSimpleNameFromClass(SORT_CLASS))).forEach(p -> {
                            Javadoc javadoc = getJavadoc(methodDeclaration);
                            String paramDescription = null;
                            if (javadoc != null) {
                                paramDescription = getJavadocParameter(javadoc, p.getNameAsString());
                            }
                            parameters.add(
                                    createParameter(p.getNameAsString(), paramDescription)
                            );
                        }
                );
            }
            addPathAnnotation(methodDeclaration, SEARCH_PATH + exportPathConfig.b);
            addGETAnnotation(methodDeclaration);
            List<NormalAnnotationExpr> responses = null;
            if (isPageReturnType(methodDeclaration)) {
                addJaxRsProducesAnnotation(methodDeclaration, MEDIATYPE_JSON, MEDIATYPE_JSON_HAL);
            } else {
                responses = Collections.singletonList(
                        createApiResponseAnnotation20xWithContentForType(
                                classOrInterfaceDeclaration.findCompilationUnit().get(), 200,
                                unwrapOptionalClassOrInterfaceType(methodDeclaration.getType())));
            }
            addOperationAnnotation(methodDeclaration,
                    parameters,
                    null,
                    responses,
                    String.format("Custom finder by %s for %s.", createCustomNaming(methodDeclaration), parameterSummary));
            if (!methodDeclaration.getParentNode().get().equals(classOrInterfaceDeclaration)) {
                saveClassOrInterfaceToFile((ClassOrInterfaceDeclaration) methodDeclaration.getParentNode().get());
            }
        }
    }

}
