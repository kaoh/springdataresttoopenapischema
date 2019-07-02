package de.ohmesoftware.springdataresttoopenapischema;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.utils.Pair;

import java.util.Arrays;

/**
 * Resource method handler for update.
 * <p>
 * Because the "update" method cannot be added directly to a repository because it does match
 * the naming conventions and a custom interface must be added.
 * </p>
 *
 * @author <a href="mailto:karsten@simless.com">Karsten Ohme
 * (karsten@simless.com)</a>
 */
public abstract class UpdateResourceMethodHandler extends ResourceMethodHandler {

    private static final String SAVE_METHOD = "save";
    private static final String UPDATE_METHOD_PARAM = "entity";
    private static final String CUSTOM_REPOSITORY_NAME_TEMPLATE = "Custom%sRepository";

    /**
     * Constructor.
     *
     * @param sourceFile      The source file.
     * @param sourcePath      The source path of the Java sources.
     * @param basePath        The base path no not include package directories.
     * @param compilationUnit The compilation unit to enrich with annotations.
     */
    protected UpdateResourceMethodHandler(String sourceFile, String sourcePath, String basePath,
                                          CompilationUnit compilationUnit) {
        super(sourceFile, sourcePath, basePath, compilationUnit);
    }

    /**
     * Must be implemented to add the necessary annotation like PATCH or PUT.
     * @param bodyDeclaration The body declaration where the annotation is added.
     */
    protected abstract void addUpdateAnnotation(BodyDeclaration<?> bodyDeclaration);

    /**
     * Provides the method name of the update method.
     * @return the method name.
     */
    protected abstract String getUpdateMethodName();

    /**
     * Provides a description of the method.
     * @param className The classname for which the update method is for.
     * @return the description.
     */
    protected abstract String getDescription(String className);

    @Override
    public void addResourceAnnotations() {
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream().filter(
                c -> isConcreteRepositoryClass(c)
        ).forEach(
                c -> addUpdateOperation(compilationUnit, c)
        );
    }

    @Override
    public void removeResourceAnnotations() {
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream().filter(
                c -> isConcreteRepositoryClass(c)
        ).forEach(
                c -> removeUpdateOperation(compilationUnit, c)
        );
    }

    private void removeUpdateOperation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        ClassOrInterfaceType domainClassOrInterfaceType = getDomainClass(classOrInterfaceDeclaration);
        ClassOrInterfaceDeclaration customRepositoryClassOrInterfaceDeclaration =
                findCustomRepositoryInterface(classOrInterfaceDeclaration);
        if (customRepositoryClassOrInterfaceDeclaration != null) {
            MethodDeclaration methodDeclaration = findMethodByMethodNameAndParameters(customRepositoryClassOrInterfaceDeclaration, getUpdateMethodName(),
                    false, domainClassOrInterfaceType.asString());
            if (methodDeclaration != null && !checkResourceAnnotationPresent(methodDeclaration).isPresent()) {
                methodDeclaration.remove();
                saveClassOrInterfaceToFile(customRepositoryClassOrInterfaceDeclaration);
            }
        }
    }

    private void addUpdateOperation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        ClassOrInterfaceType domainClassOrInterfaceType = getDomainClass(classOrInterfaceDeclaration);
        // check if save method is exported
        Pair<Boolean, String> exportPathConfig = getResourceConfig(SAVE_METHOD, classOrInterfaceDeclaration, null, domainClassOrInterfaceType);
        if (!exportPathConfig.a) {
            return;
        }
        ClassOrInterfaceDeclaration customRepositoryClassOrInterfaceDeclaration =
                findCustomRepositoryInterface(classOrInterfaceDeclaration);
        // add missing interface
        if (customRepositoryClassOrInterfaceDeclaration == null) {
            CompilationUnit customInterfaceCompilationUnit;
            if (compilationUnit.getPackageDeclaration().isPresent()) {
                customInterfaceCompilationUnit = new CompilationUnit(compilationUnit.getPackageDeclaration().
                        get().getName().asString());
            }
            else {
                customInterfaceCompilationUnit = new CompilationUnit(EMPTY_STRING).removePackageDeclaration();
            }
            String customInterfaceClassName = String.format(CUSTOM_REPOSITORY_NAME_TEMPLATE,
                    domainClassOrInterfaceType.getName().getIdentifier());
            customInterfaceCompilationUnit.addImport(domainClassOrInterfaceType.asString());
            customRepositoryClassOrInterfaceDeclaration = customInterfaceCompilationUnit.addInterface(customInterfaceClassName);
            // add to initial search class as extension
            classOrInterfaceDeclaration.addExtendedType(customInterfaceClassName);
        }
        if (exportPathConfig.b != null) {
            addPathAnnotation(customRepositoryClassOrInterfaceDeclaration, exportPathConfig.b);
        }
        // add missing method
        MethodDeclaration methodDeclaration = findMethodByMethodNameAndParameters(customRepositoryClassOrInterfaceDeclaration,
                getUpdateMethodName(),
                false, domainClassOrInterfaceType.asString());
        if (methodDeclaration == null) {
            methodDeclaration = addInterfaceMethod(customRepositoryClassOrInterfaceDeclaration,
                    getUpdateMethodName(), domainClassOrInterfaceType, new Parameter(domainClassOrInterfaceType, UPDATE_METHOD_PARAM))
                    .setBody(new BlockStmt(new NodeList<>(new ReturnStmt(new NullLiteralExpr())))).setDefault(true);
        }
        addUpdateAnnotation(methodDeclaration);
        addPathAnnotation(methodDeclaration, ID_PATH);
        addOperationAnnotation(methodDeclaration,
                createRequestBodyAnnotation(classOrInterfaceDeclaration),
                Arrays.asList(
                        createApiResponse204(),
                        createApiResponseAnnotation200WithContent(
                                classOrInterfaceDeclaration)),
               getDescription(
                        getSimpleNameFromClass(
                                getDomainClass(classOrInterfaceDeclaration).asString())));

        saveClassOrInterfaceToFile(customRepositoryClassOrInterfaceDeclaration);
    }


}
