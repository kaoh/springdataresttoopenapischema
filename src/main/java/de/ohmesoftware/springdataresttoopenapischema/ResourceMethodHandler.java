package de.ohmesoftware.springdataresttoopenapischema;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.utils.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    protected static final String OPERATION_ID = "operationId";
    protected static final String OPERATION_DESCRIPTION = "description";
    protected static final String OPERATION_REQUEST_BODY = "requestBody";
    protected static final String OPERATION_RESPONSES = "responses";
    protected static final String OPERATION_PARAMETERS = "parameters";

    protected static final String SCHEMA_ANNOTATION_CLASS = "io.swagger.v3.oas.annotations.media.Schema";
    protected static final String ARRAY_SCHEMA_ANNOTATION_CLASS = "io.swagger.v3.oas.annotations.media.ArraySchema";
    protected static final String SCHEMA_IMPLEMENTATION = "implementation";
    protected static final String SCHEMA_REF = "ref";
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

    protected static final String JAXRS_PRODUCES_CLASS = "javax.ws.rs.Produces";
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
    protected static final String SEARCH_ATTRIBUTE_OR = " or ";
    private static final String UNDERSCORE = "_";

    protected static final String JSON_PROPERTY_CLASS = "com.fasterxml.jackson.annotation.JsonProperty";
    protected static final String JSON_PROPERTY_ACCESS = "access";
    protected static final String JSON_PROPERTY_WRITE_ONLY = "WRITE_ONLY";
    protected static final String JSON_IGNORE_CLASS = "com.fasterxml.jackson.annotation.JsonIgnore";

    protected static final String QUERYDSL_PREDICATE_CLASS = "com.querydsl.core.types.Predicate";
    protected static final String ANNOTATION_VALUE = "value";

    private static final String CUSTOM_METHOD_BY = "By";
    private static final String COMPONENTS_SCHEMAS = "#/components/schemas/";

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

    protected void saveClassOrInterfaceToFile(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        CompilationUnit compilationUnit = classOrInterfaceDeclaration.findCompilationUnit().orElseThrow(
                () -> new RuntimeException(String.format("Could not get compilation unit for %s",
                        classOrInterfaceDeclaration.getNameAsString()))
        );
        File newInterface = getSourceFile(classOrInterfaceDeclaration.findCompilationUnit().get(),
                getClassOrInterfaceTypeFromClassName(compilationUnit,
                        classOrInterfaceDeclaration.getNameAsString()

                ));
        try (FileWriter fileWriter = new FileWriter(newInterface)) {
            fileWriter.write(compilationUnit.toString());
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not write Java file: %s", newInterface.getAbsolutePath()), e);
        }

    }

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

    protected Type unwrapOptionalClassOrInterfaceType(Type type) {
        if (type.isClassOrInterfaceType() && type.asClassOrInterfaceType().getName().getIdentifier().equals(Optional.class.getSimpleName()) &&
                type.asClassOrInterfaceType().getTypeArguments().isPresent()) {
            type = type.asClassOrInterfaceType().getTypeArguments().get().get(0);
        }
        return type;
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

    protected NormalAnnotationExpr createParameter(String parameterName, String description) {
        return new NormalAnnotationExpr(getNameFromClass(PARAMETER_CLASS),
                new NodeList<>(Arrays.asList(
                        new MemberValuePair(PARAMETER_NAME,
                                new StringLiteralExpr(parameterName)
                        ),
                        new MemberValuePair(PARAMETER_DESCRIPTION,
                                new StringLiteralExpr(
                                        escapeString(description == null ? "No description" : description)
                                )),
                        new MemberValuePair(PARAMETER_IN,
                                new FieldAccessExpr(new TypeExpr(
                                        getClassOrInterfaceTypeFromClassName(null, PARAMETER_IN_CLASS)
                                ), PARAMETER_IN_QUERY)
                        )
                )));
    }

    // Operation annotations

    protected List<NormalAnnotationExpr> getPageableParams(MethodDeclaration methodDeclaration,
                                                           ClassOrInterfaceDeclaration sortingDomainClassOrInterfaceDeclaration) {
        return methodDeclaration.getParameters().stream().filter(p ->
                p.getType().asString().endsWith(getSimpleNameFromClass(PAGEABLE_CLASS))).
                findFirst().
                map(p -> {
                            List<NormalAnnotationExpr> annotationExprs = new ArrayList<>(addSortParams(sortingDomainClassOrInterfaceDeclaration));
                            annotationExprs.addAll(
                                    Arrays.asList(
                                            createParameter(PAGE_PARAMETER_NAME, "The page number to return."),
                                            createParameter(SIZE_PARAMETER_NAME, "The page size.")
                                    ));
                            return annotationExprs;
                        }
                ).
                orElse(Collections.emptyList());
    }

    private List<String> getSearchParametersFromClass(ClassOrInterfaceDeclaration searchDomainClassDeclaration) {
        List<String> params = new ArrayList<>();
        for (FieldDeclaration fieldDeclaration : searchDomainClassDeclaration.getFields()) {
            if (isPropertyIgnored(fieldDeclaration)) {
                continue;
            }
            VariableDeclarator variableDeclarator = fieldDeclaration.getVariables().get(0);
            if (variableDeclarator.getType().isPrimitiveType() || isPrimitiveObject(variableDeclarator.getType())) {
                params.add(variableDeclarator.getNameAsString());
            } else if (!variableDeclarator.getType().isArrayType() && isSourceCodeAvailableForClassOrInterface(searchDomainClassDeclaration, variableDeclarator.getType())) {
                if (isEnumProperty(searchDomainClassDeclaration.findCompilationUnit().get(),
                        variableDeclarator.getType())) {
                    params.add(variableDeclarator.getNameAsString());
                } else if (!isCollectionObject(variableDeclarator.getType())) {
                    params.add(variableDeclarator.getNameAsString() + SINGLE_OBJECT_SEARCH_PARAM_ELLIPSIS);
                }
            }
        }
        for (ClassOrInterfaceType extent : searchDomainClassDeclaration.getExtendedTypes()) {
            // visit interface to get information
            if (searchDomainClassDeclaration.findCompilationUnit().isPresent() && getSourceFile(
                    searchDomainClassDeclaration.findCompilationUnit().get(), extent).exists()) {
                TypeDeclaration extendTypeDeclaration = parseClassOrInterfaceType(searchDomainClassDeclaration.findCompilationUnit().get(),
                        extent);
                params.addAll(getSearchParametersFromClass(extendTypeDeclaration.asClassOrInterfaceDeclaration()));
            }
        }
        return params;
    }

    protected List<NormalAnnotationExpr> getPredicateParams(MethodDeclaration methodDeclaration,
                                                            ClassOrInterfaceDeclaration predicateDomainClassOrInterfaceDeclaration) {
        List<String> searchParams = getSearchParametersFromClass(predicateDomainClassOrInterfaceDeclaration);
        if (methodDeclaration.getParameters().stream().anyMatch(p ->
                p.getType().asString().endsWith(getSimpleNameFromClass(QUERYDSL_PREDICATE_CLASS)))) {
            List<NormalAnnotationExpr> annotationExprs = new ArrayList<>();
            searchParams.forEach(
                    p ->
                            annotationExprs.add(
                                    createParameter(p, String.format("%s search criteria. Used in SQL like fashion. Syntax: %s=value", p, p))
                            )
            );
            return annotationExprs;
        }
        return Collections.emptyList();
    }

    protected List<NormalAnnotationExpr> getSortParams(MethodDeclaration methodDeclaration,
                                                       ClassOrInterfaceDeclaration sortingDomainClassOrInterfaceDeclaration) {
        if (methodDeclaration.getParameters().stream().anyMatch(p ->
                p.getType().asString().endsWith(getSimpleNameFromClass(SORT_CLASS)))) {
            return addSortParams(sortingDomainClassOrInterfaceDeclaration);
        }
        return Collections.emptyList();
    }

    protected List<NormalAnnotationExpr> addSortParams(ClassOrInterfaceDeclaration sortingDomainClassOrInterfaceDeclaration) {
        List<String> sortParams = getSearchParametersFromClass(sortingDomainClassOrInterfaceDeclaration);
        if (sortParams.isEmpty()) {
            return Collections.emptyList();
        }
        return
                Collections.singletonList(
                        createParameter(SORT_PARAM, String.format("The sorting criteria(s). Can be passed multiple times as query with descending priority. Syntax: sort=(%s),(asc%sdesc)",
                                String.join(SEARCH_ATTRIBUTE_OR, sortParams), SEARCH_ATTRIBUTE_OR))
                );
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
        TypeDeclaration extendTypeDeclaration =
                parseClassOrInterfaceType(classOrInterfaceDeclaration.findCompilationUnit().get(),
                        getDomainClass(classOrInterfaceDeclaration));
        for (String paramClass : methodParameterClasses) {
            if (paramClass.endsWith(getSimpleNameFromClass(PAGEABLE_CLASS))) {
                parameters.addAll(getPageableParams(methodDeclaration,
                        extendTypeDeclaration.asClassOrInterfaceDeclaration()));
            } else if (paramClass.endsWith(getSimpleNameFromClass(QUERYDSL_PREDICATE_CLASS))) {
                parameters.addAll(getPredicateParams(methodDeclaration,
                        extendTypeDeclaration.asClassOrInterfaceDeclaration()));
            } else if (paramClass.endsWith(getSimpleNameFromClass(SORT_CLASS))) {
                parameters.addAll(getSortParams(methodDeclaration,
                        extendTypeDeclaration.asClassOrInterfaceDeclaration()));
            }
        }
        return parameters;
    }

    protected String createCustomNaming(MethodDeclaration methodDeclaration) {
        String methodName = methodDeclaration.getNameAsString();
        methodName = methodName.substring(methodName.indexOf(CUSTOM_METHOD_BY) + CUSTOM_METHOD_BY.length());
        return methodName;
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
        NormalAnnotationExpr jaxRsProducesAnnotationExpr = new NormalAnnotationExpr(getNameFromClass(JAXRS_PRODUCES_CLASS),
                new NodeList<>(Collections.singletonList(new MemberValuePair(ANNOTATION_VALUE,
                        new ArrayInitializerExpr(new NodeList(
                                Arrays.asList(contentTypes).stream().map(c -> new StringLiteralExpr(c)).collect(Collectors.toList())
                        ))))));
        methodDeclaration.addAnnotation(jaxRsProducesAnnotationExpr);
    }

    protected NormalAnnotationExpr createSchemaRefAnnotation(String ref) {
        return new NormalAnnotationExpr(getNameFromClass(SCHEMA_ANNOTATION_CLASS),
                new NodeList<>(Collections.singletonList(new MemberValuePair(SCHEMA_REF,
                        new StringLiteralExpr(COMPONENTS_SCHEMAS +ref)))));

    }

    protected NormalAnnotationExpr createSchemaAnnotation(Type domainClassOrInterfaceType) {
        if (domainClassOrInterfaceType.isPrimitiveType()) {
            switch (domainClassOrInterfaceType.asString()) {
                case "int":
                    domainClassOrInterfaceType = getClassOrInterfaceTypeFromClassName(null, Integer.class.getName());
                    break;
                case "long":
                    domainClassOrInterfaceType = getClassOrInterfaceTypeFromClassName(null, Long.class.getName());
                    break;
                case "double":
                    domainClassOrInterfaceType = getClassOrInterfaceTypeFromClassName(null, Double.class.getName());
                    break;
                case "float":
                    domainClassOrInterfaceType = getClassOrInterfaceTypeFromClassName(null, Float.class.getName());
                    break;
                case "char":
                    domainClassOrInterfaceType = getClassOrInterfaceTypeFromClassName(null, Character.class.getName());
                    break;
                case "byte":
                    domainClassOrInterfaceType = getClassOrInterfaceTypeFromClassName(null, Byte.class.getName());
                    break;
                case "short":
                    domainClassOrInterfaceType = getClassOrInterfaceTypeFromClassName(null, Short.class.getName());
                    break;
            }
        }
        if (!domainClassOrInterfaceType.isClassOrInterfaceType()) {
            return null;
        }
        if (isCollectionObject(domainClassOrInterfaceType)) {
            // use ArraySchema
            return new NormalAnnotationExpr(getNameFromClass(ARRAY_SCHEMA_ANNOTATION_CLASS),
                    new NodeList<>(Collections.singleton(
                            new MemberValuePair(CONTENT_SCHEMA, createSchemaAnnotation(
                                    getTypeOfCollection(domainClassOrInterfaceType))))
                    ));
        }
        return new NormalAnnotationExpr(getNameFromClass(SCHEMA_ANNOTATION_CLASS),
                new NodeList<>(Collections.singletonList(new MemberValuePair(SCHEMA_IMPLEMENTATION,
                        new ClassExpr(domainClassOrInterfaceType)))));
    }

    protected NormalAnnotationExpr createContentAnnotation(NormalAnnotationExpr schemaAnnotationExpr, String mediaType) {
        return new NormalAnnotationExpr(getNameFromClass(CONTENT_ANNOTATION_CLASS),
                new NodeList<>(Arrays.asList(
                        new MemberValuePair(CONTENT_MEDIATYPE, new StringLiteralExpr(mediaType)),
                        schemaAnnotationExpr.getNameAsString().equals(ARRAY_SCHEMA_ANNOTATION_CLASS) ?
                                new MemberValuePair(CONTENT_ARRAY, schemaAnnotationExpr)
                                :
                                new MemberValuePair(CONTENT_SCHEMA, schemaAnnotationExpr)
                )));
    }

    protected MemberValuePair createContentAnnotationMemberForType(Type classOrInterfaceType, boolean request) {
        NormalAnnotationExpr schemaAnnotationExpr = createSchemaAnnotation(classOrInterfaceType);
        return createContentAnnotationMember(schemaAnnotationExpr, request);
    }

    private MemberValuePair createContentAnnotationMember(NormalAnnotationExpr schemaAnnotationExpr, boolean request) {
        NormalAnnotationExpr contentJsonAnnotationExpr = createContentAnnotation(schemaAnnotationExpr, MEDIATYPE_JSON);
        NodeList<Expression> nodes = new NodeList<>(Collections.singletonList(contentJsonAnnotationExpr));
        if (!request) {
            NormalAnnotationExpr contentJsonHalAnnotationExpr = createContentAnnotation(schemaAnnotationExpr, MEDIATYPE_JSON_HAL);
            nodes.add(contentJsonHalAnnotationExpr);
        }
        return new MemberValuePair(REQUEST_BODY_API_RESPONSE_CONTENT,
                new ArrayInitializerExpr(
                        nodes)
        );
    }

    protected MemberValuePair createContentAnnotationMemberForRef(String ref, boolean request) {
        NormalAnnotationExpr schemaAnnotationExpr = createSchemaRefAnnotation(ref);
        return createContentAnnotationMember(schemaAnnotationExpr, request);
    }

    protected MemberValuePair createContentAnnotationMember(ClassOrInterfaceDeclaration classOrInterfaceDeclaration, boolean request) {
        ClassOrInterfaceType domainClassOrInterfaceType = getDomainClass(classOrInterfaceDeclaration);
        return createContentAnnotationMemberForType(domainClassOrInterfaceType, request);
    }

    protected NormalAnnotationExpr createApiResponseAnnotation20xWithContent(ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                                                             int statusCode) {
        return createApiResponseAnnotation20xWithContentAnnotation(statusCode, getDomainSummary(
                classOrInterfaceDeclaration.findCompilationUnit().get(), classOrInterfaceDeclaration),
                createContentAnnotationMember(classOrInterfaceDeclaration, false));
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

    protected NormalAnnotationExpr createApiResponseAnnotation20xWithContentForType(CompilationUnit compilationUnit,
                                                                                    int statusCode,
                                                                                    Type classOrInterfaceType) {
        return createApiResponseAnnotation20xWithContentAnnotation(statusCode, getTypeSummary(compilationUnit, classOrInterfaceType),
                createContentAnnotationMemberForType(classOrInterfaceType, false));
    }

    protected void removeMethodParameterAnnotation(MethodDeclaration methodDeclaration, String annotationClass) {
        methodDeclaration.getParameters().stream().map(Parameter::getAnnotations).forEach(n -> n.removeIf(a -> a.getName().getIdentifier().equals(getSimpleNameFromClass(annotationClass))));
    }

    private String getDomainSummary(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        ClassOrInterfaceType domainClassOrInterfaceType = getDomainClass(classOrInterfaceDeclaration);
        return getTypeSummary(compilationUnit, domainClassOrInterfaceType);
    }

    private boolean isPropertyIgnored(FieldDeclaration fieldDeclaration) {
        if (fieldDeclaration.isAnnotationPresent(getSimpleNameFromClass(JSON_IGNORE_CLASS))) {
            return true;
        }
        Optional<AnnotationExpr> jsonPropertyAnnotationExpr = fieldDeclaration.getAnnotationByName(getSimpleNameFromClass(JSON_PROPERTY_CLASS));
        return jsonPropertyAnnotationExpr.filter(annotationExpr -> annotationExpr.isNormalAnnotationExpr() && ((NormalAnnotationExpr) annotationExpr).getPairs().stream().
                anyMatch(p -> p.getName().asString().endsWith(JSON_PROPERTY_ACCESS) &&
                        p.getValue().asFieldAccessExpr().getName().asString().
                                equals(JSON_PROPERTY_WRITE_ONLY))).isPresent();
    }

    private boolean isSourceCodeAvailableForClassOrInterface(ClassOrInterfaceDeclaration containingDomainClassDeclaration,
                                                             Type classOrInterfaceType) {
        if (!classOrInterfaceType.isClassOrInterfaceType()) {
            return false;
        }
        if (!containingDomainClassDeclaration.findCompilationUnit().isPresent()) {
            return false;
        }
        return getSourceFile(containingDomainClassDeclaration.findCompilationUnit().get(),
                classOrInterfaceType.asClassOrInterfaceType()).exists();
    }

    private boolean isEnumProperty(CompilationUnit compilationUnit, Type propertyClassOrInterfaceType) {
        if (!propertyClassOrInterfaceType.isClassOrInterfaceType()) {
            return false;
        }
        TypeDeclaration extendTypeDeclaration = parseClassOrInterfaceType(compilationUnit,
                propertyClassOrInterfaceType.asClassOrInterfaceType());
        return extendTypeDeclaration.isEnumDeclaration();
    }

    private boolean isPrimitiveObject(Type classOrInterfaceType) {
        if (!classOrInterfaceType.isClassOrInterfaceType()) {
            return false;
        }
        switch (classOrInterfaceType.asClassOrInterfaceType().getName().getIdentifier()) {
            case "String":
            case "Long":
            case "Integer":
            case "Double":
            case "Float":
            case "Date":
            case "Boolean":
                return true;
        }
        return false;
    }

    protected boolean isCollectionObject(Type classOrInterfaceType) {
        if (!classOrInterfaceType.isClassOrInterfaceType()) {
            return false;
        }
        if (classOrInterfaceType.isArrayType()) {
            return true;
        }
        switch (classOrInterfaceType.asClassOrInterfaceType().getName().getIdentifier()) {
            case "Iterable":
            case "List":
            case "Set":
            case "Collection":
                return true;
        }
        return false;
    }

    private Type getTypeOfCollection(Type classOrInterfaceType) {
        if (classOrInterfaceType.isArrayType()) {
            return classOrInterfaceType.getElementType();
        }
        return classOrInterfaceType.asClassOrInterfaceType().getTypeArguments().map(
                t -> t.get(0)).orElseThrow(() -> new RuntimeException("Collection does not define type."));
    }

    protected String getTypeSummary(CompilationUnit compilationUnit, Type classOrInterfaceType) {
        if (classOrInterfaceType.isPrimitiveType() || isPrimitiveObject(classOrInterfaceType)) {
            return classOrInterfaceType.isClassOrInterfaceType() ?
                    classOrInterfaceType.asClassOrInterfaceType().getName().getIdentifier() :
                    classOrInterfaceType.asString();
        }
        if (!classOrInterfaceType.isClassOrInterfaceType()) {
            return null;
        }
        if (isCollectionObject(classOrInterfaceType)) {
            return String.format("Array of %s", toLowerCase(getTypeSummary(compilationUnit, getTypeOfCollection(classOrInterfaceType))));
        }
        TypeDeclaration extendTypeDeclaration = parseClassOrInterfaceType(compilationUnit,
                classOrInterfaceType.asClassOrInterfaceType());
        Javadoc javadoc = getJavadoc(extendTypeDeclaration);
        if (javadoc == null) {
            return null;
        }
        return getJavadocSummary(getJavadocText(javadoc));
    }

    protected NormalAnnotationExpr createRequestBodyAnnotation(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        String summary = getDomainSummary(classOrInterfaceDeclaration.findCompilationUnit().get(),
                classOrInterfaceDeclaration);
        return new NormalAnnotationExpr(getNameFromClass(REQUEST_BODY_CLASS),
                new NodeList<>(Arrays.asList(
                        new MemberValuePair(REQUEST_BODY_API_RESPONSE_DESCRIPTION, new StringLiteralExpr(summary)),
                        createContentAnnotationMember(classOrInterfaceDeclaration, true)
                )));
    }

    protected NormalAnnotationExpr createRequestBodyAnnotationForType(CompilationUnit compilationUnit, ClassOrInterfaceType classOrInterfaceType) {
        String summary = getTypeSummary(compilationUnit, classOrInterfaceType);
        return new NormalAnnotationExpr(getNameFromClass(REQUEST_BODY_CLASS),
                new NodeList<>(Arrays.asList(
                        new MemberValuePair(REQUEST_BODY_API_RESPONSE_DESCRIPTION, new StringLiteralExpr(summary)),
                        createContentAnnotationMemberForType(classOrInterfaceType, true)
                )));
    }

    protected NormalAnnotationExpr createApiResponseAnnotation200WithRef(CompilationUnit compilationUnit, String summary, String ref) {
        return createApiResponseAnnotation20xWithContentAnnotation(200, summary,
                createContentAnnotationMemberForRef(ref, false));
    }

    protected NormalAnnotationExpr createApiResponseAnnotation200WithContentForType(CompilationUnit compilationUnit, Type classOrInterfaceType) {
        return createApiResponseAnnotation20xWithContentAnnotation(200, getTypeSummary(compilationUnit, classOrInterfaceType),
                createContentAnnotationMemberForType(classOrInterfaceType, false));
    }

    protected NormalAnnotationExpr createApiResponseAnnotation200WithContent(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        return createApiResponseAnnotation20xWithContent(classOrInterfaceDeclaration, 200);
    }

    protected NormalAnnotationExpr createApiResponseAnnotation201WithContent(CompilationUnit compilationUnit,
                                                                             ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        return createApiResponseAnnotation20xWithContent(classOrInterfaceDeclaration, 201);
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

    protected void addOperationAnnotation(MethodDeclaration methodDeclaration,
                                          List<NormalAnnotationExpr> parameters,
                                          NormalAnnotationExpr requestBody,
                                          List<NormalAnnotationExpr> responses,
                                          String defaultSummary) {
        Javadoc javadoc = getJavadoc(methodDeclaration);
        String summary = defaultSummary;
        String description = null;
        if (javadoc != null) {
            summary = getJavadocSummary(getJavadocText(javadoc));
            description = getJavadocDescription(getJavadocText(javadoc));
        }
        NormalAnnotationExpr annotationExpr = new NormalAnnotationExpr(getNameFromClass(OPERATION_ANNOTATION_CLASS),
                new NodeList<>());
        methodDeclaration.getParentNode().ifPresent(
                t -> t.findAll(ClassOrInterfaceDeclaration.class).stream().findFirst().
                        ifPresent(
                                c -> {
                                    annotationExpr.addPair(OPERATION_ID, new StringLiteralExpr(
                                            c.getName().getIdentifier() + UNDERSCORE + methodDeclaration.getNameAsString()));
                                }
                        )
        );
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
        methodDeclaration.addAnnotation(annotationExpr);
    }

    protected void addOperationAnnotation(MethodDeclaration methodDeclaration,
                                          NormalAnnotationExpr requestBody,
                                          List<NormalAnnotationExpr> responses,
                                          String defaultSummary) {
        addOperationAnnotation(methodDeclaration, null, requestBody, responses, defaultSummary);
    }

    // JAX-RS

    protected void removeJaxRsMethodAnnotations(BodyDeclaration<?> bodyDeclaration) {
        removeJaxRsPathAnnotation(bodyDeclaration);
        removeAnnotation(bodyDeclaration, JAXRS_GET_CLASS);
        removeAnnotation(bodyDeclaration, JAXRS_POST_CLASS);
        removeAnnotation(bodyDeclaration, JAXRS_PUT_CLASS);
        removeAnnotation(bodyDeclaration, JAXRS_DELETE_CLASS);
        removeAnnotation(bodyDeclaration, JAXRS_PRODUCES_CLASS);
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

    protected Pair<Boolean, String> getResourceConfig(String methodName, ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                                      String defaultPath,
                                                      Type... parameterClassTypes) {
        AnnotationExpr methodResource;
        if (parameterClassTypes == null) {
            methodResource = findClosestMethodResourceAnnotation(classOrInterfaceDeclaration,
                    methodName);
        } else {
            methodResource = findClosestMethodResourceAnnotation(classOrInterfaceDeclaration,
                    methodName, Arrays.stream(parameterClassTypes).map(Type::asString).toArray(String[]::new));
        }
        boolean exported = true;
        String methodPath = defaultPath;
        if (methodResource != null) {
            exported = checkResourceExported(methodResource);
            String _path = getResourcePath(methodResource);
            if (_path != null) {
                methodPath = _path;
            }
        }
        return new Pair<>(exported, methodPath);
    }

    protected void removeCrudOperationAnnotationAndMethod(MethodDeclaration methodDeclaration, CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        if (methodDeclaration == null) {
            return;
        }
        if (checkIfExtendingCrudInterface(classOrInterfaceDeclaration)) {
            // if a resource annotation was added it is marked as user added method and will not be removed
            if (!checkResourceAnnotationPresent(methodDeclaration).isPresent()) {
                methodDeclaration.remove();
            }
        }
        // remove Operation annotation and JAX-RS
        removeJaxRsMethodAnnotations(methodDeclaration);
        removeAnnotation(methodDeclaration, OPERATION_ANNOTATION_CLASS);
    }

    protected void removeQuerydslOperationAnnotationAndMethod(MethodDeclaration methodDeclaration, CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        if (methodDeclaration == null) {
            return;
        }
        if (checkIfExtendingQuerydslInterface(classOrInterfaceDeclaration)) {
            // if a resource annotation was added it is marked as user added method
            if (!checkResourceAnnotationPresent(methodDeclaration).isPresent()) {
                methodDeclaration.remove();
            }
        }
        // remove Operation annotation and JAX-RS
        removeJaxRsMethodAnnotations(methodDeclaration);
        removeAnnotation(methodDeclaration, OPERATION_ANNOTATION_CLASS);
    }

    // checks

    /**
     * Checks if this class has a concrete type.
     *
     * @param classOrInterfaceDeclaration The class or interface declaration.
     * @return <code>true</code> if the repository is not generic.
     */
    protected boolean isConcreteRepositoryClass(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        if (!checkIfExtendingRepository(classOrInterfaceDeclaration)) {
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

    // finders

    protected String getMethodPath(MethodDeclaration methodDeclaration) {
        return methodDeclaration.getSignature().getName();
    }

    /**
     * Method to search repository methods matching a name and the passed parameter types, which should be passed as FQ class name.
     * <p>
     * The default {@link ClassOrInterfaceDeclaration#getMethodsBySignature(String, String...)} is not checking
     * if a FQ class name or simple class name is passed.
     * </p>
     *
     * @param classOrInterfaceDeclaration The class or interface declaration.
     * @param methodName                  The method name.
     * @param paramTypes                  The parameter types as FQ class name.
     * @return the matched method or <code>null</code>.
     */
    protected MethodDeclaration findMethodByMethodNameAndParameters(ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                                                    String methodName, String... paramTypes) {
        return findMethodByMethodNameAndParameters(classOrInterfaceDeclaration, methodName, true,
                paramTypes);
    }

    /**
     * Method to search methods matching a name and the passed parameter types, which should be passed as FQ class name.
     * <p>
     * The default {@link ClassOrInterfaceDeclaration#getMethodsBySignature(String, String...)} is not checking
     * if a FQ class name or simple class name is passed.
     * </p>
     *
     * @param classOrInterfaceDeclaration The class or interface declaration.
     * @param methodName                  The method name.
     * @param paramTypes                  The parameter types as FQ class name.
     * @param repositoryMethodsOnly       <code>true</code> to list only repository methods.
     * @return the matched method or <code>null</code>.
     */
    protected MethodDeclaration findMethodByMethodNameAndParameters(ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                                                    String methodName, boolean repositoryMethodsOnly,
                                                                    String... paramTypes) {
        if (repositoryMethodsOnly && !checkIfExtendingRepository(classOrInterfaceDeclaration)) {
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

    protected MethodDeclaration findClosestMethod(ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                                  String methodName, String... paramTypes) {
        MethodDeclaration methodDeclaration = findMethodByMethodNameAndParameters(classOrInterfaceDeclaration,
                methodName, paramTypes);
        if (methodDeclaration != null) {
            return methodDeclaration;
        }
        // check in implementations, too
        for (ClassOrInterfaceType extent : classOrInterfaceDeclaration.getExtendedTypes()) {
            if (getSourceFile(classOrInterfaceDeclaration.findCompilationUnit().get(), extent).exists()) {
                TypeDeclaration extendTypeDeclaration = parseClassOrInterfaceType(
                        classOrInterfaceDeclaration.findCompilationUnit().get(), extent);
                methodDeclaration = findClosestMethod(extendTypeDeclaration.asClassOrInterfaceDeclaration(),
                        methodName, paramTypes);
                if (methodDeclaration != null) {
                    return methodDeclaration;
                }
            }
        }
        return null;
    }

    protected List<MethodDeclaration> findCustomMethods(ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                                        String customMethodPrefix, Set<String> excludedMethods) {
        List<MethodDeclaration> customerFinderMethodDeclarations =
                classOrInterfaceDeclaration.getMethods().stream().filter(
                        m -> !excludedMethods.contains(m.getSignature().getName())
                                && m.getSignature().getName().startsWith(customMethodPrefix)
                ).collect(Collectors.toList());
        // check in implementations, too
        for (ClassOrInterfaceType extent : classOrInterfaceDeclaration.getExtendedTypes()) {
            if (getSourceFile(classOrInterfaceDeclaration.findCompilationUnit().get(), extent).exists()) {
                TypeDeclaration extendTypeDeclaration = parseClassOrInterfaceType(
                        classOrInterfaceDeclaration.findCompilationUnit().get(), extent);
                List<MethodDeclaration> otherMethodDeclarations = findCustomMethods(
                        extendTypeDeclaration.asClassOrInterfaceDeclaration(), customMethodPrefix, excludedMethods);
                customerFinderMethodDeclarations.addAll(otherMethodDeclarations);
            }
        }
        return customerFinderMethodDeclarations;
    }

    protected MethodDeclaration findClosestMethodFromMethodVariants(ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                                                    List<Pair<String, List<String>>> methodVariants) {
        List<Pair<MethodDeclaration, Boolean>> exportedMap = new ArrayList<>();
        for (Pair<String, List<String>> methodParameterEntry : methodVariants) {
            String[] params = methodParameterEntry.b != null ?
                    methodParameterEntry.b.toArray(new String[0]) : new String[0];
            MethodDeclaration methodDeclaration = findClosestMethod(classOrInterfaceDeclaration,
                    methodParameterEntry.a, params);
            if (methodDeclaration != null) {
                AnnotationExpr methodResource = findClosestMethodResourceAnnotation(
                        classOrInterfaceDeclaration, methodParameterEntry.a, params);
                if (methodResource != null) {
                    exportedMap.add(new Pair<>(methodDeclaration, checkResourceExported(methodResource)));
                } else if (isMethodOfConcreteRepositoryClass(methodDeclaration)) {
                    return methodDeclaration;
                }
            }
        }
        return exportedMap.stream().filter(e -> e.b).findFirst().map(m -> m.a).orElse(
                exportedMap.stream().filter(e -> !e.b).findFirst().map(m -> m.a).orElse(null)
        );
    }

    protected AnnotationExpr findClosestMethodResourceAnnotation(ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                                                 String methodName, String... paramTypes) {
        MethodDeclaration methodDeclaration = findMethodByMethodNameAndParameters(classOrInterfaceDeclaration,
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
            if (getSourceFile(classOrInterfaceDeclaration.findCompilationUnit().get(), extent).exists()) {
                TypeDeclaration extendTypeDeclaration = parseClassOrInterfaceType(
                        classOrInterfaceDeclaration.findCompilationUnit().get(), extent);
                AnnotationExpr annotationExpr = findClosestMethodResourceAnnotation(
                        extendTypeDeclaration.asClassOrInterfaceDeclaration(), methodName, paramTypes);
                if (annotationExpr != null) {
                    return annotationExpr;
                }
            }
        }
        return null;
    }

    protected Javadoc findClosestMethodJavadoc(ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                               String methodName, String... paramTypes) {
        MethodDeclaration methodDeclaration = findMethodByMethodNameAndParameters(classOrInterfaceDeclaration,
                methodName, paramTypes);
        if (methodDeclaration != null) {
            Optional<Javadoc> javadoc = methodDeclaration.getJavadoc();
            if (javadoc.isPresent()) {
                return javadoc.get();
            }
        }
        // check in implementations, too
        for (ClassOrInterfaceType extent : classOrInterfaceDeclaration.getExtendedTypes()) {
            if (getSourceFile(classOrInterfaceDeclaration.findCompilationUnit().get(), extent).exists()) {
                TypeDeclaration extendTypeDeclaration = parseClassOrInterfaceType(
                        classOrInterfaceDeclaration.findCompilationUnit().get(), extent);
                Javadoc javadoc = findClosestMethodJavadoc(extendTypeDeclaration.asClassOrInterfaceDeclaration(),
                        methodName, paramTypes);
                if (javadoc != null) {
                    return javadoc;
                }
            }
        }
        return null;
    }
}
