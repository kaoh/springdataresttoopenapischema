package de.ohmesoftware.springdataresttoopenapischema;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enriches the passed source path and sub directories and adds or sets the "description" property of the @Schema annotation
 * from the Javadoc.
 *
 * @author <a href="mailto:k_o_@users.sourceforge.net">Karsten Ohme
 * (k_o_@users.sourceforge.net)</a>
 */
public class Enricher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Enricher.class);

    private static final String OPERATION_ANNOTATION_SIMPLE_NAME = "Operation";
    private static final String OPERATION_ANNOTATION_CLASS = "io.swagger.v3.oas.annotations.Operation";
    private static final String EMPTY_STRING = "";
    private static final String SPACE_STRING = " ";
    private static final String INCLUDE_EXCLUDE_SEPARATOR = ",";
    private static final String QUOTATION_MARK_STRING = "\"";
    private static final String OPERATION_SUMMARY = "summary";
    private static final String OPERATION_DESCRIPTION = "description";
    private static final String OPERATION_REQUEST_BODY = "requestBody";
    private static final String OPERATION_RESPONSES = "responses";

    private static final String SCHEMA_ANNOTATION_SIMPLE_NAME = "Schema";
    private static final String SCHEMA_ANNOTATION_CLASS = "io.swagger.v3.oas.annotations.media.Schema";
    private static final String SCHEMA_IMPLEMENTATION = "implementation";
    private static final String CONTENT_ANNOTATION_SIMPLE_NAME = "Content";
    private static final String CONTENT_ANNOTATION_CLASS = "io.swagger.v3.oas.annotations.media.Content";
    private static final String CONTENT_MEDIATYPE = "mediaType";
    private static final String CONTENT_SCHEMA = "schema";
    private static final String MEDIATYPE_JSON = "application/json;charset=UTF-8";
    private static final String MEDIATYPE_JSON_HAL = "application/hal+json;charset=UTF-8";
    private static final String REQUEST_BODY_SIMPLE_NAME = "RequestBody";
    private static final String REQUEST_BODY_CLASS = "io.swagger.v3.oas.annotations.parameters.RequestBody";
    private static final String REQUEST_BODY_DESCRIPTION = "description";
    private static final String API_RESPONSE_SIMPLE_NAME = "ApiResponse";
    private static final String API_RESPONSE_CLASS = "io.swagger.v3.oas.annotations.responses.ApiResponse";
    private static final String API_RESPONSE_DESCRIPTION = "description";
    private static final String API_RESPONSE_RESPONSE_CODE = "responseCode";
    private static final String REQUEST_BODY_API_RESPONSE_CONTENT = "content";

    private static final String REPOSITORY_REST_RESOURCE_ANNOTATION_SIMPLE_NAME = "RepositoryRestResource";
    private static final String REPOSITORY_REST_RESOURCE_CLASS = "org.springframework.data.rest.core.annotation.RepositoryRestResource";

    private static final String RESOURCE_ANNOTATION_SIMPLE_NAME = "RestResource";
    private static final String RESOURCE_CLASS = "org.springframework.data.rest.core.annotation.RestResource";

    private static final String PARAMATER_ANNOTATION_SIMPLE_NAME = "Parameter";
    private static final String PARAMATER_CLASS = "io.swagger.v3.oas.annotations.Parameter";

    private static final String FIND_BY_ID_METHOD = "findById";

    private static final String RESOURCE_EXPORTED = "exported";
    private static final String RESOURCE_PATH = "path";

    private static final String CREATE_METHOD_PATH = "create";
    private static final String UPDATE_METHOD_PATH = "update";

    private static final String GLOB = "glob:";

    private static final String DOT = ".";
    private static final String PLURAL_S = "s";
    private static final String SLASH = "/";
    private static final String JAVA_EXT = ".java";
    private static final String CLASS_EXT = ".class";

    private static final String PAGING_AND_SORTING_REPOSITORY = "PagingAndSortingRepository";
    private static final String QUERY_DSL_PREDICATE_EXECUTOR = "QuerydslPredicateExecutor";
    private static final String CRUD_REPOSITORY = "CrudRepository";
    private static final String REPOSITORY = "Repository";

    private static final String JAXRS_PATH_CLASS = "javax.ws.rs.Path";
    private static final String JAXRS_PATH_SIMPLE_NAME = "Path";
    private static final String JAXRS_GET_CLASS = "javax.ws.rs.GET";
    private static final String JAXRS_GET_SIMPLE_NAME = "GET";
    private static final String JAXRS_POST_CLASS = "javax.ws.rs.POST";
    private static final String JAXRS_POST_SIMPLE_NAME = "POST";
    private static final String JAXRS_PUT_CLASS = "javax.ws.rs.PUT";
    private static final String JAXRS_PUT_SIMPLE_NAME = "PUT";
    private static final String JAXRS_DELETE_CLASS = "javax.ws.rs.DELETE";
    private static final String JAXRS_DELETE_SIMPLE_NAME = "DELETE";


    /**
     * The source path to enrich.
     */
    private String sourcePath;

    /**
     * The base path without the package directories.
     */
    private String basePath;

    /**
     * The includes.
     */
    private Set<String> includes;

    /**
     * The excludes.
     */
    private Set<String> excludes;

    /**
     * Constructor.
     *
     * @param sourcePath The source path to enrich.
     * @param includes   The includes.
     * @param excludes   The excludes.
     */
    public Enricher(String sourcePath, Set<String> includes, Set<String> excludes) {
        this.sourcePath = sourcePath;
        if (sourcePath.endsWith("/")) {
            this.sourcePath = sourcePath.substring(0, sourcePath.length() - 1);
        }
        this.includes = includes;
        this.excludes = excludes;
    }

    private static final String EXCLUDES_OPT = "-excludes";
    private static final String INCLUDES_OPT = "-includes";
    private static final String SOURCE_OPT = "-sourcePath";

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.err.println("No command line options passed.");
            System.exit(-1);
        }
        String sourcePath = parseOption(args, SOURCE_OPT, true, null);
        String includes = parseOption(args, INCLUDES_OPT, false, null);
        String excludes = parseOption(args, EXCLUDES_OPT, false, null);
        Enricher enricher = new Enricher(sourcePath,
                includes == null ? null : Arrays.stream(includes.split(INCLUDE_EXCLUDE_SEPARATOR)).map(String::trim).collect(Collectors.toSet()),
                excludes == null ? null : Arrays.stream(excludes.split(INCLUDE_EXCLUDE_SEPARATOR)).map(String::trim).collect(Collectors.toSet())
        );
        enricher.enrich();
    }

    private static String parseOption(String[] args, String option, boolean required,
                                      String _default) {
        Optional<String> optionArg = Arrays.stream(args).filter(s -> s.equals(option)).findFirst();
        if (!optionArg.isPresent() && required) {
            System.err.println(String.format("Required option '%s' is missing.", option));
            System.exit(-2);
        }
        // get next element after option
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(option)) {
                if (args.length > i + 1) {
                    return args[i + 1];
                }
                System.err.println(String.format("Required option argument for '%s' is missing.", option));
                System.exit(-2);
            }
        }
        if (required) {
            System.err.println(String.format("Required option '%s' is missing.", option));
            System.exit(-2);
        }
        return _default;
    }


    public void enrich() {
        LOGGER.info(String.format("Enriching source path '%s'", sourcePath));
        try {
            Files.walkFileTree(Paths.get(sourcePath), new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path path,
                                                 BasicFileAttributes attrs) throws IOException {
                    LOGGER.debug(String.format("Checking file '%s' for inclusion / exclusion", path.getFileName().toString()));
                    if (includes != null && !includes.isEmpty()) {
                        boolean handle = false;
                        for (String include : includes) {
                            PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(GLOB + include);
                            if (pathMatcher.matches(path) && path.toFile().isFile()) {
                                LOGGER.debug(String.format("Included file: '%s'", path.getFileName().toString()));
                                // handle
                                handle = true;
                                break;
                            }
                        }
                        if (!handle) {
                            LOGGER.debug(String.format("Not included file: '%s'", path.getFileName().toString()));
                            return FileVisitResult.CONTINUE;
                        }
                    }

                    if (excludes != null && !excludes.isEmpty()) {
                        for (String exclude : excludes) {
                            PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(GLOB + exclude);
                            if (pathMatcher.matches(path)) {
                                // skip sub dirs if a directory
                                if (path.toFile().isDirectory()) {
                                    return FileVisitResult.SKIP_SUBTREE;
                                }
                                LOGGER.debug(String.format("Excluded file: '%s'", path.getFileName().toString()));
                                // ignore if excludes
                                return FileVisitResult.CONTINUE;
                            }
                        }
                    }
                    // handle
                    handleSchema(path);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    LOGGER.warn(String.format("Could not check file '%s'", file.getFileName().toString()));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LOGGER.error("Could not walk through source files.", e);
            throw new RuntimeException("Could not walk through source files.", e);
        }
    }

    private String getJavadoc(BodyDeclaration bodyDeclaration) {
        Javadoc javadoc = bodyDeclaration.getComment().filter(Comment::isJavadocComment).map(c -> c.asJavadocComment().parse()).orElse(null);
        if (javadoc != null) {
            return javadoc.getDescription().getElements().stream().map(d -> d.toText().trim()).collect(Collectors.joining(SPACE_STRING));
        }
        return null;
    }

    private void getBaseSourcePath(CompilationUnit compilationUnit, String path) {
        String _package = compilationUnit.getPackageDeclaration().map(p -> p.getName().asString()).orElse(EMPTY_STRING);
        String packagePath = _package.replace(".", "/");
        int overlap = 0;
        for (int i = packagePath.length(); i >= 0; i--) {
            if (sourcePath.endsWith(packagePath.substring(0, i))) {
                overlap = i;
                break;
            }
        }
        basePath = sourcePath.substring(0, sourcePath.length() - overlap);
    }

    private CompilationUnit parseFile(File file) {
        try {
            return JavaParser.parse(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not find file.", e);
        }
    }

    private void handleSchema(Path path) throws IOException {
        LOGGER.info(String.format("Handling file: '%s'", path.getFileName().toString()));
        CompilationUnit compilationUnit = parseFile(path.toFile());
        getBaseSourcePath(compilationUnit, path.getFileName().toString());
        List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarations = new ArrayList<>(compilationUnit.
                findAll(ClassOrInterfaceDeclaration.class));

        for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : classOrInterfaceDeclarations) {
            // check if annotated with RepositoryRestResource
            Optional<NormalAnnotationExpr> restResourceOptional = checkResourceAnnotationPresent(classOrInterfaceDeclaration);
            if (restResourceOptional.isPresent() && checkResourceExported(restResourceOptional.get(), false)) {
                String resourcePath = getResourcePath(restResourceOptional.get());
                if (resourcePath == null) {
                    resourcePath = getDomainPath(compilationUnit, classOrInterfaceDeclaration);
                }
                // add JAX-RS path annotation
                addPathAnnotation(classOrInterfaceDeclaration, resourcePath);
                addAllOperations(compilationUnit, classOrInterfaceDeclaration);
            } else {
//                remove all JAX-RS and Operation annotations and overridden methods
                removeAnnotation(compilationUnit, classOrInterfaceDeclaration, JAXRS_PATH_SIMPLE_NAME,
                        JAXRS_PATH_CLASS);
                removeAllOperations(compilationUnit, classOrInterfaceDeclaration);
            }
            try (FileWriter fileWriter = new FileWriter(path.toFile())) {
                fileWriter.write(compilationUnit.toString());
            }
        }
    }

    private void removeAllOperations(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        removeFindByOperation(compilationUnit, classOrInterfaceDeclaration);
    }

    private void addAllOperations(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        addFindByIdOperation(compilationUnit, classOrInterfaceDeclaration);
    }

    private void removeFindByOperation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        MethodDeclaration methodDeclaration = findClosestMethod(compilationUnit, classOrInterfaceDeclaration, FIND_BY_ID_METHOD, String.class.getSimpleName());
        removeCrudOperation(methodDeclaration, compilationUnit, classOrInterfaceDeclaration);
    }

    private void removeCrudOperation(MethodDeclaration methodDeclaration, CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        if (methodDeclaration == null) {
            return;
        }
        for (ClassOrInterfaceType classOrInterfaceType : classOrInterfaceDeclaration.getExtendedTypes()) {
            switch (classOrInterfaceType.getName().getIdentifier()) {
                case PAGING_AND_SORTING_REPOSITORY:
                case CRUD_REPOSITORY:
                    methodDeclaration.remove();
                    break;
                case QUERY_DSL_PREDICATE_EXECUTOR:
                case REPOSITORY:
                    // only remove Operation annotation and JAX-RS
                    removeJaxRsAnnotations(compilationUnit, methodDeclaration);
                    removeAnnotation(compilationUnit, methodDeclaration, OPERATION_ANNOTATION_SIMPLE_NAME, OPERATION_ANNOTATION_CLASS);
                    break;
            }
        }
    }

    private void removeJaxRsAnnotations(CompilationUnit compilationUnit, BodyDeclaration<?> bodyDeclaration) {
        removeAnnotation(compilationUnit, bodyDeclaration, JAXRS_PATH_SIMPLE_NAME, JAXRS_PATH_CLASS);
        removeAnnotation(compilationUnit, bodyDeclaration, JAXRS_GET_SIMPLE_NAME, JAXRS_GET_CLASS);
        removeAnnotation(compilationUnit, bodyDeclaration, JAXRS_POST_SIMPLE_NAME, JAXRS_POST_CLASS);
        removeAnnotation(compilationUnit, bodyDeclaration, JAXRS_PUT_SIMPLE_NAME, JAXRS_PUT_CLASS);
        removeAnnotation(compilationUnit, bodyDeclaration, JAXRS_DELETE_SIMPLE_NAME, JAXRS_DELETE_CLASS);
    }

    private void addFindByIdOperation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        MethodDeclaration methodDeclaration = findClosestMethod(compilationUnit, classOrInterfaceDeclaration, FIND_BY_ID_METHOD, String.class.getSimpleName());
        // add missing method automatically if extending CRUD interface
        if (methodDeclaration == null) {
            ClassOrInterfaceType domainClassOrInterfaceType = getDomainClass(compilationUnit, classOrInterfaceDeclaration);
            if (checkIfExtendingCrudInterface(compilationUnit, classOrInterfaceDeclaration)) {
                ClassOrInterfaceType idClass = getIDClass(compilationUnit, classOrInterfaceDeclaration);
                methodDeclaration = classOrInterfaceDeclaration.addMethod(FIND_BY_ID_METHOD, Modifier.Keyword.PUBLIC).setParameters(
                        new NodeList<>(Collections.singletonList(new Parameter(
                                idClass, "id")))).setType(domainClassOrInterfaceType);
            }
        }
        if (methodDeclaration == null) {
            removeFindByOperation(compilationUnit, classOrInterfaceDeclaration);
            return;
        }
        NormalAnnotationExpr methodResource = findClosestMethodResourceAnnotation(compilationUnit, classOrInterfaceDeclaration, FIND_BY_ID_METHOD,
                String.class.getSimpleName());
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
            // clean annotations in case of an update
            removeJaxRsAnnotations(compilationUnit, methodDeclaration);
            removeAnnotation(compilationUnit, methodDeclaration, OPERATION_ANNOTATION_SIMPLE_NAME, OPERATION_ANNOTATION_CLASS);
            if (methodPath != null) {
                addPathAnnotation(methodDeclaration, methodPath);
            }
            addMarkerAnnotation(methodDeclaration, JAXRS_GET_SIMPLE_NAME, JAXRS_GET_CLASS);
            Javadoc javadoc = findClosestMethodJavadoc(compilationUnit, classOrInterfaceDeclaration, FIND_BY_ID_METHOD,
                    String.class.getSimpleName());
            addOperationAnnotation();
        } else {
            // remove and method
            removeFindByOperation(compilationUnit, classOrInterfaceDeclaration);
        }
    }

    private String getMethodPath(MethodDeclaration methodDeclaration) {
        return methodDeclaration.getSignature().getName();
    }

    private ClassOrInterfaceType getIDClass(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        for (ClassOrInterfaceType extent : classOrInterfaceDeclaration.getExtendedTypes()) {
            switch (extent.getName().getIdentifier()) {
                case PAGING_AND_SORTING_REPOSITORY:
                case CRUD_REPOSITORY:
                    if (!extent.getTypeArguments().isPresent()) {
                        throw new IllegalArgumentException(String.format("Repository does not specify an ID type: %s", extent.getName().getIdentifier()));
                    }
                    return getClassOrInterfaceTypeFromClassName(compilationUnit, extent.getTypeArguments().get().get(1).asString());
                default:
                    // visit interface to get information
                    Pair<CompilationUnit, ClassOrInterfaceDeclaration> compilationUnitClassOrInterfaceDeclarationPair = parseExtent(compilationUnit, extent);
                    ClassOrInterfaceType domainClassOrInterfaceType = getIDClass(compilationUnitClassOrInterfaceDeclarationPair.a,
                            compilationUnitClassOrInterfaceDeclarationPair.b);
                    if (domainClassOrInterfaceType != null) {
                        return domainClassOrInterfaceType;
                    }
            }
        }
        return null;
    }

    private ClassOrInterfaceType getClassOrInterfaceTypeFromClassName(CompilationUnit compilationUnit, String className) {
        className = getFullClassName(compilationUnit, className);
        String[] packages = className.split("\\.");
        ClassOrInterfaceType prevClassOrInterfaceType = null;
        for (int i=0; i<packages.length; i++) {
            ClassOrInterfaceType classOrInterfaceType = new ClassOrInterfaceType(prevClassOrInterfaceType, packages[i]);
            prevClassOrInterfaceType = classOrInterfaceType;
        }
        return prevClassOrInterfaceType;
    }

    private boolean checkIfExtendingCrudInterface(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        for (ClassOrInterfaceType extent : classOrInterfaceDeclaration.getExtendedTypes()) {
            switch (extent.getName().getIdentifier()) {
                case PAGING_AND_SORTING_REPOSITORY:
                case CRUD_REPOSITORY:
                    return true;
                default:
                    // visit interface to get information
                    Pair<CompilationUnit, ClassOrInterfaceDeclaration> compilationUnitClassOrInterfaceDeclarationPair = parseExtent(compilationUnit, extent);
                    boolean extending = checkIfExtendingCrudInterface(compilationUnitClassOrInterfaceDeclarationPair.a,
                            compilationUnitClassOrInterfaceDeclarationPair.b);
                    if (extending) {
                        return extending;
                    }
            }
        }
        return false;
    }

    private ClassOrInterfaceType getDomainClass(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        for (ClassOrInterfaceType extent : classOrInterfaceDeclaration.getExtendedTypes()) {
            switch (extent.getName().getIdentifier()) {
                case PAGING_AND_SORTING_REPOSITORY:
                case QUERY_DSL_PREDICATE_EXECUTOR:
                case REPOSITORY:
                case CRUD_REPOSITORY:
                    if (!extent.getTypeArguments().isPresent()) {
                        throw new IllegalArgumentException(String.format("Repository does not specify a domain type: %s", extent.getName().getIdentifier()));
                    }
                    return getClassOrInterfaceTypeFromClassName(compilationUnit, extent.getTypeArguments().get().get(0).asString());
                    default:
                        // visit interface to get information
                        Pair<CompilationUnit, ClassOrInterfaceDeclaration> compilationUnitClassOrInterfaceDeclarationPair = parseExtent(compilationUnit, extent);
                        ClassOrInterfaceType domainClassOrInterfaceType = getDomainClass(compilationUnitClassOrInterfaceDeclarationPair.a,
                                compilationUnitClassOrInterfaceDeclarationPair.b);
                        if (domainClassOrInterfaceType != null) {
                            return domainClassOrInterfaceType;
                        }
            }
        }
        return null;
    }

    private String getDomainPath(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        return getDomainPath(getDomainClass(compilationUnit, classOrInterfaceDeclaration).getName().getIdentifier());
    }

    private String getDomainPath(String string) {
        return string.substring(0, 1).toLowerCase() + string.substring(1) + PLURAL_S;
    }

    private ClassOrInterfaceDeclaration findReturnEntityType(MethodDeclaration methodDeclaration) {
        CallableDeclaration.Signature signature = methodDeclaration.getSignature();
        return null;
    }

    private boolean checkResourceExported(NormalAnnotationExpr resource) {
        return resource.getPairs().stream().filter(p -> p.getName().getIdentifier().equals(RESOURCE_EXPORTED)).map(p -> p.getValue().asBooleanLiteralExpr().getValue())
                .findFirst().orElse(false);
    }

    private String getResourcePath(NormalAnnotationExpr resource) {
        return resource.getPairs().stream().filter(p -> p.getName().getIdentifier().equals(RESOURCE_PATH)).
                map(p -> p.getValue().asStringLiteralExpr().asString()).findFirst().
                orElse(null);
    }

    private MethodDeclaration findClosestMethod(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                                String methodName, String... paramTypes) {
        List<MethodDeclaration> findByIdMethodDeclarations = classOrInterfaceDeclaration.getMethodsBySignature(methodName,
                paramTypes);
        if (!findByIdMethodDeclarations.isEmpty()) {
            return findByIdMethodDeclarations.stream().findFirst().get();
        }
        // check in implementations, too
        for (ClassOrInterfaceType extent : classOrInterfaceDeclaration.getExtendedTypes()) {
            if (getSourceFile(compilationUnit, extent).exists()) {
                Pair<CompilationUnit, ClassOrInterfaceDeclaration> compilationUnitClassOrInterfaceDeclarationPair = parseExtent(compilationUnit, extent);
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

    private String getFullClassName(CompilationUnit compilationUnit,
                                    String className) {
        if (className.contains(DOT)) {
            return className;
        }
        switch (className) {
            case "String":
            case "Long":
            case "Integer":
            case "Double":
            case "Float":
            case "Boolean":
                return String.class.getPackage().getName() + DOT + className;
        }
        return compilationUnit.getImports().stream().filter(i -> !i.isAsterisk() && i.getName().getIdentifier().endsWith(className)).
                map(i -> i.getName().asString()).findFirst().orElseThrow(() -> new RuntimeException(
                String.format("Could not resolve import for type: %s", className)));
    }

    private String getFullClassName(CompilationUnit compilationUnit,
                                    ClassOrInterfaceType extent) {
        String className = extent.getName().getIdentifier();
        return getFullClassName(compilationUnit, className);
    }

    private File getSourceFile(CompilationUnit compilationUnit,
                               ClassOrInterfaceType extent) {
        String className = getFullClassName(compilationUnit, extent);
        // get File
        String sourcePath = basePath + className + JAVA_EXT;
        return new File(sourcePath);
    }

    private Pair<CompilationUnit, ClassOrInterfaceDeclaration> parseExtent(CompilationUnit compilationUnit,
                                                         ClassOrInterfaceType extent) {

        CompilationUnit newCompilationUnit = parseFile(getSourceFile(compilationUnit, extent));
        ClassOrInterfaceDeclaration newClassOrInterfaceDeclaration = newCompilationUnit.findFirst(ClassOrInterfaceDeclaration.class).
                orElseThrow(() -> new RuntimeException(
                        String.format("Could not parse extend type: %s", extent.getName().getIdentifier())));
        return new Pair<>(newCompilationUnit, newClassOrInterfaceDeclaration);
    }

    private NormalAnnotationExpr findClosestMethodResourceAnnotation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                                                     String methodName, String... paramTypes) {
        List<MethodDeclaration> findByIdMethodDeclarations = classOrInterfaceDeclaration.getMethodsBySignature(methodName,
                paramTypes);
        if (!findByIdMethodDeclarations.isEmpty()) {
            MethodDeclaration methodDeclaration = findByIdMethodDeclarations.stream().findFirst().get();
            // first found annotation in class / interface hierarchy
            Optional<NormalAnnotationExpr> annotationExprOptional = checkResourceAnnotationPresent(methodDeclaration);
            if (annotationExprOptional.isPresent()) {
                return annotationExprOptional.get().asNormalAnnotationExpr();
            }
        }
        // check in implementations, too
        for (ClassOrInterfaceType extent : classOrInterfaceDeclaration.getExtendedTypes()) {
            if (getSourceFile(compilationUnit, extent).exists()) {
                Pair<CompilationUnit, ClassOrInterfaceDeclaration> compilationUnitClassOrInterfaceDeclarationPair = parseExtent(compilationUnit, extent);
                NormalAnnotationExpr annotationExpr = findClosestMethodResourceAnnotation(compilationUnitClassOrInterfaceDeclarationPair.a,
                        compilationUnitClassOrInterfaceDeclarationPair.b, methodName, paramTypes);
                if (annotationExpr != null) {
                    return annotationExpr;
                }
            }
        }
        return null;
    }

    private Javadoc findClosestMethodJavadoc(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
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
                Pair<CompilationUnit, ClassOrInterfaceDeclaration> compilationUnitClassOrInterfaceDeclarationPair = parseExtent(compilationUnit, extent);
                Javadoc javadoc = findClosestMethodJavadoc(compilationUnitClassOrInterfaceDeclarationPair.a,
                        compilationUnitClassOrInterfaceDeclarationPair.b, methodName, paramTypes);
                if (javadoc != null) {
                    return javadoc;
                }
            }
        }
        return null;
    }

    private Optional<NormalAnnotationExpr> checkResourceAnnotationPresent(BodyDeclaration<? extends BodyDeclaration> bodyDeclaration) {
        Optional<AnnotationExpr> restResourceOptional =
                bodyDeclaration.getAnnotationByName(REPOSITORY_REST_RESOURCE_ANNOTATION_SIMPLE_NAME);
        if (!restResourceOptional.isPresent()) {
            restResourceOptional =
                    bodyDeclaration.getAnnotationByName(RESOURCE_ANNOTATION_SIMPLE_NAME);
        }
        return restResourceOptional.
                map(Expression::asNormalAnnotationExpr);
    }

    private boolean checkResourceExported(NormalAnnotationExpr resource,
                                          boolean repoAnnotationRequired) {
        return resource.getPairs().stream().filter(p -> p.getName().getIdentifier().equals(RESOURCE_EXPORTED))
                .map(p -> p.getValue().asBooleanLiteralExpr().getValue()).findFirst().orElse(!repoAnnotationRequired);
    }

    private void addPathAnnotation(BodyDeclaration<?> bodyDeclaration, String path) {
        AnnotationExpr annotationExpr = bodyDeclaration.getAnnotationByName(JAXRS_PATH_SIMPLE_NAME).orElse(null);
        if (annotationExpr == null) {
            annotationExpr = bodyDeclaration.addAndGetAnnotation(JAXRS_PATH_CLASS);
        }
        Optional<MemberValuePair> memberValuePairOptional = (((NormalAnnotationExpr) annotationExpr).getPairs().stream().filter(
                a -> a.getName().getIdentifier().equals(RESOURCE_PATH)
        ).findFirst());
        if (!memberValuePairOptional.isPresent()) {
            ((NormalAnnotationExpr) annotationExpr).addPair(RESOURCE_PATH,
                    path);
        } else {
            memberValuePairOptional.get().setValue(new NameExpr(path));
        }
    }

    private Name getNameFromClass(String fqClassName) {
        String[] packages = fqClassName.split("\\.");
        Name prevName = null;
        for (int i=0; i<packages.length; i++) {
            Name name = new Name(prevName, packages[i]);
            prevName = name;
        }
        return prevName;
    }

    private void createContentAnnotation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration,
                                         AnnotationDeclaration annotationDeclaration, String mediaType) {
        ClassOrInterfaceType domainClassOrInterfaceType = getDomainClass(compilationUnit, classOrInterfaceDeclaration);

        NormalAnnotationExpr schemaAnnotationExpr = new NormalAnnotationExpr(getNameFromClass(SCHEMA_ANNOTATION_CLASS),
                new NodeList<>(Collections.singletonList(new MemberValuePair(SCHEMA_IMPLEMENTATION,
                        new StringLiteralExpr(domainClassOrInterfaceType.getName().getIdentifier()+CLASS_EXT)))));

        NormalAnnotationExpr contentJsonAnnotationExpr = new NormalAnnotationExpr(getNameFromClass(CONTENT_ANNOTATION_CLASS),
                new NodeList<>(Arrays.asList(
                        new MemberValuePair(CONTENT_MEDIATYPE, new StringLiteralExpr(MEDIATYPE_JSON)),
                        new MemberValuePair(CONTENT_SCHEMA, schemaAnnotationExpr)
                        )));
        NormalAnnotationExpr contentJsonHalAnnotationExpr = new NormalAnnotationExpr(getNameFromClass(CONTENT_ANNOTATION_CLASS),
                new NodeList<>(Arrays.asList(
                        new MemberValuePair(CONTENT_MEDIATYPE, new StringLiteralExpr(MEDIATYPE_JSON_HAL)),
                        new MemberValuePair(CONTENT_SCHEMA, schemaAnnotationExpr)
                )));

        AnnotationExpr annotationExpr = bodyDeclaration.getAnnotationByName(JAXRS_PATH_SIMPLE_NAME).orElse(null);
        if (annotationExpr == null) {
            annotationExpr = bodyDeclaration.addAndGetAnnotation(JAXRS_PATH_CLASS);
        }
        Optional<MemberValuePair> memberValuePairOptional = (((NormalAnnotationExpr) annotationExpr).getPairs().stream().filter(
                a -> a.getName().getIdentifier().equals(RESOURCE_PATH)
        ).findFirst());
        if (!memberValuePairOptional.isPresent()) {
            ((NormalAnnotationExpr) annotationExpr).addPair(RESOURCE_PATH,
                    path);
        } else {
            memberValuePairOptional.get().setValue(new NameExpr(path));
        }
    }

    private void addOperationAnnotation(BodyDeclaration<?> bodyDeclaration, String summary, String description) {
        AnnotationExpr annotationExpr = bodyDeclaration.getAnnotationByName(JAXRS_PATH_SIMPLE_NAME).orElse(null);
        if (annotationExpr == null) {
            annotationExpr = bodyDeclaration.addAndGetAnnotation(JAXRS_PATH_CLASS);
        }
        Optional<MemberValuePair> memberValuePairOptional = (((NormalAnnotationExpr) annotationExpr).getPairs().stream().filter(
                a -> a.getName().getIdentifier().equals(RESOURCE_PATH)
        ).findFirst());
        if (!memberValuePairOptional.isPresent()) {
            ((NormalAnnotationExpr) annotationExpr).addPair(RESOURCE_PATH,
                    path);
        } else {
            memberValuePairOptional.get().setValue(new NameExpr(path));
        }
    }

    private void addMarkerAnnotation(BodyDeclaration<?> bodyDeclaration, String annotationSimpleName,
                                     String annotationClass) {
        bodyDeclaration.getAnnotationByName(annotationSimpleName).orElse(bodyDeclaration.addAndGetAnnotation(annotationClass));
    }

    private void removeAnnotation(CompilationUnit compilationUnit, BodyDeclaration<?> bodyDeclaration, String annotationSimpleName,
                                  String annotationClass) {
        bodyDeclaration.getAnnotationByName(annotationSimpleName).ifPresent(bodyDeclaration::remove);
        compilationUnit.getImports().stream().filter(i -> !i.isAsterisk()
                && i.getName().asString().equals(annotationClass)).forEach(Node::remove);
    }

    private String escapeString(String string) {
        return QUOTATION_MARK_STRING +
                string.trim().replace("\n", SPACE_STRING).
                        replace("\r", EMPTY_STRING).
                        replace("\"", "\\\"").
                        replaceAll("\\s+", SPACE_STRING) + QUOTATION_MARK_STRING;
    }

}
