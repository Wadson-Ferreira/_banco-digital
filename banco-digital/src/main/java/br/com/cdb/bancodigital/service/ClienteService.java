package br.com.cdb.bancodigital.service;

import br.com.cdb.bancodigital.dto.mapper.ClienteMapper;
import br.com.cdb.bancodigital.dto.request.ClienteCreateRequestDTO;
import br.com.cdb.bancodigital.dto.request.ClienteUpdateRequestDTO;
import br.com.cdb.bancodigital.dto.response.ClienteResponseDTO;
import br.com.cdb.bancodigital.entity.Cliente;
import br.com.cdb.bancodigital.service.exception.DataIntegrityException;
import br.com.cdb.bancodigital.service.exception.ObjectNotFoundException;
import br.com.cdb.bancodigital.repository.ClienteRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    public ClienteService(ClienteRepository clienteRepository, ClienteMapper clienteMapper){
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
    }

    @Transactional
    public ClienteResponseDTO create(ClienteCreateRequestDTO dto){
        //valida se o cpf já existe no banco de dados
        clienteRepository.findByCpf(dto.getCpf()).ifPresent(obj ->{
            throw new DataIntegrityException("CPF já cadastrado no sistema.");
        });
        //valida a regra de negócio para idade
        validateAge(dto.getDataNascimento());

        Cliente entity = clienteMapper.toCliente(dto);
        entity = clienteRepository.save(entity);
        return clienteMapper.toClienteResponseDTO(entity);
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO findByCpf (String cpf){
        Cliente cliente = clienteRepository.findByCpf(cpf)
                .orElseThrow(()-> new ObjectNotFoundException("Cliente não encontrado! cpf: " + cpf));
        return clienteMapper.toClienteResponseDTO(cliente);
    }

    @Transactional
    public ClienteResponseDTO update(Long id, ClienteUpdateRequestDTO dto){
        Cliente entity = findById(id);
        clienteMapper.updateClienteFromDto(dto, entity);
        entity = clienteRepository.save(entity);
        return clienteMapper.toClienteResponseDTO(entity);
    }

    @Transactional(readOnly = true)
    public Cliente findById(Long id){
        return clienteRepository.findById(id)
                .orElseThrow(()-> new ObjectNotFoundException("Cliente não encontrado! Id: " + id));
    }

    @Transactional
    public void delete(Long id) {
        Cliente cliente = findById(id);
        try {
            clienteRepository.delete(cliente);
            clienteRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityException(
                    "Não é possível excluir cliente com dados relacionados.");
        }
    }


    private void validateAge(LocalDate birthDate) {
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 18){
            throw new DataIntegrityException("Cliente deve ser maior de 18 anos.");
        }
    }
}
