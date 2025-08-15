package com.codvortex.repository;

import com.codvortex.domain.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileRepository extends JpaRepository<File, Long> {

    @Query("SELECT f FROM File f WHERE f.name = :name AND f.size = :size")
    File findByNameAndSize(@Param("name") String name, @Param("size") long size);

}
