package com.smarttrader.controller;

import com.smarttrader.system.KillSwitchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SystemController {

    private final KillSwitchService killSwitchService;

    public SystemController(KillSwitchService killSwitchService) {
        this.killSwitchService = killSwitchService;
    }

    @GetMapping("/system/status")
    @PreAuthorize("hasAnyRole('VIEWER', 'TRADER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        return ResponseEntity.ok(Map.of(
            "killSwitchActive", killSwitchService.isActive(),
            "status", "UP"
        ));
    }

    @PostMapping("/system/kill-switch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> activateKillSwitch(@RequestParam(defaultValue = "Manual override") String reason) {
        killSwitchService.activate(reason, "ADMIN");
        return ResponseEntity.ok(Map.of("message", "Kill switch activated"));
    }

    @PostMapping("/paper/start")
    @PreAuthorize("hasAnyRole('TRADER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> startPaperTrading() {
        // Implementation for changing mode to PAPER
        return ResponseEntity.ok(Map.of("message", "Paper trading started"));
    }

    @PostMapping("/paper/stop")
    @PreAuthorize("hasAnyRole('TRADER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> stopPaperTrading() {
        return ResponseEntity.ok(Map.of("message", "Paper trading stopped"));
    }

    @PostMapping("/live/start")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> startLiveTrading() {
        // Implementation for changing mode to LIVE
        return ResponseEntity.ok(Map.of("message", "Live trading started"));
    }

    @PostMapping("/live/stop")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> stopLiveTrading() {
        return ResponseEntity.ok(Map.of("message", "Live trading stopped"));
    }
}
