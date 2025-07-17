package br.com.cdb.bancodigital.dto.response;

import lombok.Data;

@Data
public class EnderecoResponseDTO {
    private String rua;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;
}