package de.ohmesoftware.springdataresttoopenapischema;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.utils.Pair;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
public class CustomRepositoryResourceMethodHandler extends ResourceMethodHandler {

    private static final String UPDATE_METHOD = "update";
    private static final String UPDATE_METHOD_PARAM = "entity";

    /**
     * Constructor.
     *
     * @param sourceFile      The source file.
     * @param sourcePath      The source path of the Java sources.
     * @param basePath        The base path no not include package directories.
     * @param compilationUnit The compilation unit to enrich with annotations.
     */
    protected CustomRepositoryResourceMethodHandler(String sourceFile, String sourcePath, String basePath,
                                                    CompilationUnit compilationUnit) {
        super(sourceFile, sourcePath, basePath, compilationUnit);
    }

    @Override
    public void addResourceAnnotations() {
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream().filter(
                c -> isConcreteRepositoryClass(compilationUnit, c)
        ).forEach(
                c -> addCustomRepositoryOperation(compilationUnit, c)
        );
    }

    @Override
    public void removeResourceAnnotations() {
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream().filter(
                c -> isConcreteRepositoryClass(compilationUnit, c)
        ).forEach(
                c -> removeCustomRepositoryOperation(compilationUnit, c)
        );
    }

    private void removeCustomRepositoryOperation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        ClassOrInterfaceDeclaration customRepositoryClassOrInterfaceDeclaration =
                findCustomRepositoryInterface(compilationUnit, classOrInterfaceDeclaration);
        if (customRepositoryClassOrInterfaceDeclaration != null) {
            customRepositoryClassOrInterfaceDeclaration.getMethods().stream(
            ).filter(m -> !checkResourceAnnotationPresent(m).isPresent() && !isUpdateMethod(compilationUnit, classOrInterfaceDeclaration, m)
                    ).forEach(m ->
            {
                removeJaxRsPathAnnotation(m);
                // keep HTTP JAX-RS verb, it cannot be detected
                removeAnnotation(m, OPERATION_ANNOTATION_CLASS);
            });
        }
    }

    private boolean isUpdateMethod(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration, MethodDeclaration methodDeclaration) {
        ClassOrInterfaceType domainClassType = getDomainClass(compilationUnit, classOrInterfaceDeclaration);
        if (methodDeclaration.getName().toString().equals(UPDATE_METHOD)
        && methodDeclaration.getParameters().size() == 1 && methodDeclaration.getParameter(0).getType().equals(domainClassType)) {
            return true;
        }
        return false;
    }

    private void addCustomRepositoryOperation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        ClassOrInterfaceDeclaration customRepositoryClassOrInterfaceDeclaration =
                findCustomRepositoryInterface(compilationUnit, classOrInterfaceDeclaration);
        if (customRepositoryClassOrInterfaceDeclaration != null) {
            customRepositoryClassOrInterfaceDeclaration.getMethods().stream(
            ).filter(m -> !checkResourceAnnotationPresent(m).isPresent() && !isUpdateMethod(compilationUnit, classOrInterfaceDeclaration, m)
            ).forEach(m ->
            {
                // skip if annotated with Resource annotation
                Pair<Boolean, String> exportPathConfig = getResourceConfig(m.getNameAsString(), classOrInterfaceDeclaration,
                         null, m.getParameters().stream().map(Parameter::getType).toArray(Type[]::new));
                if (!exportPathConfig.a) {
                    return;
                }
                if (exportPathConfig.b != null) {
                    addPathAnnotation(customRepositoryClassOrInterfaceDeclaration, exportPathConfig.b);
                }

                List<NormalAnnotationExpr> responses = null;
                if (m.getType().isClassOrInterfaceType()) {
                    responses = Collections.singletonList(
                            createApiResponseAnnotation200WithContentForType(m.getType()));
                }
                else if (m.getType().isArrayType() || !isNoCollectionObject(m.getType())) {
                    responses = Collections.singletonList(
                            createApiResponseAnnotation200WithContentForListType(m.getType()));
                }
                else if (m.getType().isVoidType()) {

                }
                addOperationAnnotation(m,
                        createRequestBodyAnnotation(compilationUnit, classOrInterfaceDeclaration),
                        responses,
                        null);

            });
            saveClassOrInterfaceToFile(customRepositoryClassOrInterfaceDeclaration);
        }
    }


}
