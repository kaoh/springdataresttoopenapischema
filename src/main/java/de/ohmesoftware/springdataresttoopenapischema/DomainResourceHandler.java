package de.ohmesoftware.springdataresttoopenapischema;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

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
     * @param disablePut Disabled the PUT command.
     * @param searchableAnnotation The searchable annotation.
     * @param sortableAnnotation The sortable annotation.
     */
    protected DomainResourceHandler(String sourceFile, String sourcePath, String basePath,
                                    CompilationUnit compilationUnit, boolean disablePut,
                                    String searchableAnnotation, String sortableAnnotation) {
        super(sourceFile, sourcePath, basePath, compilationUnit);
        resourceMethodHandlers = new ArrayList<>();
        resourceMethodHandlers.add(new FindByIdResourceMethodHandler(sourceFile, sourcePath, basePath, compilationUnit));
        resourceMethodHandlers.add(new CustomFinderResourceMethodHandler(sourceFile, sourcePath, basePath, compilationUnit,
                searchableAnnotation,
                sortableAnnotation));
        resourceMethodHandlers.add(new CustomRemoveResourceMethodHandler(sourceFile, sourcePath, basePath, compilationUnit));
        resourceMethodHandlers.add(new FinderResourceMethodHandler(sourceFile, sourcePath, basePath, compilationUnit,
                searchableAnnotation,
                sortableAnnotation));
        resourceMethodHandlers.add(new DeleteByIdResourceMethodHandler(sourceFile, sourcePath, basePath, compilationUnit));
        resourceMethodHandlers.add(new CreateResourceMethodHandler(sourceFile, sourcePath, basePath, compilationUnit));
        resourceMethodHandlers.add(new PutResourceMethodHandler(sourceFile, sourcePath, basePath, compilationUnit, disablePut));
        resourceMethodHandlers.add(new PatchResourceMethodHandler(sourceFile, sourcePath, basePath, compilationUnit));
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
                    resourcePath = getDomainPath(classOrInterfaceDeclaration);
                    if (resourcePath == null) {
                        // this means this class has no domain type, this is a special class, skip it
                        return;
                    }
                }
                // add JAX-RS path annotation
                addPathAnnotation(classOrInterfaceDeclaration, resourcePath);
                addTagAnnotation(classOrInterfaceDeclaration,
                        String.format("%s Methods", getSimpleNameFromClass(getDomainClass(classOrInterfaceDeclaration).asString())));
                addAllOperations();
                try (FileWriter fileWriter = new FileWriter(new File(sourceFile))) {
                    fileWriter.write(compilationUnit.toString());
                } catch (IOException e) {
                    throw new RuntimeException(String.format("Could not write source file: %s", sourcePath), e);
                }
            }
        }
    }

    @Override
    public void removeResourceAnnotations() {
        List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarations = compilationUnit.
                findAll(ClassOrInterfaceDeclaration.class);
        for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : classOrInterfaceDeclarations) {
            if(!isCustomInterface(classOrInterfaceDeclaration)) {
                // remove all JAX-RS annotations
                removeAnnotation(classOrInterfaceDeclaration, JAXRS_PATH_CLASS);
                removeAnnotation(classOrInterfaceDeclaration, TAG_CLASS);
                removeAllOperations();
                removeImport(compilationUnit, JAXRS_PATH_CLASS);
            }
        }
    }

    private void removeAllOperations() {
        resourceMethodHandlers.forEach(ResourceMethodHandler::removeResourceAnnotations);
    }

    private void addAllOperations() {
        resourceMethodHandlers.forEach(ResourceMethodHandler::addResourceAnnotations);
    }

}
