package br.com.cdb.bancodigital.dto.response;

import br.com.cdb.bancodigital.entity.enums.TipoConta;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContaResponseDTO {
    private Long numero;
    private Long agencia;
    private BigDecimal saldo;
    private TipoConta tipoConta;
    private Long clienteId;
    private BigDecimal taxaManutencao;
    private BigDecimal taxaRendimentoAnual;
}
