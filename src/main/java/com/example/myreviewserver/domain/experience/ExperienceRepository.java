package com.example.myreviewserver.domain.experience;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port for Experience aggregate.
 * Implemented by adapter.outbound.persistence.
 */
public interface ExperienceRepository {

	Experience save(Experience experience);

	Optional<Experience> findById(Long id);

	Optional<Experience> findByIdAndUserId(Long id, Long userId);

	List<Experience> findUpcomingByUserIdOrderByReservationAscIdAsc(Long userId);

	List<Experience> findCompletedByUserIdOrderByReservationAscIdAsc(Long userId);

	boolean deleteByIdAndUserId(Long id, Long userId);

	void deleteAllByUserId(Long userId);
}
