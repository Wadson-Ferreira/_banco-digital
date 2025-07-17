package br.com.cdb.bancodigital.dto.response;

import br.com.cdb.bancodigital.entity.enums.TipoSeguro;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ApoliceResponseDTO {
    private Long id;
    private String numero;
    private LocalDate dataContratacao;
    private BigDecimal valorApolice;
    private String condicoes;
    private Long cartaoId;
    private TipoSeguro tipoSeguro;
}