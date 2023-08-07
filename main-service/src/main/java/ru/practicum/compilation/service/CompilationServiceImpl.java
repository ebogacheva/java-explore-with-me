package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ExploreConflictException;
import ru.practicum.exception.EWMElementNotFoundException;

import static ru.practicum.utils.EWMCommonMethods.pageRequestOf;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto newCompilationDto) {
        List<Long> ids = newCompilationDto.getEvents();
        List<Event> events = getEvents(ids);
        Compilation newCompilation = compilationMapper.newCompilationDtoToCompilation(newCompilationDto, events);
        Compilation compilation = compilationRepository.save(newCompilation);
        List<EventShortDto> dtos = getShortEventDtos(events);
        return compilationMapper.toCompilationDto(compilation, dtos);
    }

    @Override
    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = getCompilationIfExist(compId);
        updateCompilation(compilation, request);
        Compilation updated = compilationRepository.save(compilation);
        List<EventShortDto> dtos = getShortEventDtos(updated.getEvents());
        return compilationMapper.toCompilationDto(compilation, dtos);
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        Compilation compilation = getCompilationIfExist(compId);
        compilationRepository.delete(compilation);
    }

    @Override
    public List<CompilationDto> getAll(Boolean pinned, Integer from, Integer size) {
        Page<Compilation> compilations = compilationRepository.findAllByPinned(pinned, pageRequestOf(from, size));
        return compilationMapper.pageToList(compilations)
                .stream()
                .map(compilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto get(Long compId) {
        return compilationMapper.toCompilationDto(getCompilationIfExist(compId));
    }

    private List<EventShortDto> getShortEventDtos(List<Event> events) {
        return events.stream().map(eventMapper::eventToEventShortDto).collect(Collectors.toList());
    }

    private void updateCompilation(Compilation compilation, UpdateCompilationRequest request) {
        List<Long> ids = request.getEvents();
        if (Objects.nonNull(ids)) {
            List<Event> updatedList = getEvents(ids);
            compilation.setEvents(updatedList);
        }
        Boolean pinned = request.getPinned();
        if (Objects.nonNull(pinned)) {
            compilation.setPinned(pinned);
        }
        String title = request.getTitle();
        if (Objects.nonNull(title)) {
            Optional<Compilation> titleIsNotUnique = compilationRepository.findFirst1ByTitle(title);
            if (titleIsNotUnique.isPresent() && !compilation.getTitle().equals(title)) {
                throw new ExploreConflictException("Данный заголовок подборки уже существует.");
            }
            compilation.setTitle(title);
        }
    }

    private List<Event> getEvents(List<Long> eventIds) {
        if (!eventIds.isEmpty()) {
            List<Event> events = eventRepository.findAllByIdIn(eventIds);
            if (events.size() < eventIds.size()) {
                throw new EWMElementNotFoundException("События из подборки не найдены.");
            }
            return events;
        }
        return List.of();
    }

    private Compilation getCompilationIfExist(Long comId) {
        return compilationRepository.findById(comId)
                .orElseThrow(() -> new EWMElementNotFoundException("Подборка не найдена."));
    }

}
