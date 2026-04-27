package com.workflow.backend.services;

import com.workflow.backend.models.PoliticaNegocio;
import com.workflow.backend.repositories.PoliticaNegocioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PoliticaNegocioService {

    private final PoliticaNegocioRepository politicaRepository;

    public List<PoliticaNegocio> obtenerTodas() {
        return politicaRepository.findByActivoTrue();
    }

    public Optional<PoliticaNegocio> obtenerPorIdOptional(String id) {
        return politicaRepository.findById(id);
    }

    public PoliticaNegocio crear(PoliticaNegocio politica) {
        politica.setCreadoEn(LocalDateTime.now());
        politica.setActualizadoEn(LocalDateTime.now());
        return politicaRepository.save(politica);
    }

    public PoliticaNegocio obtenerPorId(String id) {
        return politicaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Política no encontrada"));
    }

    public PoliticaNegocio actualizar(PoliticaNegocio politica) {
        politica.setActualizadoEn(LocalDateTime.now());
        return politicaRepository.save(politica);
    }

    public void eliminar(String id) {
        politicaRepository.deleteById(id);
    }
}