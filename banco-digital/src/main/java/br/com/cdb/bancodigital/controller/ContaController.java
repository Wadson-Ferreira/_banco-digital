package br.com.cdb.bancodigital.controller;

import br.com.cdb.bancodigital.dto.request.AbrirContaRequestDTO;
import br.com.cdb.bancodigital.dto.request.TransacaoRequestDTO;
import br.com.cdb.bancodigital.dto.response.ContaResponseDTO;
import br.com.cdb.bancodigital.service.ContaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/contas")
public class ContaController {
    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @PostMapping
    public ResponseEntity<ContaResponseDTO> abrirConta(@Valid @RequestBody AbrirContaRequestDTO dto){
        ContaResponseDTO responseDTO = contaService.abrirConta(dto.getClienteId(), dto.getTipoConta());
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{numero}")
                .buildAndExpand(responseDTO.getNumero()).toUri();
        return ResponseEntity.created(uri).body(responseDTO);
    }

    @GetMapping("/{numero}")
    public ResponseEntity<ContaResponseDTO> getConta(@PathVariable Long numero){
        return ResponseEntity.ok(contaService.getAccountDetails(numero));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ContaResponseDTO>> getContasPorCliente(@PathVariable Long clienteId){
        return ResponseEntity.ok(contaService.findByClienteId(clienteId));
    }

    @PostMapping("/{numero}/depositos")
    public ResponseEntity<ContaResponseDTO> depositar(@PathVariable Long numero, @Valid @RequestBody TransacaoRequestDTO dto) {
        return ResponseEntity.ok(contaService.depositar(numero, dto.getValor()));
    }

    @PostMapping("/{numero}/saques")
    public ResponseEntity<ContaResponseDTO> sacar(@PathVariable Long numero, @Valid @RequestBody TransacaoRequestDTO dto) {
        return ResponseEntity.ok(contaService.sacar(numero, dto.getValor()));
    }

    @PostMapping("/transferencias")
    public ResponseEntity<Void> transferir(@RequestParam Long origem, @RequestParam Long destino, @RequestParam BigDecimal valor) {
        contaService.transferir(origem, destino, valor);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{numero}")
    public ResponseEntity<Void> delete(@PathVariable Long numero) {
        contaService.delete(numero);
        return ResponseEntity.noContent().build();
    }
}
