package de.ohmesoftware.springdataresttoopenapischema;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handler for adding annotation for the domain.
 *
 * @author <a href="mailto:karsten@simless.com">Karsten Ohme
 * (karsten@simless.com)</a>
 */
public class DomainResourceHandler extends ResourceHandler {

    private List<ResourceMethodHandler> resourceMethodHandlers;

    /**
     * Constructor.
     *
     * @param sourceFile The source file.
     * @param sourcePath      The source path of the Java sources.
     * @param basePath        The base path no not include package directories.
     * @param compilationUnit The compilation unit to enrich with annotations.
     */
    protected DomainResourceHandler(String sourceFile, String sourcePath, String basePath, CompilationUnit compilationUnit) {
        super(sourceFile, sourcePath, basePath, compilationUnit);
        resourceMethodHandlers = new ArrayList<>();
        resourceMethodHandlers.add(new FindByIdResourceMethodHandler(sourceFile, sourcePath, basePath, compilationUnit));
        resourceMethodHandlers.add(new CustomFinderResourceMethodHandler(sourceFile, sourcePath, basePath, compilationUnit));
        resourceMethodHandlers.add(new CustomRemoveResourceMethodHandler(sourceFile, sourcePath, basePath, compilationUnit));
        resourceMethodHandlers.add(new FinderResourceMethodHandler(sourceFile, sourcePath, basePath, compilationUnit));
        resourceMethodHandlers.add(new DeleteByIdResourceMethodHandler(sourceFile, sourcePath, basePath, compilationUnit));
    }

    @Override
    public void addResourceAnnotations() {
        List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarations = compilationUnit.
                findAll(ClassOrInterfaceDeclaration.class);

        for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : classOrInterfaceDeclarations) {
            // clean all first
            removeResourceAnnotations();
            // check if annotated with RepositoryRestResource
            Optional<AnnotationExpr> restResourceOptional = checkResourceAnnotationPresent(classOrInterfaceDeclaration);
            if (restResourceOptional.isPresent() && checkResourceExported(restResourceOptional.get(), false)) {
                String resourcePath = getResourcePath(restResourceOptional.get());
                if (resourcePath == null) {
                    resourcePath = getDomainPath(compilationUnit, classOrInterfaceDeclaration);
                }
                // add JAX-RS path annotation
                addPathAnnotation(classOrInterfaceDeclaration, resourcePath);
                addAllOperations();
                try (FileWriter fileWriter = new FileWriter(new File(sourceFile))) {
                    fileWriter.write(compilationUnit.toString());
                } catch (IOException e) {
                    throw new RuntimeException(String.format("Could not write soruce file: %s", sourcePath), e);
                }
            }
        }
    }

    @Override
    public void removeResourceAnnotations() {
        List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarations = compilationUnit.
                findAll(ClassOrInterfaceDeclaration.class);
        for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : classOrInterfaceDeclarations) {
            // remove all JAX-RS annotations
            removeAnnotation(compilationUnit, classOrInterfaceDeclaration,
                    JAXRS_PATH_CLASS);
            removeAllOperations();
            removeImport(compilationUnit, JAXRS_PATH_CLASS);
        }
    }

    private void removeAllOperations() {
        resourceMethodHandlers.forEach(ResourceMethodHandler::removeResourceAnnotations);
    }

    private void addAllOperations() {
        resourceMethodHandlers.forEach(ResourceMethodHandler::addResourceAnnotations);
    }

}
