package de.ohmesoftware.springdataresttoopenapischema;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.*;
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

    private static final String QUOTATION_MARK_STRING = "\"";

    private static final String DOT = ".";
    private static final String PLURAL_S = "s";
    private static final String SLASH = "/";
    private static final String JAVA_EXT = ".java";

    protected static final String PAGING_AND_SORTING_REPOSITORY = "PagingAndSortingRepository";
    protected static final String QUERY_DSL_PREDICATE_EXECUTOR = "QuerydslPredicateExecutor";
    protected static final String CRUD_REPOSITORY = "CrudRepository";
    protected static final String REPOSITORY = "Repository";

    private static final String JAVADOC_PARAM_TAG = "param";

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
     * @param sourcePath      The source path of the Java sources.
     * @param basePath        The base path no not include package directories.
     * @param compilationUnit The compilation unit to enrich with annotations.
     */
    protected ResourceHandler(String sourcePath, String basePath, CompilationUnit compilationUnit) {
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
            throw new RuntimeException("Could not find file.", e);
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

    protected File getSourceFile(CompilationUnit compilationUnit,
                                 ClassOrInterfaceType extent) {
        String className = getFullClassName(compilationUnit, extent);
        // get File
        String sourcePath = basePath + className + JAVA_EXT;
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

    // annotations

    protected void removeAnnotation(CompilationUnit compilationUnit, BodyDeclaration<?> bodyDeclaration,
                                    String annotationClass) {
        bodyDeclaration.getAnnotationByName(getSimpleNameFromClass(annotationClass)).ifPresent(bodyDeclaration::remove);
        compilationUnit.getImports().stream().filter(i -> !i.isAsterisk()
                && i.getName().asString().equals(annotationClass)).forEach(Node::remove);
    }

    // class

    protected void removeImport(CompilationUnit compilationUnit, String fqClassName) {
        compilationUnit.getImports().stream().filter(i -> !i.isAsterisk() &&
                i.getName().asString().equals(fqClassName)).forEach(i -> i.remove());
    }

    protected String getSimpleNameFromClass(String fqClassName) {
        String[] packages = fqClassName.split("\\.");
        return packages[packages.length-1];
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

    // domain

    protected ClassOrInterfaceType getIDClass(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
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
                    Pair<CompilationUnit, ClassOrInterfaceDeclaration> compilationUnitClassOrInterfaceDeclarationPair = parseClassOrInterfaceType(compilationUnit, extent);
                    ClassOrInterfaceType domainClassOrInterfaceType = getIDClass(compilationUnitClassOrInterfaceDeclarationPair.a,
                            compilationUnitClassOrInterfaceDeclarationPair.b);
                    if (domainClassOrInterfaceType != null) {
                        return domainClassOrInterfaceType;
                    }
            }
        }
        return null;
    }

    protected ClassOrInterfaceType getDomainClass(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
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
                    Pair<CompilationUnit, ClassOrInterfaceDeclaration> compilationUnitClassOrInterfaceDeclarationPair = parseClassOrInterfaceType(compilationUnit, extent);
                    ClassOrInterfaceType domainClassOrInterfaceType = getDomainClass(compilationUnitClassOrInterfaceDeclarationPair.a,
                            compilationUnitClassOrInterfaceDeclarationPair.b);
                    if (domainClassOrInterfaceType != null) {
                        return domainClassOrInterfaceType;
                    }
            }
        }
        return null;
    }

    protected String getDomainPath(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        return getDomainPath(getDomainClass(compilationUnit, classOrInterfaceDeclaration).getName().getIdentifier());
    }

    protected String getDomainPath(String string) {
        return string.substring(0, 1).toLowerCase() + string.substring(1) + PLURAL_S;
    }

    // JAX-RS

    protected void removeJaxRsAnnotations(CompilationUnit compilationUnit, BodyDeclaration<?> bodyDeclaration) {
        removeAnnotation(compilationUnit, bodyDeclaration, JAXRS_PATH_CLASS);
    }

    protected void addPathAnnotation(BodyDeclaration<?> bodyDeclaration, String path) {
        bodyDeclaration.addSingleMemberAnnotation(RESOURCE_PATH, new NameExpr(path));
    }

    // Resource annotations

    protected boolean checkResourceExported(NormalAnnotationExpr resource) {
        return resource.getPairs().stream().filter(p -> p.getName().getIdentifier().equals(RESOURCE_EXPORTED)).map(p -> p.getValue().asBooleanLiteralExpr().getValue())
                .findFirst().orElse(false);
    }

    protected String getResourcePath(NormalAnnotationExpr resource) {
        return resource.getPairs().stream().filter(p -> p.getName().getIdentifier().equals(RESOURCE_PATH)).
                map(p -> p.getValue().asStringLiteralExpr().asString()).findFirst().
                orElse(null);
    }

    protected Pair<CompilationUnit, ClassOrInterfaceDeclaration> parseClassOrInterfaceType(CompilationUnit compilationUnit,
                                                                                           ClassOrInterfaceType classOrInterfaceType) {

        CompilationUnit newCompilationUnit = parseFile(getSourceFile(compilationUnit, classOrInterfaceType));
        ClassOrInterfaceDeclaration newClassOrInterfaceDeclaration = newCompilationUnit.findFirst(ClassOrInterfaceDeclaration.class).
                orElseThrow(() -> new RuntimeException(
                        String.format("Could not parse class or interface type: %s", classOrInterfaceType.getName().getIdentifier())));
        return new Pair<>(newCompilationUnit, newClassOrInterfaceDeclaration);
    }

    protected Optional<NormalAnnotationExpr> checkResourceAnnotationPresent(BodyDeclaration<? extends BodyDeclaration> bodyDeclaration) {
        Optional<AnnotationExpr> restResourceOptional =
                bodyDeclaration.getAnnotationByName(getSimpleNameFromClass(REPOSITORY_REST_RESOURCE_CLASS));
        if (!restResourceOptional.isPresent()) {
            restResourceOptional =
                    bodyDeclaration.getAnnotationByName(getSimpleNameFromClass(RESOURCE_CLASS));
        }
        return restResourceOptional.
                map(Expression::asNormalAnnotationExpr);
    }

    protected boolean checkResourceExported(NormalAnnotationExpr resource,
                                            boolean repoAnnotationRequired) {
        return resource.getPairs().stream().filter(p -> p.getName().getIdentifier().equals(RESOURCE_EXPORTED))
                .map(p -> p.getValue().asBooleanLiteralExpr().getValue()).findFirst().orElse(!repoAnnotationRequired);
    }

    // javadoc

    protected String escapeString(String string) {
        return QUOTATION_MARK_STRING +
                string.trim().replace("\n", SPACE_STRING).
                        replace("\r", EMPTY_STRING).
                        replace("\"", "\\\"").
                        replaceAll("\\s+", SPACE_STRING) + QUOTATION_MARK_STRING;
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
        String[] commentParts = javadoc.split("<p>");
        return commentParts[0];
    }

    protected String getJavadocDescription(String javadoc) {
        String[] commentParts = javadoc.split("<p>");
        return commentParts.length > 1 ? commentParts[1] : null;
    }
}
