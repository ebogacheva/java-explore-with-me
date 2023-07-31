package ru.practicum.event.repository;


import org.mapstruct.ap.internal.util.Strings;
import org.springframework.stereotype.Repository;
import ru.practicum.event.dto.EventFilterParams;
import ru.practicum.event.dto.EventFilterParamsDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Repository
public class CustomEventRepositoryImpl implements CustomEventRepository{

    @PersistenceContext
    private EntityManager entityManager;

    public List<Event> adminEventsSearch(EventFilterParams params) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> cq = cb.createQuery(Event.class);
        Root<Event> eventRoot = cq.from(Event.class);
        Predicate criteria = cb.conjunction();

        addInitiatorsFilter(criteria, cb, eventRoot,params.getIds());
        addStateFilter(criteria, cb, eventRoot, params.getStates());
        addCategoryFilter(criteria, cb, eventRoot, params.getCategories());
        addRangeStartFilter(criteria, cb, eventRoot, params.getRangeStart());
        addRangeEndFilter(criteria, cb, eventRoot, params.getRangeEnd());
        cq.select(eventRoot).where(criteria);

        return entityManager.createQuery(cq)
                .setFirstResult(params.getFrom())
                .setMaxResults(params.getSize())
                .getResultList();
    }

    public List<Event> publicEventsSearch(EventFilterParams params) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> cq = cb.createQuery(Event.class);
        Root<Event> eventRoot = cq.from(Event.class);
        Predicate criteria = cb.conjunction();

        if (Objects.isNull(params.getRangeStart())) {
            params.setRangeStart(LocalDateTime.now());
        }

        addStateFilter(criteria, cb, eventRoot, List.of(EventState.PUBLISHED));
        addTextFilter(criteria, cb, eventRoot, params.getText());
        addCategoryFilter(criteria, cb, eventRoot, params.getCategories());
        addPaidFilter(criteria, cb, eventRoot, params.getPaid());
        addRangeStartFilter(criteria, cb, eventRoot, params.getRangeStart());
        addRangeEndFilter(criteria, cb, eventRoot, params.getRangeEnd());

        return entityManager.createQuery(cq)
                .setFirstResult(params.getFrom())
                .setMaxResults(params.getSize())
                .getResultList();
    }

    private void addPaidFilter(Predicate criteria, CriteriaBuilder cb, Root<Event> eventRoot, Boolean paid) {
        if (Objects.nonNull(paid)) {
            criteria = cb.equal(eventRoot.get("paid"), paid);
        }
    }

    private void addTextFilter(Predicate criteria, CriteriaBuilder cb, Root<Event> eventRoot, String text) {
        if(!Objects.isNull(text) && !Strings.isEmpty(text)) {
            String searchValue = ("%" + text + "%").toLowerCase();
            Predicate annotation = cb.like(cb.lower(eventRoot.get("annotation")), searchValue);
            Predicate description = cb.like(cb.lower(eventRoot.get("annotation")), searchValue);
            criteria = cb.or(annotation, description);
        }
    }

    private void addInitiatorsFilter(Predicate criteria, CriteriaBuilder cb, Root<Event> eventRoot, List<Long> ids) {
        if (!ids.isEmpty()) {
            criteria = cb.and(criteria, eventRoot.get("initiator").get("id").in(ids));
        }
    }

    private void addStateFilter(Predicate criteria, CriteriaBuilder cb, Root<Event> eventRoot, List<EventState> states) {
        if (!states.isEmpty()) {
            criteria = cb.and(criteria, eventRoot.get("state").in(states));
        }
    }
    private void addCategoryFilter(Predicate criteria, CriteriaBuilder cb, Root<Event> eventRoot, List<Long> categories) {
        if(!categories.isEmpty()) {
            criteria = cb.and(criteria, eventRoot.get("category").in(categories));
        }
    }

    private void addRangeStartFilter(Predicate criteria, CriteriaBuilder cb, Root<Event> eventRoot, LocalDateTime rangeStart) {
        if(rangeStart != null) {
            criteria = cb.and(criteria, cb.greaterThanOrEqualTo(eventRoot.get("eventDate"),  rangeStart));
        }
    }

    private void addRangeEndFilter(Predicate criteria, CriteriaBuilder cb, Root<Event> eventRoot, LocalDateTime rangeEnd) {
        if(rangeEnd != null) {
            criteria = cb.and(criteria, cb.lessThanOrEqualTo(eventRoot.get("eventDate"),  rangeEnd));
        }
    }
}


