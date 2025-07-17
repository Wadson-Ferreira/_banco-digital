package br.com.cdb.bancodigital.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ContratarSeguroRequestDTO {
    @NotNull(message = "O ID do cartão de crédito é obrigatório.")
    private Long cartaoId;

    @NotNull(message = "O ID do tipo de seguro é obrigatório.")
    private Long seguroId;

    @Positive(message = "O número de dias de viagem deve ser positivo.")
    private Integer diasDeViagem;
}