package br.com.cdb.bancodigital.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RenovarSeguroViagemRequestDTO {
    @NotNull(message = "O número de dias para renovação é obrigatório.")
    @Positive(message = "O número de dias deve ser positivo.")
    private Integer diasAdicionais;
}
