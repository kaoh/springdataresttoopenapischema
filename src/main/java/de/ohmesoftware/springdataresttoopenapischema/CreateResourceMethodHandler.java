package de.ohmesoftware.springdataresttoopenapischema;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.utils.Pair;

import java.util.Collections;

/**
 * Resource method handler for create.
 *
 * @author <a href="mailto:karsten@simless.com">Karsten Ohme
 * (karsten@simless.com)</a>
 */
public class CreateResourceMethodHandler extends ResourceMethodHandler {

    private static final String SAVE_METHOD = "save";
    private static final String SAVE_METHOD_PARAM = "entity";

    /**
     * Constructor.
     *
     * @param sourceFile      The source file.
     * @param sourcePath      The source path of the Java sources.
     * @param basePath        The base path no not include package directories.
     * @param compilationUnit The compilation unit to enrich with annotations.
     */
    protected CreateResourceMethodHandler(String sourceFile, String sourcePath, String basePath,
                                          CompilationUnit compilationUnit) {
        super(sourceFile, sourcePath, basePath, compilationUnit);
    }

    @Override
    public void addResourceAnnotations() {
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream().filter(
                c -> isConcreteRepositoryClass(c)
        ).forEach(
                c -> addCreateOperation(compilationUnit, c)
        );
    }

    @Override
    public void removeResourceAnnotations() {
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream().filter(
                c -> isConcreteRepositoryClass(c)
        ).forEach(
                c -> removeCreateOperation(compilationUnit, c)
        );
    }

    private void removeCreateOperation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        ClassOrInterfaceType domainClassOrInterfaceType = getDomainClass(classOrInterfaceDeclaration);
        MethodDeclaration methodDeclaration = findClosestMethod(classOrInterfaceDeclaration,
                SAVE_METHOD, domainClassOrInterfaceType.asString());
        if (methodDeclaration != null) {
            removeMethodParameterAnnotation(methodDeclaration, JAXRS_PATH_PARAM_CLASS);
            removeMethodParameterAnnotation(methodDeclaration, PARAMETER_CLASS);
            removeCrudOperationAnnotationAndMethod(methodDeclaration, compilationUnit, classOrInterfaceDeclaration);
        }
    }

    private void addCreateOperation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        ClassOrInterfaceType domainClassOrInterfaceType = getDomainClass(classOrInterfaceDeclaration);
        MethodDeclaration methodDeclaration = findClosestMethod(classOrInterfaceDeclaration, SAVE_METHOD,
                domainClassOrInterfaceType.asString());
        if (isHidden(methodDeclaration)) {
            return;
        }
        // check if this method is using a concrete class
        if (methodDeclaration != null && !isMethodOfConcreteRepositoryClass(methodDeclaration)) {
            methodDeclaration = null;
        }
        // add missing method automatically if extending CRUD interface
        if (methodDeclaration == null) {
            if (checkIfExtendingCrudInterface(classOrInterfaceDeclaration)) {
                methodDeclaration = addInterfaceMethod(classOrInterfaceDeclaration,
                        SAVE_METHOD, domainClassOrInterfaceType, new Parameter(domainClassOrInterfaceType, SAVE_METHOD_PARAM));
            }
        }
        if (methodDeclaration == null) {
            return;
        }
        Pair<Boolean, String> exportPathConfig  = getResourceConfig(SAVE_METHOD, classOrInterfaceDeclaration,
                null, domainClassOrInterfaceType);
        if (exportPathConfig.a) {
            if (exportPathConfig.b != null) {
                addPathAnnotation(classOrInterfaceDeclaration, exportPathConfig.b);
            }
            addPOSTAnnotation(methodDeclaration);
            addOperationAnnotation(methodDeclaration,
                    createRequestBodyAnnotation(classOrInterfaceDeclaration),
                    Collections.singletonList(
                            createApiResponseAnnotation201WithContent(compilationUnit,
                                    classOrInterfaceDeclaration)),
                    String.format("Creates a(n) %s.",
                            getSimpleNameFromClass(
                                    getDomainClass(classOrInterfaceDeclaration).asString())));
        }
    }


}
