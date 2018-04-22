/*
 * Copyright (c) 2018. Sergio López Jiménez and Universidad de Valladolid
 * All rights reserved
 */

package com.Sergio.EasyRMT.Repository;

import com.Sergio.EasyRMT.Model.ObjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ObjectRepository extends JpaRepository<ObjectEntity, Integer> {
    @Modifying
    @Query(value = "delete from easyrmt.object where object.idobject = ?1", nativeQuery = true)
    void deleteObject(int integer);
}
