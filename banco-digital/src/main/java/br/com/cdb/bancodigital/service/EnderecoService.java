package br.com.cdb.bancodigital.service;

import br.com.cdb.bancodigital.dto.externo.BrasilApiCepDTO;
import br.com.cdb.bancodigital.dto.mapper.EnderecoMapper;
import br.com.cdb.bancodigital.dto.request.EnderecoRequestDTO;
import br.com.cdb.bancodigital.service.exception.BusinessException;
import br.com.cdb.bancodigital.service.exception.ObjectNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class EnderecoService {

    private final RestTemplate restTemplate;
    private final EnderecoMapper enderecoMapper;

    public EnderecoService(RestTemplate restTemplate, EnderecoMapper enderecoMapper) {
        this.restTemplate = restTemplate;
        this.enderecoMapper = enderecoMapper;
    }

    public EnderecoRequestDTO findByCep(String cep) {
        String cepTratado = cep.replaceAll("\\D", "");
        if (cepTratado.length() != 8) {
            throw new BusinessException("Formato de CEP inválido. Deve conter 8 dígitos.");
        }

        String url = "https://brasilapi.com.br/api/cep/v1/" + cepTratado;

        BrasilApiCepDTO brasilApiDto;
        try {
            brasilApiDto = restTemplate.getForObject(url, BrasilApiCepDTO.class);
        } catch (HttpClientErrorException e) {
            // se for 404, lançamos ObjectNotFound
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ObjectNotFoundException(
                        "CEP não encontrado na base de dados externa: " + cep);
            }
            // para qualquer outro 4xx/5xx, transformamos em BusinessException
            throw new BusinessException(
                    "Não foi possível consultar o serviço de CEP. Tente novamente mais tarde.");
        } catch (Exception e) {
            throw new BusinessException(
                    "Não foi possível consultar o serviço de CEP. Tente novamente mais tarde.");
        }

        // null‑check fora do try
        if (brasilApiDto == null) {
            throw new ObjectNotFoundException("CEP não encontrado: " + cep);
        }

        return enderecoMapper.toEnderecoRequestDTO(brasilApiDto);
    }

}
