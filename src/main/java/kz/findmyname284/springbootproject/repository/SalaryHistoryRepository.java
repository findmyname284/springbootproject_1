package kz.findmyname284.springbootproject.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kz.findmyname284.springbootproject.model.Employee;
import kz.findmyname284.springbootproject.model.SalaryHistory;

public interface SalaryHistoryRepository extends JpaRepository<SalaryHistory, Long> {
    @Query("SELECT sh FROM SalaryHistory sh WHERE " +
            "sh.employee = :employee AND " +
            "(sh.startDate <= :endDate AND (sh.endDate IS NULL OR sh.endDate >= :startDate))")
    List<SalaryHistory> findByEmployeeAndPeriod(
            @Param("employee") Employee employee,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    SalaryHistory findFirstByEmployeeOrderByStartDateDesc(Employee employee);
}
