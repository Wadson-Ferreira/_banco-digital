package br.com.cdb.bancodigital.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LimiteRequestDTO {
    @NotNull(message = "O novo limite é obrigatório.")
    @Positive(message = "O limite deve ser um valor positivo.")
    private BigDecimal novoLimite;
}