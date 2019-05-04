package de.ohmesoftware.springdataresttoopenapischema;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.utils.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resource method handler for findById.
 *
 * @author <a href="mailto:karsten@simless.com">Karsten Ohme
 * (karsten@simless.com)</a>
 */
public class FinderResourceMethodHandler extends ResourceMethodHandler {

    private static final String FIND_ALL_METHOD = "findAll";
    private static final String PAGEABLE_CLASS = "org.springframework.data.domain.Pageable";
    private static final String SORT_CLASS = "org.springframework.data.domain.Sort";
    private static final String QUERYDSL_PREDICATE_CLASS = "com.querydsl.core.types.Predicate";
    private static final String PREDICATE_PARAM = "predicate";
    private static final String SORT_PARAM = "sort";
    private static final String PAGEABLE_PARAM = "pageable";

    /**
     * Constructor.
     *
     * @param sourceFile      The source file.
     * @param sourcePath      The source path of the Java sources.
     * @param basePath        The base path no not include package directories.
     * @param compilationUnit The compilation unit to enrich with annotations.
     */
    protected FinderResourceMethodHandler(String sourceFile, String sourcePath, String basePath, CompilationUnit compilationUnit) {
        super(sourceFile, sourcePath, basePath, compilationUnit);
    }

    @Override
    public void addResourceAnnotations() {
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(
                c -> addFindAllOperation(compilationUnit, c)
        );
    }

    @Override
    public void removeResourceAnnotations() {
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(
                c -> removeFindAllOperation(compilationUnit, c)
        );
    }

    private void removeFindAllOperation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        MethodDeclaration methodDeclaration = findClosestMethod(compilationUnit, classOrInterfaceDeclaration,
                FIND_ALL_METHOD);
        if (methodDeclaration != null) {
            removeMethodParameterAnnotation(methodDeclaration, JAXRS_PATH_PARAM_CLASS);
            removeMethodParameterAnnotation(methodDeclaration, PARAMETER_CLASS);
            removeQuerydslOperationAnnotationAndMethod(methodDeclaration, compilationUnit, classOrInterfaceDeclaration);
        }
    }

    private void addFindAllOperation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        List<Pair<String, List<String>>> methodVariants = new ArrayList<>();
        methodVariants.add(new Pair<>(FIND_ALL_METHOD, Collections.singletonList(PREDICATE_PARAM)));
        methodVariants.add(new Pair<>(FIND_ALL_METHOD, Collections.singletonList(PAGEABLE_PARAM)));
        methodVariants.add(new Pair<>(FIND_ALL_METHOD, Collections.singletonList(SORT_PARAM)));
        methodVariants.add(new Pair<>(FIND_ALL_METHOD, null));
        MethodDeclaration methodDeclaration = findClosestMethodFromMethodVariants(compilationUnit, classOrInterfaceDeclaration,
                methodVariants);
        // add missing method automatically if extending CRUD or QuerydslPredicator interface
        if (methodDeclaration == null) {
            ClassOrInterfaceType domainClassOrInterfaceType = getDomainClass(compilationUnit, classOrInterfaceDeclaration);
            // query dsl has preference
            if (checkIfExtendingQuerydslInterface(compilationUnit, classOrInterfaceDeclaration)) {
                methodDeclaration = addInterfaceMethod(classOrInterfaceDeclaration,
                        FIND_ALL_METHOD, getOptionalWrapper(domainClassOrInterfaceType), new Parameter(
                                getClassOrInterfaceTypeFromClassName(compilationUnit, QUERYDSL_PREDICATE_CLASS), PREDICATE_PARAM));
            } else if (checkIfExtendingCrudInterface(compilationUnit, classOrInterfaceDeclaration)) {
                methodDeclaration = addInterfaceMethod(classOrInterfaceDeclaration,
                        FIND_ALL_METHOD, getOptionalWrapper(domainClassOrInterfaceType), new Parameter(
                                getClassOrInterfaceTypeFromClassName(compilationUnit, PAGEABLE_CLASS), PAGEABLE_PARAM));
            }
        }
        if (methodDeclaration == null) {
            return;
        }
        List<String> params = methodDeclaration.getParameters().stream().map(n -> n.getName().asString()).collect(Collectors.toList());
        NormalAnnotationExpr methodResource = findClosestMethodResourceAnnotation(compilationUnit, classOrInterfaceDeclaration,
                FIND_ALL_METHOD, params.toArray(new String[0]));
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
            for (String param : params) {
                String description = null;
                switch (param) {
                    case PREDICATE_PARAM:
                        description = "The query predicate.";
                        break;
                    case SORT_PARAM:
                        description = "The sort parameters.";
                        break;
                    case PAGEABLE_PARAM:
                        description = "The paging parameters.";
                        break;
                }
                if (description != null) {
                    addPathParamAnnotation(methodDeclaration, param, true, description);
                }
            }
            addGETAnnotation(methodDeclaration);
            addOperationAnnotation(methodDeclaration, createRequestBodyAnnotation(compilationUnit,
                    classOrInterfaceDeclaration),
                    Collections.singletonList(
                            createApiResponseAnnotation200WithContent(compilationUnit,
                                    classOrInterfaceDeclaration)),
                    String.format("Finds all %s.",
                            getSimpleNameFromClass(
                                    getDomainClass(compilationUnit, classOrInterfaceDeclaration).asString())));
        }
    }

}
