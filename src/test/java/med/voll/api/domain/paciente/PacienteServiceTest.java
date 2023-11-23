package med.voll.api.domain.paciente;

import med.voll.api.domain.endereco.DadosEndereco;
import med.voll.api.domain.endereco.Endereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Arrays;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@SpringBootTest
@AutoConfigureJsonTesters
class PacienteServiceTest
{

	@MockBean
	private PacienteRepository pacienteRepository;

	@Autowired
	private PacienteService pacienteService;

	private DadosEndereco criarDadosEndereco()
	{
		return new DadosEndereco("Rua da mais paz", "Bairro da paz", "00000000", "A Cidade da Paz", "CP", "Apto", "085");
	}

	private DadosCadastroPaciente criarDadosCadastroPaciente()
	{
		return new DadosCadastroPaciente("Josh", "josh@example.com", "123456789", "123.456.789-01", criarDadosEndereco());
	}

	private DadosCadastroPaciente criarDadosCadastroPaciente(String nome, String email, String telefone, String cpf)
	{
		return new DadosCadastroPaciente(nome, email, telefone, cpf, this.criarDadosEndereco());
	}

	private DadosEndereco criarDadosEndereco(String logradouro, String bairro, String cep, String cidade, String uf, String complemento,
			String numero)
	{
		return new DadosEndereco(logradouro, bairro, cep, cidade, uf, complemento, numero);
	}

	private Paciente criarPaciente()
	{
		return new Paciente(criarDadosCadastroPaciente());
	}

	private DadosDetalhamentoPaciente criarDadosDetalhamentoPaciente()
	{
		return new DadosDetalhamentoPaciente(criarPaciente());
	}

	private void assertDadosDetalhamentoMedicoIguais(DadosDetalhamentoPaciente esperado, DadosDetalhamentoPaciente retornado)
	{
		assertEquals(esperado.id(), retornado.id());
		assertEquals(esperado.nome(), retornado.nome());
		assertEquals(esperado.email(), retornado.email());
		assertEquals(esperado.cpf(), retornado.cpf());
		assertEquals(esperado.telefone(), retornado.telefone());

		assertEnderecoIgual(esperado.endereco(), retornado.endereco());
	}

	private void assertDadosDetalhamentoPacienteIguais(DadosDetalhamentoPaciente esperado, Paciente retornado)
	{
		assertEquals(esperado.id(), retornado.getId());
		assertEquals(esperado.nome(), retornado.getNome());
		assertEquals(esperado.email(), retornado.getEmail());
		assertEquals(esperado.cpf(), retornado.getCpf());
		assertEquals(esperado.telefone(), retornado.getTelefone());

		assertEnderecoIgual(esperado.endereco(), retornado.getEndereco());
	}

	private void assertEnderecoIgual(Endereco esperado, Endereco retornado)
	{
		assertEquals(esperado.getLogradouro(), retornado.getLogradouro());
		assertEquals(esperado.getBairro(), retornado.getBairro());
		assertEquals(esperado.getCep(), retornado.getCep());
		assertEquals(esperado.getNumero(), retornado.getNumero());
		assertEquals(esperado.getComplemento(), retornado.getComplemento());
		assertEquals(esperado.getCidade(), retornado.getCidade());
		assertEquals(esperado.getUf(), retornado.getUf());
	}

	private void assertEnderecoIgual(DadosEndereco esperado, Endereco retornado)
	{
		assertEquals(esperado.logradouro(), retornado.getLogradouro());
		assertEquals(esperado.bairro(), retornado.getBairro());
		assertEquals(esperado.cep(), retornado.getCep());
		assertEquals(esperado.numero(), retornado.getNumero());
		assertEquals(esperado.complemento(), retornado.getComplemento());
		assertEquals(esperado.cidade(), retornado.getCidade());
		assertEquals(esperado.uf(), retornado.getUf());
	}

	@Test
	@DisplayName("Deve cadastrar um paciente com sucesso")
	void cadastrarCenario1()
	{
		// Arrange
		DadosCadastroPaciente dadosCadastroPaciente = criarDadosCadastroPaciente();
		DadosDetalhamentoPaciente dadosDetalhamentoPacienteEsperado = criarDadosDetalhamentoPaciente();

		// Act
		DadosDetalhamentoPaciente dadosDetalhamentoPacienteRetornado = pacienteService.cadastrar(dadosCadastroPaciente);

		// Assert
		assertNotNull(dadosDetalhamentoPacienteEsperado);
		this.assertDadosDetalhamentoMedicoIguais(dadosDetalhamentoPacienteEsperado, dadosDetalhamentoPacienteRetornado);
	}

	@Test
	@DisplayName("Deve retornar uma página de dados listagem paciente")
	void listarCenario1()
	{
		// Arrange
		DadosCadastroPaciente dadosCadastroPaciente1 = criarDadosCadastroPaciente();
		DadosCadastroPaciente dadosCadastroPaciente2 = criarDadosCadastroPaciente("Bob", "bob@example.com", "987654321", "987.654.321-01");

		List<Paciente> pacientes = Arrays.asList(new Paciente(dadosCadastroPaciente1), new Paciente(dadosCadastroPaciente2));

		when(pacienteRepository.findAllByAtivoTrue(Mockito.any(Pageable.class))).thenReturn(
				new PageImpl<>(pacientes, PageRequest.of(0, 10), 2));

		// Act
		Page<DadosListagemPaciente> dadosListagemPacientes = pacienteService.listar(PageRequest.of(0, 10));

		// Assert
		assertEquals(2, dadosListagemPacientes.getTotalElements());
		assertEquals(2, dadosListagemPacientes.getContent().size());

		assertEquals("Josh", dadosListagemPacientes.getContent().get(0).nome());
		assertEquals("josh@example.com", dadosListagemPacientes.getContent().get(0).email());
		assertEquals("123.456.789-01", dadosListagemPacientes.getContent().get(0).cpf());

		assertEquals("Bob", dadosListagemPacientes.getContent().get(1).nome());
		assertEquals("bob@example.com", dadosListagemPacientes.getContent().get(1).email());
		assertEquals("987.654.321-01", dadosListagemPacientes.getContent().get(1).cpf());
	}

	@Test
	@DisplayName("Deve atualizar dados de um paciente")
	void atualizarCenario1()
	{
		// Arrange
		Long id = 1L;
		String nomeAtualizado = "Nome Atualizado";
		String telefoneAtualizado = "75614569815";
		DadosEndereco enderecoAtualizado = criarDadosEndereco("Rua Nova", "Bairro Novo", "12345678950", "Cidade Nova", "CN", "Casa", "546");

		DadosAtualizacaoPaciente dados = new DadosAtualizacaoPaciente(id, nomeAtualizado, telefoneAtualizado, enderecoAtualizado);

		Paciente paciente = criarPaciente();
		paciente.setId(id);

		when(pacienteRepository.getReferenceById(paciente.getId())).thenReturn(paciente);

		// Act
		DadosDetalhamentoPaciente dadosDetalhamentoPaciente = pacienteService.atualizar(dados);

		// Assert
		assertEquals(id, dadosDetalhamentoPaciente.id());
		assertEquals(nomeAtualizado, dadosDetalhamentoPaciente.nome());
		assertEquals(telefoneAtualizado, dadosDetalhamentoPaciente.telefone());
		this.assertEnderecoIgual(enderecoAtualizado, dadosDetalhamentoPaciente.endereco());
	}

	@Test
	@DisplayName("Deve excluir um paciente")
	void excluirCenario1()
	{
		// Arrange
		Long id = 1L;
		Paciente paciente = criarPaciente();
		paciente.setId(id);

		when(pacienteRepository.getReferenceById(paciente.getId())).thenReturn(paciente);

		// Act
		pacienteService.excluir(id);

		// Assert
		assertEquals(false, paciente.getAtivo());
	}

	@Test
	@DisplayName("Deve detalhar os dados corretos do paciente")
	void detalharCenario1()
	{
		// Arrange
		Long id = 1L;
		Paciente paciente = criarPaciente();

		when(pacienteRepository.getReferenceById(id)).thenReturn(paciente);

		// Act
		DadosDetalhamentoPaciente dadosDetalhado = pacienteService.detalhar(id);

		// Assert
		assertEquals(paciente.getId(), dadosDetalhado.id());
		assertEquals(paciente.getNome(), dadosDetalhado.nome());
		assertEquals(paciente.getEmail(), dadosDetalhado.email());
		assertEquals(paciente.getTelefone(), dadosDetalhado.telefone());
		assertEquals(paciente.getEndereco(), dadosDetalhado.endereco());
	}
}
