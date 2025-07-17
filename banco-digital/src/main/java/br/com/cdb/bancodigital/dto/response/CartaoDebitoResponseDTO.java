package br.com.cdb.bancodigital.dto.response;

import br.com.cdb.bancodigital.entity.enums.StatusCartao;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartaoDebitoResponseDTO {
    private Long id;
    private String numero;
    private StatusCartao status;
    private Long contaNumero;
    private BigDecimal limiteDiario;
    private BigDecimal gastoDiario;
}