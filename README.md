# Introduction

Library and command line for scanning a source path and sub directories and adding or
setting the Swagger OpenAPI `io.swagger.v3.oas.annotations.Operation` annotation for Spring Data REST.

# Features

The library in its current state was created for setting the Operation documentation of Spring Data REST.

Limitations:

* PUT for creation is not documented. POST should be sufficient in all cases.
* Only collection and item method resources are considered.
* The resolution of class names only works if no wild card imports are used.

# Usage

Spring Data REST does not use JAX-RS annotations but scans the offered repository interface methods using a naming convention for the method semantics.
This semantics are also used by this library. 

* Apply the usual `@RepositoryRestResource` and `@Resource` annotations to the repository interfaces optionally with `exported` 
and `path` set.
* Override the implementations in the concrete implementation and add Javadoc comments for the method
  * The `Operation` summary is using the first part of the comment.
  * The `Operation` description is using any text after a paragraph `<p>`
  * `@param` tags are used for the `@Parameter` description.
* Mark overridden method from the `CrudRepository`, `PagingAndSortingRepository` and `QuerydslPredicateExecutor` with 
  `@RestResource`, otherwise they will be removed in the next run 
* `findById`, `findAll` and `deleteById` are using default description if not explicitly defined.
* Because PUT for updates and POST for creations are using both the `save` method it cannot be detected which one
to be documented. Use the same method signature as `save` method and name the methods `create` 
and `update`. E.g.:
```
  <S extends User> S create(S entity);

  <S extends User> S update(S entity);
```

* Pass the source path with excludes and includes to the library or the Main class
   * __NOTE:__ The `exclude` and `include` options are using a glob expression. Take note that to use a wild card over path 
separators two asterisks have to be used. 

## Java

```
Enricher enricher = new Enricher(buildPath(User.class.getPackage().getName()),
            Collections.singleton("**User.java"), Collections.singleton("**.bak"));
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
            <argument>**User.java</argument>
          </arguments>
        </configuration>
        <dependencies>
            <dependency>
                <groupId>de.ohmesoftware</groupId>
                <artifactId>javadoctoopenapischema</artifactId>
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
