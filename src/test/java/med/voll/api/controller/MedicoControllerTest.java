package med.voll.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import med.voll.api.domain.endereco.DadosEndereco;
import med.voll.api.domain.endereco.Endereco;
import med.voll.api.domain.medico.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MedicoControllerTest
{
	@Autowired
	private MockMvc mvc;

	@Autowired
	private JacksonTester<DadosCadastroMedico> dadosCadastroMedicoJson;

	@Autowired
	private JacksonTester<DadosAtualizacaoMedico> dadosAtualizacaoMedicoJson;

	@Autowired
	private JacksonTester<DadosDetalhamentoMedico> dadosDetalhamentoMedicoJson;

	@Autowired
	private MedicoRepository medicoRepository;

	@Autowired
	private MedicoService medicoService;

	@Autowired
	private ObjectMapper objectMapper;

	private Medico medico;

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
		return new DadosCadastroMedico("Josh", "josh@example.com", "123456789", "123456", Especialidade.ORTOPEDIA, criarDadosEndereco());
	}

	private DadosCadastroMedico criarDadosCadastroMedico(String nome, String email, String telefone, String crm,
			Especialidade especialidade, DadosEndereco endereco)
	{
		return new DadosCadastroMedico(nome, email, telefone, crm, especialidade, endereco);
	}

	private DadosAtualizacaoMedico criarDadosAtualizacaoMedico()
	{
		return new DadosAtualizacaoMedico(this.medico.getId(), "Josh Silva", "11983047521",
				this.criarDadosEndereco("Rua sem paz", "Bairro sem paz", "99999-999", "Cidade sem paz", "CP", "Casa 2", "3"));
	}

	@BeforeEach
	public void setup()
	{
		this.medicoRepository.deleteAll();

		DadosCadastroMedico dadosCadastroMedico = this.criarDadosCadastroMedico();

		DadosDetalhamentoMedico dadosDetalhamentoMedico = this.medicoService.cadastrar(dadosCadastroMedico);

		this.medico = this.medicoRepository.findById(dadosDetalhamentoMedico.id()).orElseThrow();
	}

	@Test
	@DisplayName("Deve devolver codigo http 400 quando informacoes estao invalidas")
	@WithMockUser
	void cadastrarCenario1() throws Exception
	{
		// Act
		MockHttpServletResponse response = mvc.perform(post("/medicos")).andReturn().getResponse();

		// Assert
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	@DisplayName("Deve devolver codigo HTTP 200 quando informações estão válidas e criar o médico")
	@WithMockUser
	void cadastrarCenario2() throws Exception
	{
		// Arrange
		DadosCadastroMedico dadosCadastro = new DadosCadastroMedico("Medico", "medico@voll.med", "61999999999", "654321",
				Especialidade.CARDIOLOGIA, this.criarDadosEndereco());

		// Act
		MockHttpServletResponse response = mvc.perform(
						post("/medicos").contentType(MediaType.APPLICATION_JSON).content(dadosCadastroMedicoJson.write(dadosCadastro).getJson()))
				.andReturn().getResponse();

		DadosDetalhamentoMedico dadosDetalhamento = new DadosDetalhamentoMedico(this.medico.getId() + 1, dadosCadastro.nome(),
				dadosCadastro.email(), dadosCadastro.crm(), dadosCadastro.telefone(), dadosCadastro.especialidade(),
				new Endereco(dadosCadastro.endereco()));
		String jsonEsperado = dadosDetalhamentoMedicoJson.write(dadosDetalhamento).getJson();

		// Assert
		assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
		assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);
	}

	@Test
	@DisplayName("Deve devolver código HTTP 400 quando informações estão ausentes ao cadastrar médico")
	@WithMockUser
	void cadastrarCenario3() throws Exception
	{
		// Act
		MockHttpServletResponse response = mvc.perform(post("/medicos").contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse();

		// Assert
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	@DisplayName("Deve devolver código HTTP 200 e listar médicos")
	@WithMockUser
	void listarCenario1() throws Exception
	{
		// Arrange
		DadosCadastroMedico dadosCadastroMedico2 = this.criarDadosCadastroMedico("curupira", "curupira@gmail.com", "11786429418", "654321",
				Especialidade.CARDIOLOGIA, this.criarDadosEndereco());

		DadosDetalhamentoMedico dadosDetalhamentoMedico2 = this.medicoService.cadastrar(dadosCadastroMedico2);

		Medico medico2 = this.medicoRepository.findById(dadosDetalhamentoMedico2.id()).orElseThrow();

		DadosListagemMedico dadosListagemMedico1 = new DadosListagemMedico(this.medico);
		DadosListagemMedico dadosListagemMedico2 = new DadosListagemMedico(medico2);

		Page<DadosListagemMedico> listagemEsperada = new PageImpl<>(List.of(dadosListagemMedico2, dadosListagemMedico1));

		// Act
		MockHttpServletResponse response = mvc.perform(get("/medicos").contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse();

		// Assert
		String jsonEsperado = this.objectMapper.writeValueAsString(listagemEsperada);
		String jsonRetornado = response.getContentAsString();

		JsonNode nodeEsperado = this.objectMapper.readTree(jsonEsperado);
		JsonNode nodeRetornado = this.objectMapper.readTree(jsonRetornado);

		System.out.println("Esperado: " + nodeEsperado.get("content"));
		System.out.println("Retornado: " + nodeRetornado.get("content"));

		assertThat(nodeRetornado.get("content")).isEqualTo(nodeEsperado.get("content"));

		assertThat(nodeRetornado.get("totalPages").intValue()).isEqualTo(nodeEsperado.get("totalPages").intValue());
		assertThat(nodeRetornado.get("totalElements").intValue()).isEqualTo(nodeEsperado.get("totalElements").intValue());
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
	}

	@Test
	@DisplayName("Deve devolver código HTTP 200 e atualizar as informações médicos")
	@WithMockUser
	void atualizarCenario1() throws Exception
	{
		// Arrange
		DadosAtualizacaoMedico dadosAtualizadoMedico = this.criarDadosAtualizacaoMedico();

		// Act
		MockHttpServletResponse response = this.mvc.perform(put("/medicos").contentType(MediaType.APPLICATION_JSON)
				.content(this.dadosAtualizacaoMedicoJson.write(dadosAtualizadoMedico).getJson())).andReturn().getResponse();

		this.medico = this.medicoRepository.findById(this.medico.getId()).orElseThrow();

		DadosDetalhamentoMedico dadosDetalhamentoMedico = new DadosDetalhamentoMedico(this.medico);

		String jsonEsperado = dadosDetalhamentoMedicoJson.write(dadosDetalhamentoMedico).getJson();

		// Assert
		assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
	}

	@Test
	@DisplayName("Deve devolver código HTTP 400 quando requisição não inclui corpo")
	@WithMockUser
	void atualizarCenario2() throws Exception
	{
		// Arrange
		MockHttpServletResponse response = mvc.perform(put("/medicos").contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse();

		// Assert
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	@DisplayName("Deve devolver código 204 e excluir o médico")
	@WithMockUser
	void excluirCenario1() throws Exception
	{
		// Act
		MockHttpServletResponse response = this.mvc.perform(delete("/medicos/{id}", this.medico.getId())).andReturn().getResponse();

		this.medico = this.medicoRepository.findById(this.medico.getId()).orElseThrow();

		// Assert
		assertThat(this.medico.getAtivo()).isFalse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
	}

	@Test
	@DisplayName("Deve retornar código HTTP 204 ao tentar excluir um médico já excluído")
	@WithMockUser
	void excluirCenario2() throws Exception
	{
		// Arrange
		this.medico.excluir();

		// Act
		MockHttpServletResponse response = mvc.perform(delete("/medicos/{id}", this.medico.getId()).contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		// Assert
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
		assertThat(this.medico.getAtivo()).isFalse();
	}

	@Test
	@DisplayName("Deve retornar  código HTTP 404 ao tentar excluir médico inexistente")
	@WithMockUser
	void excluirCenario3() throws Exception
	{
		// Arrange
		Long medicoId = 1L;

		// Act
		MockHttpServletResponse response = mvc.perform(delete("/medicos/{id}", medicoId).contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		// Assert
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
	}

	@Test
	@DisplayName("Deve devolver código HTTP 200 e detalhar as informações de um médico")
	@WithMockUser
	void detalharCenario1() throws Exception
	{
		// Act
		MockHttpServletResponse response = this.mvc.perform(get("/medicos/{id}", this.medico.getId())).andReturn().getResponse();

		this.medico = this.medicoRepository.findById(this.medico.getId()).orElseThrow();

		DadosDetalhamentoMedico dadosDetalhamentoMedico = new DadosDetalhamentoMedico(this.medico);
		String jsonEsperado = dadosDetalhamentoMedicoJson.write(dadosDetalhamentoMedico).getJson();

		// Assert
		assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
	}

	@Test
	@DisplayName("Deve retornar código HTTP 404 ao tentar detalhar um médico com ID inexistente")
	@WithMockUser
	void detalharCenario2() throws Exception
	{
		// Arrange
		Long medicoId = 1L;

		// Act
		MockHttpServletResponse response = mvc.perform(get("/medicos/{id}", medicoId).contentType(MediaType.APPLICATION_JSON)).andReturn()
				.getResponse();

		// Assert
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
	}
}