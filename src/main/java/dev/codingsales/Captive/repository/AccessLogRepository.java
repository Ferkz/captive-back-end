package dev.codingsales.Captive.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.codingsales.Captive.entity.AccessLog;
@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog,Long>  {
}
