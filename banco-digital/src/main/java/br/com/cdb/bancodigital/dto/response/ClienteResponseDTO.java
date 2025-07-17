package br.com.cdb.bancodigital.dto.response;

import br.com.cdb.bancodigital.entity.enums.CategoriaClientes;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClienteResponseDTO {
    private Long id;
    private String cpf;
    private String nome;
    private LocalDate dataNascimento;
    private CategoriaClientes categoriaClientes;
    private EnderecoResponseDTO endereco;
    private List<ContaResponseDTO> contas;
}
