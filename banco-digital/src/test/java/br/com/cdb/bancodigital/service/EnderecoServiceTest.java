package br.com.cdb.bancodigital.service;

import br.com.cdb.bancodigital.dto.externo.BrasilApiCepDTO;
import br.com.cdb.bancodigital.dto.mapper.EnderecoMapper;
import br.com.cdb.bancodigital.dto.request.EnderecoRequestDTO;
import br.com.cdb.bancodigital.service.exception.BusinessException;
import br.com.cdb.bancodigital.service.exception.ObjectNotFoundException;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EnderecoServiceTest {

    private AutoCloseable mocks;

    @InjectMocks
    private EnderecoService service;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private EnderecoMapper mapper;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void findByCep_DeveRetornarDTO_QuandoCepValidoEAPIRetornaObjeto() {
        // Arrange
        String rawCep = "12.345-678";
        String cepTratado = "12345678";
        BrasilApiCepDTO apiDto = new BrasilApiCepDTO();
        apiDto.setCep(cepTratado);
        apiDto.setEstado("SP");
        apiDto.setCidade("São Paulo");
        apiDto.setBairro("Centro");
        apiDto.setRua("Rua A");

        EnderecoRequestDTO expectedDto = new EnderecoRequestDTO();
        // ... preencher expectedDto se desejar checar valores

        when(restTemplate.getForObject(
                "https://brasilapi.com.br/api/cep/v1/" + cepTratado,
                BrasilApiCepDTO.class))
                .thenReturn(apiDto);
        when(mapper.toEnderecoRequestDTO(apiDto))
                .thenReturn(expectedDto);

        // Act
        EnderecoRequestDTO actual = service.findByCep(rawCep);

        // Assert
        assertNotNull(actual);
        assertEquals(expectedDto, actual);
        verify(restTemplate).getForObject(
                "https://brasilapi.com.br/api/cep/v1/" + cepTratado,
                BrasilApiCepDTO.class);
        verify(mapper).toEnderecoRequestDTO(apiDto);
    }

    @Test
    void findByCep_DeveLancarBusinessException_QuandoFormatoInvalido() {
        // Cep com menos de 8 dígitos após remover não dígitos
        String invalidCep = "1234-56";

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.findByCep(invalidCep)
        );
        assertTrue(ex.getMessage().contains("Formato de CEP inválido"));
        // Nenhuma chamada ao RestTemplate deve ocorrer
        verifyNoInteractions(restTemplate, mapper);
    }

    @Test
    void findByCep_DeveLancarObjectNotFound_QuandoAPIRetornaNull() {
        String cep = "87654321";
        when(restTemplate.getForObject(
                "https://brasilapi.com.br/api/cep/v1/" + cep,
                BrasilApiCepDTO.class))
                .thenReturn(null);

        ObjectNotFoundException ex = assertThrows(
                ObjectNotFoundException.class,
                () -> service.findByCep(cep)
        );
        assertTrue(ex.getMessage().contains("CEP não encontrado"));
        verify(restTemplate).getForObject(anyString(), eq(BrasilApiCepDTO.class));
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void findByCep_DeveLancarObjectNotFound_Quando404DaAPI() {
        String cep = "11223344";
        // Simula 404 Not Found
        when(restTemplate.getForObject(
                "https://brasilapi.com.br/api/cep/v1/" + cep,
                BrasilApiCepDTO.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        assertThrows(
                ObjectNotFoundException.class,
                () -> service.findByCep(cep),
                "Esperava ObjectNotFoundException em caso de 404"
        );

        verify(restTemplate).getForObject(anyString(), eq(BrasilApiCepDTO.class));
        verifyNoInteractions(mapper);
    }

    @Test
    void findByCep_DeveLancarBusinessException_QuandoOutraException() {
        String cep = "99887766";
        // Simula erro genérico (timeout, parse, etc.)
        when(restTemplate.getForObject(
                "https://brasilapi.com.br/api/cep/v1/" + cep,
                BrasilApiCepDTO.class))
                .thenThrow(new RuntimeException("timeout"));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.findByCep(cep)
        );
        assertTrue(ex.getMessage().contains("Não foi possível consultar"));
        verify(restTemplate).getForObject(anyString(), eq(BrasilApiCepDTO.class));
        verifyNoInteractions(mapper);
    }
}
