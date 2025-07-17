package br.com.cdb.bancodigital.dto.request;

import br.com.cdb.bancodigital.entity.enums.StatusCartao;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlterarEstadoCartaoRequestDTO {
    @NotNull(message = "O novo estado é obrigatório.")
    private StatusCartao novoEstado;
}
