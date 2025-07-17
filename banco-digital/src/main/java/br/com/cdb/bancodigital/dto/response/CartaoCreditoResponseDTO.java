package br.com.cdb.bancodigital.dto.response;

import br.com.cdb.bancodigital.entity.enums.StatusCartao;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartaoCreditoResponseDTO {
    private Long id;
    private String numero;
    private StatusCartao status;
    private Long contaNumero;
    private BigDecimal limiteCredito;
    private BigDecimal valorGastoMes;
}