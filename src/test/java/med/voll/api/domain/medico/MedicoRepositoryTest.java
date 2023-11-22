package med.voll.api.domain.medico;

import med.voll.api.domain.consulta.Consulta;
import med.voll.api.domain.endereco.DadosEndereco;
import med.voll.api.domain.paciente.DadosCadastroPaciente;
import med.voll.api.domain.paciente.Paciente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.data.domain.Pageable;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class MedicoRepositoryTest
{
	@Autowired
	private MedicoRepository medicoRepository;

	@Autowired
	private TestEntityManager em;

	@BeforeEach
	void setUp()
	{
		this.medicoRepository.deleteAll();
	}

	private void cadastrarConsulta(Medico medico, Paciente paciente, LocalDateTime data)
	{
		em.persist(new Consulta(null, medico, paciente, data, false, null));
	}

	private Medico cadastrarMedico(String nome, String email, String crm, Especialidade especialidade)
	{
		Medico medico = new Medico(dadosMedico(nome, email, crm, especialidade));
		em.persist(medico);
		return medico;
	}

	private Paciente cadastrarPaciente(String nome, String email, String cpf)
	{
		Paciente paciente = new Paciente(dadosPaciente(nome, email, cpf));
		em.persist(paciente);
		return paciente;
	}

	private DadosCadastroMedico dadosMedico(String nome, String email, String crm, Especialidade especialidade)
	{
		return new DadosCadastroMedico(nome, email, "61999999999", crm, especialidade, dadosEndereco());
	}

	private DadosCadastroPaciente dadosPaciente(String nome, String email, String cpf)
	{
		return new DadosCadastroPaciente(nome, email, "61999999999", cpf, dadosEndereco());
	}

	private DadosEndereco dadosEndereco()
	{
		return new DadosEndereco("rua xpto", "bairro", "00000000", "Brasilia", "DF", null, null);
	}

	@Test
	@DisplayName("Deveria devolver null quando o único médico cadastrado não estiver disponível na data")
	void escolherMedicoAleatorioLivreNaDataCenario1()
	{
		//giver or arrange
		LocalDateTime proximaSegundaAs10 = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).atTime(10, 0);
		Medico medico = cadastrarMedico("Medico", "medico@voll.med", "452163", Especialidade.CARDIOLOGIA);
		Paciente paciente = cadastrarPaciente("Paciente", "paciente@email.com", "00000000000");
		cadastrarConsulta(medico, paciente, proximaSegundaAs10);

		// when or act
		Medico medicoLivre = medicoRepository.escolherMedicoAleatorioLivreNaData(Especialidade.CARDIOLOGIA, proximaSegundaAs10);

		// then or assert
		assertThat(medicoLivre).isNull();
	}

	@Test
	@DisplayName("Deveria devolver medico quando ele estiver disponível na data")
	void escolherMedicoAleatorioLivreNaDataCenario2()
	{
		//giver or arrange
		LocalDateTime proximaSegundaAs10 = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).atTime(10, 0);
		Medico medico = cadastrarMedico("Medico", "medico@voll.med", "965478", Especialidade.CARDIOLOGIA);

		// when or act
		Medico medicoLivre = medicoRepository.escolherMedicoAleatorioLivreNaData(Especialidade.CARDIOLOGIA, proximaSegundaAs10);

		// then or assert
		assertThat(medicoLivre).isEqualTo(medico);
	}

	@Test
	@DisplayName("Deve encontrar o status ativo de um médico por ID")
	void findAtivoByIdCenario1()
	{
		// Arrange
		Medico medico = cadastrarMedico("Medico", "medico@voll.med", "452163", Especialidade.CARDIOLOGIA);

		// Act
		Boolean ativo = medicoRepository.findAtivoById(medico.getId());

		// Assert
		assertThat(ativo).isNotNull().isTrue();
	}

	@Test
	@DisplayName("Deve encontrar o status inativo de um médico por ID")
	void findAtivoByIdCenario2()
	{
		// Arrange
		Medico medico = cadastrarMedico("Medico", "medico@voll.med", "452163", Especialidade.CARDIOLOGIA);
		medico.excluir();
		em.persistAndFlush(medico);

		// Act
		Boolean ativo = medicoRepository.findAtivoById(medico.getId());

		// Assert
		assertThat(ativo).isNotNull().isFalse();
	}

	@Test
	@DisplayName("Deve encontrar médicos ativos")
	void findAllByAtivoTrue()
	{
		// Arrange
		var medico1 = cadastrarMedico("Medico1", "medico1@voll.med", "896214", Especialidade.CARDIOLOGIA);
		var medico2 = cadastrarMedico("Medico2", "medico2@voll.med", "789012", Especialidade.DERMATOLOGIA);

		// Act
		Pageable pageable = PageRequest.of(0, 10);
		Page<Medico> medicosAtivos = medicoRepository.findAllByAtivoTrue(pageable);

		// Assert
		assertThat(medicosAtivos).isNotNull();
		assertThat(medicosAtivos.getContent()).containsExactly(medico1, medico2);
	}
}