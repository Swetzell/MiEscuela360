package com.miescuela360.service;

import com.miescuela360.model.Padre;
import com.miescuela360.repository.PadreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PadreService {

    @Autowired
    private PadreRepository padreRepository;

    @Transactional(readOnly = true)
    public List<Padre> findAll() {
        return padreRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Padre> findById(Long id) {
        return padreRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Padre> findByDni(String dni) {
        return padreRepository.findByDni(dni);
    }

    @Transactional
    public Padre save(Padre padre) {
        return padreRepository.save(padre);
    }

    @Transactional
    public void deleteById(Long id) {
        padreRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByDni(String dni) {
        return padreRepository.existsByDni(dni);
    }
} 