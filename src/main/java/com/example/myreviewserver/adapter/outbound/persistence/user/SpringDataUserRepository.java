package com.example.myreviewserver.adapter.outbound.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {
}
