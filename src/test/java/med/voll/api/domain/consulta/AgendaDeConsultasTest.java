package med.voll.api.domain.consulta;

import med.voll.api.domain.ValidacaoException;
import med.voll.api.domain.consulta.validacoes.cancelamento.ValidadorCancelamentoConsulta;
import med.voll.api.domain.endereco.DadosEndereco;
import med.voll.api.domain.medico.DadosCadastroMedico;
import med.voll.api.domain.medico.Especialidade;
import med.voll.api.domain.medico.Medico;
import med.voll.api.domain.medico.MedicoRepository;
import med.voll.api.domain.paciente.DadosCadastroPaciente;
import med.voll.api.domain.paciente.Paciente;
import med.voll.api.domain.paciente.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@SpringBootTest
@AutoConfigureJsonTesters
class AgendaDeConsultasTest
{
	@MockBean
	private ConsultaRepository consultaRepository;

	@MockBean
	private MedicoRepository medicoRepository;

	@MockBean
	private PacienteRepository pacienteRepository;

	@MockBean
	private List<ValidadorCancelamentoConsulta> validadoresCancelamentoConsulta;

	@Autowired
	private AgendaDeConsultas agendaDeConsultas;

	@BeforeEach
	void setUp()
	{
		MockitoAnnotations.openMocks(this);
	}

	private DadosAgendamentoConsulta criarDadosAgendamentoConsulta()
	{
		return criarDadosAgendamentoConsulta(1L, 1L, LocalDateTime.now().plusDays(1), Especialidade.ORTOPEDIA);
	}

	private DadosAgendamentoConsulta criarDadosAgendamentoConsulta(Long idMedico, Long idPaciente, LocalDateTime data,
			Especialidade especialidade)
	{
		return new DadosAgendamentoConsulta(idMedico, idPaciente, data, especialidade);
	}

	private DadosEndereco criarDadosEndereco()
	{
		return new DadosEndereco("Rua da mais paz", "Bairro da paz", "00000000", "A Cidade da Paz", "CP", "Apto", "085");
	}

	private DadosCadastroMedico criarDadosCadastroMedico()
	{
		return new DadosCadastroMedico("Josh", "josh@example.com", "123456789", "123456", Especialidade.ORTOPEDIA, criarDadosEndereco());
	}

	private DadosCadastroPaciente criarDadosCadastroPaciente()
	{
		return new DadosCadastroPaciente("Josh", "josh@example.com", "123456789", "123.456.789-01", criarDadosEndereco());
	}

	private Paciente criarPaciente()
	{
		Paciente paciente = new Paciente(this.criarDadosCadastroPaciente());
		paciente.setId(1L);

		return paciente;
	}

	private Medico criarMedico()
	{
		Medico medico = new Medico(this.criarDadosCadastroMedico());
		medico.setId(1L);

		return medico;
	}

	@Test
	@DisplayName("Deve agendar uma consulta com sucesso")
	void agendarConsultaComSucesso()
	{
		// Arrange
		Medico medico = criarMedico();
		Paciente paciente = criarPaciente();

		DadosAgendamentoConsulta dadosAgendamento = this.criarDadosAgendamentoConsulta(medico.getId(), paciente.getId(),
				LocalDateTime.now().plusHours(1), Especialidade.ORTOPEDIA);

		DadosAgendamentoConsulta dadosAgendamentoConsultaEsperado = criarDadosAgendamentoConsulta(medico.getId(), paciente.getId(),
				LocalDateTime.now().plusHours(1), Especialidade.ORTOPEDIA);

		Consulta consultaEsperada = new Consulta(null, medico, paciente, dadosAgendamentoConsultaEsperado.data(), false, null);
		DadosDetalhamentoConsulta dadosDetalhamentoConsultaEsperado = new DadosDetalhamentoConsulta(consultaEsperada);

		// Configurar mocks para permitir o agendamento
		when(pacienteRepository.findAtivoById(dadosAgendamento.idMedico())).thenReturn(true);
		when(pacienteRepository.existsById(dadosAgendamento.idPaciente())).thenReturn(true);
		when(pacienteRepository.getReferenceById(dadosAgendamento.idPaciente())).thenReturn(paciente);

		when(medicoRepository.findAtivoById(dadosAgendamento.idMedico())).thenReturn(true);
		when(medicoRepository.existsById(dadosAgendamento.idMedico())).thenReturn(true);
		when(medicoRepository.getReferenceById(dadosAgendamento.idMedico())).thenReturn(medico);

		when(consultaRepository.existsByPacienteIdAndDataBetween(dadosAgendamento.idPaciente(), dadosAgendamento.data().withHour(7),
				dadosAgendamento.data().withHour(18))).thenReturn(false);
		when(consultaRepository.existsByMedicoIdAndDataAndMotivoCancelamentoIsNull(dadosAgendamento.idMedico(),
				dadosAgendamento.data())).thenReturn(false);
		when(consultaRepository.save(any())).thenReturn(new Consulta(1L, medico, paciente, dadosAgendamento.data(), false, null));

		// Act
		DadosDetalhamentoConsulta dadosDetalhamentoConsulta = agendaDeConsultas.agendar(dadosAgendamento);

		// Assert
		assertThat(dadosDetalhamentoConsulta).isEqualTo(dadosDetalhamentoConsultaEsperado);
	}

	@Test
	@DisplayName("Deve lançar ValidacaoException ao agendar consulta com paciente inexistente")
	void agendarCenario2()
	{
		// Arrange
		DadosAgendamentoConsulta dadosAgendamentoConsulta = criarDadosAgendamentoConsulta();

		when(pacienteRepository.existsById(dadosAgendamentoConsulta.idPaciente())).thenReturn(false);

		// Act and Assert
		assertThrows(ValidacaoException.class, () -> agendaDeConsultas.agendar(dadosAgendamentoConsulta));
	}

	@Test
	@DisplayName("Deve lançar ValidacaoException ao agendar consulta com médico inexistente")
	void agendarCenario3()
	{
		// Arrange
		DadosAgendamentoConsulta dadosAgendamentoConsulta = criarDadosAgendamentoConsulta();

		when(pacienteRepository.existsById(dadosAgendamentoConsulta.idPaciente())).thenReturn(true);

		// Act and Assert
		assertThrows(ValidacaoException.class, () -> agendaDeConsultas.agendar(dadosAgendamentoConsulta));
	}

	@Test
	@DisplayName("Deve cancelar uma consulta com sucesso")
	void cancelarCenario1()
	{
		// Arrange
		DadosCancelamentoConsulta dadosCancelamentoConsulta = new DadosCancelamentoConsulta(1L, MotivoCancelamento.OUTROS);
		Medico medico = criarMedico();
		Paciente paciente = criarPaciente();
		Consulta consulta = new Consulta(1L, medico, paciente, LocalDateTime.now().plusDays(1).plusSeconds(1), false, null);

		when(consultaRepository.existsById(dadosCancelamentoConsulta.idConsulta())).thenReturn(true);
		when(consultaRepository.getReferenceById(dadosCancelamentoConsulta.idConsulta())).thenReturn(consulta);

		// Act
		agendaDeConsultas.cancelar(dadosCancelamentoConsulta);

		// Assert
		assertEquals(true, consulta.getCancelada());
	}

	@Test
	@DisplayName("Deve lançar ValidacaoException ao cancelar consulta inexistente")
	void cancelarCenario2()
	{
		// Arrange
		DadosCancelamentoConsulta dadosCancelamentoConsulta = new DadosCancelamentoConsulta(1L, MotivoCancelamento.PACIENTE_DESISTIU);

		when(consultaRepository.existsById(dadosCancelamentoConsulta.idConsulta())).thenReturn(false);

		// Act and Assert
		assertThrows(ValidacaoException.class, () -> agendaDeConsultas.cancelar(dadosCancelamentoConsulta));
	}
}