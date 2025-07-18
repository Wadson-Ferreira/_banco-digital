package br.com.cdb.bancodigital.dto.request;

import br.com.cdb.bancodigital.entity.enums.CategoriaCliente;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

@Data
public class ClienteCreateRequestDTO {
    @CPF(message = "CPF inválido")
    @NotBlank(message = "CPF é obrigatório.")
    private String cpf;

    @NotBlank(message = "Nome é obrigatório.")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres.")
    private String nome;

    @NotNull(message = "Data de nascimento é obrigatória.")
    @Past(message = "Data de nascimento deve ser no passado.")
    private LocalDate dataNascimento;

    @NotNull(message = "Categoria do cliente é obrigatória.")
    private CategoriaCliente categoriaCliente;

    @NotNull(message = "Endereço é obrigatório.")
    private EnderecoRequestDTO endereco;
}
