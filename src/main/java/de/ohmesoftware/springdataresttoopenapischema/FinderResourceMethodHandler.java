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
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream().filter(
                c -> isConcreteRepositoryClass(c)
        ).forEach(
                c -> addFindAllOperation(compilationUnit, c)
        );
    }

    @Override
    public void removeResourceAnnotations() {
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream().filter(
                c -> isConcreteRepositoryClass(c)
        ).forEach(
                c -> removeFindAllOperation(compilationUnit, c)
        );
    }

    private void removeFindAllOperation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        MethodDeclaration methodDeclaration = findClosestMethod(classOrInterfaceDeclaration,
                FIND_ALL_METHOD, QUERYDSL_PREDICATE_CLASS, PAGEABLE_CLASS);
        if (methodDeclaration != null) {
            removeMethodParameterAnnotation(methodDeclaration, JAXRS_PATH_PARAM_CLASS);
            removeMethodParameterAnnotation(methodDeclaration, PARAMETER_CLASS);
            removeQuerydslOperationAnnotationAndMethod(methodDeclaration, compilationUnit, classOrInterfaceDeclaration);
        }
        methodDeclaration = findClosestMethod(classOrInterfaceDeclaration,
                FIND_ALL_METHOD, PAGEABLE_CLASS);
        if (methodDeclaration != null) {
            removeMethodParameterAnnotation(methodDeclaration, JAXRS_PATH_PARAM_CLASS);
            removeMethodParameterAnnotation(methodDeclaration, PARAMETER_CLASS);
            removeCrudOperationAnnotationAndMethod(methodDeclaration, compilationUnit, classOrInterfaceDeclaration);
        }
        methodDeclaration = findClosestMethod(classOrInterfaceDeclaration,
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
        // check if this method is using a precise class
        if (methodDeclaration != null && !isMethodOfConcreteRepositoryClass(methodDeclaration)) {
            methodDeclaration = null;
        }
        ClassOrInterfaceType domainClassOrInterfaceType = getDomainClass(classOrInterfaceDeclaration);
        // add missing method automatically if extending CRUD or QuerydslPredicator interface
        if (methodDeclaration == null) {
            // query dsl has preference
            if (checkIfExtendingQuerydslInterface(classOrInterfaceDeclaration)) {
                methodDeclaration = addInterfaceMethod(classOrInterfaceDeclaration,
                        FIND_ALL_METHOD, getWrapperForType(domainClassOrInterfaceType, PAGE_CLASS),
                        new Parameter(
                                getClassOrInterfaceTypeFromClassName(compilationUnit, QUERYDSL_PREDICATE_CLASS), PREDICATE_PARAM),
                        new Parameter(
                                getClassOrInterfaceTypeFromClassName(compilationUnit, PAGEABLE_CLASS), PAGEABLE_PARAM)
                );
            } else if (checkIfExtendingCrudInterface(classOrInterfaceDeclaration)) {
                methodDeclaration = addInterfaceMethod(classOrInterfaceDeclaration,
                        FIND_ALL_METHOD, getWrapperForType(domainClassOrInterfaceType, PAGE_CLASS), new Parameter(
                                getClassOrInterfaceTypeFromClassName(compilationUnit, PAGEABLE_CLASS), PAGEABLE_PARAM));
            }
        }
        if (methodDeclaration == null) {
            return;
        }
        List<String> params = getMethodParameterTypes(methodDeclaration);
        hidePageableSortAndPredicateMethodParameters(methodDeclaration);
        AnnotationExpr methodResource = findClosestMethodResourceAnnotation(classOrInterfaceDeclaration,
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
            List<NormalAnnotationExpr> parameters = getPageableSortingAndPredicateParameterAnnotations(methodDeclaration,
                    classOrInterfaceDeclaration, params);
            addGETAnnotation(methodDeclaration);
            List<NormalAnnotationExpr> responses = null;
            String defaultDescription;
            if (isPageReturnType(methodDeclaration)) {
                defaultDescription = String.format("Finds all %s%s and returns the result paginated.",
                        getSimpleNameFromClass(
                                getDomainClass(classOrInterfaceDeclaration).asString()), PLURAL_S);
                addJaxRsProducesAnnotation(methodDeclaration, MEDIATYPE_JSON, MEDIATYPE_JSON_HAL);
            }
            else {
                responses = Collections.singletonList(
                        createApiResponseAnnotation200WithContentForType(classOrInterfaceDeclaration.findCompilationUnit().get(),
                                methodDeclaration.getType()));
                defaultDescription = String.format("Finds all %s%s and returns the result as array.",
                        getSimpleNameFromClass(
                                getDomainClass(classOrInterfaceDeclaration).asString()), PLURAL_S);
            }
            addOperationAnnotation(methodDeclaration, parameters,
                    null,
                    responses,
                    defaultDescription);
        }
    }

}
