package com.example.myreviewserver.adapter.outbound.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {

	List<UserJpaEntity> findByIsDeletedAndDeletedAtBefore(Integer isDeleted, Instant deletedAt);
}
