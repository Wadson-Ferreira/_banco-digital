package br.com.cdb.bancodigital.controller;

import br.com.cdb.bancodigital.dto.request.*;
import br.com.cdb.bancodigital.dto.response.CartaoCreditoResponseDTO;
import br.com.cdb.bancodigital.dto.response.CartaoDebitoResponseDTO;
import br.com.cdb.bancodigital.service.CartaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cartoes")
public class CartaoController {
    private final CartaoService cartaoService;

    public CartaoController(CartaoService cartaoService) {
        this.cartaoService = cartaoService;
    }

    @PostMapping("/credito")
    public ResponseEntity<CartaoCreditoResponseDTO> criarCartaoCredito(@Valid @RequestBody CartaoCreditoCreateRequestDTO dto) {
        return ResponseEntity.status(201).body(cartaoService.criarCartaoDeCredito(dto));
    }

    @PostMapping("/debito")
    public ResponseEntity<CartaoDebitoResponseDTO> criarCartaoDebito(@Valid @RequestBody CartaoDebitoCreateRequestDTO dto) {
        return ResponseEntity.status(201).body(cartaoService.criarCartaoDebito(dto));
    }

    @GetMapping("/conta/{numeroConta}")
    public ResponseEntity<List<Object>> findByConta(@PathVariable Long numeroConta) {
        return ResponseEntity.ok(cartaoService.findByConta(numeroConta));
    }

    @PostMapping("/{id}/pagamentos")
    public ResponseEntity<Void> fazerPagamento(@PathVariable Long id, @Valid @RequestBody PagamentoRequestDTO dto) {
        cartaoService.fazerPagamento(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/limite-credito")
    public ResponseEntity<CartaoCreditoResponseDTO> ajustarLimiteCredito(@PathVariable Long id,
                                                                         @Valid @RequestBody LimiteRequestDTO dto) {
        return ResponseEntity.ok(cartaoService.ajustarLimiteCredito(id, dto));
    }
    @PatchMapping("/{id}/limite-diario-debito")
    public ResponseEntity<CartaoDebitoResponseDTO> ajustarLimiteDiarioDebito(@PathVariable Long id,
                                                                             @Valid @RequestBody LimiteRequestDTO dto) {
        return ResponseEntity.ok(cartaoService.ajustarLimiteDiarioDebito(id, dto));
    }
    @PatchMapping("/{id}/cartao-debito-estado")
    public ResponseEntity<CartaoDebitoResponseDTO> alterarEstadoCartaoDebito (@PathVariable Long id,
                                                                              @Valid @RequestBody AlterarEstadoCartaoRequestDTO dto){
        return ResponseEntity.ok(cartaoService.alterarEstadoCD(id,dto));
    }
    @PatchMapping("/{id}/cartao-credito-estado")
    public ResponseEntity<CartaoCreditoResponseDTO> alterarEstadoCartaoCredito (@PathVariable Long id, @Valid @RequestBody AlterarEstadoCartaoRequestDTO dto){
        return ResponseEntity.ok(cartaoService.alterarEstadoCC(id,dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cartaoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
