package de.ohmesoftware.springdataresttoopenapischema;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
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
    private static final String ITERABLE_CLASS = "java.lang.Iterable";
    private static final String PREDICATE_PARAM = "predicate";
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
                FIND_ALL_METHOD, QUERYDSL_PREDICATE_CLASS, PAGEABLE_CLASS);
        if (methodDeclaration != null) {
            removeMethodParameterAnnotation(methodDeclaration, JAXRS_PATH_PARAM_CLASS);
            removeMethodParameterAnnotation(methodDeclaration, PARAMETER_CLASS);
            removeQuerydslOperationAnnotationAndMethod(methodDeclaration, compilationUnit, classOrInterfaceDeclaration);
        }
        methodDeclaration = findClosestMethod(compilationUnit, classOrInterfaceDeclaration,
                FIND_ALL_METHOD, PAGEABLE_CLASS);
        if (methodDeclaration != null) {
            removeMethodParameterAnnotation(methodDeclaration, JAXRS_PATH_PARAM_CLASS);
            removeMethodParameterAnnotation(methodDeclaration, PARAMETER_CLASS);
            removeCrudOperationAnnotationAndMethod(methodDeclaration, compilationUnit, classOrInterfaceDeclaration);
        }
        methodDeclaration = findClosestMethod(compilationUnit, classOrInterfaceDeclaration,
                FIND_ALL_METHOD, SORT_CLASS);
        if (methodDeclaration != null) {
            removeMethodParameterAnnotation(methodDeclaration, JAXRS_PATH_PARAM_CLASS);
            removeMethodParameterAnnotation(methodDeclaration, PARAMETER_CLASS);
            removeCrudOperationAnnotationAndMethod(methodDeclaration, compilationUnit, classOrInterfaceDeclaration);
        }
    }

    private void addFindAllOperation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        List<Pair<String, List<String>>> methodVariants = new ArrayList<>();
        methodVariants.add(new Pair<>(FIND_ALL_METHOD, Arrays.asList(QUERYDSL_PREDICATE_CLASS, PAGEABLE_CLASS)));
        methodVariants.add(new Pair<>(FIND_ALL_METHOD, Collections.singletonList(PAGEABLE_CLASS)));
        methodVariants.add(new Pair<>(FIND_ALL_METHOD, Collections.singletonList(SORT_CLASS)));
        methodVariants.add(new Pair<>(FIND_ALL_METHOD, null));
        MethodDeclaration methodDeclaration = findClosestMethodFromMethodVariants(compilationUnit, classOrInterfaceDeclaration,
                methodVariants);
        // add missing method automatically if extending CRUD or QuerydslPredicator interface
        if (methodDeclaration == null) {
            ClassOrInterfaceType domainClassOrInterfaceType = getDomainClass(compilationUnit, classOrInterfaceDeclaration);
            // query dsl has preference
            if (checkIfExtendingQuerydslInterface(compilationUnit, classOrInterfaceDeclaration)) {
                methodDeclaration = addInterfaceMethod(classOrInterfaceDeclaration,
                        FIND_ALL_METHOD, getWrapperForType(domainClassOrInterfaceType, PAGE_CLASS),
                        new Parameter(
                                getClassOrInterfaceTypeFromClassName(compilationUnit, QUERYDSL_PREDICATE_CLASS), PREDICATE_PARAM),
                        new Parameter(
                                getClassOrInterfaceTypeFromClassName(compilationUnit, PAGEABLE_CLASS), PAGEABLE_PARAM)
                        );
            } else if (checkIfExtendingCrudInterface(compilationUnit, classOrInterfaceDeclaration)) {
                methodDeclaration = addInterfaceMethod(classOrInterfaceDeclaration,
                        FIND_ALL_METHOD, getWrapperForType(domainClassOrInterfaceType, PAGE_CLASS), new Parameter(
                                getClassOrInterfaceTypeFromClassName(compilationUnit, PAGEABLE_CLASS), PAGEABLE_PARAM));
            }
        }
        if (methodDeclaration == null) {
            return;
        }
        List<String> params = methodDeclaration.getParameters().stream().map(n -> n.getType().asString()).collect(Collectors.toList());
        AnnotationExpr methodResource = findClosestMethodResourceAnnotation(compilationUnit, classOrInterfaceDeclaration,
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
            List<NormalAnnotationExpr> parameters = new ArrayList<>();
            Pair<CompilationUnit, ClassOrInterfaceDeclaration> compilationUnitClassOrInterfaceDeclarationPair =
                    parseClassOrInterfaceType(compilationUnit, getDomainClass(compilationUnit, classOrInterfaceDeclaration));

            for (String param : params) {
                if (param.equals(getSimpleNameFromClass(PAGEABLE_CLASS))) {
                    parameters.addAll(getPageableParams(methodDeclaration,
                            compilationUnitClassOrInterfaceDeclarationPair.b));
                }
                else if (param.endsWith(getSimpleNameFromClass(QUERYDSL_PREDICATE_CLASS))) {
                    parameters.addAll(getPredicateParams(compilationUnit, methodDeclaration,
                            compilationUnitClassOrInterfaceDeclarationPair.b));
                }
                else if (param.endsWith(getSimpleNameFromClass(SORT_CLASS))) {
                    parameters.addAll(getSortParams(compilationUnit, methodDeclaration,
                            compilationUnitClassOrInterfaceDeclarationPair.b));
                }
            }
            addGETAnnotation(methodDeclaration);
            addOperationAnnotation(methodDeclaration, parameters,
                    null,
                    Collections.singletonList(
                            createApiResponseAnnotation200WithContent(compilationUnit,
                                    classOrInterfaceDeclaration)),
                    String.format("Finds all %s%s.",
                            getSimpleNameFromClass(
                                    getDomainClass(compilationUnit, classOrInterfaceDeclaration).asString()), PLURAL_S));
        }
    }

}
