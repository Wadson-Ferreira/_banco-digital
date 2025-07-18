package br.com.cdb.bancodigital.dto.request;

import br.com.cdb.bancodigital.entity.enums.CategoriaCliente;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClienteUpdateRequestDTO {
    @NotBlank(message = "Nome é obrigatório.")
    @Size(min = 3, max = 100)
    private String nome;

    @NotNull(message = "Categoria do cliente é obrigatória.")
    private CategoriaCliente categoriaCliente;

    @Valid
    @NotNull(message = "Endereço é obrigatório.")
    private EnderecoRequestDTO endereco;
}
