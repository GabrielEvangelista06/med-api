package med.voll.api.domain.medico;

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
class MedicoServiceTest
{

	@MockBean
	private MedicoRepository medicoRepository;

	@Autowired
	private MedicoService medicoService;

	private DadosEndereco criarDadosEndereco()
	{
		return new DadosEndereco("Rua da mais paz", "Bairro da paz", "00000000", "A Cidade da Paz", "CP", "Apto", "085");
	}

	private DadosEndereco criarDadosEndereco(String logradouro, String bairro, String cep, String cidade, String uf, String complemento,
			String numero)
	{
		return new DadosEndereco(logradouro, bairro, cep, cidade, uf, complemento, numero);
	}

	private DadosCadastroMedico criarDadosCadastroMedico()
	{
		return new DadosCadastroMedico("Josh", "josh@example.com", "1234567890", "123456", Especialidade.ORTOPEDIA, criarDadosEndereco());
	}

	private DadosCadastroMedico criarDadosCadastroMedico(String nome, String email, String telefone, String crm,
			Especialidade especialidade, DadosEndereco endereco)
	{
		return new DadosCadastroMedico(nome, email, telefone, crm, especialidade, endereco);
	}

	private Medico criarMedico()
	{
		return new Medico(this.criarDadosCadastroMedico());
	}

	private DadosDetalhamentoMedico criarDadosDetalhamentoMedico()
	{
		return new DadosDetalhamentoMedico(this.criarMedico());
	}

	private void assertDadosDetalhamentoMedicoIguais(DadosDetalhamentoMedico esperado, DadosDetalhamentoMedico retornado)
	{
		assertEquals(esperado.id(), retornado.id());
		assertEquals(esperado.nome(), retornado.nome());
		assertEquals(esperado.email(), retornado.email());
		assertEquals(esperado.crm(), retornado.crm());
		assertEquals(esperado.telefone(), retornado.telefone());
		assertEquals(esperado.especialidade(), retornado.especialidade());

		assertEnderecoIgual(esperado.endereco(), retornado.endereco());
	}

	private void assertMedicoIgualDadosDetalhamento(DadosDetalhamentoMedico esperado, Medico retornado)
	{
		assertEquals(esperado.id(), retornado.getId());
		assertEquals(esperado.nome(), retornado.getNome());
		assertEquals(esperado.email(), retornado.getEmail());
		assertEquals(esperado.crm(), retornado.getCrm());
		assertEquals(esperado.telefone(), retornado.getTelefone());
		assertEquals(esperado.especialidade(), retornado.getEspecialidade());

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
	@DisplayName("Deve cadastrar um médico com sucesso")
	void cadastrarCenario1()
	{
		// Arrange
		DadosCadastroMedico dadosCadastroMedico = criarDadosCadastroMedico();
		DadosDetalhamentoMedico dadosDetalhamentoMedicoEsperado = this.criarDadosDetalhamentoMedico();

		// Act
		DadosDetalhamentoMedico dadosDetalhamentoMedicoRetornado = this.medicoService.cadastrar(dadosCadastroMedico);

		// Assert
		assertNotNull(dadosDetalhamentoMedicoRetornado);
		this.assertDadosDetalhamentoMedicoIguais(dadosDetalhamentoMedicoEsperado, dadosDetalhamentoMedicoRetornado);
	}

	@Test
	@DisplayName("Deve retornar uma página de dados listagem médico")
	void listarCenario1()
	{
		// Arrange
		DadosCadastroMedico dadosCadastroMedico1 = this.criarDadosCadastroMedico();

		DadosCadastroMedico dadosCadastroMedico2 = this.criarDadosCadastroMedico("Fred", "fred@example.com", "4564654964", "654321",
				Especialidade.CARDIOLOGIA, criarDadosEndereco());

		List<Medico> medicos = Arrays.asList(new Medico(dadosCadastroMedico1), new Medico(dadosCadastroMedico2));

		when(this.medicoRepository.findAllByAtivoTrue(Mockito.any(Pageable.class))).thenReturn(
				new PageImpl<>(medicos, PageRequest.of(0, 10), 2));

		// Act
		Page<DadosListagemMedico> dadosListagemMedicos = this.medicoService.listar(PageRequest.of(0, 10));

		// Assert
		assertEquals(2, dadosListagemMedicos.getTotalElements());
		assertEquals(2, dadosListagemMedicos.getContent().size());

		assertEquals("Josh", dadosListagemMedicos.getContent().get(0).nome());
		assertEquals("josh@example.com", dadosListagemMedicos.getContent().get(0).email());
		assertEquals("123456", dadosListagemMedicos.getContent().get(0).crm());
		assertEquals(Especialidade.ORTOPEDIA, dadosListagemMedicos.getContent().get(0).especialidade());

		assertEquals("Fred", dadosListagemMedicos.getContent().get(1).nome());
		assertEquals("fred@example.com", dadosListagemMedicos.getContent().get(1).email());
		assertEquals("654321", dadosListagemMedicos.getContent().get(1).crm());
		assertEquals(Especialidade.CARDIOLOGIA, dadosListagemMedicos.getContent().get(1).especialidade());
	}

	@Test
	@DisplayName("Deve atualizar dados de um médico")
	void atualizarCenario1()
	{
		// Arrange
		Long id = 1L;
		String nomeAtualizado = "Nome Atualizado";
		String telefoneAtualizado = "75614569815";
		DadosEndereco enderecoAtualizado = this.criarDadosEndereco("Rua Nova", "Bairro Novo", "12345678950", "Cidade Nova", "CN", "Casa",
				"546");

		DadosAtualizacaoMedico dados = new DadosAtualizacaoMedico(id, nomeAtualizado, telefoneAtualizado, enderecoAtualizado);

		Medico medico = this.criarMedico();
		medico.setId(id);

		when(this.medicoRepository.getReferenceById(medico.getId())).thenReturn(medico);

		// Act
		DadosDetalhamentoMedico dadosDetalhamentoMedico = this.medicoService.atualizar(dados);

		// Assert
		assertEquals(id, dadosDetalhamentoMedico.id());
		assertEquals(nomeAtualizado, dadosDetalhamentoMedico.nome());
		assertEquals(telefoneAtualizado, dadosDetalhamentoMedico.telefone());
		this.assertEnderecoIgual(enderecoAtualizado, dadosDetalhamentoMedico.endereco());
	}

	@Test
	@DisplayName("Deve excluir um médico")
	void excluirCenario1()
	{
		// Arrange
		Long id = 1L;
		Medico medico = this.criarMedico();
		medico.setId(id);

		when(this.medicoRepository.getReferenceById(medico.getId())).thenReturn(medico);

		// Act
		this.medicoService.excluir(id);

		// Assert
		assertEquals(false, medico.getAtivo());
	}

	@Test
	@DisplayName("Deve detalhar os dados corretos do médico")
	void detalharCenario1()
	{
		// Arrange
		Long id = 1L;
		Medico medico = this.criarMedico();

		when(this.medicoRepository.getReferenceById(id)).thenReturn(medico);

		// Act
		DadosDetalhamentoMedico dadosDetalhado = this.medicoService.detalhar(id);

		// Assert
		this.assertMedicoIgualDadosDetalhamento(dadosDetalhado, medico);
	}
}