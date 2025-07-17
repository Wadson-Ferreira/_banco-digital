package br.com.cdb.bancodigital.dto.request;

import br.com.cdb.bancodigital.entity.enums.TipoConta;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AbrirContaRequestDTO {
    @NotNull(message = "O ID do cliente é obrigatório.")
    private Long clienteId;

    @NotNull(message = "O tipo da conta é obrigatório.")
    private TipoConta tipoConta;
}
