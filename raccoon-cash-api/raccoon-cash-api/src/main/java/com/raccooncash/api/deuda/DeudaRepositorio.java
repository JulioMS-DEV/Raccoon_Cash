package com.raccooncash.api.deuda;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeudaRepositorio extends JpaRepository<Deuda, Long> {
    List<Deuda> findAllByUsuarioIdAndActiveTrueOrderByCreatedAtDesc(Long usuarioId);
    List<Deuda> findAllByUsuarioIdAndActiveTrue(Long usuarioId);
    Optional<Deuda> findByIdAndUsuarioIdAndActiveTrue(Long id, Long usuarioId);
}
