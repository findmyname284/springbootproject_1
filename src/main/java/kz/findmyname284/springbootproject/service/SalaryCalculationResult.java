package kz.findmyname284.springbootproject.service;

import java.math.BigDecimal;
import java.util.Map;

public class SalaryCalculationResult {
    private BigDecimal totalSalary;
    private Map<BigDecimal, Long> daysPerSalary;

    public SalaryCalculationResult() {
    }

    public SalaryCalculationResult(BigDecimal totalSalary, Map<BigDecimal, Long> daysPerSalary) {
        this.totalSalary = totalSalary;
        this.daysPerSalary = daysPerSalary;
    }

    public BigDecimal getTotalSalary() {
        return totalSalary;
    }

    public void setTotalSalary(BigDecimal totalSalary) {
        this.totalSalary = totalSalary;
    }

    public Map<BigDecimal, Long> getDaysPerSalary() {
        return daysPerSalary;
    }

    public void setDaysPerSalary(Map<BigDecimal, Long> daysPerSalary) {
        this.daysPerSalary = daysPerSalary;
    }

}