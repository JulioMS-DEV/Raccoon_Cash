package com.raccooncash.api.panel;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class PanelControlador {

    private final PanelServicio dashboardService;

    public PanelControlador(PanelServicio dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ResumenPanelRespuesta> getSummary(@RequestHeader("X-Usuario-Id") Long usuarioId) {
        return ResponseEntity.ok(dashboardService.getSummary(usuarioId));
    }
}
