package de.ohmesoftware.springdataresttoopenapischema;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
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
    protected static final String SCHEMA_IMPLEMENTATION = "implementation";
    protected static final String CONTENT_ANNOTATION_CLASS = "io.swagger.v3.oas.annotations.media.Content";
    protected static final String CONTENT_MEDIATYPE = "mediaType";
    protected static final String CONTENT_SCHEMA = "schema";
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

    // annotations

    protected void addMarkerAnnotation(BodyDeclaration<?> bodyDeclaration,
                                       String annotationClass) {
        bodyDeclaration.addAndGetAnnotation(annotationClass);
    }

    // Operation annotations

    protected List<NormalAnnotationExpr> getPageableParams(MethodDeclaration methodDeclaration,
                                                           ClassOrInterfaceDeclaration sortingDomainClassOrInterfaceDeclaration) {
        return methodDeclaration.getParameters().stream().filter(p ->
                p.getTypeAsString().equals(getSimpleNameFromClass(PAGEABLE_CLASS))).
                findFirst().
                map(p -> {
                            List<NormalAnnotationExpr> annotationExprs = addSortParams(sortingDomainClassOrInterfaceDeclaration);
                            annotationExprs.addAll(
                                    Arrays.asList(
                                            new NormalAnnotationExpr(getNameFromClass(PARAMETER_CLASS),
                                                    new NodeList<>(Arrays.asList(
                                                            new MemberValuePair(PAGE_PARAMETER_NAME,
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
                                                    new NodeList<>(Collections.singleton(
                                                            new MemberValuePair(SIZE_PARAMETER_NAME,
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

    protected List<NormalAnnotationExpr> getSortParams(CompilationUnit compilationUnit, MethodDeclaration methodDeclaration,
                                                       ClassOrInterfaceDeclaration sortingDomainClassOrInterfaceDeclaration) {
        List<String> sortParams = getSearchParametersFromClassOrInterface(compilationUnit, sortingDomainClassOrInterfaceDeclaration);
        return methodDeclaration.getParameters().stream().filter(p ->
                p.getTypeAsString().equals(getSimpleNameFromClass(SORT_CLASS))).
                findFirst().
                map(p ->
                        Collections.singletonList(
                                new NormalAnnotationExpr(getNameFromClass(PARAMETER_CLASS),
                                        new NodeList<>(Collections.singleton(
                                                new MemberValuePair(SORT_PARAM,
                                                        new StringLiteralExpr(
                                                                escapeString(
                                                                        String.format("The sorting criteria(s). Syntax: ((%s)=<value>,(asc|desc))*",
                                                                                String.join(SEARCH_ATTRIBUTE_OR, sortParams))
                                                                ))
                                                )))
                                ))).
                orElse(Collections.emptyList());
    }

    protected List<NormalAnnotationExpr> addSortParams(ClassOrInterfaceDeclaration sortingDomainClassOrInterfaceDeclaration) {
        List<String> sortParams = getSearchParametersFromClassOrInterface(compilationUnit, sortingDomainClassOrInterfaceDeclaration);
        return
                Collections.singletonList(
                        new NormalAnnotationExpr(getNameFromClass(PARAMETER_CLASS),
                                new NodeList<>(Arrays.asList(
                                        new MemberValuePair(SORT_PARAM,
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
                                                                                       MemberValuePair contentAnnotationmemberValuePair) {
        return new NormalAnnotationExpr(getNameFromClass(API_RESPONSE_CLASS),
                new NodeList<>(Arrays.asList(
                        new MemberValuePair(API_RESPONSE_RESPONSE_CODE, new StringLiteralExpr(Integer.toString(statusCode))),
                        new MemberValuePair(REQUEST_BODY_API_RESPONSE_DESCRIPTION, new StringLiteralExpr(
                                escapeString(summary))),
                        contentAnnotationmemberValuePair
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
        Optional<AnnotationExpr> jsonPropertyAnnotationExpr = fieldDeclaration.getAnnotationByName(JSON_PROPERTY_CLASS);
        return jsonPropertyAnnotationExpr.filter(annotationExpr -> ((NormalAnnotationExpr) annotationExpr).getPairs().stream().
                anyMatch(p -> p.getName().getIdentifier().equals(JSON_PROPERTY_ACCESS) &&
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
                                                   String methodName, ClassOrInterfaceType returnType,
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
                case QUERY_DSL_PREDICATE_EXECUTOR:
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
            methodDeclaration.remove();
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
            methodDeclaration.remove();
        }
        // remove Operation annotation and JAX-RS
        removeJaxRsAnnotations(compilationUnit, methodDeclaration);
        removeAnnotation(compilationUnit, methodDeclaration, OPERATION_ANNOTATION_CLASS);
    }

    // finders

    protected String getMethodPath(MethodDeclaration methodDeclaration) {
        return methodDeclaration.getSignature().getName();
    }

    protected MethodDeclaration findClosestMethod(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                                  String methodName, String... paramTypes) {
        List<MethodDeclaration> findByIdMethodDeclarations;
        if (paramTypes == null) {
            findByIdMethodDeclarations = classOrInterfaceDeclaration.getMethodsBySignature(methodName);
        }
        else {
            findByIdMethodDeclarations = classOrInterfaceDeclaration.getMethodsBySignature(methodName,
                    paramTypes);
        }
        if (!findByIdMethodDeclarations.isEmpty()) {
            return findByIdMethodDeclarations.stream().findFirst().get();
        }
        // check in implementations, too
        for (ClassOrInterfaceType extent : classOrInterfaceDeclaration.getExtendedTypes()) {
            if (getSourceFile(compilationUnit, extent).exists()) {
                Pair<CompilationUnit, ClassOrInterfaceDeclaration> compilationUnitClassOrInterfaceDeclarationPair = parseClassOrInterfaceType(compilationUnit, extent);
                MethodDeclaration methodDeclaration = findClosestMethod(compilationUnitClassOrInterfaceDeclarationPair.a,
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
                NormalAnnotationExpr methodResource = findClosestMethodResourceAnnotation(compilationUnit,
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

    protected NormalAnnotationExpr findClosestMethodResourceAnnotation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                                                       String methodName, String... paramTypes) {
        List<MethodDeclaration> findByIdMethodDeclarations;
        if (paramTypes != null) {
            findByIdMethodDeclarations = classOrInterfaceDeclaration.getMethodsBySignature(methodName,
                    paramTypes);
        } else {
            findByIdMethodDeclarations = classOrInterfaceDeclaration.getMethodsBySignature(methodName);
        }
        if (!findByIdMethodDeclarations.isEmpty()) {
            MethodDeclaration methodDeclaration = findByIdMethodDeclarations.stream().findFirst().get();
            // first found annotation in class / interface hierarchy
            Optional<AnnotationExpr> annotationExprOptional = checkResourceAnnotationPresent(methodDeclaration);
            if (annotationExprOptional.isPresent()) {
                return annotationExprOptional.get().asNormalAnnotationExpr();
            }
        }
        // check in implementations, too
        for (ClassOrInterfaceType extent : classOrInterfaceDeclaration.getExtendedTypes()) {
            if (getSourceFile(compilationUnit, extent).exists()) {
                Pair<CompilationUnit, ClassOrInterfaceDeclaration> compilationUnitClassOrInterfaceDeclarationPair = parseClassOrInterfaceType(compilationUnit, extent);
                NormalAnnotationExpr annotationExpr = findClosestMethodResourceAnnotation(compilationUnitClassOrInterfaceDeclarationPair.a,
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
        List<MethodDeclaration> findByIdMethodDeclarations = classOrInterfaceDeclaration.getMethodsBySignature(methodName,
                paramTypes);
        if (!findByIdMethodDeclarations.isEmpty()) {
            MethodDeclaration methodDeclaration = findByIdMethodDeclarations.stream().findFirst().get();
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
