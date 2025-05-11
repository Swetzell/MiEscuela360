package com.miescuela360.service;

import com.miescuela360.model.Madre;
import com.miescuela360.repository.MadreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MadreService {

    @Autowired
    private MadreRepository madreRepository;

    @Transactional(readOnly = true)
    public List<Madre> findAll() {
        return madreRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Madre> findById(Long id) {
        return madreRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Madre> findByDni(String dni) {
        return madreRepository.findByDni(dni);
    }

    @Transactional
    public Madre save(Madre madre) {
        return madreRepository.save(madre);
    }

    @Transactional
    public void deleteById(Long id) {
        madreRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByDni(String dni) {
        return madreRepository.existsByDni(dni);
    }
} 