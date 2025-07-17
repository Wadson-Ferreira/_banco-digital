package br.com.cdb.bancodigital.dto.request;

import br.com.cdb.bancodigital.entity.enums.TipoSeguro;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeguroRequestDTO {
    @NotNull(message = "Tipo de seguro é obrigatório.")
    private TipoSeguro tipoSeguro;

    @NotNull(message = "Custo mensal é obrigatório.")
    @Positive(message = "Custo mensal deve ser um valor positivo.")
    private BigDecimal custoMensal;
}
