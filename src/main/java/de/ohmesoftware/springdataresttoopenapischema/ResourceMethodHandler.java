package de.ohmesoftware.springdataresttoopenapischema;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.utils.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * base class for processing resources.
 *
 * @author <a href="mailto:karsten@simless.com">Karsten Ohme
 * (karsten@simless.com)</a>
 */
public abstract class ResourceMethodHandler extends ResourceHandler {

    protected static final String OPERATION_ANNOTATION_CLASS = "io.swagger.v3.oas.annotations.Operation";
    protected static final String OPERATION_SUMMARY = "summary";
    protected static final String OPERATION_DESCRIPTION = "description";
    protected static final String OPERATION_REQUEST_BODY = "requestBody";
    protected static final String OPERATION_RESPONSES = "responses";
    protected static final String OPERATION_PARAMETERS = "parameters";

    protected static final String SCHEMA_ANNOTATION_CLASS = "io.swagger.v3.oas.annotations.media.Schema";
    protected static final String ARRAY_SCHEMA_ANNOTATION_CLASS = "io.swagger.v3.oas.annotations.media.ArraySchema";
    protected static final String SCHEMA_IMPLEMENTATION = "implementation";
    protected static final String CONTENT_ANNOTATION_CLASS = "io.swagger.v3.oas.annotations.media.Content";
    protected static final String CONTENT_MEDIATYPE = "mediaType";
    protected static final String CONTENT_SCHEMA = "schema";
    protected static final String CONTENT_ARRAY = "array";
    protected static final String MEDIATYPE_JSON = "application/json;charset=UTF-8";
    protected static final String MEDIATYPE_JSON_HAL = "application/hal+json;charset=UTF-8";
    protected static final String REQUEST_BODY_CLASS = "io.swagger.v3.oas.annotations.parameters.RequestBody";
    protected static final String REQUEST_BODY_API_RESPONSE_DESCRIPTION = "description";
    protected static final String API_RESPONSE_CLASS = "io.swagger.v3.oas.annotations.responses.ApiResponse";
    protected static final String API_RESPONSE_RESPONSE_CODE = "responseCode";
    protected static final String REQUEST_BODY_API_RESPONSE_CONTENT = "content";
    protected static final String PARAMETER_CLASS = "io.swagger.v3.oas.annotations.Parameter";
    protected static final String PARAMETER_DESCRIPTION = "description";
    protected static final String PARAMETER_REQUIRED = "required";
    protected static final String PARAMETER_IN = "in";
    protected static final String PARAMETER_NAME = "name";
    protected static final String PARAMETER_HIDDEN = "hidden";
    protected static final String PARAMETER_IN_CLASS = "io.swagger.v3.oas.annotations.enums.ParameterIn";
    protected static final String PARAMETER_IN_QUERY = "QUERY";


    protected static final String JAXRS_GET_CLASS = "javax.ws.rs.GET";
    protected static final String JAXRS_POST_CLASS = "javax.ws.rs.POST";
    protected static final String JAXRS_PUT_CLASS = "javax.ws.rs.PUT";
    protected static final String JAXRS_DELETE_CLASS = "javax.ws.rs.DELETE";
    protected static final String JAXRS_PATCH_CLASS = "javax.ws.rs.PATCH";

    protected static final String JAXRS_PATH_PARAM_CLASS = "javax.ws.rs.PathParam";
    protected static final String JAXRS_QUERY_PARAM_CLASS = "javax.ws.rs.QueryParam";
    protected static final String JAXRS_QUERY_PATH_PARAM_VALUE = "value";

    protected static final String PAGEABLE_PARAM = "pageable";
    protected static final String PAGEABLE_CLASS = "org.springframework.data.domain.Pageable";
    protected static final String PAGE_CLASS = "org.springframework.data.domain.Page";
    protected static final String PAGE_PARAMETER_NAME = "page";
    protected static final String SIZE_PARAMETER_NAME = "size";
    protected static final String SORT_CLASS = "org.springframework.data.domain.Sort";
    protected static final String SORT_PARAM = "sort";
    protected static final String SINGLE_OBJECT_SEARCH_PARAM_ELLIPSIS = ".*";
    protected static final String SEARCH_ATTRIBUTE_OR = "|";

    protected static final String JSON_PROPERTY_CLASS = "com.fasterxml.jackson.annotation.JsonProperty";
    protected static final String JSON_PROPERTY_ACCESS = "access";
    protected static final String JSON_PROPERTY_WRITE_ONLY = "WRITE_ONLY";
    protected static final String JSON_IGNORE_CLASS = "com.fasterxml.jackson.annotation.JsonIgnore";

    protected static final String QUERYDSL_PREDICATE_CLASS = "com.querydsl.core.types.Predicate";
    protected static final String PRODUCES_CLASS = "javax.ws.rs.Produces";
    protected static final String ANNOTATION_VALUE = "value";


    // TODO: maybe this can be improved without artificial methods
    protected static final String CREATE_METHOD_PATH = "create";
    protected static final String UPDATE_METHOD_PATH = "update";

    /**
     * Constructor.
     *
     * @param sourceFile      The source file.
     * @param sourcePath      The source path of the Java sources.
     * @param basePath        The base path no not include package directories.
     * @param compilationUnit The compilation unit to enrich with annotations.
     */
    protected ResourceMethodHandler(String sourceFile, String sourcePath, String basePath, CompilationUnit compilationUnit) {
        super(sourceFile, sourcePath, basePath, compilationUnit);
    }

    public abstract void addResourceAnnotations();

    public abstract void removeResourceAnnotations();

    // class

    protected Name getNameFromClass(String fqClassName) {
        String[] packages = fqClassName.split("\\.");
        Name prevName = null;
        for (int i = 0; i < packages.length; i++) {
            Name name = new Name(prevName, packages[i]);
            prevName = name;
        }
        return prevName;
    }

    protected ClassOrInterfaceType getOptionalWrapper(ClassOrInterfaceType classOrInterfaceType) {
        return getWrapperForType(classOrInterfaceType, Optional.class.getName());
    }

    protected ClassOrInterfaceType getWrapperForType(ClassOrInterfaceType classOrInterfaceType, String fqClassName) {
        return getClassOrInterfaceTypeFromClassName(compilationUnit,
                fqClassName).setTypeArguments(classOrInterfaceType);
    }

    protected ClassOrInterfaceType unwrapOptionalClassOrInterfaceType(ClassOrInterfaceType classOrInterfaceType) {
        if (classOrInterfaceType.getName().getIdentifier().equals(Optional.class.getSimpleName()) &&
                classOrInterfaceType.getTypeArguments().isPresent()) {
            classOrInterfaceType = (ClassOrInterfaceType) classOrInterfaceType.getTypeArguments().get().get(0);
        }
        return classOrInterfaceType;
    }

    protected List<String> getMethodParameterTypes(MethodDeclaration methodDeclaration) {
        return methodDeclaration.getParameters().stream().map(n -> n.getType().asString()).collect(Collectors.toList());
    }

    protected boolean isIterableReturnType(MethodDeclaration methodDeclaration) {
        return methodDeclaration.getType().isClassOrInterfaceType()
        && methodDeclaration.getType().asClassOrInterfaceType().getName().asString().endsWith(getSimpleNameFromClass(Iterable.class.getName()));
    }

    protected boolean isPageReturnType(MethodDeclaration methodDeclaration) {
        return methodDeclaration.getType().isClassOrInterfaceType()
                && methodDeclaration.getType().asClassOrInterfaceType().getName().asString().endsWith(getSimpleNameFromClass(PAGE_CLASS));
    }

    // annotations

    protected void addMarkerAnnotation(BodyDeclaration<?> bodyDeclaration,
                                       String annotationClass) {
        bodyDeclaration.addAndGetAnnotation(annotationClass);
    }

    // Operation annotations

    protected List<NormalAnnotationExpr> getPageableParams(MethodDeclaration methodDeclaration,
                                                           ClassOrInterfaceDeclaration sortingDomainClassOrInterfaceDeclaration) {
        return methodDeclaration.getParameters().stream().filter(p ->
                p.getType().asString().endsWith(getSimpleNameFromClass(PAGEABLE_CLASS))).
                findFirst().
                map(p -> {
                            List<NormalAnnotationExpr> annotationExprs = new ArrayList<>(addSortParams(compilationUnit, sortingDomainClassOrInterfaceDeclaration));
                            annotationExprs.addAll(
                                    Arrays.asList(
                                            new NormalAnnotationExpr(getNameFromClass(PARAMETER_CLASS),
                                                    new NodeList<>(Arrays.asList(
                                                            new MemberValuePair(PARAMETER_NAME,
                                                                    new StringLiteralExpr(PAGE_PARAMETER_NAME)
                                                            ),
                                                            new MemberValuePair(PARAMETER_DESCRIPTION,
                                                                    new StringLiteralExpr(
                                                                            escapeString("The page number to return.")
                                                                    )),
                                                            new MemberValuePair(PARAMETER_IN,
                                                                    new FieldAccessExpr(new TypeExpr(
                                                                            getClassOrInterfaceTypeFromClassName(compilationUnit, PARAMETER_IN_CLASS)
                                                                    ), PARAMETER_IN_QUERY)
                                                            )
                                                    ))),
                                            new NormalAnnotationExpr(getNameFromClass(PARAMETER_CLASS),
                                                    new NodeList<>(Arrays.asList(
                                                            new MemberValuePair(PARAMETER_NAME,
                                                                    new StringLiteralExpr(SIZE_PARAMETER_NAME)
                                                            ),
                                                            new MemberValuePair(PARAMETER_DESCRIPTION,
                                                                    new StringLiteralExpr(
                                                                            escapeString("The page size.")
                                                                    ))
                                                    )))));
                            return annotationExprs;
                        }
                ).
                orElse(Collections.emptyList());
    }

    private List<String> getSearchParametersFromClassOrInterface(CompilationUnit compilationUnit,
                                                                 ClassOrInterfaceDeclaration sortingDomainClassOrInterfaceDeclaration) {
        List<String> params = new ArrayList<>();
        for (FieldDeclaration fieldDeclaration : sortingDomainClassOrInterfaceDeclaration.getFields()) {
            if (isPropertyIgnored(fieldDeclaration)) {
                continue;
            }
            VariableDeclarator variableDeclarator = fieldDeclaration.getVariables().get(0);
            if (isPrimitive(variableDeclarator.getType()) || isPrimitiveObject((ClassOrInterfaceType) variableDeclarator.getType())) {
                params.add(variableDeclarator.getNameAsString());
            } else if (isSingularObject((ClassOrInterfaceType) variableDeclarator.getType())) {
                params.add(variableDeclarator.getNameAsString() + SINGLE_OBJECT_SEARCH_PARAM_ELLIPSIS);
            }
        }
        for (ClassOrInterfaceType extent : sortingDomainClassOrInterfaceDeclaration.getExtendedTypes()) {
            // visit interface to get information
            if (getSourceFile(compilationUnit, extent).exists()) {
                Pair<CompilationUnit, ClassOrInterfaceDeclaration> compilationUnitClassOrInterfaceDeclarationPair = parseClassOrInterfaceType(compilationUnit, extent);
                params.addAll(getSearchParametersFromClassOrInterface(compilationUnitClassOrInterfaceDeclarationPair.a,
                        compilationUnitClassOrInterfaceDeclarationPair.b));
            }
        }
        return params;
    }

    protected List<NormalAnnotationExpr> getPredicateParams(CompilationUnit compilationUnit, MethodDeclaration methodDeclaration,
                                                            ClassOrInterfaceDeclaration predicateDomainClassOrInterfaceDeclaration) {
        List<String> searchParams = getSearchParametersFromClassOrInterface(compilationUnit, predicateDomainClassOrInterfaceDeclaration);
        if (methodDeclaration.getParameters().stream().anyMatch(p ->
                p.getType().asString().endsWith(getSimpleNameFromClass(QUERYDSL_PREDICATE_CLASS)))) {
            List<NormalAnnotationExpr> annotationExprs = new ArrayList<>();
            searchParams.forEach(
                    p ->
                            annotationExprs.add(new NormalAnnotationExpr(getNameFromClass(PARAMETER_CLASS),
                                    new NodeList(Arrays.asList(
                                            new MemberValuePair(PARAMETER_NAME,
                                                    new StringLiteralExpr(p)
                                            ),
                                            new MemberValuePair(PARAMETER_DESCRIPTION,
                                                    new StringLiteralExpr(
                                                            escapeString(
                                                                    String.format("%s search criteria. Syntax: %s=<value>", p, p))
                                                    )
                                            ),
                                            new MemberValuePair(PARAMETER_IN,
                                                    new FieldAccessExpr(new TypeExpr(
                                                            getClassOrInterfaceTypeFromClassName(compilationUnit, PARAMETER_IN_CLASS)
                                                    ), PARAMETER_IN_QUERY)
                                            )
                                    ))
                            ))
            );
            return annotationExprs;
        }
        return Collections.emptyList();
    }

    protected List<NormalAnnotationExpr> getSortParams(CompilationUnit compilationUnit, MethodDeclaration methodDeclaration,
                                                       ClassOrInterfaceDeclaration sortingDomainClassOrInterfaceDeclaration) {
        if (methodDeclaration.getParameters().stream().filter(p ->
                p.getType().asString().endsWith(getSimpleNameFromClass(SORT_CLASS))).findAny().isPresent()) {
            return addSortParams(compilationUnit, sortingDomainClassOrInterfaceDeclaration);
        }
        return Collections.emptyList();
    }

    protected List<NormalAnnotationExpr> addSortParams(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration sortingDomainClassOrInterfaceDeclaration) {
        List<String> sortParams = getSearchParametersFromClassOrInterface(compilationUnit, sortingDomainClassOrInterfaceDeclaration);
        if (sortParams.isEmpty()) {
            return Collections.emptyList();
        }
        return
                Collections.singletonList(
                        new NormalAnnotationExpr(getNameFromClass(PARAMETER_CLASS),
                                new NodeList<>(Arrays.asList(
                                        new MemberValuePair(PARAMETER_NAME,
                                                new StringLiteralExpr(SORT_PARAM)
                                        ),
                                        new MemberValuePair(PARAMETER_DESCRIPTION,
                                                new StringLiteralExpr(
                                                        escapeString(
                                                                String.format("The sorting criteria(s). Syntax: ((%s)=<value>,(asc|desc))*",
                                                                        String.join(SEARCH_ATTRIBUTE_OR, sortParams))
                                                        ))
                                        ),
                                        new MemberValuePair(PARAMETER_IN,
                                                new FieldAccessExpr(new TypeExpr(
                                                        getClassOrInterfaceTypeFromClassName(compilationUnit, PARAMETER_IN_CLASS)
                                                ), PARAMETER_IN_QUERY)
                                        )
                                ))
                        ));
    }

    protected void hidePageableSortAndPredicateMethodParameters(MethodDeclaration methodDeclaration) {
        methodDeclaration.getParameters().stream().filter(p -> QUERYDSL_PREDICATE_CLASS.endsWith(p.getType().asString())).
                forEach(p -> addParameterHideAnnotation(methodDeclaration, p.getNameAsString()));
        methodDeclaration.getParameters().stream().filter(p -> SORT_CLASS.endsWith(p.getType().asString())).
                forEach(p -> addParameterHideAnnotation(methodDeclaration, p.getNameAsString()));
        methodDeclaration.getParameters().stream().filter(p -> PAGEABLE_CLASS.endsWith(p.getType().asString())).
                forEach(p -> addParameterHideAnnotation(methodDeclaration, p.getNameAsString()));
    }

    protected List<NormalAnnotationExpr> getPageableSortingAndPredicateParameterAnnotations(MethodDeclaration methodDeclaration, ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                                                                            List<String> methodParameterClasses) {
        List<NormalAnnotationExpr> parameters = new ArrayList<>();
        Pair<CompilationUnit, ClassOrInterfaceDeclaration> compilationUnitClassOrInterfaceDeclarationPair =
                parseClassOrInterfaceType(compilationUnit, getDomainClass(compilationUnit, classOrInterfaceDeclaration));
        for (String paramClass : methodParameterClasses) {
            if (paramClass.endsWith(getSimpleNameFromClass(PAGEABLE_CLASS))) {
                parameters.addAll(getPageableParams(methodDeclaration,
                        compilationUnitClassOrInterfaceDeclarationPair.b));
            } else if (paramClass.endsWith(getSimpleNameFromClass(QUERYDSL_PREDICATE_CLASS))) {
                parameters.addAll(getPredicateParams(compilationUnit, methodDeclaration,
                        compilationUnitClassOrInterfaceDeclarationPair.b));
            } else if (paramClass.endsWith(getSimpleNameFromClass(SORT_CLASS))) {
                parameters.addAll(getSortParams(compilationUnit, methodDeclaration,
                        compilationUnitClassOrInterfaceDeclarationPair.b));
            }
        }
        return parameters;
    }

    protected void addPathParamAnnotation(MethodDeclaration methodDeclaration, String parameterName, boolean required,
                                          String defaultParamDescription) {
        addParameterAnnotation(methodDeclaration, parameterName, JAXRS_PATH_PARAM_CLASS, required, defaultParamDescription);
    }

    protected void addQueryParamAnnotation(MethodDeclaration methodDeclaration, String parameterName, boolean required,
                                           String defaultParamDescription) {
        addParameterAnnotation(methodDeclaration, parameterName, JAXRS_QUERY_PARAM_CLASS, required, defaultParamDescription);
    }

    protected void addParameterAnnotation(MethodDeclaration methodDeclaration, String parameterName,
                                          String jaxRsAnnotationClass, boolean required, String defaultParamDescription) {
        Javadoc javadoc = getJavadoc(methodDeclaration);
        String paramDescription = defaultParamDescription;
        if (javadoc != null) {
            paramDescription = getJavadocParameter(javadoc, parameterName);
        }
        AnnotationExpr jaxRsAnnotationExpr = new NormalAnnotationExpr(getNameFromClass(jaxRsAnnotationClass),
                new NodeList<>(Collections.singletonList(new MemberValuePair(JAXRS_QUERY_PATH_PARAM_VALUE,
                        new StringLiteralExpr(parameterName)))));
        methodDeclaration.getParameters().stream().filter(p -> p.getNameAsString().equals(parameterName)).
                forEach(p -> p.addAnnotation(jaxRsAnnotationExpr));
        if (required || paramDescription != null) {
            NormalAnnotationExpr openApiAnnotationExpr = new NormalAnnotationExpr(getNameFromClass(PARAMETER_CLASS), new NodeList<>());
            if (required) {
                openApiAnnotationExpr.addPair(PARAMETER_REQUIRED, new BooleanLiteralExpr(required));
            }
            if (paramDescription != null) {
                openApiAnnotationExpr.addPair(PARAMETER_DESCRIPTION, new StringLiteralExpr(escapeString(paramDescription)));
            }
            methodDeclaration.getParameters().stream().filter(p -> p.getNameAsString().equals(parameterName)).
                    forEach(p -> p.addAnnotation(openApiAnnotationExpr));
        }
    }

    protected void addParameterHideAnnotation(MethodDeclaration methodDeclaration, String parameterName) {
        NormalAnnotationExpr openApiAnnotationExpr = new NormalAnnotationExpr(getNameFromClass(PARAMETER_CLASS), new NodeList<>());
        openApiAnnotationExpr.addPair(PARAMETER_HIDDEN, new BooleanLiteralExpr(true));
        openApiAnnotationExpr.addPair(PARAMETER_NAME, new StringLiteralExpr(escapeString(parameterName)));
        methodDeclaration.getParameters().stream().filter(p -> p.getNameAsString().equals(parameterName)).
                forEach(p -> p.addAnnotation(openApiAnnotationExpr));
    }

    protected void addJaxRsProducesAnnotation(MethodDeclaration methodDeclaration, String... contentTypes) {
        NormalAnnotationExpr jaxRsProducesAnnotationExpr = new NormalAnnotationExpr(getNameFromClass(PRODUCES_CLASS),
                new NodeList<>(Collections.singletonList(new MemberValuePair(ANNOTATION_VALUE,
                        new ArrayInitializerExpr(new NodeList(
                                Arrays.asList(contentTypes).stream().map(c -> new StringLiteralExpr(c)).collect(Collectors.toList())
                        ))))));
        methodDeclaration.addAnnotation(jaxRsProducesAnnotationExpr);
    }

    protected NormalAnnotationExpr createSchemaAnnotation(String domainClass) {
        return new NormalAnnotationExpr(getNameFromClass(SCHEMA_ANNOTATION_CLASS),
                new NodeList<>(Collections.singletonList(new MemberValuePair(SCHEMA_IMPLEMENTATION,
                        new ClassExpr(new ClassOrInterfaceType(null, domainClass))))));
    }

    protected NormalAnnotationExpr createContentAnnotation(NormalAnnotationExpr schemaAnnotationExpr, String mediaType) {
        return new NormalAnnotationExpr(getNameFromClass(CONTENT_ANNOTATION_CLASS),
                new NodeList<>(Arrays.asList(
                        new MemberValuePair(CONTENT_MEDIATYPE, new StringLiteralExpr(mediaType)),
                        new MemberValuePair(CONTENT_SCHEMA, schemaAnnotationExpr)
                )));
    }

    protected NormalAnnotationExpr createListContentAnnotation(NormalAnnotationExpr schemaAnnotationExpr, String mediaType) {
        return new NormalAnnotationExpr(getNameFromClass(CONTENT_ANNOTATION_CLASS),
                new NodeList<>(Arrays.asList(
                        new MemberValuePair(CONTENT_MEDIATYPE, new StringLiteralExpr(mediaType)),
                        new MemberValuePair(CONTENT_ARRAY,
                                new NormalAnnotationExpr(getNameFromClass(ARRAY_SCHEMA_ANNOTATION_CLASS),
                                        new NodeList<>(Collections.singleton(
                                                new MemberValuePair(CONTENT_SCHEMA, schemaAnnotationExpr))
                                        ))
                        ))));
    }

    protected MemberValuePair createContentAnnotationMemberForListType(ClassOrInterfaceType classOrInterfaceType) {
        NormalAnnotationExpr schemaAnnotationExpr = createSchemaAnnotation(classOrInterfaceType.asString());
        NormalAnnotationExpr contentJsonAnnotationExpr = createListContentAnnotation(schemaAnnotationExpr, MEDIATYPE_JSON);
        NormalAnnotationExpr contentJsonHalAnnotationExpr = createListContentAnnotation(schemaAnnotationExpr, MEDIATYPE_JSON_HAL);
        return new MemberValuePair(REQUEST_BODY_API_RESPONSE_CONTENT,
                new ArrayInitializerExpr(
                        new NodeList<>(Arrays.asList(contentJsonAnnotationExpr, contentJsonHalAnnotationExpr))
                ));
    }

    protected MemberValuePair createContentAnnotationMemberForType(ClassOrInterfaceType classOrInterfaceType) {
        NormalAnnotationExpr schemaAnnotationExpr = createSchemaAnnotation(classOrInterfaceType.asString());
        NormalAnnotationExpr contentJsonAnnotationExpr = createContentAnnotation(schemaAnnotationExpr, MEDIATYPE_JSON);
        NormalAnnotationExpr contentJsonHalAnnotationExpr = createContentAnnotation(schemaAnnotationExpr, MEDIATYPE_JSON_HAL);
        return new MemberValuePair(REQUEST_BODY_API_RESPONSE_CONTENT,
                new ArrayInitializerExpr(
                        new NodeList<>(Arrays.asList(contentJsonAnnotationExpr, contentJsonHalAnnotationExpr))
                ));
    }

    protected MemberValuePair createContentAnnotationMember(CompilationUnit compilationUnit,
                                                            ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        ClassOrInterfaceType domainClassOrInterfaceType = getDomainClass(compilationUnit, classOrInterfaceDeclaration);
        return createContentAnnotationMemberForType(domainClassOrInterfaceType);
    }

    protected NormalAnnotationExpr createApiResponseAnnotation20xWithContent(CompilationUnit compilationUnit,
                                                                             ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                                                             int statusCode) {
        return createApiResponseAnnotation20xWithContentAnnotation(statusCode, getDomainSummary(compilationUnit, classOrInterfaceDeclaration),
                createContentAnnotationMember(compilationUnit, classOrInterfaceDeclaration));
    }

    protected NormalAnnotationExpr createApiResponseAnnotation20xWithContentAnnotation(int statusCode,
                                                                                       String summary,
                                                                                       MemberValuePair contentAnnotationMemberValuePair) {
        return new NormalAnnotationExpr(getNameFromClass(API_RESPONSE_CLASS),
                new NodeList<>(Arrays.asList(
                        new MemberValuePair(API_RESPONSE_RESPONSE_CODE, new StringLiteralExpr(Integer.toString(statusCode))),
                        new MemberValuePair(REQUEST_BODY_API_RESPONSE_DESCRIPTION, new StringLiteralExpr(
                                escapeString(summary))),
                        contentAnnotationMemberValuePair
                )));
    }

    protected NormalAnnotationExpr createApiResponseAnnotation20xWithContentForType(int statusCode, ClassOrInterfaceType classOrInterfaceType) {

        return createApiResponseAnnotation20xWithContentAnnotation(statusCode, getTypeSummary(classOrInterfaceType),
                createContentAnnotationMemberForType(classOrInterfaceType));
    }

    protected void removeMethodParameterAnnotation(MethodDeclaration methodDeclaration, String annotationClass) {
        methodDeclaration.getParameters().stream().map(Parameter::getAnnotations).forEach(n -> n.removeIf(a -> a.getName().getIdentifier().equals(getSimpleNameFromClass(annotationClass))));
    }

    private String getDomainSummary(CompilationUnit compilationUnit,
                                    ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        ClassOrInterfaceType domainClassOrInterfaceType = getDomainClass(compilationUnit, classOrInterfaceDeclaration);
        return getTypeSummary(domainClassOrInterfaceType);
    }

    private boolean isPropertyIgnored(FieldDeclaration fieldDeclaration) {
        if (fieldDeclaration.isAnnotationPresent(getSimpleNameFromClass(JSON_IGNORE_CLASS))) {
            return true;
        }
        Optional<AnnotationExpr> jsonPropertyAnnotationExpr = fieldDeclaration.getAnnotationByName(getSimpleNameFromClass(JSON_PROPERTY_CLASS));
        return jsonPropertyAnnotationExpr.filter(annotationExpr -> ((NormalAnnotationExpr) annotationExpr).getPairs().stream().
                anyMatch(p -> p.getName().asString().endsWith(JSON_PROPERTY_ACCESS) &&
                        p.getValue().asFieldAccessExpr().getName().asString().
                                equals(JSON_PROPERTY_WRITE_ONLY))).isPresent();
    }

    private boolean isPrimitive(Type type) {
        return type instanceof PrimitiveType;
    }

    private boolean isPrimitiveObject(ClassOrInterfaceType classOrInterfaceType) {
        switch (classOrInterfaceType.getName().getIdentifier()) {
            case "String":
            case "Long":
            case "Integer":
            case "Double":
            case "Float":
            case "Boolean":
                return true;
        }
        return false;
    }

    private boolean isSingularObject(ClassOrInterfaceType classOrInterfaceType) {
        switch (classOrInterfaceType.getName().getIdentifier()) {
            case "Map":
            case "List":
            case "Set":
            case "Collection":
                return false;
        }
        return true;
    }

    private String getTypeSummary(ClassOrInterfaceType classOrInterfaceType) {
        if (isPrimitiveObject(classOrInterfaceType)) {
            return null;
        }
        Pair<CompilationUnit, ClassOrInterfaceDeclaration> compilationUnitClassOrInterfaceDeclarationPair =
                parseClassOrInterfaceType(compilationUnit, classOrInterfaceType);
        Javadoc javadoc = getJavadoc(compilationUnitClassOrInterfaceDeclarationPair.b);
        if (javadoc == null) {
            return null;
        }
        return getJavadocSummary(getJavadocText(javadoc));
    }

    protected NormalAnnotationExpr createRequestBodyAnnotation(CompilationUnit compilationUnit,
                                                               ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        String summary = getDomainSummary(compilationUnit, classOrInterfaceDeclaration);
        return new NormalAnnotationExpr(getNameFromClass(REQUEST_BODY_CLASS),
                new NodeList<>(Arrays.asList(
                        new MemberValuePair(REQUEST_BODY_API_RESPONSE_DESCRIPTION, new StringLiteralExpr(summary)),
                        createContentAnnotationMember(compilationUnit, classOrInterfaceDeclaration)
                )));
    }

    protected NormalAnnotationExpr createRequestBodyAnnotationForType(ClassOrInterfaceType classOrInterfaceType) {
        String summary = getTypeSummary(classOrInterfaceType);
        return new NormalAnnotationExpr(getNameFromClass(REQUEST_BODY_CLASS),
                new NodeList<>(Arrays.asList(
                        new MemberValuePair(REQUEST_BODY_API_RESPONSE_DESCRIPTION, new StringLiteralExpr(summary)),
                        createContentAnnotationMemberForType(classOrInterfaceType)
                )));
    }

    protected NormalAnnotationExpr createApiResponseAnnotation200WithContentForListType(ClassOrInterfaceType classOrInterfaceType) {
        return createApiResponseAnnotation20xWithContentAnnotation(200, getTypeSummary(classOrInterfaceType),
                createContentAnnotationMemberForListType(classOrInterfaceType));
    }

    protected NormalAnnotationExpr createApiResponseAnnotation200WithContent(CompilationUnit compilationUnit,
                                                                             ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        return createApiResponseAnnotation20xWithContent(compilationUnit, classOrInterfaceDeclaration, 200);
    }

    protected NormalAnnotationExpr createApiResponseAnnotation201WithContent(CompilationUnit compilationUnit,
                                                                             ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        return createApiResponseAnnotation20xWithContent(compilationUnit, classOrInterfaceDeclaration, 201);
    }

    protected NormalAnnotationExpr createApiResponse204() {
        return new NormalAnnotationExpr(getNameFromClass(API_RESPONSE_CLASS),
                new NodeList<>(Arrays.asList(
                        new MemberValuePair(API_RESPONSE_RESPONSE_CODE, new StringLiteralExpr("204")),
                        new MemberValuePair(REQUEST_BODY_API_RESPONSE_DESCRIPTION, new StringLiteralExpr("No Content."))
                )));
    }

    protected MethodDeclaration addInterfaceMethod(ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                                   String methodName, Type returnType,
                                                   Parameter... parameters) {
        return classOrInterfaceDeclaration.addMethod(methodName,
                Modifier.Keyword.PUBLIC).setParameters(
                new NodeList(Arrays.asList(parameters))).setType(returnType).removeBody().removeModifier(
                Modifier.Keyword.PUBLIC);
    }

    protected void addOperationAnnotation(BodyDeclaration<?> bodyDeclaration,
                                          List<NormalAnnotationExpr> parameters,
                                          NormalAnnotationExpr requestBody,
                                          List<NormalAnnotationExpr> responses,
                                          String defaultSummary) {
        Javadoc javadoc = getJavadoc(bodyDeclaration);
        String summary = defaultSummary;
        String description = null;
        if (javadoc != null) {
            summary = getJavadocSummary(getJavadocText(javadoc));
            description = getJavadocDescription(getJavadocText(javadoc));
        }
        NormalAnnotationExpr annotationExpr = new NormalAnnotationExpr(getNameFromClass(OPERATION_ANNOTATION_CLASS),
                new NodeList<>());
        if (summary != null) {
            annotationExpr.addPair(OPERATION_SUMMARY, new StringLiteralExpr(escapeString(summary)));
        }
        if (description != null) {
            annotationExpr.addPair(OPERATION_DESCRIPTION, new StringLiteralExpr(escapeString(description)));
        }
        if (parameters != null && !parameters.isEmpty()) {
            annotationExpr.addPair(OPERATION_PARAMETERS, new ArrayInitializerExpr(new NodeList(parameters)));
        }
        if (requestBody != null) {
            annotationExpr.addPair(OPERATION_REQUEST_BODY, requestBody);
        }
        if (responses != null && !responses.isEmpty()) {
            annotationExpr.addPair(OPERATION_RESPONSES, new ArrayInitializerExpr(new NodeList(responses)));
        }
        bodyDeclaration.addAnnotation(annotationExpr);
    }

    protected void addOperationAnnotation(BodyDeclaration<?> bodyDeclaration,
                                          NormalAnnotationExpr requestBody,
                                          List<NormalAnnotationExpr> responses,
                                          String defaultSummary) {
        addOperationAnnotation(bodyDeclaration, null, requestBody, responses, defaultSummary);
    }

    // JAX-RS

    protected void removeJaxRsAnnotations(CompilationUnit compilationUnit, BodyDeclaration<?> bodyDeclaration) {
        super.removeJaxRsAnnotations(compilationUnit, bodyDeclaration);
        removeAnnotation(compilationUnit, bodyDeclaration, JAXRS_GET_CLASS);
        removeAnnotation(compilationUnit, bodyDeclaration, JAXRS_POST_CLASS);
        removeAnnotation(compilationUnit, bodyDeclaration, JAXRS_PUT_CLASS);
        removeAnnotation(compilationUnit, bodyDeclaration, JAXRS_DELETE_CLASS);
    }

    protected void addGETAnnotation(BodyDeclaration<?> bodyDeclaration) {
        addMarkerAnnotation(bodyDeclaration, JAXRS_GET_CLASS);
    }

    protected void addPOSTAnnotation(BodyDeclaration<?> bodyDeclaration) {
        addMarkerAnnotation(bodyDeclaration, JAXRS_POST_CLASS);
    }

    protected void addDELETEAnnotation(BodyDeclaration<?> bodyDeclaration) {
        addMarkerAnnotation(bodyDeclaration, JAXRS_DELETE_CLASS);
    }

    protected void addPUTAnnotation(BodyDeclaration<?> bodyDeclaration) {
        addMarkerAnnotation(bodyDeclaration, JAXRS_PUT_CLASS);
    }

    protected void addPATCHAnnotation(BodyDeclaration<?> bodyDeclaration) {
        addMarkerAnnotation(bodyDeclaration, JAXRS_PATCH_CLASS);
    }

    // CRUD

    protected boolean checkIfExtendingCrudInterface(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        for (ClassOrInterfaceType extent : classOrInterfaceDeclaration.getExtendedTypes()) {
            switch (extent.getName().getIdentifier()) {
                case PAGING_AND_SORTING_REPOSITORY:
                case CRUD_REPOSITORY:
                    return true;
                default:
                    // visit interface to get information
                    if (getSourceFile(compilationUnit, extent).exists()) {
                        Pair<CompilationUnit, ClassOrInterfaceDeclaration> compilationUnitClassOrInterfaceDeclarationPair = parseClassOrInterfaceType(compilationUnit, extent);
                        boolean extending = checkIfExtendingCrudInterface(compilationUnitClassOrInterfaceDeclarationPair.a,
                                compilationUnitClassOrInterfaceDeclarationPair.b);
                        if (extending) {
                            return extending;
                        }
                    }
            }
        }
        return false;
    }

    protected boolean checkIfExtendingQuerydslInterface(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        for (ClassOrInterfaceType extent : classOrInterfaceDeclaration.getExtendedTypes()) {
            switch (extent.getName().getIdentifier()) {
                case QUERYDSL_PREDICATE_EXECUTOR:
                    return true;
                default:
                    // visit interface to get information
                    if (getSourceFile(compilationUnit, extent).exists()) {
                        Pair<CompilationUnit, ClassOrInterfaceDeclaration> compilationUnitClassOrInterfaceDeclarationPair = parseClassOrInterfaceType(compilationUnit, extent);
                        boolean extending = checkIfExtendingQuerydslInterface(compilationUnitClassOrInterfaceDeclarationPair.a,
                                compilationUnitClassOrInterfaceDeclarationPair.b);
                        if (extending) {
                            return extending;
                        }
                    }
            }
        }
        return false;
    }

    protected void removeCrudOperationAnnotationAndMethod(MethodDeclaration methodDeclaration, CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        if (methodDeclaration == null) {
            return;
        }
        if (checkIfExtendingCrudInterface(compilationUnit, classOrInterfaceDeclaration)) {
            // if a resource annotation was added it is marked as user added method and will not be removed
            if (!checkResourceAnnotationPresent(methodDeclaration).isPresent()) {
                methodDeclaration.remove();
            }
        }
        // remove Operation annotation and JAX-RS
        removeJaxRsAnnotations(compilationUnit, methodDeclaration);
        removeAnnotation(compilationUnit, methodDeclaration, OPERATION_ANNOTATION_CLASS);
    }

    protected void removeQuerydslOperationAnnotationAndMethod(MethodDeclaration methodDeclaration, CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        if (methodDeclaration == null) {
            return;
        }
        if (checkIfExtendingQuerydslInterface(compilationUnit, classOrInterfaceDeclaration)) {
            // if a resource annotation was added it is marked as user added method
            if (!checkResourceAnnotationPresent(methodDeclaration).isPresent()) {
                methodDeclaration.remove();
            }
        }
        // remove Operation annotation and JAX-RS
        removeJaxRsAnnotations(compilationUnit, methodDeclaration);
        removeAnnotation(compilationUnit, methodDeclaration, OPERATION_ANNOTATION_CLASS);
    }

    // finders

    protected String getMethodPath(MethodDeclaration methodDeclaration) {
        return methodDeclaration.getSignature().getName();
    }

    private boolean checkIfExtendingRepository(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        if (checkIfExtendingCrudInterface(compilationUnit, classOrInterfaceDeclaration)) {
            return true;
        }
        for (ClassOrInterfaceType extent : classOrInterfaceDeclaration.getExtendedTypes()) {
            switch (extent.getName().getIdentifier()) {
                case REPOSITORY:
                    return true;
                default:
                    // visit interface to get information
                    if (getSourceFile(compilationUnit, extent).exists()) {
                        Pair<CompilationUnit, ClassOrInterfaceDeclaration> compilationUnitClassOrInterfaceDeclarationPair = parseClassOrInterfaceType(compilationUnit, extent);
                        boolean extending = checkIfExtendingRepository(compilationUnitClassOrInterfaceDeclarationPair.a,
                                compilationUnitClassOrInterfaceDeclarationPair.b);
                        if (extending) {
                            return extending;
                        }
                    }
            }
        }
        return false;
    }

    /**
     * Checks if this class has a concrete type.
     *
     * @param compilationUnit             The compilation unit.
     * @param classOrInterfaceDeclaration The class or interface declaration.
     * @return <code>true</code> if the repository is not generic.
     */
    protected boolean isConcreteRepositoryClass(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        if (!checkIfExtendingRepository(compilationUnit, classOrInterfaceDeclaration)) {
            return false;
        }
       return isConcreteClass(classOrInterfaceDeclaration);
    }

    /**
     * Checks if this class has a concrete type.
     *
     * @param classOrInterfaceDeclaration The class or interface declaration.
     * @return <code>true</code> if the repository is not generic.
     */
    protected boolean isConcreteClass(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        NodeList<TypeParameter> typeParameters = classOrInterfaceDeclaration.getTypeParameters();
        // concrete classes have no types
        if (typeParameters.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Checks if this method is from a class with a concrete type.
     *
     * @param methodDeclaration The method declaration.
     * @return <code>true</code> if the repository is not generic.
     */
    protected boolean isMethodOfConcreteRepositoryClass(MethodDeclaration methodDeclaration) {
        Optional<Node> parent = methodDeclaration.getParentNode();
        if (parent.isPresent() && parent.get() instanceof ClassOrInterfaceDeclaration) {
            return isConcreteClass((ClassOrInterfaceDeclaration) parent.get());
        }
        return false;
    }

    /**
     * Method to search methods matching a name and the passed parameter types, which should be passed as FQ class name.
     * <p>
     * The default {@link ClassOrInterfaceDeclaration#getMethodsBySignature(String, String...)} is not checking
     * if a FQ class name or simple class name is passed.
     * </p>
     *
     * @param compilationUnit             The compilation unit.
     * @param classOrInterfaceDeclaration The class or interface declaration.
     * @param methodName                  The method name.
     * @param paramTypes                  The parameter types as FQ class name.
     * @return the matched method or <code>null</code>.
     */
    protected MethodDeclaration findMethodByMethodNameAndParameters(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                                                    String methodName, String... paramTypes) {
        if (!checkIfExtendingRepository(compilationUnit, classOrInterfaceDeclaration)) {
            return null;
        }
        List<MethodDeclaration> methodDeclarations = classOrInterfaceDeclaration.getMethodsByName(methodName);
        // align FQ names to simple names if necessary
        for (MethodDeclaration methodDeclaration : methodDeclarations) {
            List<String> inspectedMethodParams = new ArrayList<>();
            List<String> targetMethodParams = new ArrayList<>();
            if (methodDeclaration.getParameters().size() != paramTypes.length) {
                continue;
            }
            for (int i = 0; i < methodDeclaration.getParameters().size(); i++) {
                if (methodDeclaration.getParameter(i).getTypeAsString().contains(DOT) &&
                        !paramTypes[i].contains(DOT)) {
                    inspectedMethodParams.add(getSimpleNameFromClass(methodDeclaration.getParameter(i).
                            getTypeAsString()));
                    targetMethodParams.add(paramTypes[i]);
                } else if (paramTypes[i].contains(DOT) &&
                        !methodDeclaration.getParameter(i).getTypeAsString().contains(DOT)) {
                    inspectedMethodParams.add(methodDeclaration.getParameter(i).
                            getTypeAsString());
                    targetMethodParams.add(getSimpleNameFromClass(paramTypes[i]));
                } else {
                    inspectedMethodParams.add(methodDeclaration.getParameter(i).
                            getTypeAsString());
                    targetMethodParams.add(paramTypes[i]);
                }
            }
            if (inspectedMethodParams.equals(targetMethodParams)) {
                return methodDeclaration;
            }
        }
        return null;
    }

    protected MethodDeclaration findClosestMethod(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                                  String methodName, String... paramTypes) {
        MethodDeclaration methodDeclaration = findMethodByMethodNameAndParameters(compilationUnit, classOrInterfaceDeclaration,
                methodName, paramTypes);
        if (methodDeclaration != null) {
            return methodDeclaration;
        }
        // check in implementations, too
        for (ClassOrInterfaceType extent : classOrInterfaceDeclaration.getExtendedTypes()) {
            if (getSourceFile(compilationUnit, extent).exists()) {
                Pair<CompilationUnit, ClassOrInterfaceDeclaration> compilationUnitClassOrInterfaceDeclarationPair = parseClassOrInterfaceType(compilationUnit, extent);
                methodDeclaration = findClosestMethod(compilationUnitClassOrInterfaceDeclarationPair.a,
                        compilationUnitClassOrInterfaceDeclarationPair.b,
                        methodName, paramTypes);
                if (methodDeclaration != null) {
                    return methodDeclaration;
                }
            }
        }
        return null;
    }

    protected List<MethodDeclaration> findCustomMethods(CompilationUnit compilationUnit,
                                                        ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                                        String customMethodPrefix, Set<String> excludedMethods) {
        if (!checkIfExtendingRepository(compilationUnit, classOrInterfaceDeclaration)) {
            return Collections.emptyList();
        }
        List<MethodDeclaration> customerFinderMethodDeclarations =
                classOrInterfaceDeclaration.getMethods().stream().filter(
                        m -> !excludedMethods.contains(m.getSignature().getName())
                                && m.getSignature().getName().startsWith(customMethodPrefix)
                ).collect(Collectors.toList());
        // check in implementations, too
        for (ClassOrInterfaceType extent : classOrInterfaceDeclaration.getExtendedTypes()) {
            if (getSourceFile(compilationUnit, extent).exists()) {
                Pair<CompilationUnit, ClassOrInterfaceDeclaration> compilationUnitClassOrInterfaceDeclarationPair = parseClassOrInterfaceType(compilationUnit, extent);
                List<MethodDeclaration> otherMethodDeclarations = findCustomMethods(compilationUnitClassOrInterfaceDeclarationPair.a,
                        compilationUnitClassOrInterfaceDeclarationPair.b, customMethodPrefix, excludedMethods);
                customerFinderMethodDeclarations.addAll(otherMethodDeclarations);
            }
        }
        return customerFinderMethodDeclarations;
    }

    protected MethodDeclaration findClosestMethodFromMethodVariants(CompilationUnit compilationUnit,
                                                                    ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                                                    List<Pair<String, List<String>>> methodVariants) {
        for (Pair<String, List<String>> methodParameterEntry : methodVariants) {
            String[] params = methodParameterEntry.b != null ?
                    methodParameterEntry.b.toArray(new String[0]) : null;
            MethodDeclaration methodDeclaration = findClosestMethod(compilationUnit, classOrInterfaceDeclaration,
                    methodParameterEntry.a, params);
            if (methodDeclaration != null) {
                AnnotationExpr methodResource = findClosestMethodResourceAnnotation(compilationUnit,
                        classOrInterfaceDeclaration, methodParameterEntry.a, params);
                if (methodResource != null) {
                    if (checkResourceExported(methodResource)) {
                        return methodDeclaration;
                    }
                } else {
                    return methodDeclaration;
                }
            }
        }
        return null;
    }

    protected AnnotationExpr findClosestMethodResourceAnnotation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                                                 String methodName, String... paramTypes) {
        MethodDeclaration methodDeclaration = findMethodByMethodNameAndParameters(compilationUnit, classOrInterfaceDeclaration,
                methodName, paramTypes);
        if (methodDeclaration != null) {
            // first found annotation in class / interface hierarchy
            Optional<AnnotationExpr> annotationExprOptional = checkResourceAnnotationPresent(methodDeclaration);
            if (annotationExprOptional.isPresent()) {
                return annotationExprOptional.get();
            }
        }
        // check in implementations, too
        for (ClassOrInterfaceType extent : classOrInterfaceDeclaration.getExtendedTypes()) {
            if (getSourceFile(compilationUnit, extent).exists()) {
                Pair<CompilationUnit, ClassOrInterfaceDeclaration> compilationUnitClassOrInterfaceDeclarationPair = parseClassOrInterfaceType(compilationUnit, extent);
                AnnotationExpr annotationExpr = findClosestMethodResourceAnnotation(compilationUnitClassOrInterfaceDeclarationPair.a,
                        compilationUnitClassOrInterfaceDeclarationPair.b, methodName, paramTypes);
                if (annotationExpr != null) {
                    return annotationExpr;
                }
            }
        }
        return null;
    }

    protected Javadoc findClosestMethodJavadoc(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                               String methodName, String... paramTypes) {
        MethodDeclaration methodDeclaration = findMethodByMethodNameAndParameters(compilationUnit, classOrInterfaceDeclaration,
                methodName, paramTypes);
        if (methodDeclaration != null) {
            Optional<Javadoc> javadoc = methodDeclaration.getJavadoc();
            if (javadoc.isPresent()) {
                return javadoc.get();
            }
        }
        // check in implementations, too
        for (ClassOrInterfaceType extent : classOrInterfaceDeclaration.getExtendedTypes()) {
            if (getSourceFile(compilationUnit, extent).exists()) {
                Pair<CompilationUnit, ClassOrInterfaceDeclaration> compilationUnitClassOrInterfaceDeclarationPair = parseClassOrInterfaceType(compilationUnit, extent);
                Javadoc javadoc = findClosestMethodJavadoc(compilationUnitClassOrInterfaceDeclarationPair.a,
                        compilationUnitClassOrInterfaceDeclarationPair.b, methodName, paramTypes);
                if (javadoc != null) {
                    return javadoc;
                }
            }
        }
        return null;
    }
}
