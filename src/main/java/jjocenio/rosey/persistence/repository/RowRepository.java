package jjocenio.rosey.persistence.repository;

import jjocenio.rosey.persistence.Row;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface RowRepository extends CrudRepository<Row, Long> {

    @Query(value = "select status, count(*) from Row group by status")
    List<Object[]> countGroupByStatus();

    @Modifying
    @Query(value = "update Row set status = 'PENDING'")
    @Transactional
    void updateProcessingToPending();

    @Query(value = "select max(id) from Row")
    Long getMaxRowId();

    List<Row> findAllByStatus(Row.Status status);
}
