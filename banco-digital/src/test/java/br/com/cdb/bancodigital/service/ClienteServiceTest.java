package br.com.cdb.bancodigital.service;

import br.com.cdb.bancodigital.dto.mapper.ClienteMapper;
import br.com.cdb.bancodigital.dto.request.ClienteCreateRequestDTO;
import br.com.cdb.bancodigital.dto.request.ClienteUpdateRequestDTO;
import br.com.cdb.bancodigital.dto.response.ClienteResponseDTO;
import br.com.cdb.bancodigital.entity.Cliente;
import br.com.cdb.bancodigital.repository.ClienteRepository;
import br.com.cdb.bancodigital.service.exception.DataIntegrityException;
import br.com.cdb.bancodigital.service.exception.ObjectNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClienteServiceTest {

    @InjectMocks
    private ClienteService clienteService;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;


    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void create_DeveSalvarCliente_QuandoCpfNaoExisteEIdadeMaiorQue18() {
        ClienteCreateRequestDTO dto = new ClienteCreateRequestDTO();
        dto.setCpf("12345678900");
        dto.setDataNascimento(LocalDate.now().minusYears(25));

        Cliente entity = new Cliente();
        ClienteResponseDTO responseDTO = new ClienteResponseDTO();

        when(clienteRepository.findByCpf(dto.getCpf())).thenReturn(Optional.empty());
        when(clienteMapper.toCliente(dto)).thenReturn(entity);
        when(clienteRepository.save(entity)).thenReturn(entity);
        when(clienteMapper.toClienteResponseDTO(entity)).thenReturn(responseDTO);

        ClienteResponseDTO result = clienteService.create(dto);

        assertNotNull(result);
        verify(clienteRepository).save(entity);
    }

    @Test
    void create_DeveLancarExcecao_QuandoCpfJaExiste() {
        ClienteCreateRequestDTO dto = new ClienteCreateRequestDTO();
        dto.setCpf("12345678900");
        dto.setDataNascimento(LocalDate.now().minusYears(25));

        when(clienteRepository.findByCpf(dto.getCpf())).thenReturn(Optional.of(new Cliente()));

        assertThrows(DataIntegrityException.class, () -> clienteService.create(dto));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void create_DeveLancarExcecao_QuandoIdadeMenorQue18() {
        ClienteCreateRequestDTO dto = new ClienteCreateRequestDTO();
        dto.setCpf("12345678900");
        dto.setDataNascimento(LocalDate.now().minusYears(17));

        when(clienteRepository.findByCpf(dto.getCpf())).thenReturn(Optional.empty());

        assertThrows(DataIntegrityException.class, () -> clienteService.create(dto));
    }

    @Test
    void findByCpf_DeveRetornarCliente_QuandoCpfExistente() {
        String cpf = "12345678900";
        Cliente cliente = new Cliente();
        ClienteResponseDTO responseDTO = new ClienteResponseDTO();

        when(clienteRepository.findByCpf(cpf)).thenReturn(Optional.of(cliente));
        when(clienteMapper.toClienteResponseDTO(cliente)).thenReturn(responseDTO);

        ClienteResponseDTO result = clienteService.findByCpf(cpf);

        assertNotNull(result);
    }

    @Test
    void findByCpf_DeveLancarExcecao_QuandoCpfNaoExistente() {
        String cpf = "00000000000";

        when(clienteRepository.findByCpf(cpf)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> clienteService.findByCpf(cpf));
    }

    @Test
    void update_DeveAtualizarCliente_QuandoIdExistente() {
        Long id = 1L;
        ClienteUpdateRequestDTO dto = new ClienteUpdateRequestDTO();
        Cliente entity = new Cliente();
        ClienteResponseDTO responseDTO = new ClienteResponseDTO();

        when(clienteRepository.findById(id)).thenReturn(Optional.of(entity));
        doNothing().when(clienteMapper).updateClienteFromDto(dto, entity);
        when(clienteRepository.save(entity)).thenReturn(entity);
        when(clienteMapper.toClienteResponseDTO(entity)).thenReturn(responseDTO);

        ClienteResponseDTO result = clienteService.update(id, dto);

        assertNotNull(result);
    }

    @Test
    void update_DeveLancarExcecao_QuandoIdNaoExiste() {
        Long id = 2L;
        ClienteUpdateRequestDTO dto = new ClienteUpdateRequestDTO();

        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> clienteService.update(id, dto));
    }

    @Test
    void delete_DeveRemoverCliente_QuandoIdExistente() {
        Long id = 1L;
        Cliente cliente = new Cliente();

        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));
        doNothing().when(clienteRepository).delete(cliente);
        doNothing().when(clienteRepository).flush();

        assertDoesNotThrow(() -> clienteService.delete(id));
        verify(clienteRepository).delete(cliente);
        verify(clienteRepository).flush();
    }

    @Test
    void delete_DeveLancarExcecao_QuandoViolacaoIntegridade() {
        Long id = 1L;
        Cliente cliente = new Cliente();

        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));
        doThrow(DataIntegrityViolationException.class).when(clienteRepository).flush();

        assertThrows(DataIntegrityException.class, () -> clienteService.delete(id));
    }

    @Test
    void findById_DeveRetornarCliente_QuandoIdExistente() {
        Long id = 1L;
        Cliente cliente = new Cliente();

        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));

        Cliente result = clienteService.findById(id);

        assertEquals(cliente, result);
    }

    @Test
    void findById_DeveLancarExcecao_QuandoIdNaoExiste() {
        Long id = 100L;

        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> clienteService.findById(id));
    }
}
