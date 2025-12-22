package au.com.telstra.simcardactivator.repository;

import au.com.telstra.simcardactivator.entity.SimActivation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimActivationRepository extends JpaRepository<SimActivation, Long> {
}
