package com.example.springtest.repository;

import com.example.springtest.model.Website;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebsiteRepository extends JpaRepository<Website, Long> {
    Website findByUrl(String url);
}
