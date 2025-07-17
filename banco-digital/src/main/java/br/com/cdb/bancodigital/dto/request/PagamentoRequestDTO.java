package br.com.cdb.bancodigital.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PagamentoRequestDTO {
    @NotNull(message = "O valor do pagamento é obrigatório.")
    @Positive(message = "O valor do pagamento deve ser positivo.")
    private BigDecimal valor;
}
