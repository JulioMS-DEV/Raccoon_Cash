package com.raccooncash.api.reporte;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReporteControlador {

    private final ReporteServicio reportService;

    public ReporteControlador(ReporteServicio reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/monthly")
    public ResponseEntity<ReporteMensualRespuesta> getMonthlyReport(@RequestHeader("X-Usuario-Id") Long usuarioId,
                                                                    @RequestParam int year,
                                                                    @RequestParam int month) {
        return ResponseEntity.ok(reportService.getMonthlyReport(usuarioId, year, month));
    }

    @GetMapping("/by-category")
    public ResponseEntity<List<ReporteCategoriaRespuesta>> getByCategory(
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getByCategory(usuarioId, from, to));
    }

    @GetMapping("/by-account")
    public ResponseEntity<List<ReporteCuentaRespuesta>> getByAccount(
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getByAccount(usuarioId, from, to));
    }

    @GetMapping("/cash-flow")
    public ResponseEntity<ReporteFlujoCajaRespuesta> getCashFlow(
            @RequestHeader("X-Usuario-Id") Long usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getCashFlow(usuarioId, from, to));
    }
}
