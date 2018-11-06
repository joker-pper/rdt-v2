package com.devloper.joker.rdt_jpa_test.repository;

import com.devloper.joker.rdt_jpa_test.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends BaseRepository<User, Long>{
}
