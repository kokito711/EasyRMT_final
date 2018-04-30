package com.Sergio.EasyRMT.Repository;

import com.Sergio.EasyRMT.Model.Documentation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DocumentationRepository extends JpaRepository<Documentation, Integer> {
    @Query(value = "SELECT * FROM easyrmt.documentation where documentation.idproject = ?1 and documentation.idobject = ?2",
            nativeQuery = true)
    List<Documentation> findByProjectAndObject(int projectId, Integer objectId);

    @Query(value = "SELECT * FROM easyrmt.documentation where documentation.idproject = ?1", nativeQuery = true)
    List<Documentation> findByProject(int projectId);
}
