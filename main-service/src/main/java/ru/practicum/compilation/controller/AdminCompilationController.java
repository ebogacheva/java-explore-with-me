package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.service.CompilationServiceImpl;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/admin/compilations")
@RequiredArgsConstructor
public class AdminCompilationController {

    private final CompilationServiceImpl compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto create(@Valid @RequestBody NewCompilationDto compDto) {
        return compilationService.create(compDto);
    }

    @PatchMapping("/{compId}")
    public CompilationDto update(@PathVariable Long compId,
                                @Valid @RequestBody UpdateCompilationRequest request) {
        return compilationService.update(compId, request);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long compId) {
        compilationService.delete(compId);
    }
}
