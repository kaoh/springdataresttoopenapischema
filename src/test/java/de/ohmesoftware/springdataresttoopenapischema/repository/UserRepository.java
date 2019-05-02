/*
 * Copyright(c) 2019 Simless, Inc.
 *
 * All rights reserved.
 */

package de.ohmesoftware.springdataresttoopenapischema.repository;

import de.ohmesoftware.springdataresttoopenapischema.model.subdir.User;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

/**
 * Repository for ProfilesTypes.
 *
 * @author <a href="mailto:karsten@simless.com">Karsten Ohme
 * (karsten@simless.com)</a>
 */
@RepositoryRestResource(path = "people")
public interface UserRepository extends PagingAndSortingRepository<User, String>, QuerydslPredicateExecutor<User> {

    Optional<User> findByFirstName(String firstName);

    void removeByUsername(String username);

    <S extends User> S create(S entity);

    <S extends User> S update(S entity);

}
