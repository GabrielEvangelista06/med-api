package med.voll.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import med.voll.api.domain.endereco.DadosEndereco;
import med.voll.api.domain.endereco.Endereco;
import med.voll.api.domain.paciente.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PacienteControllerTest
{
	@Autowired
	private MockMvc mvc;

	@Autowired
	private JacksonTester<DadosCadastroPaciente> dadosCadastroPacienteJson;

	@Autowired
	private JacksonTester<DadosAtualizacaoPaciente> dadosAtualizacaoPacienteJson;

	@Autowired
	private JacksonTester<DadosDetalhamentoPaciente> dadosDetalhamentoPacienteJson;

	@Autowired
	private PacienteRepository pacienteRepository;

	@Autowired
	private PacienteService pacienteService;

	@Autowired
	private ObjectMapper objectMapper;

	private Paciente paciente;

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

	private DadosAtualizacaoPaciente criarDadosAtualizacaoPaciente()
	{
		return new DadosAtualizacaoPaciente(this.paciente.getId(), "Josh Silva", "1234567890", null);
	}

	@BeforeEach
	public void setup()
	{
		this.pacienteRepository.deleteAll();

		DadosCadastroPaciente dadosCadastroPaciente = this.criarDadosCadastroPaciente();

		DadosDetalhamentoPaciente dadosDetalhamentoPaciente = this.pacienteService.cadastrar(dadosCadastroPaciente);

		this.paciente = this.pacienteRepository.findById(dadosDetalhamentoPaciente.id()).orElseThrow();
	}

	@Test
	@DisplayName("Deve devolver código HTTP 400 quando as informações de cadastro forem inválidas")
	@WithMockUser
	void cadastrarCenario1() throws Exception
	{
		// Act
		MockHttpServletResponse response = mvc.perform(post("/pacientes")).andReturn().getResponse();

		// Assert
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	@DisplayName("Deve devolver código HTTP 200 quando as informações de cadastro forem válidas e criar o paciente")
	@WithMockUser
	void cadastrarCenario2() throws Exception
	{
		// Arrange
		DadosCadastroPaciente dadosCadastro = new DadosCadastroPaciente("Paciente", "paciente@example.com", "61999999999", "987.654.321-01", this.criarDadosEndereco());

		// Act
		MockHttpServletResponse response = mvc.perform(post("/pacientes").contentType(MediaType.APPLICATION_JSON)
				.content(dadosCadastroPacienteJson.write(dadosCadastro).getJson())).andReturn().getResponse();

		DadosDetalhamentoPaciente dadosDetalhamento = new DadosDetalhamentoPaciente(this.paciente.getId() + 1, dadosCadastro.nome(),
				dadosCadastro.email(), dadosCadastro.cpf(), dadosCadastro.telefone(), new Endereco(dadosCadastro.endereco()));
		String jsonEsperado = dadosDetalhamentoPacienteJson.write(dadosDetalhamento).getJson();

		// Assert
		assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
		assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);
	}

	@Test
	@DisplayName("Deve devolver código HTTP 200 com a lista de pacientes")
	@WithMockUser
	void listar() throws Exception
	{
		// Arrange
		DadosCadastroPaciente dadosCadastroPaciente2 = this.criarDadosCadastroPaciente("curupira", "curupira@gmail.com", "11786429418",
				"987.654.321-01");

		DadosDetalhamentoPaciente dadosDetalhamentoPaciente2 = this.pacienteService.cadastrar(dadosCadastroPaciente2);

		Paciente paciente2 = this.pacienteRepository.findById(dadosDetalhamentoPaciente2.id()).orElseThrow();

		DadosListagemPaciente dadosListagemPaciente1 = new DadosListagemPaciente(this.paciente);
		DadosListagemPaciente dadosListagemPaciente2 = new DadosListagemPaciente(paciente2);

		Page<DadosListagemPaciente> listagemEsperada = new PageImpl<>(List.of(dadosListagemPaciente2, dadosListagemPaciente1));

		// Act
		MockHttpServletResponse response = mvc.perform(get("/pacientes").contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse();

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
	@DisplayName("Deve devolver código HTTP 200 e atualizar as informações do paciente")
	@WithMockUser
	void atualizarCenario1() throws Exception
	{
		// Arrange
		DadosAtualizacaoPaciente dadosAtualizacaoPaciente = this.criarDadosAtualizacaoPaciente();

		// Act
		MockHttpServletResponse response = this.mvc.perform(put("/pacientes").contentType(MediaType.APPLICATION_JSON)
				.content(this.dadosAtualizacaoPacienteJson.write(dadosAtualizacaoPaciente).getJson())).andReturn().getResponse();

		this.paciente = this.pacienteRepository.findById(this.paciente.getId()).orElseThrow();

		DadosDetalhamentoPaciente dadosDetalhamentoPaciente = new DadosDetalhamentoPaciente(this.paciente);

		String jsonEsperado = dadosDetalhamentoPacienteJson.write(dadosDetalhamentoPaciente).getJson();

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
		MockHttpServletResponse response = mvc.perform(put("/pacientes").contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse();

		// Assert
		assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	@DisplayName("Deve devolver código 204 e excluir o paciente")
	@WithMockUser
	void excluirCenario1() throws Exception
	{
		// Act
		MockHttpServletResponse response = this.mvc.perform(delete("/pacientes/{id}", this.paciente.getId())).andReturn().getResponse();

		this.paciente = this.pacienteRepository.findById(this.paciente.getId()).orElseThrow();

		// Assert
		assertThat(this.paciente.getAtivo()).isFalse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
	}

	@Test
	@DisplayName("Deve retornar código HTTP 204 ao tentar excluir um paciente já excluído")
	@WithMockUser
	void excluirCenario2() throws Exception
	{
		// Arrange
		this.paciente.excluir();

		// Act
		MockHttpServletResponse response = mvc.perform(
				delete("/pacientes/{id}", this.paciente.getId()).contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse();

		// Assert
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
		assertThat(this.paciente.getAtivo()).isFalse();
	}

	@Test
	@DisplayName("Deve retornar código HTTP 404 ao tentar excluir paciente inexistente")
	@WithMockUser
	void excluirCenario3() throws Exception
	{
		// Arrange
		Long pacienteId = 1L;

		// Act
		MockHttpServletResponse response = mvc.perform(delete("/pacientes/{id}", pacienteId).contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		// Assert
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
	}

	@Test
	@DisplayName("Deve devolver código HTTP 200 e detalhar as informações de um paciente")
	@WithMockUser
	void detalharCenario1() throws Exception
	{
		// Act
		MockHttpServletResponse response = this.mvc.perform(get("/pacientes/{id}", this.paciente.getId())).andReturn().getResponse();

		this.paciente = this.pacienteRepository.findById(this.paciente.getId()).orElseThrow();

		DadosDetalhamentoPaciente dadosDetalhamentoPaciente = new DadosDetalhamentoPaciente(this.paciente);
		String jsonEsperado = dadosDetalhamentoPacienteJson.write(dadosDetalhamentoPaciente).getJson();

		// Assert
		assertThat(response.getContentAsString()).isEqualTo(jsonEsperado);
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
	}

	@Test
	@DisplayName("Deve retornar código HTTP 404 ao tentar detalhar um paciente com ID inexistente")
	@WithMockUser
	void detalharCenario2() throws Exception
	{
		// Arrange
		Long pacienteId = 1L;

		// Act
		MockHttpServletResponse response = mvc.perform(get("/pacientes/{id}", pacienteId).contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		// Assert
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
	}
}