package kz.findmyname284.springbootproject.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import kz.findmyname284.springbootproject.model.Employee;
import kz.findmyname284.springbootproject.model.SalaryHistory;
import kz.findmyname284.springbootproject.model.User;
import kz.findmyname284.springbootproject.repository.EmployeeRepository;
import kz.findmyname284.springbootproject.repository.SalaryHistoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final SalaryHistoryRepository salaryHistoryRepository;

    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }

    public void deleteById(Long id) {
        employeeRepository.deleteById(id);
    }

    public Optional<Employee> findByUser(User user) {
        return employeeRepository.findByUser(user);
    }

    public Optional<Employee> findByIin(String iin) {
        return employeeRepository.findByIin(iin);
    }

    public SalaryCalculationResult calculateMonthlySalary(Long employeeId, YearMonth targetMonth) {
        try {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

            LocalDate monthStart = targetMonth.atDay(1);
            LocalDate monthEnd = targetMonth.atEndOfMonth();

            List<SalaryHistory> salaryHistory = salaryHistoryRepository.findByEmployeeAndPeriod(
                    employee,
                    monthStart,
                    monthEnd);

            LocalDate calculationStart = employee.getHireDate().isAfter(monthStart)
                    ? employee.getHireDate()
                    : monthStart;

            LocalDate calculationEnd = monthEnd;

            BigDecimal totalSalary = BigDecimal.ZERO;
            Map<BigDecimal, Long> daysPerSalary = new HashMap<>();

            for (SalaryHistory entry : salaryHistory) {
                LocalDate originalStart = entry.getStartDate();
                LocalDate originalEnd = entry.getEndDate() != null
                        ? entry.getEndDate()
                        : calculationEnd;

                LocalDate entryPeriodStart = originalStart.isAfter(calculationStart)
                        ? originalStart
                        : calculationStart;
                LocalDate entryPeriodEnd = originalEnd.isBefore(calculationEnd)
                        ? originalEnd
                        : calculationEnd;

                if (entryPeriodStart.isAfter(entryPeriodEnd))
                    continue;

                long originalWorkingDays = calculateWorkingDays(originalStart, originalEnd);
                if (originalWorkingDays == 0)
                    continue;

                long entryWorkingDaysInMonth = calculateWorkingDays(entryPeriodStart, entryPeriodEnd);

                BigDecimal dailyRate = entry.getSalary()
                        .divide(BigDecimal.valueOf(originalWorkingDays), 10, RoundingMode.HALF_UP);

                BigDecimal periodSalary = dailyRate.multiply(BigDecimal.valueOf(entryWorkingDaysInMonth));
                totalSalary = totalSalary.add(periodSalary);

                daysPerSalary.merge(entry.getSalary(), entryWorkingDaysInMonth, Long::sum);
            }

            SalaryCalculationResult result = new SalaryCalculationResult();
            result.setTotalSalary(totalSalary.setScale(2, RoundingMode.HALF_UP));
            result.setDaysPerSalary(daysPerSalary);

            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private long calculateWorkingDays(LocalDate start, LocalDate end) {
        long days = 0;
        LocalDate date = start;

        while (!date.isAfter(end)) {
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                days++;
            }
            date = date.plusDays(1);
        }

        return days;
    }
}
