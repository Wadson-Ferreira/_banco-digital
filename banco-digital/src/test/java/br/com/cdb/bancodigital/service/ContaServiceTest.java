package br.com.cdb.bancodigital.service;

import br.com.cdb.bancodigital.dto.mapper.ContaMapper;
import br.com.cdb.bancodigital.dto.response.ContaResponseDTO;
import br.com.cdb.bancodigital.entity.*;
import br.com.cdb.bancodigital.entity.enums.CategoriaCliente;
import br.com.cdb.bancodigital.entity.enums.TipoConta;
import br.com.cdb.bancodigital.service.exception.BusinessException;
import br.com.cdb.bancodigital.service.exception.DataIntegrityException;
import br.com.cdb.bancodigital.service.exception.ObjectNotFoundException;
import br.com.cdb.bancodigital.repository.ContaRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContaServiceTest {

    private AutoCloseable mocks;

    @InjectMocks
    private ContaService service;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private ClienteService clienteService;

    @Mock
    private ContaMapper contaMapper;

    private Cliente clienteExemplo;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        // Cliente de exemplo com categoria SUPER (taxaManutencao=8.00, taxaRendimento=2.0)
        clienteExemplo = new Cliente();
        clienteExemplo.setId(42L);
        clienteExemplo.setCategoriaCliente(CategoriaCliente.SUPER);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void getAccountDetails_DeveRetornarDTO_QuandoContaExiste() {
        ContaCorrente cc = new ContaCorrente(clienteExemplo);
        cc.setNumero(1L);
        when(contaRepository.findById(1L)).thenReturn(Optional.of(cc));
        ContaResponseDTO dto = new ContaResponseDTO();
        when(contaMapper.toContaResponseDTO(cc)).thenReturn(dto);

        ContaResponseDTO result = service.getAccountDetails(1L);

        assertSame(dto, result);
    }

    @Test
    void getAccountDetails_DeveLancarObjectNotFound_QuandoNaoExiste() {
        when(contaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class,
                () -> service.getAccountDetails(99L));
    }

    @Test
    void abrirConta_Corrente_DeveConfigurarTaxaManutencaoETipo() {
        when(clienteService.findById(42L)).thenReturn(clienteExemplo);
        ContaCorrente cc = new ContaCorrente(clienteExemplo);
        cc.setAgencia(1001L);
        when(contaRepository.save(any(ContaCorrente.class))).thenReturn(cc);
        ContaResponseDTO dto = new ContaResponseDTO();
        when(contaMapper.toContaResponseDTO(cc)).thenReturn(dto);

        ContaResponseDTO result = service.abrirConta(42L, TipoConta.CORRENTE);

        assertSame(dto, result);
        verify(contaRepository).save(Mockito.argThat(c ->
                c instanceof ContaCorrente &&
                        ((ContaCorrente) c).getTaxaManutencao().equals(CategoriaCliente.SUPER.getTaxaManutencao()) &&
                        c.getAgencia().equals(1001L)
        ));
    }

    @Test
    void abrirConta_Poupanca_DeveConfigurarTaxaRendimentoETipo() {
        when(clienteService.findById(42L)).thenReturn(clienteExemplo);
        ContaPoupanca cp = new ContaPoupanca(clienteExemplo);
        cp.setAgencia(1001L);
        when(contaRepository.save(any(ContaPoupanca.class))).thenReturn(cp);
        ContaResponseDTO dto = new ContaResponseDTO();
        when(contaMapper.toContaResponseDTO(cp)).thenReturn(dto);

        ContaResponseDTO result = service.abrirConta(42L, TipoConta.POUPANCA);

        assertSame(dto, result);
        verify(contaRepository).save(Mockito.argThat(c ->
                c instanceof ContaPoupanca &&
                        ((ContaPoupanca) c).getTaxaRendimentoAnual().equals(CategoriaCliente.SUPER.getTaxaRendimento()) &&
                        c.getAgencia().equals(1001L)
        ));
    }

    @Test
    void depositar_DeveSomarSaldo_QuandoValorPositivo() {
        ContaCorrente cc = new ContaCorrente(clienteExemplo);
        cc.setNumero(5L);
        cc.setSaldo(new BigDecimal("10.00"));
        when(contaRepository.findById(5L)).thenReturn(Optional.of(cc));
        when(contaRepository.save(cc)).thenReturn(cc);
        ContaResponseDTO dto = new ContaResponseDTO();
        when(contaMapper.toContaResponseDTO(cc)).thenReturn(dto);

        ContaResponseDTO result = service.depositar(5L, new BigDecimal("15.00"));

        assertSame(dto, result);
        assertEquals(new BigDecimal("25.00"), cc.getSaldo());
    }

    @Test
    void depositar_DeveLancarBusinessException_QuandoValorNaoPositivo() {
        assertThrows(BusinessException.class,
                () -> service.depositar(1L, BigDecimal.ZERO));
    }

    @Test
    void sacar_DeveSubtrairSaldo_QuandoSaldoSuficiente() {
        ContaCorrente cc = new ContaCorrente(clienteExemplo);
        cc.setNumero(6L);
        cc.setSaldo(new BigDecimal("50.00"));
        when(contaRepository.findById(6L)).thenReturn(Optional.of(cc));
        when(contaRepository.save(cc)).thenReturn(cc);
        ContaResponseDTO dto = new ContaResponseDTO();
        when(contaMapper.toContaResponseDTO(cc)).thenReturn(dto);

        ContaResponseDTO result = service.sacar(6L, new BigDecimal("20.00"));

        assertSame(dto, result);
        assertEquals(new BigDecimal("30.00"), cc.getSaldo());
    }

    @Test
    void sacar_DeveLancarBusinessException_QuandoValorNaoPositivo() {
        assertThrows(BusinessException.class,
                () -> service.sacar(1L, BigDecimal.ZERO));
    }

    @Test
    void sacar_DeveLancarBusinessException_QuandoSaldoInsuficiente() {
        ContaCorrente cc = new ContaCorrente(clienteExemplo);
        cc.setNumero(7L);
        cc.setSaldo(new BigDecimal("5.00"));
        when(contaRepository.findById(7L)).thenReturn(Optional.of(cc));

        assertThrows(BusinessException.class,
                () -> service.sacar(7L, new BigDecimal("10.00")));
    }

    @Test
    void transferir_DeveChamarSacarEDepositar_QuandoContasDiferentes() {
        ContaService spy = Mockito.spy(service);
        ContaResponseDTO dummyDto = new ContaResponseDTO();

        doReturn(dummyDto).when(spy).sacar(1L, BigDecimal.TEN);
        doReturn(dummyDto).when(spy).depositar(2L, BigDecimal.TEN);

        spy.transferir(1L, 2L, BigDecimal.TEN);

        verify(spy).sacar(1L, BigDecimal.TEN);
        verify(spy).depositar(2L, BigDecimal.TEN);
    }


    @Test
    void transferir_DeveLancarBusinessException_QuandoMesmaConta() {
        assertThrows(BusinessException.class,
                () -> service.transferir(1L, 1L, BigDecimal.ONE));
    }

    @Test
    void findByClienteId_DeveRetornarListaDTO_QuandoExistiremContas() {
        ContaCorrente cc = new ContaCorrente(clienteExemplo);
        cc.setNumero(8L);
        when(clienteService.findById(42L)).thenReturn(clienteExemplo);
        when(contaRepository.findByClienteId(42L))
                .thenReturn(List.of(cc));
        ContaResponseDTO dto = new ContaResponseDTO();
        when(contaMapper.toContaResponseDTO(cc)).thenReturn(dto);

        List<ContaResponseDTO> list = service.findByClienteId(42L);

        assertEquals(1, list.size());
        assertSame(dto, list.getFirst());
    }

    @Test
    void delete_DeveExcluir_QuandoSemCartoes() {
        ContaCorrente cc = new ContaCorrente(clienteExemplo);
        cc.setNumero(9L);
        cc.setCartoes(Collections.emptyList());
        when(contaRepository.findById(9L)).thenReturn(Optional.of(cc));
        doNothing().when(contaRepository).delete(cc);
        doNothing().when(contaRepository).flush();

        assertDoesNotThrow(() -> service.delete(9L));
        verify(contaRepository).delete(cc);
        verify(contaRepository).flush();
    }

    @Test
    void delete_DeveLancarDataIntegrityException_QuandoTemCartoes() {
        ContaCorrente cc = new ContaCorrente(clienteExemplo);
        cc.setNumero(10L);
        cc.setCartoes(List.of(new CartaoCredito()));
        when(contaRepository.findById(10L)).thenReturn(Optional.of(cc));

        assertThrows(DataIntegrityException.class,
                () -> service.delete(10L));
    }

    @Test
    void delete_DeveLancarDataIntegrityException_QuandoViolacaoBanco() {
        ContaCorrente cc = new ContaCorrente(clienteExemplo);
        cc.setNumero(11L);
        cc.setCartoes(Collections.emptyList());
        when(contaRepository.findById(11L)).thenReturn(Optional.of(cc));
        doThrow(new org.springframework.dao.DataIntegrityViolationException("fk"))
                .when(contaRepository).flush();

        assertThrows(DataIntegrityException.class,
                () -> service.delete(11L));
    }
}
