package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.service.CompilationServiceImpl;

import java.util.List;

@RestController
@RequestMapping(path = "/compilations")
@RequiredArgsConstructor
public class PublicCompilationController {

    private final CompilationServiceImpl compilationService;

    @GetMapping
    public List<CompilationDto> get(@RequestParam(required = false, defaultValue = "false") Boolean pinned,
                             @RequestParam(required = false, defaultValue = "0") Integer from,
                             @RequestParam(required = false, defaultValue = "10")Integer size) {
        return compilationService.getAll(pinned, from, size);
    }

    @GetMapping(value = "/{compId}")
    public CompilationDto get(@PathVariable Long compId) {
        return compilationService.get(compId);
    }

}
