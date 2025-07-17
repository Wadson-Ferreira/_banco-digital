package br.com.cdb.bancodigital.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransacaoRequestDTO {
    @NotNull(message = "O valor da transação é obrigatório.")
    @Positive(message = "O valor deve ser positivo.")
    private BigDecimal valor;
}