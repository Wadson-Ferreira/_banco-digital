package br.com.cdb.bancodigital.dto.response;

import br.com.cdb.bancodigital.entity.enums.TipoSeguro;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SeguroResponseDTO {
    private Long id;
    private TipoSeguro tipoSeguro;
    private BigDecimal custoMensal;
}