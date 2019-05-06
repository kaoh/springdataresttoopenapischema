package de.ohmesoftware.springdataresttoopenapischema;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.VoidType;

/**
 * Resource method handler for findById and deleteById.
 *
 * @author <a href="mailto:karsten@simless.com">Karsten Ohme
 * (karsten@simless.com)</a>
 */
public abstract class MethodByIdResourceMethodHandler extends ResourceMethodHandler {

    private static final String METHOD_BY_ID_PARAM = "id";

    private String methodByIdName;

    private boolean returnVoid;

    /**
     * Constructor.
     *
     * @param sourceFile      The source file.
     * @param sourcePath      The source path of the Java sources.
     * @param basePath        The base path no not include package directories.
     * @param compilationUnit The compilation unit to enrich with annotations.
     */
    protected MethodByIdResourceMethodHandler(String sourceFile, String sourcePath, String basePath,
                                              CompilationUnit compilationUnit,
                                              String methodByIdName, boolean returnVoid) {
        super(sourceFile, sourcePath, basePath, compilationUnit);
        this.methodByIdName = methodByIdName;
        this.returnVoid = returnVoid;
    }

    @Override
    public void addResourceAnnotations() {
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream().filter(
                c -> isConcreteRepositoryClass(compilationUnit, c)
        ).forEach(
                c -> addMethodByIdOperation(compilationUnit, c)
        );
    }

    @Override
    public void removeResourceAnnotations() {
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class).stream().filter(
                c -> isConcreteRepositoryClass(compilationUnit, c)
        ).forEach(
                c -> removeMethodIdByOperation(compilationUnit, c)
        );
    }

    private void removeMethodIdByOperation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        MethodDeclaration methodDeclaration = findClosestMethod(compilationUnit, classOrInterfaceDeclaration,
                methodByIdName, String.class.getName());
        if (methodDeclaration != null) {
            removeMethodParameterAnnotation(methodDeclaration, JAXRS_PATH_PARAM_CLASS);
            removeMethodParameterAnnotation(methodDeclaration, PARAMETER_CLASS);
            removeCrudOperationAnnotationAndMethod(methodDeclaration, compilationUnit, classOrInterfaceDeclaration);
        }
    }

    private void addMethodByIdOperation(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        MethodDeclaration methodDeclaration = findClosestMethod(compilationUnit, classOrInterfaceDeclaration, methodByIdName, String.class.getName());
        // check if this method is using a concrete class
        if (methodDeclaration != null && !isMethodOfConcreteRepositoryClass(methodDeclaration)) {
            methodDeclaration = null;
        }
        // add missing method automatically if extending CRUD interface
        if (methodDeclaration == null) {
            if (checkIfExtendingCrudInterface(compilationUnit, classOrInterfaceDeclaration)) {
                ClassOrInterfaceType domainClassOrInterfaceType = getDomainClass(compilationUnit, classOrInterfaceDeclaration);
                ClassOrInterfaceType idClass = getIDClass(compilationUnit, classOrInterfaceDeclaration);
                methodDeclaration = addInterfaceMethod(classOrInterfaceDeclaration,
                        methodByIdName, returnVoid ? new VoidType() : getOptionalWrapper(domainClassOrInterfaceType), new Parameter(
                                idClass, METHOD_BY_ID_PARAM));
            }
        }
        if (methodDeclaration == null) {
            return;
        }
        AnnotationExpr methodResource = findClosestMethodResourceAnnotation(compilationUnit, classOrInterfaceDeclaration, methodByIdName,
                String.class.getName());
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
            addPathParamAnnotation(methodDeclaration, METHOD_BY_ID_PARAM, true, "The database id.");
            additionalMethodByIdOperation(methodDeclaration, classOrInterfaceDeclaration);
        }
    }

    protected abstract void additionalMethodByIdOperation(MethodDeclaration methodDeclaration, ClassOrInterfaceDeclaration classOrInterfaceDeclaration);

}
