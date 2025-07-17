package br.com.cdb.bancodigital.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartaoDebitoCreateRequestDTO {
    @NotNull(message = "O número da conta é obrigatório.")
    private Long numeroConta;

    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 4, max = 6, message = "A senha deve ter entre 4 e 6 dígitos.")
    private String senha;

    @NotNull(message = "O limite diário é obrigatório.")
    @Positive(message = "O limite diário deve ser um valor positivo.")
    private BigDecimal limiteDiario;
}
