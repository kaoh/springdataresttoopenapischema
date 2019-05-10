package de.ohmesoftware.springdataresttoopenapischema;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.utils.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * base class for processing resources.
 *
 * @author <a href="mailto:karsten@simless.com">Karsten Ohme
 * (karsten@simless.com)</a>
 */
public abstract class ResourceHandler {

    protected static final String EMPTY_STRING = "";
    protected static final String SPACE_STRING = " ";

    protected static final String JAXRS_PATH_CLASS = "javax.ws.rs.Path";

    protected static final String RESOURCE_EXPORTED = "exported";
    protected static final String RESOURCE_PATH = "path";

    protected static final String REPOSITORY_REST_RESOURCE_CLASS = "org.springframework.data.rest.core.annotation.RepositoryRestResource";

    protected static final String RESOURCE_CLASS = "org.springframework.data.rest.core.annotation.RestResource";

    protected static final String TAG_CLASS = "io.swagger.v3.oas.annotations.tags.Tag";
    protected static final String TAG_NAME = "name";

    private static final String QUOTATION_MARK_STRING = "\"";

    protected static final String DOT = ".";
    protected static final String PLURAL_S = "s";
    protected static final String SLASH = "/";
    private static final String JAVA_EXT = ".java";

    protected static final String PAGING_AND_SORTING_REPOSITORY = "PagingAndSortingRepository";
    protected static final String QUERYDSL_PREDICATE_EXECUTOR = "QuerydslPredicateExecutor";
    protected static final String CRUD_REPOSITORY = "CrudRepository";
    protected static final String REPOSITORY = "Repository";

    private static final String JAVADOC_PARAM_TAG = "param";
    private static final String PARAGRAPH_START = "<p>";
    private static final String PARAGRAPH_END = "</p>";

    /**
     * The source file.
     */
    protected String sourceFile;

    /**
     * The source path of the Java sources.
     */
    protected String sourcePath;

    /**
     * The base path no not include package directories.
     */
    protected String basePath;

    /**
     * The compilation unit to enrich with annotations.
     */
    protected CompilationUnit compilationUnit;

    protected List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarations;

    /**
     * Constructor.
     *
     * @param sourceFile      The source file.
     * @param sourcePath      The source path of the Java sources.
     * @param basePath        The base path no not include package directories.
     * @param compilationUnit The compilation unit to enrich with annotations.
     */
    protected ResourceHandler(String sourceFile, String sourcePath, String basePath, CompilationUnit compilationUnit) {
        this.sourceFile = sourceFile;
        this.sourcePath = sourcePath;
        this.compilationUnit = compilationUnit;
        this.basePath = basePath;
    }

    public abstract void addResourceAnnotations();

    public abstract void removeResourceAnnotations();

    public static CompilationUnit parseFile(File file) {
        try {
            return JavaParser.parse(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(String.format("Could not find file: %s", file), e);
        }
    }

    public static String getBaseSourcePath(CompilationUnit compilationUnit, String sourcePath) {
        String _package = compilationUnit.getPackageDeclaration().map(p -> p.getName().asString()).orElse(EMPTY_STRING);
        String packagePath = _package.replace(".", SLASH);
        int overlap = 0;
        for (int i = packagePath.length(); i >= 0; i--) {
            if (sourcePath.endsWith(packagePath.substring(0, i))) {
                overlap = i;
                break;
            }
        }
        return sourcePath.substring(0, sourcePath.length() - overlap);
    }

    protected File getSourceFile(CompilationUnit compilationUnit, ClassOrInterfaceType extent) {
        String className = getFullClassName(compilationUnit, extent);
        // get File
        String sourcePath = basePath + className.replace('.', '/') + JAVA_EXT;
        return new File(sourcePath);
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
            case "Date":
            case "Boolean":
                return String.class.getPackage().getName() + DOT + className;
            case "Optional":
                return Optional.class.getPackage().getName() + DOT + className;
        }
        return compilationUnit.getImports().stream().filter(i -> !i.isAsterisk() && i.getName().getIdentifier().equals(className)).
                map(i -> i.getName().asString()).findFirst().orElse(
                        compilationUnit.getPackageDeclaration().map(p -> p.getName().asString() + DOT + className).
                                orElseThrow(
                                        () -> new RuntimeException(
                                                String.format("Could not resolve import for type: %s", className))
                                ));
    }

    private String getFullClassName(CompilationUnit compilationUnit, ClassOrInterfaceType extent) {
        return getFullClassName(compilationUnit, extent.getNameAsString());
    }

    // annotations

    protected void removeAnnotation(BodyDeclaration<?> bodyDeclaration,
                                    String annotationClass) {
        bodyDeclaration.getAnnotationByName(getSimpleNameFromClass(annotationClass)).ifPresent(bodyDeclaration::remove);
    }

    // class

    protected void removeImport(CompilationUnit compilationUnit, String fqClassName) {
        compilationUnit.getImports().stream().filter(i -> !i.isAsterisk() &&
                i.getName().asString().equals(fqClassName)).forEach(i -> i.remove());
    }

    protected String getSimpleNameFromClass(String fqClassName) {
        String[] packages = fqClassName.split("\\.");
        return packages[packages.length - 1];
    }

    protected ClassOrInterfaceType getClassOrInterfaceTypeFromClassName(CompilationUnit compilationUnit, String className) {
        className = getFullClassName(compilationUnit, className);
        String[] packages = className.split("\\.");
        ClassOrInterfaceType prevClassOrInterfaceType = null;
        for (int i = 0; i < packages.length; i++) {
            ClassOrInterfaceType classOrInterfaceType = new ClassOrInterfaceType(prevClassOrInterfaceType, packages[i]);
            prevClassOrInterfaceType = classOrInterfaceType;
        }
        return prevClassOrInterfaceType;
    }

    protected boolean checkIfExtendingCrudInterface(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        for (ClassOrInterfaceType extent : classOrInterfaceDeclaration.getExtendedTypes()) {
            switch (extent.getName().getIdentifier()) {
                case PAGING_AND_SORTING_REPOSITORY:
                case CRUD_REPOSITORY:
                    return true;
                default:
                    // visit interface to get information
                    if (getSourceFile(classOrInterfaceDeclaration.findCompilationUnit().get(), extent).exists()) {
                        TypeDeclaration extendTypeDeclaration = parseClassOrInterfaceType(
                                classOrInterfaceDeclaration.findCompilationUnit().get(), extent);
                        boolean extending = checkIfExtendingCrudInterface(extendTypeDeclaration.asClassOrInterfaceDeclaration());
                        if (extending) {
                            return extending;
                        }
                    }
            }
        }
        return false;
    }

    protected boolean checkIfExtendingQuerydslInterface(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        for (ClassOrInterfaceType extent : classOrInterfaceDeclaration.getExtendedTypes()) {
            switch (extent.getName().getIdentifier()) {
                case QUERYDSL_PREDICATE_EXECUTOR:
                    return true;
                default:
                    // visit interface to get information
                    if (getSourceFile(classOrInterfaceDeclaration.findCompilationUnit().get(), extent).exists()) {
                        TypeDeclaration extendTypeDeclaration = parseClassOrInterfaceType(
                                classOrInterfaceDeclaration.findCompilationUnit().get(), extent);
                        boolean extending = checkIfExtendingQuerydslInterface(extendTypeDeclaration.asClassOrInterfaceDeclaration());
                        if (extending) {
                            return extending;
                        }
                    }
            }
        }
        return false;
    }

    protected boolean checkIfExtendingRepository(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        if (checkIfExtendingCrudInterface(classOrInterfaceDeclaration)) {
            return true;
        }
        for (ClassOrInterfaceType extent : classOrInterfaceDeclaration.getExtendedTypes()) {
            switch (extent.getName().getIdentifier()) {
                case REPOSITORY:
                    return true;
                default:
                    // visit interface to get information
                    if (getSourceFile(classOrInterfaceDeclaration.findCompilationUnit().get(), extent).exists()) {
                        TypeDeclaration extendTypeDeclaration = parseClassOrInterfaceType(
                                classOrInterfaceDeclaration.findCompilationUnit().get(), extent);
                        boolean extending = checkIfExtendingRepository(extendTypeDeclaration.asClassOrInterfaceDeclaration());
                        if (extending) {
                            return extending;
                        }
                    }
            }
        }
        return false;
    }

    protected boolean isCustomInterface(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        return !checkIfExtendingCrudInterface(
                classOrInterfaceDeclaration)
                &&
                !checkIfExtendingQuerydslInterface(
                        classOrInterfaceDeclaration)
                &&
                !checkIfExtendingRepository(
                        classOrInterfaceDeclaration);
    }

    protected ClassOrInterfaceDeclaration findCustomRepositoryInterface(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        for (ClassOrInterfaceType extent : classOrInterfaceDeclaration.getExtendedTypes()) {
            // visit interface to get information
            if (getSourceFile(classOrInterfaceDeclaration.findCompilationUnit().get(), extent).exists()) {
                TypeDeclaration extendTypeDeclaration = parseClassOrInterfaceType(
                        classOrInterfaceDeclaration.findCompilationUnit().get(), extent);
                if (isCustomInterface(
                        extendTypeDeclaration.asClassOrInterfaceDeclaration())) {
                    return extendTypeDeclaration.asClassOrInterfaceDeclaration();
                } else {
                    ClassOrInterfaceDeclaration foundInterface = findCustomRepositoryInterface(extendTypeDeclaration.asClassOrInterfaceDeclaration());
                    if (foundInterface != null) {
                        return foundInterface;
                    }
                }
            }
        }
        return null;
    }

    // domain

    protected ClassOrInterfaceType getIDClass(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
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
                    TypeDeclaration extendTypeDeclaration = parseClassOrInterfaceType(
                            classOrInterfaceDeclaration.findCompilationUnit().get(), extent);
                    ClassOrInterfaceType domainClassOrInterfaceType = getIDClass(extendTypeDeclaration.asClassOrInterfaceDeclaration());
                    if (domainClassOrInterfaceType != null) {
                        return domainClassOrInterfaceType;
                    }
            }
        }
        return null;
    }

    protected ClassOrInterfaceType getDomainClass(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        for (ClassOrInterfaceType extent : classOrInterfaceDeclaration.getExtendedTypes()) {
            switch (extent.getName().getIdentifier()) {
                case PAGING_AND_SORTING_REPOSITORY:
                case QUERYDSL_PREDICATE_EXECUTOR:
                case REPOSITORY:
                case CRUD_REPOSITORY:
                    if (!extent.getTypeArguments().isPresent()) {
                        throw new IllegalArgumentException(String.format("Repository does not specify a domain type: %s", extent.getName().getIdentifier()));
                    }
                    if (!extent.findCompilationUnit().isPresent()) {
                        throw new IllegalArgumentException(
                                String.format("Could not find compilation unit for %s", extent.getNameAsString()));
                    }
                    return getClassOrInterfaceTypeFromClassName(extent.findCompilationUnit().get(), extent.getTypeArguments().get().get(0).asString());
                default:
                    // domain class must be first type parameter
                    if (extent.getTypeArguments().isPresent()) {
                        if (!extent.findCompilationUnit().isPresent()) {
                            throw new IllegalArgumentException(
                                    String.format("Could not find compilation unit for %s", extent.getNameAsString()));
                        }
                        return getClassOrInterfaceTypeFromClassName(extent.findCompilationUnit().get(), extent.getTypeArguments().get().get(0).asString());
                    }
                    else {
                        // visit interface to get information
                        TypeDeclaration extendTypeDeclaration = parseClassOrInterfaceType(
                                classOrInterfaceDeclaration.findCompilationUnit().get(), extent);
                        ClassOrInterfaceType domainClassOrInterfaceType = getDomainClass(extendTypeDeclaration.asClassOrInterfaceDeclaration());
                        if (domainClassOrInterfaceType != null) {
                            return domainClassOrInterfaceType;
                        }
                    }
            }
        }
        return null;
    }

    protected String getDomainPath(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        return getDomainPath(getDomainClass(classOrInterfaceDeclaration).getName().getIdentifier());
    }

    protected String getDomainPath(String string) {
        return toLowerCase(string) + PLURAL_S;
    }

    protected String toLowerCase(String string) {
        return string.substring(0, 1).toLowerCase() + string.substring(1);
    }

    // JAX-RS

    protected void removeJaxRsPathAnnotation(BodyDeclaration<?> bodyDeclaration) {
        removeAnnotation(bodyDeclaration, JAXRS_PATH_CLASS);
    }

    protected void addPathAnnotation(BodyDeclaration<?> bodyDeclaration, String path) {
        bodyDeclaration.addSingleMemberAnnotation(JAXRS_PATH_CLASS, new NameExpr(quoteString(SLASH + path)));
    }

    protected void addTagAnnotation(BodyDeclaration<?> bodyDeclaration, String name) {
        bodyDeclaration.addAndGetAnnotation(TAG_CLASS).addPair(TAG_NAME, new StringLiteralExpr(escapeString(name)));
    }

    // Resource annotations

    protected boolean checkResourceExported(AnnotationExpr resource) {
        if (resource.isMarkerAnnotationExpr()) {
            return true;
        }
        return resource.asNormalAnnotationExpr().getPairs().stream().filter(p -> p.getName().getIdentifier().equals(RESOURCE_EXPORTED)).map(p -> p.getValue().asBooleanLiteralExpr().getValue())
                .findFirst().orElse(true);
    }

    protected String getResourcePath(AnnotationExpr resource) {
        if (!resource.isNormalAnnotationExpr()) {
            return null;
        }
        return resource.asNormalAnnotationExpr().getPairs().stream().filter(p -> p.getName().getIdentifier().equals(RESOURCE_PATH)).
                map(p -> p.getValue().asStringLiteralExpr().asString()).findFirst().
                orElse(null);
    }

    protected TypeDeclaration parseClassOrInterfaceType(CompilationUnit compilationUnit, ClassOrInterfaceType classOrInterfaceType) {

        CompilationUnit newCompilationUnit = parseFile(getSourceFile(compilationUnit, classOrInterfaceType));
        TypeDeclaration newClassOrInterfaceDeclaration = newCompilationUnit.findFirst(TypeDeclaration.class).
                orElseThrow(() -> new RuntimeException(
                        String.format("Could not parse type: %s", classOrInterfaceType.asString())));
        return newClassOrInterfaceDeclaration;
    }

    protected Optional<AnnotationExpr> checkResourceAnnotationPresent(BodyDeclaration<? extends BodyDeclaration> bodyDeclaration) {
        Optional<AnnotationExpr> restResourceOptional =
                bodyDeclaration.getAnnotationByName(getSimpleNameFromClass(REPOSITORY_REST_RESOURCE_CLASS));
        if (!restResourceOptional.isPresent()) {
            restResourceOptional =
                    bodyDeclaration.getAnnotationByName(getSimpleNameFromClass(RESOURCE_CLASS));
        }
        return restResourceOptional;
    }

    protected boolean checkResourceExported(AnnotationExpr resource,
                                            boolean repoAnnotationRequired) {
        if (resource.isMarkerAnnotationExpr()) {
            return true;
        }
        return resource.asNormalAnnotationExpr().getPairs().stream().filter(p -> p.getName().getIdentifier().equals(RESOURCE_EXPORTED))
                .map(p -> p.getValue().asBooleanLiteralExpr().getValue()).findFirst().orElse(!repoAnnotationRequired);
    }

    // javadoc

    protected String quoteString(String string) {
        return QUOTATION_MARK_STRING + string + QUOTATION_MARK_STRING;
    }

    protected String escapeString(String string) {
        return string.trim().replace("\n", SPACE_STRING).
                replace("\r", EMPTY_STRING).
                replace("<", "&lt;").
                replace(">", "&gt;").
                replace("\"", "\\\"").
                replaceAll("\\s+", SPACE_STRING);
    }

    protected Javadoc getJavadoc(BodyDeclaration bodyDeclaration) {
        return bodyDeclaration.getComment().filter(Comment::isJavadocComment).map(c -> c.asJavadocComment().parse()).orElse(null);
    }

    protected String getJavadocParameter(Javadoc javadoc, String parameter) {
        return javadoc.getBlockTags().stream().filter(t -> t.getTagName() != null && t.getTagName().equals(JAVADOC_PARAM_TAG)
                && t.getName().isPresent() && t.getName().get().equals(parameter)).
                map(t -> t.getContent().toText().trim()).findFirst().orElse(null);
    }

    protected String getJavadocText(Javadoc javadoc) {
        return javadoc.getDescription().getElements().stream().map(d -> d.toText().trim()).collect(Collectors.joining(SPACE_STRING));
    }

    protected String getJavadocSummary(String javadoc) {
        String[] commentParts = javadoc.split(PARAGRAPH_START);
        return commentParts[0].trim();
    }

    protected String getJavadocDescription(String javadoc) {
        String[] commentParts = javadoc.split(PARAGRAPH_START);
        if (commentParts.length > 1) {
            String description = commentParts[1].trim();
            if (description.endsWith(PARAGRAPH_START)) {
                description = description.substring(0, description.length()-PARAGRAPH_START.length());
            }
            if (description.endsWith(PARAGRAPH_END)) {
                description = description.substring(0, description.length()-PARAGRAPH_END.length());
            }
            return description;
        }
        return null;
    }
}
