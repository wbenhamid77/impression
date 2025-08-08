package com.example.Impression.repositories;

import com.example.Impression.entities.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdminRepository extends JpaRepository<Admin, UUID> {

    List<Admin> findByDepartement(String departement);

    List<Admin> findByMatricule(String matricule);
}