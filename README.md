# Introduction

Library and command line for scanning a source path and sub directories and adding or
setting the Swagger OpenAPI `io.swagger.v3.oas.annotations.Operation` annotation for Spring Data REST.

# Features

The library in its current state was created for setting the Operation documentation of Spring Data REST.

Limitations:

* PUT for creation is not documented. POST should be sufficient in all cases.
* Only collection and item method resources are considered.
* Domain models must be classes. No interfaces are supported. Only fields will be inspected.
* No inner classes and enums are supported.
* The resolution of class names only works if no wild card imports are used.
* Custom finders using pagination are not supported.
* If own intermediate repository interfaces (e.g. for adding some default annotations) are used with generics, the domain 
class must be the first type parameter.  
* The method `T update(T entity)` is a reserved name in custom repositories.
* To override methods in repositories a concrete type must be used, 
e.g to overwrite `<S extends T> S save(S entity)` `User save(User entity)`must be used.

# Usage

Spring Data REST does not use JAX-RS annotations but scans the offered repository interface methods using a naming convention for the method semantics.
This semantics are also used by this library. 

__NOTE:__ The library will remove its own added methods and annotations as preparation. This should work flawlessly but keep 
a backup if this fails in some situations. 
 
## Source Code Preparation

* Apply the Spring REST `@RepositoryRestResource` and `@Resource` annotations to the repository interfaces to set the `exported` 
and `path` property. This will be honored by this library.
* To get non default Javadoc comments for the methods override the implementations in the concrete implementation 
  * The `Operation` summary is using the first part of the comment.
  * The `Operation` description is using any text after a paragraph `<p>`
  * `@param` tags are used for the `@Parameter` description.
* Mark overridden methods from the `CrudRepository`, `PagingAndSortingRepository` and `QuerydslPredicateExecutor` with 
  `@RestResource`, otherwise they will be removed in the next run 
* `findById`, `findAll`, `deleteById`, the create and the update method are using default descriptions if not explicitly defined.
* Because PUT for updates and POST for creations are using both the `save` method an additional marker method 
is added for the PUT call. An `T update(T entity)` method is added to a custom repository interface, which is created if it does not exists, yet.
* Custom repositories can use an arbitrary method namings. Also the returned content and HTTP method cannot be determined. All `Operation`
and JAX-RS annotations must be manually added.

## Library Execution

* Pass the source path with excludes and includes to the library or the Main class
   * __NOTE:__ The `exclude` and `include` options are using a glob expression. Take note that to use a wild card over path 
separators two asterisks have to be used. 

## Java

```
Enricher enricher = new Enricher(buildPath(User.class.getPackage().getName()),
            Collections.singleton("**UserRepository.java"), Collections.singleton("**.bak"));
enricher.enrich();
```

## Maven

```
    <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>exec-maven-plugin</artifactId>
        <version>1.6.0</version>
        <executions>
          <execution>
            <goals>
              <goal>java</goal>
            </goals>
            <phase>generate-sources</phase>
          </execution>
        </executions>
        <configuration>
          <mainClass>de.ohmesoftware.springdataresttoopenapischema.Enricher</mainClass>
          <includePluginDependencies>true</includePluginDependencies>
          <arguments>
            <argument>-sourcePath</argument>
            <argument>src/test/java/my/domain/project/model</argument>
            <argument>-excludes</argument>
            <argument>**.bak</argument>
            <argument>-includes</argument>
            <argument>**UserRepository.java</argument>
          </arguments>
        </configuration>
        <dependencies>
            <dependency>
                <groupId>de.ohmesoftware</groupId>
                <artifactId>springdataresttoopenapischema</artifactId>
                <version>0.0.1-SNAPSHOT</version>
            </dependency>
        </dependencies>
    </plugin>
```

# Deployment + Release

See https://central.sonatype.org/pages/apache-maven.html


# For Snapshots

    mvn clean deploy

## For Releases

```
mvn release:clean release:prepare
mvn release:perform
```

Release the deployment using Nexus See https://central.sonatype.org/pages/releasing-the-deployment.html
Or alternatively do it with Maven:

```
cd target/checkout
mvn nexus-staging:release
```
