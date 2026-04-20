package com.workflow.backend.services;

import com.workflow.backend.models.PoliticaNegocio;
import com.workflow.backend.repositories.PoliticaNegocioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PoliticaNegocioService {

    private final PoliticaNegocioRepository politicaRepository;

    public List<PoliticaNegocio> obtenerTodas() {
        return politicaRepository.findByActivoTrue();
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
}