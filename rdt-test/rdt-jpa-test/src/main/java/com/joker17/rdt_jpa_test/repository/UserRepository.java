package com.joker17.rdt_jpa_test.repository;

import com.joker17.rdt_jpa_test.domain.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends BaseRepository<User, Long>{
}
