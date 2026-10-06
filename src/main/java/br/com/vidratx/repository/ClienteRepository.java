package br.com.vidratx.repository;

import br.com.vidratx.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository
        extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByIdAndEmpresaId(
            Long id,
            Long empresaId
    );

    List<Cliente> findAllByEmpresaIdOrderByNomeAsc(
            Long empresaId
    );

    List<Cliente> findAllByEmpresaIdAndNomeContainingIgnoreCaseOrderByNomeAsc(
            Long empresaId,
            String nome
    );

    boolean existsByEmpresaIdAndCpfCnpj(
            Long empresaId,
            String cpfCnpj
    );

    boolean existsByEmpresaIdAndCpfCnpjAndIdNot(
            Long empresaId,
            String cpfCnpj,
            Long id
    );

    List<Cliente> findAllByEmpresaIdAndWhatsappInOrderByIdAsc(
            Long empresaId,
            Collection<String> whatsapps
    );
}
