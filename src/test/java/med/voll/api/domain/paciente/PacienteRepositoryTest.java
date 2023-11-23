package med.voll.api.domain.paciente;

import med.voll.api.domain.endereco.DadosEndereco;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PacienteRepositoryTest
{
	@Autowired
	private PacienteRepository pacienteRepository;

	@Autowired
	private TestEntityManager em;

	@BeforeEach
	void setUp()
	{
		this.pacienteRepository.deleteAll();
	}

	private Paciente cadastrarPaciente(String nome, String email, String cpf)
	{
		Paciente paciente = new Paciente(dadosPaciente(nome, email, cpf));
		em.persistAndFlush(paciente);
		return paciente;
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
	@DisplayName("Deve encontrar o status ativo de um paciente por ID")
	void findAtivoByIdCenario1()
	{
		// Arrange
		Paciente paciente = cadastrarPaciente("Paciente", "paciente@voll.med", "123456");

		// Act
		Boolean ativo = pacienteRepository.findAtivoById(paciente.getId());

		// Assert
		assertThat(ativo).isNotNull().isTrue();
	}

	@Test
	@DisplayName("Deve encontrar o status inativo de um médico por ID")
	void findAtivoByIdCenario2()
	{
		// Arrange
		Paciente paciente = cadastrarPaciente("Paciente", "paciente@voll.med", "123456");
		paciente.excluir();
		em.persistAndFlush(paciente);

		// Act
		Boolean ativo = pacienteRepository.findAtivoById(paciente.getId());

		// Assert
		assertThat(ativo).isNotNull().isFalse();
	}

	@Test
	@DisplayName("Deve retornar uma lista de pacientes ativos")
	void findAllByAtivoTrue()
	{
		// Arrange
		var paciente1 = cadastrarPaciente("Paciente1", "paciente1@voll.med", "789012");
		var paciente2 = cadastrarPaciente("Paciente2", "paciente2@voll.med", "456789");

		// Act
		Pageable pageable = PageRequest.of(0, 10);
		Page<Paciente> pacientesAtivos = pacienteRepository.findAllByAtivoTrue(pageable);

		// Assert
		assertThat(pacientesAtivos).isNotNull();
		Assertions.assertThat(pacientesAtivos.getContent()).containsExactly(paciente1, paciente2);
	}
}