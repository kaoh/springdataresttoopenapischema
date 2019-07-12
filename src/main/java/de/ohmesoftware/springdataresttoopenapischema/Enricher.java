package de.ohmesoftware.springdataresttoopenapischema;

import com.github.javaparser.ast.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
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

    private static final String INCLUDE_EXCLUDE_SEPARATOR = ",";

    private static final String GLOB = "glob:";

    private static final String SLASH = "/";

    private static final String EXCLUDES_OPT = "-excludes";
    private static final String INCLUDES_OPT = "-includes";
    private static final String SOURCE_OPT = "-sourcePath";
    private static final String DISABLED_PUT = "-disablePUT";
    private static final String SORTABLE_ANNOTATION = "-sortableAnnotation";
    private static final String SEARCHABLE_ANNOTATION = "-searchableAnnotation";

    /**
     * The source path to enrich.
     */
    private String sourcePath;

    /**
     * The includes.
     */
    private Set<String> includes;

    /**
     * The excludes.
     */
    private Set<String> excludes;

    /**
     * Disables the PUT command.
     */
    private boolean disabledPut;

    /**
     * The Sortable annotation.
     */
    private String sortableAnnotation;

    /**
     * The Searchable annotation.
     */
    private String searchableAnnotation;

    /**
     * Constructor.
     *
     * @param sourcePath The source path to enrich.
     * @param includes   The includes.
     * @param excludes   The excludes.
     * @param disablePut Disables the PUT command.
     * @param searchableAnnotation The searchable annotation.
     * @param sortableAnnotation The sortable annotation.
     */
    public Enricher(String sourcePath, Set<String> includes, Set<String> excludes, boolean disablePut,
                    String searchableAnnotation, String sortableAnnotation) {
        this.sourcePath = sourcePath;
        if (sourcePath.endsWith(SLASH)) {
            this.sourcePath = sourcePath.substring(0, sourcePath.length() - 1);
        }
        this.includes = includes;
        this.excludes = excludes;
        this.disabledPut = disablePut;
        this.searchableAnnotation = searchableAnnotation;
        this.sortableAnnotation = sortableAnnotation;
    }


    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.err.println("No command line options passed.");
            System.exit(-1);
        }
        String sourcePath = parseOption(args, SOURCE_OPT, true, null);
        String includes = parseOption(args, INCLUDES_OPT, false, null);
        String excludes = parseOption(args, EXCLUDES_OPT, false, null);
        String sortableAnnotation = parseOption(args, SORTABLE_ANNOTATION, false, null);
        String searchableAnnotation = parseOption(args, SEARCHABLE_ANNOTATION, false, null);
        boolean disablePut = parseFlag(args, DISABLED_PUT);
        Enricher enricher = new Enricher(sourcePath,
                includes == null ? null : Arrays.stream(includes.split(INCLUDE_EXCLUDE_SEPARATOR)).map(String::trim).collect(Collectors.toSet()),
                excludes == null ? null : Arrays.stream(excludes.split(INCLUDE_EXCLUDE_SEPARATOR)).map(String::trim).collect(Collectors.toSet()),
                disablePut,
                searchableAnnotation, sortableAnnotation
        );
        enricher.enrich();
    }

    private static boolean parseFlag(String[] args, String option) {
        Optional<String> optionArg = Arrays.stream(args).filter(s -> s.equals(option)).findFirst();
        if (optionArg.isPresent()) {
            return true;
        }
        return false;
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
                    LOGGER.debug(String.format("Checking file '%s' for inclusion / exclusion", path.toAbsolutePath().toString()));
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
                    handleResource(path);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    LOGGER.warn(String.format("Could not check file '%s'", file.toFile().getAbsolutePath()), exc);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LOGGER.error("Could not walk through source files.", e);
            throw new RuntimeException("Could not walk through source files.", e);
        }
    }

    private void handleResource(Path path) throws IOException {
        LOGGER.info(String.format("Handling file: '%s'", path.getFileName().toString()));
        CompilationUnit compilationUnit = ResourceHandler.parseFile(path.toFile());
        String basePath = ResourceHandler.getBaseSourcePath(compilationUnit, sourcePath);
        DomainResourceHandler domainResourceHandler = new DomainResourceHandler(path.toString(), sourcePath, basePath,
                compilationUnit, disabledPut, searchableAnnotation, sortableAnnotation);
        domainResourceHandler.addResourceAnnotations();
    }

}
