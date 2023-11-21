package med.voll.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import med.voll.api.domain.endereco.DadosEndereco;
import med.voll.api.domain.endereco.Endereco;
import med.voll.api.domain.medico.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
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

	@MockBean
	private MedicoRepository medicoRepository;

	@Autowired
	private ObjectMapper objectMapper;

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

	private Medico criarMedico()
	{
		return new Medico(this.criarDadosCadastroMedico());
	}

	private DadosAtualizacaoMedico criarDadosAtualizacaoMedico()
	{
		return new DadosAtualizacaoMedico(1L, "Fred Mercury", "11983047521",
				this.criarDadosEndereco("Rua sem paz", "Bairro sem paz", "99999-999", "Cidade sem paz", "CSP", "Casa 2", "3"));
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
	@DisplayName("Deve devolver codigo HTTP 200 quando informações estão válidas")
	@WithMockUser
	void cadastrarCenario2() throws Exception
	{
		// Arrange
		DadosCadastroMedico dadosCadastro = new DadosCadastroMedico("Medico", "medico@voll.med", "61999999999", "123456",
				Especialidade.CARDIOLOGIA, this.criarDadosEndereco());

		// Act
		MockHttpServletResponse response = mvc.perform(
						post("/medicos").contentType(MediaType.APPLICATION_JSON).content(dadosCadastroMedicoJson.write(dadosCadastro).getJson()))
				.andReturn().getResponse();

		DadosDetalhamentoMedico dadosDetalhamento = new DadosDetalhamentoMedico(null, dadosCadastro.nome(), dadosCadastro.email(),
				dadosCadastro.crm(), dadosCadastro.telefone(), dadosCadastro.especialidade(), new Endereco(dadosCadastro.endereco()));
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
	void listar() throws Exception
	{
		// Arrange
		Medico medico1 = this.criarMedico();
		medico1.setId(1L);

		DadosCadastroMedico dadosCadastroMedico2 = this.criarDadosCadastroMedico("curupira", "curupira@gmail.com", "11786429418", "654321",
				Especialidade.CARDIOLOGIA, this.criarDadosEndereco());
		Medico medico2 = new Medico(dadosCadastroMedico2);
		medico2.setId(2L);

		List<Medico> medicos = List.of(medico1, medico2);

		Page<Medico> paginaMedicos = new PageImpl<>(medicos);

		DadosListagemMedico dadosListagemMedico1 = new DadosListagemMedico(medico1);
		DadosListagemMedico dadosListagemMedico2 = new DadosListagemMedico(medico2);

		Page<DadosListagemMedico> paginaDadosListagemMedico = new PageImpl<>(List.of(dadosListagemMedico1, dadosListagemMedico2));

		when(this.medicoRepository.findAllByAtivoTrue(any(Pageable.class))).thenReturn(paginaMedicos);

		// Act
		MockHttpServletResponse result = mvc.perform(get("/medicos").contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse();

		// Assert
		assertThat(result.getContentAsString()).isEqualTo(objectMapper.writeValueAsString(paginaDadosListagemMedico));
		assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());
	}

	@Test
	@DisplayName("Deve devolver código HTTP 200 e atualizar as informações médicos")
	@WithMockUser
	void atualizarCenario1() throws Exception
	{
		// Arrange
		DadosAtualizacaoMedico dadosAtualizadoMedico = this.criarDadosAtualizacaoMedico();
		Medico medico = this.criarMedico();
		medico.setId(1L);

		when(this.medicoRepository.getReferenceById(anyLong())).thenReturn(medico);

		// Act
		MockHttpServletResponse response = this.mvc.perform(put("/medicos").contentType(MediaType.APPLICATION_JSON)
				.content(this.dadosAtualizacaoMedicoJson.write(dadosAtualizadoMedico).getJson())).andReturn().getResponse();

		DadosDetalhamentoMedico dadosDetalhamentoMedico = new DadosDetalhamentoMedico(medico);
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
		// Arrange
		Medico medico = this.criarMedico();
		medico.setId(1L);

		when(this.medicoRepository.getReferenceById(anyLong())).thenReturn(medico);

		// Act
		MockHttpServletResponse response = this.mvc.perform(delete("/medicos/{id}", medico.getId())).andReturn().getResponse();

		// Assert
		assertThat(medico.getAtivo()).isFalse();
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
	}

	@Test
	@DisplayName("Deve retornar código HTTP 204 ao tentar excluir um médico inativo")
	@WithMockUser
	void excluirCenario2() throws Exception
	{
		// Arrange
		Medico medico = this.criarMedico();
		medico.setId(1L);
		medico.excluir();

		when(this.medicoRepository.getReferenceById(anyLong())).thenReturn(medico);

		// Act
		MockHttpServletResponse response = mvc.perform(delete("/medicos/{id}", medico.getId()).contentType(MediaType.APPLICATION_JSON))
				.andReturn().getResponse();

		// Assert
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
		assertThat(medico.getAtivo()).isFalse();
	}

	@Test
	@DisplayName("Deve retornar  código HTTP 404 ao tentar excluir médico inexistente")
	@WithMockUser
	void excluirCenario3() throws Exception
	{
		// Arrange
		Long medicoId = 1L;

		when(this.medicoRepository.getReferenceById(anyLong())).thenThrow(EntityNotFoundException.class);

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
		// Arrange
		Medico medico = this.criarMedico();
		medico.setId(1L);

		when(this.medicoRepository.getReferenceById(anyLong())).thenReturn(medico);

		// Act
		MockHttpServletResponse response = this.mvc.perform(get("/medicos/{id}", medico.getId())).andReturn().getResponse();

		DadosDetalhamentoMedico dadosDetalhamentoMedico = new DadosDetalhamentoMedico(medico);
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
		when(this.medicoRepository.getReferenceById(medicoId)).thenThrow(EntityNotFoundException.class);

		// Act
		MockHttpServletResponse response = mvc.perform(get("/medicos/{id}", medicoId).contentType(MediaType.APPLICATION_JSON)).andReturn()
				.getResponse();

		// Assert
		assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
	}
}