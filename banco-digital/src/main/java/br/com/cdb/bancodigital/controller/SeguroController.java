package br.com.cdb.bancodigital.controller;

import br.com.cdb.bancodigital.dto.response.SeguroResponseDTO;
import br.com.cdb.bancodigital.service.SeguroService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seguros")
public class SeguroController {

    private final SeguroService seguroService;

    public SeguroController(SeguroService seguroService) {
        this.seguroService = seguroService;
    }

    @GetMapping
    public ResponseEntity<List<SeguroResponseDTO>> listarSegurosDisponiveis() {
        return ResponseEntity.ok(seguroService.listarSegurosDisponiveis());
    }
}
