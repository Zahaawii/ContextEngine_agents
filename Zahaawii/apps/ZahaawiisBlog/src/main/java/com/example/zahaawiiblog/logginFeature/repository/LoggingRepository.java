package com.example.zahaawiiblog.logginFeature.repository;

import com.example.zahaawiiblog.logginFeature.entity.Logging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoggingRepository extends JpaRepository<Logging, Long> {
}
